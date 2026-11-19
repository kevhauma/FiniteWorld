package io.github.kevhauma.finiteworld;

import io.github.kevhauma.finiteworld.edge.EdgeCapDensityFunction;
import io.github.kevhauma.finiteworld.edge.EdgeDensityFunction;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

/**
 * TEST-1: builds the Finite world preset's generator for fixed seeds and radii and checks the
 * design's hard rules. Edge: no land past the radius (asserted). Biomes: checklist coverage inside
 * the radius (reported; ANC-3 turns it into an assertion).
 */
public class GuaranteeGameTest {
	private static final ResourceKey<WorldPreset> FINITE = ResourceKey.create(Registries.WORLD_PRESET, FiniteWorld.id("finite"));
	private static final List<Long> SEEDS = LongStream.rangeClosed(1, 20).boxed().toList();
	private static final List<Float> RADII = List.of(3000.0F, 4000.0F);
	// Ocean floor is sampled on this grid from the radius out to EDGE_DEPTH past it.
	private static final int EDGE_STEP = 64;
	private static final int EDGE_DEPTH = 1024;
	// 32 blocks or finer, so a small biome is not stepped over.
	private static final int BIOME_STEP = 32;
	private static final List<Integer> CAVE_YS = IntStream.iterate(-56, y -> y <= 56, y -> y + 16).boxed().toList();

	@GameTest
	public void guaranteeHoldsAcrossSeeds(GameTestHelper helper) {
		RegistryAccess registries = helper.getLevel().registryAccess();
		LevelStem overworld = registries.lookupOrThrow(Registries.WORLD_PRESET).getValueOrThrow(FINITE).overworld().orElseThrow();
		NoiseBasedChunkGenerator preset = (NoiseBasedChunkGenerator) overworld.generator();
		MultiNoiseBiomeSource biomes = (MultiNoiseBiomeSource) preset.getBiomeSource();
		DimensionType dimension = overworld.type().value();
		LevelHeightAccessor heights = LevelHeightAccessor.create(dimension.minY(), dimension.height());
		// The design's checklist: vanilla's Overworld biome list without the ocean biomes.
		Set<Holder<Biome>> checklist = biomes.possibleBiomes().stream().filter(biome -> !biome.is(BiomeTags.IS_OCEAN)).collect(Collectors.toSet());

		List<Result> results = RADII.stream()
			.flatMap(radius -> SEEDS.stream().map(seed -> new Run(seed, radius)))
			.toList()
			.parallelStream()
			.map(run -> {
				NoiseGeneratorSettings settings = withRadius(preset.generatorSettings().value(), run.radius());
				NoiseBasedChunkGenerator generator = new NoiseBasedChunkGenerator(biomes, Holder.direct(settings));
				RandomState random = RandomState.create(registries.lookupOrThrow(Registries.NOISE), run.seed(), settings);
				List<int[]> edge = grid(EDGE_STEP, run.radius(), run.radius() + EDGE_DEPTH).toList();
				List<String> land = edge.stream()
					.map(p -> new int[] {p[0], p[1], generator.getBaseHeight(p[0], p[1], Heightmap.Types.OCEAN_FLOOR_WG, heights, random)})
					.filter(p -> p[2] > settings.seaLevel())
					.map(p -> "(" + p[0] + ", " + p[1] + ") floor y=" + p[2] + " C=" + random.sampleBlockValueUncached(settings.noiseRouter().continents(), p[0], 0, p[1]))
					.toList();
				return new Result(run.seed(), run.radius(), land, edge.size(), biomesInside(biomes, random, run.radius()));
			})
			.toList();

		results.forEach(result -> FiniteWorld.LOGGER.info(
			"[TEST-1] seed {} radius {}: edge {} land / {} columns; biomes {}/{} missing {}",
			result.seed(), (int) result.radius(), result.landPastRadius().size(), result.edgeColumns(),
			checklist.stream().filter(result.found()::contains).count(), checklist.size(), names(missing(checklist, result))));
		RADII.forEach(radius -> FiniteWorld.LOGGER.info("[TEST-1] radius {}: missing in some seed {}", radius.intValue(),
			names(results.stream().filter(r -> r.radius() == radius).flatMap(r -> missing(checklist, r).stream()).collect(Collectors.toSet()))));

		List<Result> failed = results.stream().filter(result -> !result.landPastRadius().isEmpty()).toList();
		helper.assertTrue(failed.isEmpty(), Component.literal("Land past the radius: " + failed.stream()
			.map(r -> "seed " + r.seed() + " radius " + (int) r.radius() + " at " + r.landPastRadius().stream().limit(5).toList())
			.collect(Collectors.joining("; "))));
		helper.succeed();
	}

	/** Every biome the climate picks on a grid inside the radius, at the surface and at cave depths. */
	private static Set<Holder<Biome>> biomesInside(MultiNoiseBiomeSource biomes, RandomState random, float radius) {
		Climate.Sampler climate = random.createClimateSampler(SamplerContext.EMPTY_UNCACHED);
		return grid(BIOME_STEP, 0, radius - 1).<Holder<Biome>>mapMulti((p, found) -> {
			int x = p[0];
			int z = p[1];
			// All climate noises but depth are 2D (y_scale 0), so one sample serves every height.
			float t = climate.temperature().sampleValue(x, 0, z);
			float h = climate.humidity().sampleValue(x, 0, z);
			float c = climate.continentalness().sampleValue(x, 0, z);
			float e = climate.erosion().sampleValue(x, 0, z);
			float w = climate.weirdness().sampleValue(x, 0, z);
			// Surface biomes are placed at climate depth 0 (OverworldBiomeBuilder), so the surface needs no height search.
			found.accept(biomes.getNoiseBiome(Climate.target(t, h, c, e, 0.0F, w)));
			CAVE_YS.forEach(y -> found.accept(biomes.getNoiseBiome(Climate.target(t, h, c, e, climate.depth().sampleValue(x, y, z), w))));
		}).collect(Collectors.toSet());
	}

	/** Grid points (x, z) whose distance from the origin lies in [min, max]. */
	private static Stream<int[]> grid(int step, double min, double max) {
		int extent = (int) max / step * step;
		return IntStream.iterate(-extent, x -> x <= extent, x -> x + step).boxed()
			.flatMap(x -> IntStream.iterate(-extent, z -> z <= extent, z -> z + step).mapToObj(z -> new int[] {x, z}))
			.filter(p -> {
				double distance = Math.sqrt((double) p[0] * p[0] + (double) p[1] * p[1]);
				return distance >= min && distance <= max;
			});
	}

	/** The Finite settings with every {@code finiteworld:edge} and {@code finiteworld:edge_cap} radius replaced (the radius only lives in continents.json until UI-1). */
	private static NoiseGeneratorSettings withRadius(NoiseGeneratorSettings settings, float radius) {
		int[] edges = {0};
		DfRewriteRule rule = new DfRewriteRule() {
			@Override
			public DensityFunction rewrite(DensityFunction function) {
				if (function instanceof EdgeDensityFunction edge) {
					edges[0]++;
					return new EdgeDensityFunction(edge.input(), radius, edge.band());
				}
				if (function instanceof EdgeCapDensityFunction cap) {
					return new EdgeCapDensityFunction(cap.input().rewriteChildren(this), radius, cap.seaLevel());
				}
				return function.rewriteChildren(this);
			}
		};
		NoiseRouter r = settings.noiseRouter();
		NoiseRouter router = new NoiseRouter(rule.rewrite(r.temperature()), rule.rewrite(r.vegetation()), rule.rewrite(r.continents()),
			rule.rewrite(r.erosion()), rule.rewrite(r.depth()), rule.rewrite(r.ridges()), rule.rewrite(r.chunkSurfaceLevel()), rule.rewrite(r.finalDensity()));
		var aquifers = settings.aquifers().map(a -> new Aquifer.Config(rule.rewrite(a.barrierNoise()), rule.rewrite(a.fluidLevelFloodednessNoise()),
			rule.rewrite(a.fluidLevelSpreadNoise()), rule.rewrite(a.lavaNoise()), rule.rewrite(a.exclusion()), rule.rewrite(a.surfaceLevel())));
		if (edges[0] == 0) {
			throw new IllegalStateException("No finiteworld:edge in the Finite noise settings");
		}
		return new NoiseGeneratorSettings(settings.noiseSettings(), settings.defaultBlock(), settings.defaultFluid(), router, settings.materialRule(),
			settings.spawnTarget(), settings.seaLevel(), settings.disableMobGeneration(), aquifers, settings.useLegacyRandomSource(), settings.debugFunctions());
	}

	private static Set<Holder<Biome>> missing(Set<Holder<Biome>> checklist, Result result) {
		return checklist.stream().filter(biome -> !result.found().contains(biome)).collect(Collectors.toSet());
	}

	private static List<String> names(Collection<Holder<Biome>> biomes) {
		return biomes.stream().map(Holder::getRegisteredName).sorted().toList();
	}

	private record Run(long seed, float radius) {
	}

	private record Result(long seed, float radius, List<String> landPastRadius, int edgeColumns, Set<Holder<Biome>> found) {
	}
}
