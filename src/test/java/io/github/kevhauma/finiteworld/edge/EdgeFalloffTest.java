package io.github.kevhauma.finiteworld.edge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EdgeFalloffTest {
	private static final float RADIUS = 4000.0F;
	private static final float BAND = 500.0F;
	// OverworldBiomeBuilder (26.3) continentalness bounds.
	private static final float MUSHROOM_FIELDS_MAX = -1.05F;
	private static final float DEEP_OCEAN_MAX = -0.455F;
	private static final float COAST_MIN = -0.19F;

	@Test
	void unchangedInsideTheBand() {
		for (double distance : new double[] {0, 1000, 3000, RADIUS - BAND}) {
			for (float c = -1.5F; c <= 1.5F; c += 0.01F) {
				assertEquals(c, EdgeFalloff.apply(c, distance, RADIUS, BAND), "c=" + c + " d=" + distance);
			}
		}
	}

	@Test
	void smoothAndMonotonicAcrossTheBand() {
		for (float c = -1.5F; c <= 1.5F; c += 0.05F) {
			float ocean = EdgeFalloff.ocean(c);
			float sign = Math.signum(ocean - c);
			float previous = c;
			for (double d = RADIUS - BAND; d <= RADIUS; d += 1.0) {
				float value = EdgeFalloff.apply(c, d, RADIUS, BAND);
				assertTrue(sign * (value - previous) >= 0, "not monotonic: c=" + c + " d=" + d);
				// a 1-block step never moves more than smoothstep's peak slope (1.5 / band) allows
				assertTrue(Math.abs(value - previous) <= Math.abs(ocean - c) * 1.5F / BAND + 1e-6F, "jump: c=" + c + " d=" + d);
				previous = value;
			}
			assertEquals(ocean, previous, "c=" + c);
		}
	}

	@Test
	void alwaysInTheOceanRangePastTheRadius() {
		for (double d : new double[] {RADIUS, RADIUS + 1, 4500, 10_000, 1_000_000}) {
			for (float c = -3.0F; c <= 3.0F; c += 0.01F) {
				float value = EdgeFalloff.apply(c, d, RADIUS, BAND);
				assertTrue(value > MUSHROOM_FIELDS_MAX && value < COAST_MIN, "c=" + c + " d=" + d + " -> " + value);
			}
		}
	}

	@Test
	void pastTheRadiusKeepsBothDeepAndShallowOcean() {
		// vanilla continentalness spans about [-1, 1]
		assertTrue(EdgeFalloff.ocean(-1.0F) < DEEP_OCEAN_MAX);
		assertTrue(EdgeFalloff.ocean(1.0F) > DEEP_OCEAN_MAX);
	}

	private static final float NARROW_BAND = 200.0F;
	private static final float OUTLINE_DEPTH = 300.0F;

	@Test
	void outlineNeverPushesLandPastTheRadius() {
		for (float outline = -5.0F; outline <= 5.0F; outline += 0.01F) {
			float inset = EdgeFalloff.inset(outline, OUTLINE_DEPTH);
			assertTrue(inset >= 0.0F && inset <= OUTLINE_DEPTH, "outline=" + outline + " -> inset " + inset);
			for (float band : new float[] {NARROW_BAND, 350.0F, BAND}) {
				for (double d : new double[] {RADIUS, RADIUS + 1, 10_000}) {
					for (float c = -3.0F; c <= 3.0F; c += 0.1F) {
						float value = EdgeFalloff.apply(c, d + inset, RADIUS, band);
						assertTrue(value > MUSHROOM_FIELDS_MAX && value < COAST_MIN, "outline=" + outline + " band=" + band + " c=" + c + " d=" + d);
					}
				}
			}
		}
		// the outline really moves the coast: a high outline pulls the full-ocean point inward by the depth
		assertEquals(1.0F, EdgeFalloff.weight(RADIUS - OUTLINE_DEPTH + EdgeFalloff.inset(1.0F, OUTLINE_DEPTH), RADIUS, BAND));
		assertTrue(EdgeFalloff.weight(RADIUS - OUTLINE_DEPTH + EdgeFalloff.inset(-1.0F, OUTLINE_DEPTH), RADIUS, BAND) < 1.0F);
	}

	@Test
	void bandWidthIsMonotonicInRuggedness() {
		assertEquals(BAND, EdgeFalloff.band(0.0F, BAND, NARROW_BAND));
		assertEquals(NARROW_BAND, EdgeFalloff.band(1.0F, BAND, NARROW_BAND));
		float previous = Float.MAX_VALUE;
		for (float r = 0.0F; r <= 1.0F; r += 0.01F) {
			float band = EdgeFalloff.band(r, BAND, NARROW_BAND);
			assertTrue(band <= previous, "band grew at ruggedness " + r);
			previous = band;
		}
	}

	@Test
	void ruggednessRisesWithLowErosionAndPeaks() {
		float valley = 0.0F;
		float peak = 2.0F / 3.0F;
		assertEquals(-1.0F, EdgeFalloff.peaksAndValleys(valley), 1e-6F);
		assertEquals(1.0F, EdgeFalloff.peaksAndValleys(peak), 1e-6F);
		assertEquals(1.0F, EdgeFalloff.ruggedness(-1.0F, peak), 1e-6F);
		for (float ridges = -1.0F; ridges <= 1.0F; ridges += 0.05F) {
			// flat (high-erosion) terrain keeps the wide band whatever the ridges
			assertEquals(0.0F, EdgeFalloff.ruggedness(0.5F, ridges));
			float previous = -1.0F;
			for (float erosion = 1.0F; erosion >= -1.0F; erosion -= 0.01F) {
				float r = EdgeFalloff.ruggedness(erosion, ridges);
				assertTrue(r >= previous && r <= 1.0F, "not monotonic in erosion: e=" + erosion + " ridges=" + ridges);
				previous = r;
			}
		}
		for (float erosion = -1.0F; erosion <= 1.0F; erosion += 0.05F) {
			assertTrue(EdgeFalloff.ruggedness(erosion, peak) >= EdgeFalloff.ruggedness(erosion, valley), "e=" + erosion);
		}
	}
}
