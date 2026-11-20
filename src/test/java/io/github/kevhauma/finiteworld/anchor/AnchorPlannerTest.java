package io.github.kevhauma.finiteworld.anchor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.Anchor;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.AnchorPlan;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnchorPlannerTest {
	private static final float RADIUS = 4000.0F;
	private static final List<AnchorPlanner.Climate> CLIMATE = List.of((x, z) -> x, (x, z) -> z, (x, z) -> x + z, (x, z) -> -x, (x, z) -> (float) Math.sin(x * 0.01) * z);

	@Test
	void gridStaysInsideTheDisc() {
		List<Anchor> grid = AnchorPlanner.grid(RADIUS, 64);
		assertTrue(grid.stream().allMatch(a -> (double) a.x() * a.x() + (double) a.z() * a.z() <= (double) RADIUS * RADIUS));
		// Close to the disc's area in grid cells.
		assertEquals(Math.PI * RADIUS * RADIUS / (64 * 64), grid.size(), grid.size() * 0.01);
	}

	@Test
	void picksTheLowestAndHighestSpotPerParameter() {
		AnchorPlan plan = AnchorPlanner.plan(CLIMATE.subList(0, 1), RADIUS, 64);
		// Temperature = x: the westmost and eastmost grid columns (x = ±3968, |z| <= 448); ties keep the first in grid order (lowest z).
		assertEquals(List.of(new Anchor(-3968, -448), new Anchor(3968, -448)), plan.anchors());
	}

	@Test
	void sameClimateGivesTheSamePlan() {
		AnchorPlan plan = AnchorPlanner.plan(CLIMATE, RADIUS, 64);
		assertEquals(plan, AnchorPlanner.plan(CLIMATE, RADIUS, 64));
		assertEquals(2 * CLIMATE.size(), plan.anchors().size());
	}
}
