# SPAWN-2 · Start in the chosen biome at the centre of the island

**Priority:** P3 · **Area:** Spawn · **Type:** Feature

## User story

As a player, I want to pick the biome I start in, so my first night is in a jungle or a cherry grove instead of wherever the seed puts me.

## Description

Let the anchor plan carry a home biome. The planner places that biome's anchor at the centre of the island first,
with a larger reach, and plans every other anchor around it. Spawn lands inside the home anchor.

## Current situation (as-is)

- [AnchorPlanner.plan](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanner.java) places every
  checklist biome hardest first, each at its best-scoring spot with `ANCHOR_RADIUS` 192 and `MIN_SPACING` 384; no spot
  is fixed.
- [AnchorPlanFunction](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanFunction.java) carries
  radius, band, `biomes` and `exclude`; `FiniteRadius.rule` rewrites the radius.
- Spawn uses the Finite noise settings' `spawn_target` (continentalness −0.11 and up, plus
  `finiteworld:spawn_distance`), which keeps it within about 300 blocks of the origin (SPAWN-1). The
  `NoiseBasedChunkGenerator.getOrigin` mixin already rewrites the spawn targets to the world's radius in memory.
- Design doc: [Anchors](../../design.md#land-generation), [Spawn](../../design.md#technical-approach), decisions 18 and 20.

## Desired result (to-be)

- `anchor_plan` has an optional `home` field, a biome id; absent means no home biome and the plan is unchanged.
- With `home`, the planner places that biome's anchor at the origin with the home reach (starting guess 384, TUNE-3),
  pulled toward the biome's inland boxes like any other anchor, then plans the rest hardest first, keeping the usual
  spacing from the home disc. The home anchor is that biome's guarantee anchor.
- Spawn lands inside the home anchor's core: the spawn search distance shrinks to fit inside it when `home` is set.
- The codec refuses a home biome that can't be a start (not on the checklist, or in the excluded groups of the
  [milestone](README.md#which-biomes-can-be-picked)), so a datapack or edited `level.dat` fails to load instead of
  spawning the player at sea.

## Acceptance criteria

- [ ] Unit test: without `home`, the plan is identical to today's for the same seed and radius.
- [ ] Unit test: with `home`, the first anchor is that biome at (0, 0) with the home reach, every other anchor keeps
  the spacing from it, and the biome has no second anchor.
- [ ] Unit test: same seed + radius + home → same plan.
- [ ] The codec rejects a home biome that is an ocean, cave biome, beach, river or mushroom fields, and one not in the
  Overworld biome list.
- [ ] TEST-1 with a home biome (desert, jungle, cherry grove, snowy plains, jagged peaks) on its seeds at radius 3,000
  and 4,000: the spawn point is in that biome and on land, and every checklist biome and structure is still found.
- [ ] `./gradlew build` passes.
- [ ] Verified in game: seed 12345, radius 4,000, home cherry grove: you spawn in a cherry grove; F3 shows it.

## Notes

- A world saved before this ticket has no `home` and must load and plan as before (covered by the first criterion).
- UI-3 adds the choice to the screen; until then, set `home` in a datapack copy of the preset to test.
