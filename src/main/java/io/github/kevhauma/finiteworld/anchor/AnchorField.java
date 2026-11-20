package io.github.kevhauma.finiteworld.anchor;

import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.Anchor;

import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

/**
 * One climate parameter's anchor offsets as a field over (x, z), free of Minecraft types (ANC-3):
 * {@code Σ falloff(distance) × offset}. Built once per compile: the anchors with a non-zero offset for the
 * parameter are bucketed on a grid of cells one anchor disc wide, so a sample reads one cell's few
 * anchors from flat arrays, with no allocation or map lookup.
 */
final class AnchorField {
	static final int CELL = 2 * AnchorPlanner.ANCHOR_RADIUS;
	private static final AnchorField EMPTY = new AnchorField(0, 0, 0, 0, new int[] {0}, new float[0], new float[0], new float[0], new float[0]);

	private final int minCellX;
	private final int minCellZ;
	private final int cellsX;
	private final int cellsZ;
	// Entries of cell i are [start[i], start[i + 1]); an anchor is listed in every cell its disc's bounding square touches.
	private final int[] start;
	private final float[] x;
	private final float[] z;
	private final float[] radiusSq;
	private final float[] offset;

	private AnchorField(int minCellX, int minCellZ, int cellsX, int cellsZ, int[] start, float[] x, float[] z, float[] radiusSq, float[] offset) {
		this.minCellX = minCellX;
		this.minCellZ = minCellZ;
		this.cellsX = cellsX;
		this.cellsZ = cellsZ;
		this.start = start;
		this.x = x;
		this.z = z;
		this.radiusSq = radiusSq;
		this.offset = offset;
	}

	/** The field of {@code parameter} (router order index into {@link Anchor#offset()}). */
	static AnchorField of(List<Anchor> anchors, int parameter) {
		List<Anchor> active = anchors.stream().filter(a -> a.offset().get(parameter) != 0.0F).toList();
		if (active.isEmpty()) {
			return EMPTY;
		}
		int minCellX = active.stream().mapToInt(a -> Math.floorDiv(a.x() - a.radius(), CELL)).min().orElseThrow();
		int minCellZ = active.stream().mapToInt(a -> Math.floorDiv(a.z() - a.radius(), CELL)).min().orElseThrow();
		int cellsX = active.stream().mapToInt(a -> Math.floorDiv(a.x() + a.radius(), CELL)).max().orElseThrow() - minCellX + 1;
		int cellsZ = active.stream().mapToInt(a -> Math.floorDiv(a.z() + a.radius(), CELL)).max().orElseThrow() - minCellZ + 1;
		List<List<Anchor>> cells = IntStream.range(0, cellsX * cellsZ)
			.mapToObj(i -> active.stream().filter(a -> touches(a, minCellX + i % cellsX, minCellZ + i / cellsX)).toList())
			.toList();
		int[] start = new int[cells.size() + 1];
		IntStream.range(0, cells.size()).forEach(i -> start[i + 1] = start[i] + cells.get(i).size());
		List<Anchor> entries = cells.stream().flatMap(List::stream).toList();
		return new AnchorField(minCellX, minCellZ, cellsX, cellsZ, start,
			floats(entries, a -> a.x()), floats(entries, a -> a.z()), floats(entries, a -> (float) a.radius() * a.radius()), floats(entries, a -> a.offset().get(parameter)));
	}

	/** Whether every sample is 0 (no anchor moves this parameter). */
	boolean isEmpty() {
		return this.offset.length == 0;
	}

	/** Hot path: the summed anchor offset at a block column. */
	float offset(int blockX, int blockZ) {
		int cellX = Math.floorDiv(blockX, CELL) - this.minCellX;
		int cellZ = Math.floorDiv(blockZ, CELL) - this.minCellZ;
		if (cellX < 0 || cellZ < 0 || cellX >= this.cellsX || cellZ >= this.cellsZ) {
			return 0.0F;
		}
		int cell = cellZ * this.cellsX + cellX;
		float sum = 0.0F;
		for (int i = this.start[cell]; i < this.start[cell + 1]; i++) {
			float dx = blockX - this.x[i];
			float dz = blockZ - this.z[i];
			sum += falloff(dx * dx + dz * dz, this.radiusSq[i]) * this.offset[i];
		}
		return sum;
	}

	/**
	 * Share of an anchor's offset at squared distance {@code distanceSq} from its centre: {@code (1 - d²/r²)²},
	 * 1 at the centre, 0 from the radius on, with zero slope at both ends so neither the centre nor the rim shows a crease.
	 */
	static float falloff(float distanceSq, float radiusSq) {
		if (distanceSq >= radiusSq) {
			return 0.0F;
		}
		float t = 1.0F - distanceSq / radiusSq;
		return t * t;
	}

	private static boolean touches(Anchor a, int cellX, int cellZ) {
		return a.x() - a.radius() < (cellX + 1) * CELL && a.x() + a.radius() >= cellX * CELL
			&& a.z() - a.radius() < (cellZ + 1) * CELL && a.z() + a.radius() >= cellZ * CELL;
	}

	private static float[] floats(List<Anchor> anchors, ToDoubleFunction<Anchor> value) {
		float[] array = new float[anchors.size()];
		IntStream.range(0, array.length).forEach(i -> array[i] = (float) value.applyAsDouble(anchors.get(i)));
		return array;
	}
}
