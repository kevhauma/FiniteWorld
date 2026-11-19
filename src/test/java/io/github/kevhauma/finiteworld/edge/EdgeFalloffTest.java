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
}
