package io.github.kevhauma.finiteworld.edge;

/**
 * Edge math, free of Minecraft types: blends vanilla continentalness into the ocean range across
 * the fade band {@code [radius - band, radius]} and holds it there past the radius.
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
}
