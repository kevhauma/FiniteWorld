package io.github.kevhauma.finiteworld.anchor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.Anchor;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnchorFieldTest {
	private static final int R = AnchorPlanner.ANCHOR_RADIUS;
	private static final float R2 = (float) R * R;
	// Spaced exactly MIN_SPACING apart (discs touch), across negative coordinates and cell borders; one moves only erosion.
	private static final List<Anchor> ANCHORS = List.of(
		new Anchor("a", 0, 0, R, List.of(0.4F, 0.0F, -0.3F, 0.0F, 0.0F)),
		new Anchor("b", AnchorPlanner.MIN_SPACING, 0, R, List.of(-AnchorPlanner.MAX_OFFSET, 0.0F, 0.2F, 0.0F, 0.0F)),
		new Anchor("c", -500, -1700, R, List.of(AnchorPlanner.MAX_OFFSET, 0.0F, 0.0F, 0.0F, 0.0F)),
		new Anchor("d", 1000, 1000, R, List.of(0.0F, 0.0F, 0.0F, 0.7F, 0.0F))
	);

	@Test
	void falloffIsFullAtTheCentreZeroAtTheRadiusAndSmoothBetween() {
		assertEquals(1.0F, AnchorField.falloff(0.0F, R2));
		assertEquals(0.0F, AnchorField.falloff(R2, R2));
		assertEquals(0.0F, AnchorField.falloff(4 * R2, R2));
		// Flat at both ends: one block off the centre or the rim barely moves it.
		assertTrue(AnchorField.falloff(1.0F, R2) > 0.9999F);
		assertTrue(AnchorField.falloff((R - 1.0F) * (R - 1.0F), R2) < 2e-4F);
		float previous = 1.0F;
		for (int d = 1; d <= R; d++) {
			float value = AnchorField.falloff((float) d * d, R2);
			assertTrue(value <= previous, "not monotonic at d=" + d);
			// d/dd (1 - d²/r²)² peaks at 8 / (3√3 r) (d = r/√3), about 1.54 / r per block
			assertTrue(previous - value <= 8.0F / (3.0F * (float) Math.sqrt(3.0) * R) + 1e-6F, "jump at d=" + d);
			previous = value;
		}
	}

	@Test
	void gridMatchesTheSumOverEveryAnchor() {
		for (int p = 0; p < 5; p++) {
			AnchorField field = AnchorField.of(ANCHORS, p);
			for (int x = -1000; x <= 1500; x += 7) {
				for (int z = -2000; z <= 1500; z += 7) {
					assertEquals(bruteForce(p, x, z), field.offset(x, z), 1e-6F, "p=" + p + " (" + x + ", " + z + ")");
				}
			}
		}
	}

	@Test
	void fullOffsetAtTheCentreAndNoneFromTheRadiusOn() {
		AnchorField temperature = AnchorField.of(ANCHORS, 0);
		assertEquals(0.4F, temperature.offset(0, 0));
		assertEquals(AnchorPlanner.MAX_OFFSET, temperature.offset(-500, -1700));
		assertEquals(0.0F, temperature.offset(R, 0));
		assertEquals(0.0F, temperature.offset(0, -R));
		assertEquals(0.0F, temperature.offset(1000, 1000));
	}

	@Test
	void touchingDiscsNeverAddUpPastMaxOffset() {
		AnchorField temperature = AnchorField.of(ANCHORS, 0);
		for (int x = -R; x <= AnchorPlanner.MIN_SPACING + R; x++) {
			assertTrue(Math.abs(temperature.offset(x, 0)) <= AnchorPlanner.MAX_OFFSET, "x=" + x);
		}
	}

	@Test
	void parameterNoAnchorMovesIsEmpty() {
		assertTrue(AnchorField.of(ANCHORS, 1).isEmpty());
		assertTrue(AnchorField.of(List.of(), 0).isEmpty());
		assertEquals(0.0F, AnchorField.of(ANCHORS, 1).offset(0, 0));
	}

	private static float bruteForce(int p, int x, int z) {
		return (float) ANCHORS.stream()
			.mapToDouble(a -> AnchorField.falloff((float) (x - a.x()) * (x - a.x()) + (float) (z - a.z()) * (z - a.z()), (float) a.radius() * a.radius()) * a.offset().get(p))
			.sum();
	}
}
