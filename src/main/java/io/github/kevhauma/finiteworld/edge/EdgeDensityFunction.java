package io.github.kevhauma.finiteworld.edge;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

/** {@code finiteworld:edge}: pushes {@code input} (continentalness) into the ocean range near and past {@code radius}. */
public record EdgeDensityFunction(DensityFunction input, float radius, float band) implements DensityFunction {
	public static final MapCodec<EdgeDensityFunction> CODEC = RecordCodecBuilder.<EdgeDensityFunction>mapCodec(
			i -> i.group(
					DensityFunction.CODEC.fieldOf("input").forGetter(EdgeDensityFunction::input),
					ExtraCodecs.POSITIVE_FLOAT.fieldOf("radius").forGetter(EdgeDensityFunction::radius),
					ExtraCodecs.POSITIVE_FLOAT.fieldOf("band").forGetter(EdgeDensityFunction::band)
				)
				.apply(i, EdgeDensityFunction::new)
		)
		.validate(edge -> edge.band <= edge.radius
			? DataResult.success(edge)
			: DataResult.error(() -> "band (" + edge.band + ") must not exceed radius (" + edge.radius + ")"));

	@Override
	public DensitySampler compileSampler(final DensityFunction.CompileContext context) {
		return new Sampler(this.input.compileSampler(context), this.radius, this.band);
	}

	@Override
	public DensityFunction rewriteChildren(final DfRewriteRule rule) {
		DensityFunction input = rule.rewrite(this.input);
		return input == this.input ? this : new EdgeDensityFunction(input, this.radius, this.band);
	}

	@Override
	public Interval range() {
		Interval in = this.input.range();
		return Interval.encapsulating(in, Interval.mapMonotonic(in, EdgeFalloff::ocean));
	}

	@Override
	public @DensityFunction.Axes int domainAxes() {
		return this.input.domainAxes() | AXIS_X | AXIS_Z;
	}

	@Override
	public MapCodec<EdgeDensityFunction> codec() {
		return CODEC;
	}

	private record Sampler(DensitySampler input, float radius, float band) implements DensitySampler {
		@Override
		public void sampleVolume(final SamplerContext context, final DensityBuffer outputBuffer, final DensityVolume volume) {
			this.input.sampleVolume(context, outputBuffer, volume);
			int index = 0;

			for (int z = 0; z < volume.sizeZ(); z++) {
				double blockZ = volume.blockZ(z);

				for (int x = 0; x < volume.sizeX(); x++) {
					double blockX = volume.blockX(x);
					double distance = Math.sqrt(blockX * blockX + blockZ * blockZ);

					for (int y = 0; y < volume.sizeY(); y++, index++) {
						outputBuffer.set(index, EdgeFalloff.apply(outputBuffer.get(index), distance, this.radius, this.band));
					}
				}
			}
		}

		@Override
		public float sampleValue(final SamplerContext context, final int blockX, final int blockY, final int blockZ) {
			double distance = Math.sqrt((double) blockX * blockX + (double) blockZ * blockZ);
			return EdgeFalloff.apply(this.input.sampleValue(context, blockX, blockY, blockZ), distance, this.radius, this.band);
		}
	}
}
