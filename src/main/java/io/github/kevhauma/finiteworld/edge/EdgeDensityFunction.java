package io.github.kevhauma.finiteworld.edge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.densityfunction.ScopedDensityBuffer;

/**
 * {@code finiteworld:edge}: pushes {@code input} (continentalness) into the ocean range near and past {@code radius}.
 * The fade band is {@code band} wide on flat terrain and {@code narrow_band} under peaks (from {@code erosion} and
 * {@code ridges}), and moves inward by up to {@code outline_depth} where the {@code outline} noise is high.
 * The extra fields are optional, so a node without them is the fixed circle of EDGE-1.
 */
public record EdgeDensityFunction(
	DensityFunction input, float radius, float band, float narrowBand, DensityFunction outline, float outlineDepth, DensityFunction erosion, DensityFunction ridges
) implements DensityFunction {
	public static final MapCodec<EdgeDensityFunction> CODEC = RecordCodecBuilder.<EdgeDensityFunction>mapCodec(
			i -> i.group(
					DensityFunction.CODEC.fieldOf("input").forGetter(EdgeDensityFunction::input),
					ExtraCodecs.POSITIVE_FLOAT.fieldOf("radius").forGetter(EdgeDensityFunction::radius),
					ExtraCodecs.POSITIVE_FLOAT.fieldOf("band").forGetter(EdgeDensityFunction::band),
					ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("narrow_band").forGetter(e -> e.narrowBand == e.band ? Optional.empty() : Optional.of(e.narrowBand)),
					DensityFunction.CODEC.optionalFieldOf("outline", DensityFunctions.zero()).forGetter(EdgeDensityFunction::outline),
					Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("outline_depth", 0.0F).forGetter(EdgeDensityFunction::outlineDepth),
					DensityFunction.CODEC.optionalFieldOf("erosion", DensityFunctions.zero()).forGetter(EdgeDensityFunction::erosion),
					DensityFunction.CODEC.optionalFieldOf("ridges", DensityFunctions.zero()).forGetter(EdgeDensityFunction::ridges)
				)
				.apply(i, (input, radius, band, narrow, outline, depth, erosion, ridges) ->
					new EdgeDensityFunction(input, radius, band, narrow.orElse(band), outline, depth, erosion, ridges))
		)
		.validate(edge -> edge.narrowBand > edge.band
			? DataResult.error(() -> "narrow_band (" + edge.narrowBand + ") must not exceed band (" + edge.band + ")")
			: edge.band + edge.outlineDepth > edge.radius
			? DataResult.error(() -> "band plus outline_depth (" + (edge.band + edge.outlineDepth) + ") must not exceed radius (" + edge.radius + ")")
			: DataResult.success(edge));

	public EdgeDensityFunction withRadius(float radius) {
		return new EdgeDensityFunction(this.input, radius, this.band, this.narrowBand, this.outline, this.outlineDepth, this.erosion, this.ridges);
	}

	@Override
	public DensitySampler compileSampler(final DensityFunction.CompileContext context) {
		return new Sampler(this.input.compileSampler(context), this.outline.compileSampler(context), this.erosion.compileSampler(context),
			this.ridges.compileSampler(context), this.radius, this.band, this.narrowBand, this.outlineDepth);
	}

	@Override
	public DensityFunction rewriteChildren(final DfRewriteRule rule) {
		DensityFunction input = rule.rewrite(this.input);
		DensityFunction outline = rule.rewrite(this.outline);
		DensityFunction erosion = rule.rewrite(this.erosion);
		DensityFunction ridges = rule.rewrite(this.ridges);
		return input == this.input && outline == this.outline && erosion == this.erosion && ridges == this.ridges
			? this
			: new EdgeDensityFunction(input, this.radius, this.band, this.narrowBand, outline, this.outlineDepth, erosion, ridges);
	}

	@Override
	public Interval range() {
		Interval in = this.input.range();
		return Interval.encapsulating(in, Interval.mapMonotonic(in, EdgeFalloff::ocean));
	}

	@Override
	public @DensityFunction.Axes int domainAxes() {
		return this.input.domainAxes() | this.outline.domainAxes() | this.erosion.domainAxes() | this.ridges.domainAxes() | AXIS_X | AXIS_Z;
	}

	@Override
	public MapCodec<EdgeDensityFunction> codec() {
		return CODEC;
	}

	private record Sampler(
		DensitySampler input, DensitySampler outline, DensitySampler erosion, DensitySampler ridges, float radius, float band, float narrowBand, float outlineDepth
	) implements DensitySampler {
		@Override
		public void sampleVolume(final SamplerContext context, final DensityBuffer outputBuffer, final DensityVolume volume) {
			this.input.sampleVolume(context, outputBuffer, volume);
			// Most volumes lie wholly inside the innermost band edge, where nothing changes; skip the extra noises there.
			double farX = Math.max(Math.abs(volume.blockX(0)), Math.abs(volume.blockX(volume.sizeX() - 1)));
			double farZ = Math.max(Math.abs(volume.blockZ(0)), Math.abs(volume.blockZ(volume.sizeZ() - 1)));
			if (Math.sqrt(farX * farX + farZ * farZ) + this.outlineDepth <= this.radius - this.band) {
				return;
			}

			try (ScopedDensityBuffer outline = context.acquireBuffer(volume);
				ScopedDensityBuffer erosion = context.acquireBuffer(volume);
				ScopedDensityBuffer ridges = context.acquireBuffer(volume)) {
				this.outline.sampleVolume(context, outline, volume);
				this.erosion.sampleVolume(context, erosion, volume);
				this.ridges.sampleVolume(context, ridges, volume);
				int index = 0;

				for (int z = 0; z < volume.sizeZ(); z++) {
					double blockZ = volume.blockZ(z);

					for (int x = 0; x < volume.sizeX(); x++) {
						double blockX = volume.blockX(x);
						double distance = Math.sqrt(blockX * blockX + blockZ * blockZ);

						for (int y = 0; y < volume.sizeY(); y++, index++) {
							outputBuffer.set(index, this.apply(outputBuffer.get(index), distance, outline.get(index), erosion.get(index), ridges.get(index)));
						}
					}
				}
			}
		}

		@Override
		public float sampleValue(final SamplerContext context, final int blockX, final int blockY, final int blockZ) {
			double distance = Math.sqrt((double) blockX * blockX + (double) blockZ * blockZ);
			float value = this.input.sampleValue(context, blockX, blockY, blockZ);
			if (distance + this.outlineDepth <= this.radius - this.band) {
				return value;
			}
			return this.apply(value, distance, this.outline.sampleValue(context, blockX, blockY, blockZ),
				this.erosion.sampleValue(context, blockX, blockY, blockZ), this.ridges.sampleValue(context, blockX, blockY, blockZ));
		}

		private float apply(float continentalness, double distance, float outline, float erosion, float ridges) {
			float band = EdgeFalloff.band(EdgeFalloff.ruggedness(erosion, ridges), this.band, this.narrowBand);
			return EdgeFalloff.apply(continentalness, distance + EdgeFalloff.inset(outline, this.outlineDepth), this.radius, band);
		}
	}
}
