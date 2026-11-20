package io.github.kevhauma.finiteworld.anchor;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.Anchor;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.AnchorPlan;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.Biome;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.Box;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.Range;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class AnchorPlannerTest {
	private static final float RADIUS = 4000.0F;
	private static final float BAND = 500.0F;
	private static final int STEP = 64;
	private static final float OPEN = Float.POSITIVE_INFINITY;
	// Smooth synthetic climate in router order, within -1..1 like vanilla's.
	private static final List<AnchorPlanner.Climate> CLIMATE = List.of(
		(x, z) -> x / RADIUS,
		(x, z) -> z / RADIUS,
		(x, z) -> (float) (Math.sin(x / 700.0) * Math.cos(z / 900.0)),
		(x, z) -> (float) Math.cos(x / 500.0 + z / 800.0),
		(x, z) -> (float) Math.sin((x - z) / 600.0)
	);
	private static final List<Range> BANDS = List.of(new Range(-OPEN, -0.6F), new Range(-0.6F, -0.2F), new Range(-0.2F, 0.2F), new Range(0.2F, 0.6F), new Range(0.6F, OPEN));
	private static final List<Range> HALVES = List.of(new Range(-OPEN, 0.0F), new Range(0.0F, OPEN));
	private static final Range ANY = new Range(-OPEN, OPEN);
	// Mushroom-like: continentalness below anything the synthetic climate reaches.
	private static final Biome MUSHROOM = new Biome("mushroom", List.of(new Box(List.of(ANY, ANY, new Range(-OPEN, -1.05F), ANY, ANY))));
	// 5 temperature x 5 humidity x 2 weirdness biomes plus the mushroom: 51 biomes. Each of the 50 has 20 boxes that do not
	// touch (every other continents and erosion slice), so they stay apart after merging: 1001 boxes, near vanilla's 849 merged.
	private static final List<Biome> CHECKLIST = Stream.concat(
		IntStream.range(0, 5).boxed().flatMap(t -> IntStream.range(0, 5).boxed().flatMap(h -> IntStream.range(0, 2).mapToObj(w -> new Biome("t" + t + "h" + h + "w" + w,
			IntStream.range(0, 20).mapToObj(i -> new Box(List.of(BANDS.get(t), BANDS.get(h), slice(2 * (i / 4), 9), slice(2 * (i % 4), 7), HALVES.get(w)))).toList())))),
		Stream.of(MUSHROOM)
	).toList();

	@Test
	void sameClimateGivesTheSamePlan() {
		assertEquals(plan(), plan());
	}

	@Test
	void everyBiomeGetsOneAnchorInsideTheBandAndApart() {
		List<Anchor> anchors = plan().anchors();
		assertEquals(CHECKLIST.stream().map(Biome::id).sorted().toList(), anchors.stream().map(Anchor::biome).sorted().toList());
		assertAll(anchors.stream().map(a -> () -> assertTrue(Math.hypot(a.x(), a.z()) + a.radius() <= RADIUS - BAND, () -> a + " reaches into the band")));
		assertAll(anchors.stream().flatMap(a -> anchors.stream().filter(b -> b != a).map(b -> () ->
			assertTrue(Math.hypot(a.x() - b.x(), a.z() - b.z()) >= AnchorPlanner.MIN_SPACING, () -> a + " too close to " + b))));
	}

	@Test
	void offsetFitsEachAnchorIntoItsBiome() {
		assertAll(plan().anchors().stream().map(a -> () -> {
			List<Float> fitted = IntStream.range(0, CLIMATE.size()).mapToObj(p -> CLIMATE.get(p).sample(a.x(), a.z()) + a.offset().get(p)).toList();
			Biome biome = CHECKLIST.stream().filter(b -> b.id().equals(a.biome())).findFirst().orElseThrow();
			assertTrue(biome.boxes().stream().anyMatch(box -> IntStream.range(0, fitted.size())
				.allMatch(p -> fitted.get(p) >= box.ranges().get(p).min() - 1e-6F && fitted.get(p) <= box.ranges().get(p).max() + 1e-6F)), () -> a + " lands at " + fitted);
		}));
	}

	@Test
	void biomeTheClimateContainsGetsANearZeroOffset() {
		Anchor central = anchor("t2h2w0");
		assertTrue(central.offset().stream().allMatch(o -> Math.abs(o) < 1e-3F), () -> central.toString());
		// The mushroom stand-in needs a real pull below continentalness -1.05.
		assertTrue(anchor("mushroom").offset().get(2) < -0.05F, () -> anchor("mushroom").toString());
	}

	@Test
	void offsetIsCappedAtMaxOffset() {
		// Continentalness below -3 is out of reach (the climate spans -1..1), so the pull is capped.
		Biome unreachable = new Biome("unreachable", List.of(new Box(List.of(ANY, ANY, new Range(-OPEN, -3.0F), ANY, ANY))));
		Anchor anchor = AnchorPlanner.plan(CLIMATE, List.of(unreachable), RADIUS, BAND, STEP).anchors().getFirst();
		assertEquals(-AnchorPlanner.MAX_OFFSET, (float) anchor.offset().get(2));
		assertTrue(plan().anchors().stream().flatMap(a -> a.offset().stream()).allMatch(o -> Math.abs(o) <= AnchorPlanner.MAX_OFFSET));
	}

	@Test
	void mergesTouchingSlicesOfABiome() {
		Box low = new Box(List.of(ANY, ANY, slice(0, 5), ANY, ANY));
		Box mid = new Box(List.of(ANY, ANY, slice(1, 5), ANY, ANY));
		Box apart = new Box(List.of(ANY, ANY, slice(3, 5), ANY, ANY));
		Box diagonal = new Box(List.of(BANDS.get(0), ANY, slice(2, 5), ANY, ANY));
		assertEquals(List.of(new Box(List.of(ANY, ANY, new Range(-OPEN, slice(1, 5).max()), ANY, ANY)), apart, diagonal),
			AnchorPlanner.merge(List.of(low, apart, diagonal, mid, low)));
		// The synthetic biomes' slices never touch, so merging keeps all 20.
		assertTrue(CHECKLIST.stream().filter(biome -> biome != MUSHROOM).allMatch(biome -> AnchorPlanner.merge(biome.boxes()).size() == 20));
	}

	@Test
	void plansWithinTheAnc1BudgetAtRadius4000() {
		// ANC-1 planned in 46-73 ms per world; the planner's own work (this sampler is near free) stays in that range. Best of 3 runs, so a busy machine does not fail it.
		long best = LongStream.range(0, 3).map(run -> {
			long start = System.nanoTime();
			plan();
			return (System.nanoTime() - start) / 1_000_000;
		}).min().orElseThrow();
		assertTrue(best <= 100, () -> "Planning took " + best + " ms");
	}

	private static AnchorPlan plan() {
		return AnchorPlanner.plan(CLIMATE, CHECKLIST, RADIUS, BAND, STEP);
	}

	private static Anchor anchor(String biome) {
		return plan().anchors().stream().filter(a -> a.biome().equals(biome)).findFirst().orElseThrow();
	}

	/** Slice {@code i} of {@code n} equal slices of -1..1, outer sides open. */
	private static Range slice(int i, int n) {
		return new Range(i == 0 ? -OPEN : -1 + 2.0F * i / n, i == n - 1 ? OPEN : -1 + 2.0F * (i + 1) / n);
	}
}
