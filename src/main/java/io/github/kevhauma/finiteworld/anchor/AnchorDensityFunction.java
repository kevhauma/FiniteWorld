package io.github.kevhauma.finiteworld.anchor;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;

/**
 * {@code finiteworld:anchor}: one climate parameter plus the world's anchor plan ({@code plan}, a
 * {@code finiteworld:anchor_plan} that all five climate nodes reference). ANC-1 passes {@code input}
 * through unchanged; ANC-3 adds the plan's offsets.
 */
public record AnchorDensityFunction(DensityFunction input, DensityFunction plan) implements DensityFunction {
	public static final MapCodec<AnchorDensityFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			DensityFunction.CODEC.fieldOf("input").forGetter(AnchorDensityFunction::input),
			DensityFunction.CODEC.fieldOf("plan").forGetter(AnchorDensityFunction::plan)
		)
		.apply(i, AnchorDensityFunction::new));

	@Override
	public DensitySampler compileSampler(final DensityFunction.CompileContext context) {
		// Computes this world's plan on the first compile and reuses it after; fails fast if plan is not a finiteworld:anchor_plan.
		((AnchorPlanFunction.Sampler) this.plan.compileSampler(context)).plan();
		return this.input.compileSampler(context);
	}

	@Override
	public DensityFunction rewriteChildren(final DfRewriteRule rule) {
		DensityFunction input = rule.rewrite(this.input);
		DensityFunction plan = rule.rewrite(this.plan);
		return input == this.input && plan == this.plan ? this : new AnchorDensityFunction(input, plan);
	}

	@Override
	public Interval range() {
		return this.input.range();
	}

	@Override
	public @DensityFunction.Axes int domainAxes() {
		return this.input.domainAxes();
	}

	@Override
	public MapCodec<AnchorDensityFunction> codec() {
		return CODEC;
	}
}
