package io.github.kevhauma.finiteworld.edge;

import io.github.kevhauma.finiteworld.FiniteRadius;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Interval;
import net.minecraft.world.level.levelgen.densityfunction.DensityBuffer;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensitySampler;
import net.minecraft.world.level.levelgen.densityfunction.DensityVolume;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;

/**
 * {@code finiteworld:edge_cap}: past {@code radius}, nothing at or above {@code sea_level} is solid.
 * Wraps the final density: vanilla's 3D noise still raises the odd islet at ocean-level
 * continentalness, which the {@code finiteworld:edge} mapping alone cannot prevent.
 */
public record EdgeCapDensityFunction(DensityFunction input, float radius, int seaLevel) implements DensityFunction {
	private static final float AIR = -1.0F;

	public static final MapCodec<EdgeCapDensityFunction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			DensityFunction.CODEC.fieldOf("input").forGetter(EdgeCapDensityFunction::input),
			FiniteRadius.CODEC.fieldOf("radius").forGetter(EdgeCapDensityFunction::radius),
			Codec.INT.fieldOf("sea_level").forGetter(EdgeCapDensityFunction::seaLevel)
		)
		.apply(i, EdgeCapDensityFunction::new));

	static boolean capped(double blockX, double blockZ, int blockY, float radius, int seaLevel) {
		return blockY >= seaLevel && blockX * blockX + blockZ * blockZ > (double) radius * radius;
	}

	@Override
	public DensitySampler compileSampler(final DensityFunction.CompileContext context) {
		return new Sampler(this.input.compileSampler(context), this.radius, this.seaLevel);
	}

	@Override
	public DensityFunction rewriteChildren(final DfRewriteRule rule) {
		DensityFunction input = rule.rewrite(this.input);
		return input == this.input ? this : new EdgeCapDensityFunction(input, this.radius, this.seaLevel);
	}

	@Override
	public Interval range() {
		return Interval.encapsulating(this.input.range(), Interval.ofExact(AIR));
	}

	@Override
	public @DensityFunction.Axes int domainAxes() {
		return this.input.domainAxes() | AXIS_X | AXIS_Y | AXIS_Z;
	}

	@Override
	public MapCodec<EdgeCapDensityFunction> codec() {
		return CODEC;
	}

	private record Sampler(DensitySampler input, float radius, int seaLevel) implements DensitySampler {
		@Override
		public void sampleVolume(final SamplerContext context, final DensityBuffer outputBuffer, final DensityVolume volume) {
			this.input.sampleVolume(context, outputBuffer, volume);
			int index = 0;

			for (int z = 0; z < volume.sizeZ(); z++) {
				for (int x = 0; x < volume.sizeX(); x++) {
					for (int y = 0; y < volume.sizeY(); y++, index++) {
						if (capped(volume.blockX(x), volume.blockZ(z), volume.blockY(y), this.radius, this.seaLevel)) {
							outputBuffer.set(index, Math.min(outputBuffer.get(index), AIR));
						}
					}
				}
			}
		}

		@Override
		public float sampleValue(final SamplerContext context, final int blockX, final int blockY, final int blockZ) {
			float value = this.input.sampleValue(context, blockX, blockY, blockZ);
			return capped(blockX, blockZ, blockY, this.radius, this.seaLevel) ? Math.min(value, AIR) : value;
		}
	}
}
