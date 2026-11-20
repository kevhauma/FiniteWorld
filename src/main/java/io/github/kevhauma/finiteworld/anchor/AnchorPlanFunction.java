package io.github.kevhauma.finiteworld.anchor;

import io.github.kevhauma.finiteworld.FiniteWorld;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.AnchorPlan;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.ExecutionException;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

/**
 * {@code finiteworld:anchor_plan}: the world's anchor plan, shared by every {@code finiteworld:anchor}
 * node that references it. The children are the vanilla climate <em>without</em> anchors, so the
 * planner never reads its own output. Compiling it with a {@code RandomState}'s context seeds those
 * children; the plan is computed on the first compile and reused for every later one with the same
 * context. It samples as 0: only its {@link Sampler#plan()} is used.
 */
public record AnchorPlanFunction(
	float radius, DensityFunction temperature, DensityFunction vegetation, DensityFunction continents, DensityFunction erosion, DensityFunction ridges
) implements DensityFunction {
	// Coarse grid step in blocks; ANC-2 refines near the best candidates.
	static final int STEP = 64;
	// RandomState makes one CompileContext and compiles every function with it, so the context identifies the world.
	// Weak (identity) keys drop the plan when the RandomState is collected; the plan holds no reference back.
	// shortcut: one plan per world, from whichever anchor_plan node compiles first; key on the node too if a world ever holds two.
	private static final Cache<CompileContext, AnchorPlan> PLANS = CacheBuilder.newBuilder().weakKeys().recordStats().build();

	public static final MapCodec<AnchorPlanFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ExtraCodecs.POSITIVE_FLOAT.fieldOf("radius").forGetter(AnchorPlanFunction::radius),
			DensityFunction.CODEC.fieldOf("temperature").forGetter(AnchorPlanFunction::temperature),
			DensityFunction.CODEC.fieldOf("vegetation").forGetter(AnchorPlanFunction::vegetation),
			DensityFunction.CODEC.fieldOf("continents").forGetter(AnchorPlanFunction::continents),
			DensityFunction.CODEC.fieldOf("erosion").forGetter(AnchorPlanFunction::erosion),
			DensityFunction.CODEC.fieldOf("ridges").forGetter(AnchorPlanFunction::ridges)
		)
		.apply(i, AnchorPlanFunction::new));

	/** How many plans have been computed in this JVM (one per Finite world load). */
	public static long plansComputed() {
		return PLANS.stats().loadCount();
	}

	@Override
	public Sampler compileSampler(final DensityFunction.CompileContext context) {
		try {
			return new Sampler(PLANS.get(context, () -> this.plan(context)));
		} catch (ExecutionException e) {
			throw new IllegalStateException("Anchor planning failed", e.getCause());
		}
	}

	private AnchorPlan plan(final DensityFunction.CompileContext context) {
		long start = System.nanoTime();
		// Climate noises are 2D (y_scale 0), so y = 0 serves every height.
		List<AnchorPlanner.Climate> climate = List.of(this.temperature, this.vegetation, this.continents, this.erosion, this.ridges).stream()
			.map(function -> function.compileSampler(context))
			.<AnchorPlanner.Climate>map(sampler -> (x, z) -> sampler.sampleValue(SamplerContext.EMPTY_UNCACHED, x, 0, z))
			.toList();
		AnchorPlan plan = AnchorPlanner.plan(climate, this.radius, STEP);
		// The hash is stable across runs (records of ints), so restarts and servers can be compared from the log.
		FiniteWorld.LOGGER.info("[ANC-1] Planned {} anchors for radius {}: {} grid points (step {}) in {} ms, plan hash {}",
			plan.anchors().size(), (int) this.radius, plan.gridPoints(), STEP, (System.nanoTime() - start) / 1_000_000, plan.hashCode());
		return plan;
	}

	@Override
	public DensityFunction rewriteChildren(final DfRewriteRule rule) {
		DensityFunction t = rule.rewrite(this.temperature);
		DensityFunction v = rule.rewrite(this.vegetation);
		DensityFunction c = rule.rewrite(this.continents);
		DensityFunction e = rule.rewrite(this.erosion);
		DensityFunction r = rule.rewrite(this.ridges);
		return t == this.temperature && v == this.vegetation && c == this.continents && e == this.erosion && r == this.ridges
			? this
			: new AnchorPlanFunction(this.radius, t, v, c, e, r);
	}

	@Override
	public Interval range() {
		return Interval.ofExact(0.0F);
	}

	/** All axes, so the compiler's axis slicing never wraps this node and parents can rely on its sampler type. */
	@Override
	public @DensityFunction.Axes int domainAxes() {
		return ALL_AXES;
	}

	@Override
	public MapCodec<AnchorPlanFunction> codec() {
		return CODEC;
	}

	public record Sampler(AnchorPlan plan) implements DensitySampler {
		@Override
		public void sampleVolume(final SamplerContext context, final DensityBuffer outputBuffer, final DensityVolume volume) {
			DensitySampler.sampleVolumeNaive(context, outputBuffer, volume, this);
		}

		@Override
		public float sampleValue(final SamplerContext context, final int blockX, final int blockY, final int blockZ) {
			return 0.0F;
		}
	}
}
