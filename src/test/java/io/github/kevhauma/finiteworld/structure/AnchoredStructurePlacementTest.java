package io.github.kevhauma.finiteworld.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AnchoredStructurePlacementTest {
	private static final ChunkPos FIRST = new ChunkPos(0, 0);
	private static final ChunkPos SECOND = new ChunkPos(10, 3);
	private static final List<ChunkPos> HOSTS = List.of(FIRST, SECOND);

	@BeforeAll
	static void bootstrap() {
		// Loading the placement class reads vanilla registries.
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void secondAnchorsRingComesBeforeTheFirstAnchorsNextRing() {
		// Nothing fits within 3 rings of the first anchor or 1 ring of the second: ring 2 of the second wins over ring 4 of the first.
		ChunkPos fit = AnchoredStructurePlacement.chunksOutward(HOSTS, 20)
			.filter(chunk -> chunk.getChessboardDistance(FIRST) >= 4 && chunk.getChessboardDistance(SECOND) >= 2)
			.findFirst()
			.orElseThrow();
		assertEquals(2, fit.getChessboardDistance(SECOND), fit::toString);
		assertEquals(SECOND, AnchoredStructurePlacement.chunksOutward(HOSTS, 20).filter(chunk -> chunk.getChessboardDistance(FIRST) >= 4).findFirst().orElseThrow());
	}

	@Test
	void visitsEachChunkOnce() {
		List<ChunkPos> visited = AnchoredStructurePlacement.chunksOutward(List.of(FIRST, SECOND, FIRST), 12).toList();
		Set<ChunkPos> square = visited.stream().filter(chunk -> chunk.getChessboardDistance(FIRST) <= 12).collect(Collectors.toSet());
		assertEquals(visited.size(), Set.copyOf(visited).size());
		assertEquals(25 * 25, square.size());
		assertTrue(visited.stream().allMatch(chunk -> chunk.getChessboardDistance(FIRST) <= 12 || chunk.getChessboardDistance(SECOND) <= 12));
	}
}
