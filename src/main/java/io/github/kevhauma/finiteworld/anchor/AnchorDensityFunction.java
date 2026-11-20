package io.github.kevhauma.finiteworld.anchor;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

/**
 * {@code finiteworld:anchor}: one climate {@code parameter} plus the world's anchor offsets for it,
 * {@code input + Σ falloff(distance) × offset}. {@code plan} is the {@code finiteworld:anchor_plan} that
 * all five climate nodes reference; its plan is turned into an {@link AnchorField} at compile time.
 */
public record AnchorDensityFunction(DensityFunction input, Parameter parameter, DensityFunction plan) implements DensityFunction {
	public static final MapCodec<AnchorDensityFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			DensityFunction.CODEC.fieldOf("input").forGetter(AnchorDensityFunction::input),
			Parameter.CODEC.fieldOf("parameter").forGetter(AnchorDensityFunction::parameter),
			DensityFunction.CODEC.fieldOf("plan").forGetter(AnchorDensityFunction::plan)
		)
		.apply(i, AnchorDensityFunction::new));

	@Override
	public DensitySampler compileSampler(final DensityFunction.CompileContext context) {
		// Computes this world's plan on the first compile and reuses it after; fails fast if plan is not a finiteworld:anchor_plan.
		AnchorPlanner.AnchorPlan plan = ((AnchorPlanFunction.Sampler) this.plan.compileSampler(context)).plan();
		AnchorField field = AnchorField.of(plan.anchors(), this.parameter.ordinal());
		DensitySampler input = this.input.compileSampler(context);
		return field.isEmpty() ? input : new Sampler(input, field);
	}

	@Override
	public DensityFunction rewriteChildren(final DfRewriteRule rule) {
		DensityFunction input = rule.rewrite(this.input);
		DensityFunction plan = rule.rewrite(this.plan);
		return input == this.input && plan == this.plan ? this : new AnchorDensityFunction(input, this.parameter, plan);
	}

	/** Discs never overlap, so a point gets at most one anchor's offset, which the planner caps at {@link AnchorPlanner#MAX_OFFSET}. */
	@Override
	public Interval range() {
		return Interval.add(this.input.range(), Interval.ofSymmetric(AnchorPlanner.MAX_OFFSET));
	}

	/** The offsets vary in x and z only, like the 2D climate noises, so the compiler can still slice off y. */
	@Override
	public @DensityFunction.Axes int domainAxes() {
		return this.input.domainAxes() | AXIS_X | AXIS_Z;
	}

	@Override
	public MapCodec<AnchorDensityFunction> codec() {
		return CODEC;
	}

	/** The climate parameters in router order, the order of {@link AnchorPlanner.Anchor#offset()}. */
	public enum Parameter implements StringRepresentable {
		TEMPERATURE("temperature"),
		VEGETATION("vegetation"),
		CONTINENTS("continents"),
		EROSION("erosion"),
		RIDGES("ridges");

		public static final StringRepresentable.EnumCodec<Parameter> CODEC = StringRepresentable.fromEnum(Parameter::values);
		private final String name;

		Parameter(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}

	private record Sampler(DensitySampler input, AnchorField field) implements DensitySampler {
		@Override
		public void sampleVolume(final SamplerContext context, final DensityBuffer outputBuffer, final DensityVolume volume) {
			this.input.sampleVolume(context, outputBuffer, volume);
			int index = 0;

			for (int z = 0; z < volume.sizeZ(); z++) {
				int blockZ = volume.blockZ(z);

				for (int x = 0; x < volume.sizeX(); x++) {
					float offset = this.field.offset(volume.blockX(x), blockZ);

					for (int y = 0; y < volume.sizeY(); y++, index++) {
						outputBuffer.set(index, outputBuffer.get(index) + offset);
					}
				}
			}
		}

		@Override
		public float sampleValue(final SamplerContext context, final int blockX, final int blockY, final int blockZ) {
			return this.input.sampleValue(context, blockX, blockY, blockZ) + this.field.offset(blockX, blockZ);
		}
	}
}
