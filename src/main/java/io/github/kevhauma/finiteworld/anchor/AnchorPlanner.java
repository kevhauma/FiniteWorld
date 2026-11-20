package io.github.kevhauma.finiteworld.anchor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Anchor planning, free of Minecraft types. ANC-1 stub: samples each climate parameter on a coarse
 * grid across the disc and keeps the spots with its lowest and highest value. ANC-2 replaces the
 * selection with the real per-biome search.
 */
public final class AnchorPlanner {
	private AnchorPlanner() {
	}

	/** One climate parameter at a block column. */
	@FunctionalInterface
	public interface Climate {
		float sample(int x, int z);
	}

	public record Anchor(int x, int z) {
	}

	public record AnchorPlan(int gridPoints, List<Anchor> anchors) {
	}

	private record Sample(Anchor at, float value) {
	}

	/** {@code climate} in router order (temperature, vegetation, continents, erosion, ridges); two anchors per parameter. */
	public static AnchorPlan plan(List<Climate> climate, float radius, int step) {
		List<Anchor> grid = grid(radius, step);
		Comparator<Sample> byValue = Comparator.comparingDouble(Sample::value);
		List<Anchor> anchors = climate.stream()
			.flatMap(parameter -> {
				List<Sample> samples = grid.stream().map(at -> new Sample(at, parameter.sample(at.x(), at.z()))).toList();
				return Stream.of(samples.stream().min(byValue), samples.stream().max(byValue));
			})
			.map(sample -> sample.orElseThrow().at())
			.toList();
		return new AnchorPlan(grid.size(), anchors);
	}

	/** Grid points on multiples of {@code step} within {@code radius} of the origin, in a fixed order. */
	static List<Anchor> grid(float radius, int step) {
		int extent = (int) radius / step * step;
		double radiusSquared = (double) radius * radius;
		return IntStream.iterate(-extent, x -> x <= extent, x -> x + step).boxed()
			.flatMap(x -> IntStream.iterate(-extent, z -> z <= extent, z -> z + step).mapToObj(z -> new Anchor(x, z)))
			.filter(at -> (double) at.x() * at.x() + (double) at.z() * at.z() <= radiusSquared)
			.toList();
	}
}
