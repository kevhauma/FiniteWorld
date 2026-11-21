package io.github.kevhauma.finiteworld.edge;

/**
 * Edge math, free of Minecraft types: blends vanilla continentalness into the ocean range across
 * the fade band {@code [radius - band, radius]} and holds it there past the radius. The band narrows
 * under rugged terrain ({@link #band}) and wanders inward by an outline {@link #inset}.
 */
public final class EdgeFalloff {
	// Kept clear of OverworldBiomeBuilder (26.3): mushroom fields below -1.05, coast from -0.19.
	public static final float OCEAN_MIN = -1.0F;
	public static final float OCEAN_MAX = -0.22F;

	private EdgeFalloff() {
	}

	/**
	 * Vanilla continentalness remapped linearly into the ocean range. It keeps the noise's variation,
	 * so the deep-ocean/ocean split (-0.455) still falls inside it and both biome families appear.
	 * Vanilla's 3D noise can still raise the odd islet here; {@link EdgeCapDensityFunction} keeps it underwater.
	 */
	public static float ocean(float continentalness) {
		return Math.clamp(-0.5F + 0.3F * continentalness, OCEAN_MIN, OCEAN_MAX);
	}

	/** Blend weight: 0 inside the band, 1 at and past the radius, smoothstep in between. */
	public static float weight(double distance, float radius, float band) {
		float t = (float) Math.clamp((distance - (radius - band)) / band, 0.0, 1.0);
		return t * t * (3.0F - 2.0F * t);
	}

	/** Continentalness at {@code distance} blocks from the world origin. */
	public static float apply(float continentalness, double distance, float radius, float band) {
		float t = weight(distance, radius, band);
		if (t == 0.0F) {
			return continentalness;
		}
		float ocean = ocean(continentalness);
		return t == 1.0F ? ocean : continentalness + t * (ocean - continentalness);
	}

	/**
	 * 0 for flat terrain, 1 for peaks: low erosion (OverworldBiomeBuilder's levels 0-2, below 0.05) times
	 * peaks-and-valleys, with low erosion alone counting half.
	 */
	public static float ruggedness(float erosion, float ridges) {
		float low = Math.clamp((0.05F - erosion) / 0.6F, 0.0F, 1.0F);
		float peaks = Math.clamp((peaksAndValleys(ridges) + 0.2F) / 0.9F, 0.0F, 1.0F);
		return low * (0.5F + 0.5F * peaks);
	}

	/** Vanilla's folded ridges (NoiseRouterData.peaksAndValleys): -1 in valleys, 1 on peaks. */
	static float peaksAndValleys(float ridges) {
		return -3.0F * (Math.abs(Math.abs(ridges) - 2.0F / 3.0F) - 1.0F / 3.0F);
	}

	/** Band width: {@code wide} on flat terrain, {@code narrow} under peaks. */
	public static float band(float ruggedness, float wide, float narrow) {
		return wide + ruggedness * (narrow - wide);
	}

	/** How far the band moves inward, in {@code [0, depth]}: never outward, so the radius stays all ocean. */
	public static float inset(float outline, float depth) {
		return depth * Math.clamp(0.5F + 0.5F * outline, 0.0F, 1.0F);
	}
}
