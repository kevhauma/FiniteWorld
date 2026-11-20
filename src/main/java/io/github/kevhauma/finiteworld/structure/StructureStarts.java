package io.github.kevhauma.finiteworld.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Which structure vanilla starts at a chunk, without generating it: the placement's candidates, then
 * the set's weighted pick (ChunkGenerator.createStructures), each pick checked as {@code /locate} does
 * (Structure.findValidGenerationPoint). Used by the guarantee test (STR-1).
 */
public record StructureStarts(ChunkGeneratorStructureState state, ChunkGenerator generator, RegistryAccess registries, StructureTemplateManager templates, LevelHeightAccessor heights) {
	/** The set's start chunks whose centre is inside the radius: placement candidates that pass its frequency and exclusion checks. */
	public Stream<ChunkPos> startChunks(StructureSet set, float radius) {
		Stream<ChunkPos> candidates = switch (set.placement()) {
			case ConcentricRingsStructurePlacement rings -> this.state.getRingPositionsFor(rings).stream();
			case RandomSpreadStructurePlacement spread -> {
				int regions = (int) Math.ceil(radius / 16.0 / spread.spacing());
				yield IntStream.rangeClosed(-regions, regions).boxed()
					.flatMap(gx -> IntStream.rangeClosed(-regions, regions).mapToObj(gz -> spread.getPotentialStructureChunk(this.state.getLevelSeed(), gx * spread.spacing(), gz * spread.spacing())));
			}
			default -> Stream.empty();
		};
		return candidates.filter(chunk -> inside(chunk, radius) && set.placement().isStructureChunk(this.state, chunk.x(), chunk.z()));
	}

	/** The structure the set starts at this chunk: vanilla's weighted pick, retried without each invalid choice. */
	public @Nullable Holder<Structure> generatingAt(StructureSet set, ChunkPos chunk) {
		List<StructureSet.StructureSelectionEntry> options = new ArrayList<>(set.structures());
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
		random.setLargeFeatureSeed(this.state.getLevelSeed(), chunk.x(), chunk.z());
		int total = options.stream().mapToInt(StructureSet.StructureSelectionEntry::weight).sum();
		while (!options.isEmpty()) {
			// Vanilla skips the draw for a one-structure set; the random is local, so drawing anyway picks the same.
			int choice = random.nextInt(total);
			int index = 0;
			while ((choice -= options.get(index).weight()) >= 0) {
				index++;
			}
			StructureSet.StructureSelectionEntry selected = options.remove(index);
			if (this.canStart(selected.structure(), chunk)) {
				return selected.structure();
			}
			total -= selected.weight();
		}
		return null;
	}

	/** StructureCheck.canCreateStructure: the structure finds a start here and its start sits in one of its biomes. */
	public boolean canStart(Holder<Structure> structure, ChunkPos chunk) {
		if (structure.value() instanceof JigsawStructure) {
			// Jigsaw starts read template palettes, which fill a plain HashMap on first use, so callers here take turns.
			// shortcut: vanilla's own worldgen threads don't take this lock; the race is vanilla's and rare. Warm the palettes first if it ever shows up in play.
			synchronized (this.templates) {
				return this.findStart(structure, chunk);
			}
		}
		return this.findStart(structure, chunk);
	}

	private boolean findStart(Holder<Structure> structure, ChunkPos chunk) {
		Climate.Sampler climate = this.state.randomState().createClimateSampler(SamplerContext.EMPTY_UNCACHED);
		return structure.value().findValidGenerationPoint(new Structure.GenerationContext(this.registries, this.generator, this.generator.getBiomeSource(), climate,
			this.state.randomState(), this.templates, this.state.getLevelSeed(), chunk, this.heights, structure.value().biomes()::contains)).isPresent();
	}

	public static boolean inside(ChunkPos chunk, float radius) {
		return (double) chunk.getMiddleBlockX() * chunk.getMiddleBlockX() + (double) chunk.getMiddleBlockZ() * chunk.getMiddleBlockZ() <= (double) radius * radius;
	}
}
