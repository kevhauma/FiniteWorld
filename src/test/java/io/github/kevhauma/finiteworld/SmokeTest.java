package io.github.kevhauma.finiteworld;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// Placeholder proving the JUnit setup runs; EDGE-1 replaces it with real tests.
class SmokeTest {
	@Test
	void modIdIsFiniteworld() {
		assertEquals("finiteworld", FiniteWorld.MOD_ID);
	}
}
