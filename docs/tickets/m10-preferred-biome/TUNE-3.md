# TUNE-3 · Settle the lean strength, the home anchor size and the pickable biomes

**Priority:** P3 · **Area:** Tuning · **Type:** Feature

## User story

As a player, I want "more of" to make a visible difference and "start in" to always put me in my biome, without either one costing me a biome or structure.

## Description

TUNE-1 settled the radius limits with a TEST-1 sweep. Do the same for the preferred biome: the lean strength, the home
anchor's reach and spawn distance, and which biomes each choice may offer.

## Current situation (as-is)

- SPAWN-2 uses a home reach of 384 and ANC-5 a lean of 0.15 per parameter, both starting guesses.
- [GuaranteeGameTest](../../../src/gametest/java/io/github/kevhauma/finiteworld/GuaranteeGameTest.java) sweeps seeds ×
  radii and checks biomes, structures, edge and spawn; it measures no biome shares and knows no choices.
- Decision 19 shows the share-of-land data this ticket needs (frozen, hot, desert per seed).

## Desired result (to-be)

- TEST-1 reports each biome's share of land per seed, and sweeps:
  - every candidate lean biome at a few strengths (for example 0.1, 0.15, 0.2, 0.3), at radius 3,000 and 5,000;
  - every candidate home biome at a few reaches (for example 256, 384, 512), at radius 3,000 and 4,000.
- The lean strength is the largest that keeps the whole guarantee on every seed and whose largest anchor offset stays
  under `MAX_OFFSET`, aiming for about double the biome's share of land.
- The home reach is the smallest that puts spawn in the home biome on every seed.
- The pickable lists: a biome is a lean choice if the chosen strength raises its share of land by at least half on
  most seeds; a biome is a home choice if every seed spawns in it.
- The design doc's decision log records the values, the lists and the sweep data, as decisions 16 and 19 do.

## Acceptance criteria

- [ ] TEST-1 prints each biome's share of land per seed, with and without a lean.
- [ ] The sweep runs over at least 20 seeds per setting and its table is in the decision log.
- [ ] The chosen lean strength and home reach replace the starting guesses in SPAWN-2 and ANC-5's code.
- [ ] The pickable lists replace the milestone's provisional ones in common code, and the codec and UI-3 use them.
- [ ] TEST-1 passes on every seed for every pickable home biome and every pickable lean biome at the chosen values.
- [ ] `./gradlew build` passes.

## Notes

- Needs SPAWN-2 and ANC-5.
- Watch for out-of-place anchors: if a strength passes the guarantee but leaves odd patches (a snowy disc in a hot
  island) on the map check, prefer the weaker one.
