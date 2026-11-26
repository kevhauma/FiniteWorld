package io.github.kevhauma.finiteworld.anchor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Anchor planning, free of Minecraft types (ANC-2). Samples the unanchored climate on a coarse grid
 * across the disc inside the edge band and scores every spot per checklist biome: its climate distance
 * to the biome's nearest parameter box plus its neighbours' mean distance. The hardest biomes (worst
 * best score) are placed first, each at its best spot that keeps the spacing, refined on a finer grid
 * around it. Each anchor records the offset that pulls its spot into the box.
 */
public final class AnchorPlanner {
	/** Blocks around an anchor centre that its offset reaches; ANC-3 fades the offset out to this radius. */
	public static final int ANCHOR_RADIUS = 192;
	// Anchor discs never overlap. Radius and spacing settled in TUNE-1 (design doc decision 18): 47 anchors need a world radius of about 2,500.
	static final int MIN_SPACING = 2 * ANCHOR_RADIUS;
	// Aim this far inside a box's edge, so the anchored spot does not sit on a biome border.
	static final float MARGIN = 0.05F;
	/**
	 * Largest offset per parameter. The anchor density function's range is computed before any plan exists, so it
	 * widens its input's range by this; discs never overlap, so a point gets at most one anchor's offset.
	 */
	public static final float MAX_OFFSET = 1.0F;
	private static final int PARAMETERS = 5;
	private static final int CONTINENTS = 2;

	private AnchorPlanner() {
	}

	/** One climate parameter at a block column. */
	@FunctionalInterface
	public interface Climate {
		float sample(int x, int z);
	}

	/** A climate range; an infinite side is open. */
	public record Range(float min, float max) {
		Range core() {
			float margin = Math.min(MARGIN, (this.max - this.min) / 4);
			return new Range(this.min + margin, this.max - margin);
		}
	}

	/** One parameter box, ranges in router order (temperature, vegetation, continents, erosion, ridges). */
	public record Box(List<Range> ranges) {
	}

	/** A checklist entry: a biome id and every box it is picked in. */
	public record Biome(String id, List<Box> boxes) {
	}

	/** One anchor: its centre, reach, and the climate offset per parameter (router order) that fits its centre into the biome. */
	public record Anchor(String biome, int x, int z, int radius, List<Float> offset) {
	}

	public record AnchorPlan(int gridPoints, List<Anchor> anchors) {
	}

	/** {@code climate} in router order; one anchor per checklist biome, its whole disc inside {@code radius - band}. */
	public static AnchorPlan plan(List<Climate> climate, List<Biome> checklist, float radius, float band, int step) {
		Grid grid = Grid.sample(climate, radius - band, step);
		float maxCentre = radius - band - ANCHOR_RADIUS;
		List<Target> hardestFirst = checklist.stream()
			.map(biome -> Target.of(biome, grid, maxCentre))
			.sorted(Comparator.comparingDouble(Target::best).reversed())
			.toList();
		List<Anchor> anchors = new ArrayList<>();
		hardestFirst.forEach(target -> anchors.add(target.place(climate, grid, maxCentre, anchors)));
		return new AnchorPlan(grid.points(), List.copyOf(anchors));
	}

	/**
	 * Climate samples on a square grid of side {@code 2 * half + 1} centred on the origin: one array per parameter
	 * (0 outside the disc), which points lie inside the disc, and each point's neighbours inside it.
	 */
	private record Grid(int step, int half, boolean[] inside, float[][] columns, int[][] neighbours) {
		static Grid sample(List<Climate> climate, float radius, int step) {
			int half = (int) radius / step;
			int n = 2 * half + 1;
			boolean[] inside = new boolean[n * n];
			float[][] columns = new float[PARAMETERS][n * n];
			for (int i = 0; i < n * n; i++) {
				int x = coordinate(i / n, half, step);
				int z = coordinate(i % n, half, step);
				inside[i] = (double) x * x + (double) z * z <= (double) radius * radius;
				if (inside[i]) {
					float[] values = AnchorPlanner.sample(climate, x, z);
					for (int p = 0; p < PARAMETERS; p++) {
						columns[p][i] = values[p];
					}
				}
			}
			int[][] neighbours = IntStream.range(0, n * n)
				.mapToObj(i -> IntStream.of(i % n > 0 ? i - 1 : -1, i % n < n - 1 ? i + 1 : -1, i - n, i + n)
					.filter(j -> j >= 0 && j < inside.length && inside[j])
					.toArray())
				.toArray(int[][]::new);
			return new Grid(step, half, inside, columns, neighbours);
		}

		/** Block coordinate of row or column {@code index}. */
		private static int coordinate(int index, int half, int step) {
			return (index - half) * step;
		}

		int x(int i) {
			return coordinate(i / (2 * this.half + 1), this.half, this.step);
		}

		int z(int i) {
			return coordinate(i % (2 * this.half + 1), this.half, this.step);
		}

		/** The climate at point {@code i}, in router order. */
		float[] climate(int i) {
			float[] climate = new float[PARAMETERS];
			for (int p = 0; p < PARAMETERS; p++) {
				climate[p] = this.columns[p][i];
			}
			return climate;
		}

		int points() {
			return (int) IntStream.range(0, this.inside.length).filter(i -> this.inside[i]).count();
		}
	}

	/** A biome with its box cores and its score at every grid point (NaN outside the disc). */
	private record Target(Biome biome, float[][] cores, float[] score, float best) {
		static Target of(Biome biome, Grid grid, float maxCentre) {
			float[][] cores = merge(biome.boxes()).stream()
				.map(box -> box.ranges().stream().map(Range::core).flatMap(r -> Stream.of(r.min(), r.max())).toList())
				.map(AnchorPlanner::toArray)
				.toArray(float[][]::new);
			float[] distance = distances(cores, grid);
			float[] score = new float[distance.length];
			for (int i = 0; i < score.length; i++) {
				score[i] = distance[i] + mean(distance, grid.neighbours()[i], distance[i]);
			}
			float best = (float) IntStream.range(0, score.length)
				.filter(i -> !Float.isNaN(score[i]) && inside(grid.x(i), grid.z(i), maxCentre))
				.mapToDouble(i -> score[i])
				.min()
				.orElseThrow(() -> new IllegalStateException("No room for anchors: radius minus band is under " + ANCHOR_RADIUS));
			return new Target(biome, cores, score, best);
		}

		Anchor place(List<Climate> climate, Grid grid, float maxCentre, List<Anchor> placed) {
			// The lowest score that keeps the spacing; fits() runs only for a better score (a loop, as this scans every grid point).
			int coarse = -1;
			for (int i = 0; i < this.score.length; i++) {
				if (this.score[i] < (coarse < 0 ? Float.POSITIVE_INFINITY : this.score[coarse]) && fits(grid.x(i), grid.z(i), maxCentre, placed)) {
					coarse = i;
				}
			}
			if (coarse < 0) {
				throw new IllegalStateException("No room for the " + this.biome.id() + " anchor; " + placed.size() + " placed");
			}
			// Refine on a quarter-step grid around the coarse spot; the coarse spot itself wins ties.
			int fine = grid.step() / 4;
			Spot centre = this.spot(grid.x(coarse), grid.z(coarse), grid.climate(coarse));
			Spot best = Stream.concat(Stream.of(centre), IntStream.rangeClosed(-2, 2).boxed()
					.flatMap(dx -> IntStream.rangeClosed(-2, 2).filter(dz -> dx != 0 || dz != 0).mapToObj(dz -> new Column(centre.x() + dx * fine, centre.z() + dz * fine)))
					.filter(c -> fits(c.x(), c.z(), maxCentre, placed))
					.map(c -> this.spot(c.x(), c.z(), sample(climate, c.x(), c.z()))))
				.min(Comparator.comparingDouble(Spot::distance))
				.orElseThrow();
			float[] core = this.cores[best.nearest()];
			List<Float> offset = IntStream.range(0, best.climate().length)
				.mapToObj(p -> Math.clamp(Math.clamp(best.climate()[p], core[2 * p], core[2 * p + 1]) - best.climate()[p], -MAX_OFFSET, MAX_OFFSET))
				.toList();
			return new Anchor(this.biome.id(), best.x(), best.z(), ANCHOR_RADIUS, offset);
		}

		private Spot spot(int x, int z, float[] climate) {
			int nearest = nearest(this.cores, climate);
			return new Spot(x, z, climate, nearest, distance(this.cores[nearest], climate));
		}
	}

	private record Spot(int x, int z, float[] climate, int nearest, float distance) {
	}

	/** A block column. */
	private record Column(int x, int z) {
	}

	/**
	 * The boxes cut to continentalness at or above {@code from}, dropping those wholly below it; all of them unchanged
	 * if none reaches it (a biome that only exists below, such as a beach).
	 */
	static List<Box> inland(List<Box> boxes, float from) {
		List<Box> inland = boxes.stream()
			.filter(box -> box.ranges().get(CONTINENTS).max() > from)
			.map(box -> {
				List<Range> ranges = new ArrayList<>(box.ranges());
				ranges.set(CONTINENTS, new Range(Math.max(ranges.get(CONTINENTS).min(), from), ranges.get(CONTINENTS).max()));
				return new Box(List.copyOf(ranges));
			})
			.toList();
		return inland.isEmpty() ? boxes : inland;
	}

	/**
	 * Joins boxes that match on every parameter but one and touch on that one, until none do. Vanilla
	 * lists a biome as many adjacent slices; merged, there are fewer boxes to score and no margin at the
	 * seams inside one biome.
	 */
	static List<Box> merge(List<Box> boxes) {
		List<Box> merged = boxes.stream().distinct().toList();
		int before;
		do {
			before = merged.size();
			for (int p = 0; p < PARAMETERS; p++) {
				merged = mergeAlong(merged, p);
			}
		} while (merged.size() < before);
		return merged;
	}

	/** Groups the boxes by their other four ranges, then joins touching runs along {@code p}. */
	private static List<Box> mergeAlong(List<Box> boxes, int p) {
		return boxes.stream()
			.collect(Collectors.groupingBy(box -> IntStream.range(0, PARAMETERS).filter(q -> q != p).mapToObj(box.ranges()::get).toList(),
				LinkedHashMap::new, Collectors.toList()))
			.values().stream()
			.flatMap(group -> {
				List<Box> joined = new ArrayList<>();
				group.stream().sorted(Comparator.comparingDouble(box -> box.ranges().get(p).min())).forEach(box -> {
					Range next = box.ranges().get(p);
					Range last = joined.isEmpty() ? null : joined.getLast().ranges().get(p);
					if (last != null && next.min() <= last.max()) {
						List<Range> ranges = new ArrayList<>(box.ranges());
						ranges.set(p, new Range(last.min(), Math.max(last.max(), next.max())));
						joined.set(joined.size() - 1, new Box(List.copyOf(ranges)));
					} else {
						joined.add(box);
					}
				});
				return joined.stream();
			})
			.toList();
	}

	private static float[] sample(List<Climate> climate, int x, int z) {
		return toArray(climate.stream().map(parameter -> parameter.sample(x, z)).toList());
	}

	private static float[] toArray(List<Float> values) {
		float[] array = new float[values.size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = values.get(i);
		}
		return array;
	}

	private static boolean inside(int x, int z, float maxCentre) {
		return (double) x * x + (double) z * z <= (double) maxCentre * maxCentre;
	}

	// Loops, not streams, in this and mean(): both run per grid point per biome.
	private static boolean fits(int x, int z, float maxCentre, List<Anchor> placed) {
		if (!inside(x, z, maxCentre)) {
			return false;
		}
		for (Anchor a : placed) {
			if ((double) (a.x() - x) * (a.x() - x) + (double) (a.z() - z) * (a.z() - z) < (double) MIN_SPACING * MIN_SPACING) {
				return false;
			}
		}
		return true;
	}

	private static float mean(float[] values, int[] indices, float fallback) {
		if (indices.length == 0) {
			return fallback;
		}
		float sum = 0;
		for (int i : indices) {
			sum += values[i];
		}
		return sum / indices.length;
	}

	/**
	 * Squared climate distance from every grid point to the nearest box core (vanilla's fitness, without
	 * depth and offset), NaN outside the disc. Hot loop (boxes x parameters x grid points): box by box over
	 * flat per-parameter arrays, so the JIT can vectorise the inner loop.
	 */
	private static float[] distances(float[][] cores, Grid grid) {
		int n = grid.inside().length;
		float[] distance = new float[n];
		float[] sum = new float[n];
		Arrays.fill(distance, Float.POSITIVE_INFINITY);
		for (float[] core : cores) {
			Arrays.fill(sum, 0.0F);
			for (int p = 0; p < PARAMETERS; p++) {
				float min = core[2 * p];
				float max = core[2 * p + 1];
				if (min == Float.NEGATIVE_INFINITY && max == Float.POSITIVE_INFINITY) {
					continue;
				}
				float[] values = grid.columns()[p];
				for (int i = 0; i < n; i++) {
					float gap = Math.max(Math.max(min - values[i], values[i] - max), 0.0F);
					sum[i] += gap * gap;
				}
			}
			for (int i = 0; i < n; i++) {
				distance[i] = Math.min(distance[i], sum[i]);
			}
		}
		for (int i = 0; i < n; i++) {
			if (!grid.inside()[i]) {
				distance[i] = Float.NaN;
			}
		}
		return distance;
	}

	/** The index of the box core nearest to {@code climate}; the lowest on ties. */
	private static int nearest(float[][] cores, float[] climate) {
		int nearest = 0;
		for (int box = 1; box < cores.length; box++) {
			if (distance(cores[box], climate) < distance(cores[nearest], climate)) {
				nearest = box;
			}
		}
		return nearest;
	}

	/** Squared climate distance to one box core, the same sum as {@link #distances}. */
	private static float distance(float[] core, float[] climate) {
		float sum = 0.0F;
		for (int p = 0; p < PARAMETERS; p++) {
			float gap = Math.max(Math.max(core[2 * p] - climate[p], climate[p] - core[2 * p + 1]), 0.0F);
			sum += gap * gap;
		}
		return sum;
	}
}
