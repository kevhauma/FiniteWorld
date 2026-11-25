package io.github.kevhauma.finiteworld.structure;

import io.github.kevhauma.finiteworld.FiniteRadius;
import io.github.kevhauma.finiteworld.FiniteWorld;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanFunction;
import io.github.kevhauma.finiteworld.anchor.AnchorPlanner.AnchorPlan;
import io.github.kevhauma.finiteworld.mixin.SpreadingPlacementAccessor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
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
import net.minecraft.world.level.levelgen.structure.placement.AbstractSpreadingStructurePlacement;
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
		WORLDS.put(state, new World(generator, registries, templates, heights, CacheBuilder.newBuilder().build()));
	}

	/**
	 * This world's fallback start: none in Default worlds, none when vanilla starts the structure inside the radius, else one
	 * chunk in a host anchor. Computed once per world, so another set's search can read it (an outpost avoids fallback villages).
	 */
	public List<ChunkPos> positions(ChunkGeneratorStructureState state) {
		World world = WORLDS.getIfPresent(state);
		if (world == null) {
			return List.of();
		}
		try {
			return world.positions().get(this, () -> this.search(state, world));
		} catch (ExecutionException | UncheckedExecutionException e) {
			throw new IllegalStateException("Fallback search failed for " + this.structure.getRegisteredName(), e.getCause());
		}
	}

	private List<ChunkPos> search(ChunkGeneratorStructureState state, World world) {
		if (!(world.generator() instanceof NoiseBasedChunkGenerator generator)) {
			return List.of();
		}
		NoiseGeneratorSettings settings = generator.generatorSettings().value();
		Optional<AnchorPlan> plan = AnchorPlanFunction.planOf(state.randomState(), settings);
		Optional<Float> radius = FiniteRadius.of(settings);
		if (plan.isEmpty() || radius.isEmpty()) {
			return List.of();
		}
		StructureStarts starts = new StructureStarts(state, generator, world.registries(), world.templates(), world.heights());
		List<StructureSet> vanillaSets = state.possibleStructureSets().stream()
			.map(Holder::value)
			.filter(set -> !(set.placement() instanceof AnchoredStructurePlacement) && set.structures().stream().anyMatch(entry -> entry.structure().value() == this.structure.value()))
			.toList();
		Optional<ChunkPos> vanilla = vanillaSets.stream()
			.flatMap(set -> starts.startChunks(set, radius.get()).filter(chunk -> this.isStructure(starts.generatingAt(set, chunk))))
			.findFirst();
		if (vanilla.isPresent()) {
			FiniteWorld.LOGGER.info("[STR-2] {}: vanilla starts one at chunk {}, no fallback", this.structure.getRegisteredName(), vanilla.get());
			return List.of();
		}
		List<ChunkPos> hosts = plan.get().anchors().stream()
			.filter(anchor -> this.structure.value().biomes().stream().anyMatch(biome -> biome.getRegisteredName().equals(anchor.biome())))
			.map(anchor -> new ChunkPos(SectionPos.blockToSectionCoord(anchor.x()), SectionPos.blockToSectionCoord(anchor.z())))
			.toList();
		Predicate<ChunkPos> allowed = allowedBy(vanillaSets, state);
		// Every anchor is inside the disc, so rings out to its diameter cover it.
		Optional<ChunkPos> fallback = chunksOutward(hosts, 2 * SectionPos.blockToSectionCoord(radius.get().intValue()) + 1)
			.filter(chunk -> StructureStarts.inside(chunk, radius.get()) && allowed.test(chunk) && starts.hasBiomeInColumn(this.structure, chunk) && starts.canStart(this.structure, chunk))
			.findFirst();
		FiniteWorld.LOGGER.info("[STR-2] {}: vanilla starts none inside radius {}, fallback at chunk {}", this.structure.getRegisteredName(), radius.get().intValue(), fallback.map(Object::toString).orElse("none found"));
		return fallback.stream().toList();
	}

	/**
	 * The exclusion zones of the vanilla sets holding this structure (an outpost keeps 10 chunks from the villages set): the
	 * other set's own candidates, as vanilla checks them, and the fallback starts of the other set's structures (a fallback village).
	 * shortcut: two anchored structures whose zones name each other would wait on each other's search; no vanilla set does that.
	 */
	private static Predicate<ChunkPos> allowedBy(List<StructureSet> vanillaSets, ChunkGeneratorStructureState state) {
		return vanillaSets.stream()
			.flatMap(set -> set.placement() instanceof AbstractSpreadingStructurePlacement spread ? ((SpreadingPlacementAccessor) spread).finiteworld$exclusionZone().stream() : Stream.empty())
			.map(zone -> {
				List<ChunkPos> fallbacks = fallbackStarts(state, zone.otherSet().value());
				return (Predicate<ChunkPos>) chunk -> !state.hasStructureChunkInRange(zone.otherSet(), chunk.x(), chunk.z(), zone.chunkCount())
					&& fallbacks.stream().noneMatch(start -> start.getChessboardDistance(chunk) <= zone.chunkCount());
			})
			.reduce(chunk -> true, Predicate::and);
	}

	/** The fallback starts of the anchored sets whose structure is one of {@code set}'s. */
	private static List<ChunkPos> fallbackStarts(ChunkGeneratorStructureState state, StructureSet set) {
		return state.possibleStructureSets().stream()
			.map(holder -> holder.value().placement())
			.filter(placement -> placement instanceof AnchoredStructurePlacement anchored && set.structures().stream().anyMatch(entry -> entry.structure().value() == anchored.structure.value()))
			.flatMap(placement -> ((AnchoredStructurePlacement) placement).positions(state).stream())
			.toList();
	}

	private boolean isStructure(Holder<Structure> generating) {
		return generating != null && generating.value() == this.structure.value();
	}

	/**
	 * Chunks in square rings around the host anchors' chunks, outward: ring 0 of every anchor, then ring 1 of every anchor,
	 * and so on out to {@code rings}, so the first fit is the one nearest to any host anchor. A chunk an earlier step
	 * already visited (a smaller ring of any anchor, or the same ring of an earlier one) is skipped, so the disc is covered
	 * once however many anchors there are. The anchor fixes the 2D climate, but a start can still fail on terrain (a
	 * pyramid below sea level) or depth (no deep dark at an ancient city's height), so the search goes on past the anchor
	 * discs. Lazy: it stops at the first fit.
	 * shortcut: a world with no valid spot at all scans every chunk inside the radius (seconds at 3,000; minutes near
	 * the 20,000 maximum, on a background thread that chunk generation waits on). None of the 100 tested worlds needs it.
	 */
	static Stream<ChunkPos> chunksOutward(List<ChunkPos> centres, int rings) {
		return IntStream.rangeClosed(0, rings).boxed()
			.flatMap(ring -> IntStream.range(0, centres.size()).boxed()
				.flatMap(i -> ring(centres.get(i), ring).filter(chunk -> IntStream.range(0, centres.size()).noneMatch(j -> {
					int distance = chunk.getChessboardDistance(centres.get(j));
					return distance < ring || distance == ring && j < i;
				}))));
	}

	/** The square ring of chunks at chessboard distance {@code ring} from {@code centre}. */
	private static Stream<ChunkPos> ring(ChunkPos centre, int ring) {
		int x = centre.x();
		int z = centre.z();
		return ring == 0 ? Stream.of(centre) : Stream.concat(
			// Top and bottom rows with their corners, then the sides between them.
			IntStream.rangeClosed(-ring, ring).boxed().flatMap(d -> Stream.of(new ChunkPos(x + d, z - ring), new ChunkPos(x + d, z + ring))),
			IntStream.rangeClosed(1 - ring, ring - 1).boxed().flatMap(d -> Stream.of(new ChunkPos(x - ring, z + d), new ChunkPos(x + ring, z + d))));
	}

	@Override
	public MapCodec<ConcentricRingsStructurePlacement> codec() {
		return CODEC;
	}

	private record World(ChunkGenerator generator, RegistryAccess registries, StructureTemplateManager templates, LevelHeightAccessor heights, Cache<AnchoredStructurePlacement, List<ChunkPos>> positions) {
	}
}
