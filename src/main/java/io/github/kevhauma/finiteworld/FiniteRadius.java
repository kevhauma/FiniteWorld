package io.github.kevhauma.finiteworld;

import io.github.kevhauma.finiteworld.anchor.AnchorPlanFunction;
import io.github.kevhauma.finiteworld.edge.EdgeCapDensityFunction;
import io.github.kevhauma.finiteworld.edge.EdgeDensityFunction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.SpawnTargetPoint;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunction;
import net.minecraft.world.level.levelgen.densityfunction.DensityFunctions;
import net.minecraft.world.level.levelgen.densityfunction.DfRewriteRule;

/**
 * The world radius, chosen once at world creation (UI-1). It lives in the {@code radius} field of every
 * {@code finiteworld:edge}, {@code finiteworld:edge_cap} and {@code finiteworld:anchor_plan} node; a new radius
 * rewrites those nodes, which inlines them (and the path to them) into the noise settings, so the world's saved
 * generator settings carry the radius itself.
 */
public final class FiniteRadius {
	public static final int MIN = 3000;
	// Planning cost grows with radius² (about 0.3 s at 4,000), so 20,000 keeps world creation to seconds.
	public static final int MAX = 20000;
	// Also a finiteworld:edge, but its radius is the spawn ramp's, not the world's.
	private static final Identifier SPAWN_DISTANCE = FiniteWorld.id("spawn_distance");

	private FiniteRadius() {
	}

	/** The radius typed into the field or the server config, if it is a whole number in [MIN, MAX]. */
	public static OptionalInt parse(String text) {
		try {
			int radius = Integer.parseInt(text.strip());
			return radius >= MIN && radius <= MAX ? OptionalInt.of(radius) : OptionalInt.empty();
		} catch (NumberFormatException e) {
			return OptionalInt.empty();
		}
	}

	/** The radius of a Finite overworld (its final density's {@code finiteworld:edge_cap}), empty for any other generator. */
	public static Optional<Float> of(WorldDimensions dimensions) {
		return dimensions.overworld() instanceof NoiseBasedChunkGenerator generator ? of(generator.generatorSettings().value()) : Optional.empty();
	}

	public static Optional<Float> of(NoiseGeneratorSettings settings) {
		return DfRewriteRule.INLINE_REFERENCE.rewrite(settings.noiseRouter().finalDensity()) instanceof EdgeCapDensityFunction cap
			? Optional.of(cap.radius())
			: Optional.empty();
	}

	/** The dimensions with the Finite overworld at this radius, held directly so it is saved with the world. */
	public static WorldDimensions withRadius(WorldDimensions dimensions, HolderLookup.Provider registries, float radius) {
		NoiseBasedChunkGenerator overworld = (NoiseBasedChunkGenerator) dimensions.overworld();
		NoiseGeneratorSettings settings = withRadius(overworld.generatorSettings().value(), radius);
		return dimensions.replaceOverworldGenerator(registries, new NoiseBasedChunkGenerator(overworld.getBiomeSource(), Holder.direct(settings)));
	}

	/**
	 * Dedicated servers: {@code level-type=finiteworld:finite} with {@code generator-settings={"radius":5000}}.
	 * Like vanilla's Superflat settings, a bad value is logged and the preset's radius kept.
	 */
	public static WorldDimensions withConfiguredRadius(WorldDimensions dimensions, HolderLookup.Provider registries, JsonObject config) {
		JsonElement value = config.get("radius");
		if (value == null || of(dimensions).isEmpty()) {
			return dimensions;
		}
		OptionalInt radius = value.isJsonPrimitive() ? parse(value.getAsString()) : OptionalInt.empty();
		if (radius.isEmpty()) {
			FiniteWorld.LOGGER.error("generator-settings radius {} is not a whole number from {} to {}; keeping {}", value, MIN, MAX, of(dimensions).orElseThrow().intValue());
			return dimensions;
		}
		return withRadius(dimensions, registries, radius.getAsInt());
	}

	/** The settings with every radius node at this radius. {@code spawn_target} only holds references, so it is left alone (see {@link #withRadius(List, float)}). */
	public static NoiseGeneratorSettings withRadius(NoiseGeneratorSettings settings, float radius) {
		DfRewriteRule rule = rule(radius);
		NoiseRouter r = settings.noiseRouter();
		NoiseRouter router = new NoiseRouter(rule.rewrite(r.temperature()), rule.rewrite(r.vegetation()), rule.rewrite(r.continents()),
			rule.rewrite(r.erosion()), rule.rewrite(r.depth()), rule.rewrite(r.ridges()), rule.rewrite(r.chunkSurfaceLevel()), rule.rewrite(r.finalDensity()));
		Optional<Aquifer.Config> aquifers = settings.aquifers().map(a -> new Aquifer.Config(rule.rewrite(a.barrierNoise()), rule.rewrite(a.fluidLevelFloodednessNoise()),
			rule.rewrite(a.fluidLevelSpreadNoise()), rule.rewrite(a.lavaNoise()), rule.rewrite(a.exclusion()), rule.rewrite(a.surfaceLevel())));
		NoiseGeneratorSettings.DebugFunctions debug = new NoiseGeneratorSettings.DebugFunctions(settings.debugFunctions().functions().stream()
			.map(entry -> new NoiseGeneratorSettings.DebugFunctionEntry(entry.label(), rule.rewrite(entry.function())))
			.toList());
		return new NoiseGeneratorSettings(settings.noiseSettings(), settings.defaultBlock(), settings.defaultFluid(), router, settings.materialRule(),
			settings.spawnTarget(), settings.seaLevel(), settings.disableMobGeneration(), aquifers, settings.useLegacyRandomSource(), debug);
	}

	/**
	 * The spawn targets with their functions at this radius, for the spawn search only: they must stay references to be saved.
	 * Without this the search would compile the registry's 4,000 anchor plan first, and the world would keep it.
	 */
	public static List<SpawnTargetPoint> withRadius(List<SpawnTargetPoint> targets, float radius) {
		DfRewriteRule rule = rule(radius);
		return targets.stream()
			.map(target -> new SpawnTargetPoint(target.parameters().entrySet().stream().collect(Collectors.toMap(entry -> {
				DensityFunction reference = new DensityFunctions.HolderHolder(entry.getKey());
				DensityFunction rewritten = rule.rewrite(reference);
				return rewritten == reference ? entry.getKey() : Holder.direct(rewritten);
			}, Map.Entry::getValue))))
			.toList();
	}

	/** Replaces the radius bottom-up; untouched references stay references, rewritten ones are inlined by {@code HolderHolder}. */
	private static DfRewriteRule rule(float radius) {
		return new DfRewriteRule() {
			@Override
			public DensityFunction rewrite(DensityFunction function) {
				if (function instanceof DensityFunctions.HolderHolder(Holder<DensityFunction> holder) && holder.is(SPAWN_DISTANCE)) {
					return function;
				}
				return switch (function.rewriteChildren(this)) {
					case EdgeDensityFunction e when e.radius() != radius -> e.withRadius(radius);
					case EdgeCapDensityFunction c when c.radius() != radius -> new EdgeCapDensityFunction(c.input(), radius, c.seaLevel());
					case AnchorPlanFunction p when p.radius() != radius ->
						new AnchorPlanFunction(radius, p.band(), p.biomes(), p.exclude(), p.temperature(), p.vegetation(), p.continents(), p.erosion(), p.ridges());
					case DensityFunction same -> same;
				};
			}
		};
	}
}
