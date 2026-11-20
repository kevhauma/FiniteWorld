package io.github.kevhauma.finiteworld.structure;

import io.github.kevhauma.finiteworld.FiniteRadius;
import io.github.kevhauma.finiteworld.FiniteWorld;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanFunction;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanner;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.AnchorPlan;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * {@code finiteworld:anchored} (STR-2): one start for {@code structure}, at the first chunk where it can start,
 * searching outward from an anchor in one of its biomes, and only when vanilla's own sets start none
 * inside the radius. It is a concentric-rings placement with a single position, so vanilla computes that
 * position once per world (ChunkGeneratorStructureState#generateRingPositions, redirected here by a mixin)
 * and {@code /locate structure} finds it. Without a Finite plan (Default worlds) it has no position.
 */
public class AnchoredStructurePlacement extends ConcentricRingsStructurePlacement {
	// Typed as the parent so codec() can override it.
	public static final MapCodec<ConcentricRingsStructurePlacement> CODEC = Structure.CODEC.fieldOf("structure")
		.xmap(AnchoredStructurePlacement::new, placement -> ((AnchoredStructurePlacement) placement).structure());
	// Each world's generation context, registered when its ChunkMap is built. The values hold no reference to the state, so weak keys can drop it.
	private static final Cache<ChunkGeneratorStructureState, World> WORLDS = CacheBuilder.newBuilder().weakKeys().build();

	private final Holder<Structure> structure;

	public AnchoredStructurePlacement(Holder<Structure> structure) {
		// Ring fields are unused: the position comes from positions(), never vanilla's ring search.
		super(Vec3i.ZERO, FrequencyReductionMethod.DEFAULT, 1.0F, 0, Optional.empty(), 0, 1, 1, HolderSet.empty());
		this.structure = structure;
	}

	public Holder<Structure> structure() {
		return this.structure;
	}

	/** Called once per world before structure positions are computed; {@code heights} must not reference the level. */
	public static void register(ChunkGeneratorStructureState state, ChunkGenerator generator, RegistryAccess registries, StructureTemplateManager templates, LevelHeightAccessor heights) {
		WORLDS.put(state, new World(generator, registries, templates, heights));
	}

	/** This world's fallback start: none in Default worlds, none when vanilla starts the structure inside the radius, else one chunk in a host anchor. */
	public List<ChunkPos> positions(ChunkGeneratorStructureState state) {
		World world = WORLDS.getIfPresent(state);
		if (world == null || !(world.generator() instanceof NoiseBasedChunkGenerator generator)) {
			return List.of();
		}
		NoiseGeneratorSettings settings = generator.generatorSettings().value();
		Optional<AnchorPlan> plan = AnchorPlanFunction.planOf(state.randomState(), settings);
		Optional<Float> radius = FiniteRadius.of(settings);
		if (plan.isEmpty() || radius.isEmpty()) {
			return List.of();
		}
		StructureStarts starts = new StructureStarts(state, generator, world.registries(), world.templates(), world.heights());
		Optional<ChunkPos> vanilla = state.possibleStructureSets().stream()
			.map(Holder::value)
			.filter(set -> !(set.placement() instanceof AnchoredStructurePlacement) && set.structures().stream().anyMatch(entry -> entry.structure().value() == this.structure.value()))
			.flatMap(set -> starts.startChunks(set, radius.get()).filter(chunk -> this.isStructure(starts.generatingAt(set, chunk))))
			.findFirst();
		if (vanilla.isPresent()) {
			FiniteWorld.LOGGER.info("[STR-2] {}: vanilla starts one at chunk {}, no fallback", this.structure.getRegisteredName(), vanilla.get());
			return List.of();
		}
		Optional<ChunkPos> fallback = plan.get().anchors().stream()
			.filter(anchor -> this.structure.value().biomes().stream().anyMatch(biome -> biome.getRegisteredName().equals(anchor.biome())))
			.flatMap(anchor -> chunksOutward(anchor, radius.get()))
			.filter(chunk -> StructureStarts.inside(chunk, radius.get()) && starts.hasBiomeInColumn(this.structure, chunk) && starts.canStart(this.structure, chunk))
			.findFirst();
		FiniteWorld.LOGGER.info("[STR-2] {}: vanilla starts none inside radius {}, fallback at chunk {}", this.structure.getRegisteredName(), radius.get().intValue(), fallback.map(Object::toString).orElse("none found"));
		return fallback.stream().toList();
	}

	private boolean isStructure(Holder<Structure> generating) {
		return generating != null && generating.value() == this.structure.value();
	}

	/**
	 * Chunks in square rings around the anchor's chunk, outward, far enough to cover the whole disc of {@code radius}.
	 * The anchor fixes the 2D climate, but a start can still fail on terrain (a pyramid below sea level) or depth (no
	 * deep dark at an ancient city's height), so the search goes on past the anchor's disc. Lazy: it stops at the first fit.
	 * shortcut: a world with no valid spot at all scans every chunk inside the radius (seconds at 3,000; minutes near
	 * the 20,000 maximum, on a background thread that chunk generation waits on). None of the 100 tested worlds needs it.
	 */
	private static Stream<ChunkPos> chunksOutward(AnchorPlanner.Anchor anchor, float radius) {
		int x = SectionPos.blockToSectionCoord(anchor.x());
		int z = SectionPos.blockToSectionCoord(anchor.z());
		// The anchor is inside the disc, so rings out to its diameter cover it.
		return Stream.concat(Stream.of(new ChunkPos(x, z)), IntStream.rangeClosed(1, 2 * SectionPos.blockToSectionCoord((int) radius) + 1).boxed()
			.flatMap(ring -> Stream.concat(
				// Top and bottom rows with their corners, then the sides between them.
				IntStream.rangeClosed(-ring, ring).boxed().flatMap(d -> Stream.of(new ChunkPos(x + d, z - ring), new ChunkPos(x + d, z + ring))),
				IntStream.rangeClosed(1 - ring, ring - 1).boxed().flatMap(d -> Stream.of(new ChunkPos(x - ring, z + d), new ChunkPos(x + ring, z + d))))));
	}

	@Override
	public MapCodec<ConcentricRingsStructurePlacement> codec() {
		return CODEC;
	}

	private record World(ChunkGenerator generator, RegistryAccess registries, StructureTemplateManager templates, LevelHeightAccessor heights) {
	}
}
