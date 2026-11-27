# ISL-1 · Raise small islands past the radius

**Priority:** P3 · **Area:** Islands · **Type:** Feature

## User story

As a player whose island is running low on sand and gravel, I want a few small islands out in the ocean past my coast, so I can sail out and stock up without the world becoming a second continent.

## Description

Add a world-level layout of island spots in a ring past the radius and a density function that raises a low mound at
each spot, outside the edge cap. This ticket makes the islands' shape; ISL-2 makes them out of one material each.

## Current situation (as-is)

- [EdgeDensityFunction](../../../src/main/java/io/github/kevhauma/finiteworld/edge/EdgeDensityFunction.java) pushes
  continentalness into the ocean range (−1 to −0.22) near and past the radius; deep and shallow ocean both appear.
- [EdgeCapDensityFunction](../../../src/main/java/io/github/kevhauma/finiteworld/edge/EdgeCapDensityFunction.java)
  wraps `finiteworld:overworld/final_density`: past the radius, nothing at or above sea level is solid.
- [AnchorPlanFunction](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanFunction.java) computes
  the anchor plan once per world from the compile context and caches it (weak keys).
- [FiniteRadius](../../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java) rewrites the radius of
  `edge`, `edge_cap` and `anchor_plan`.
- [GuaranteeGameTest](../../../src/gametest/java/io/github/kevhauma/finiteworld/GuaranteeGameTest.java) asserts no
  ocean floor above sea level on a 64-block grid from the radius to 1,024 past it.
- Design doc: [Goals](../../design.md#goals-and-non-goals), [Beyond the radius](../../design.md#beyond-the-radius),
  decision 3.

## Desired result (to-be)

- `finiteworld:resource_islands` (fields `input`, `radius`, optional `enabled` default false) wraps the `edge_cap`
  node in `final_density.json` and returns `max(input, mound)`. The preset JSON sets `enabled: true`.
- The layout: spots spaced around a ring from 250 to 1,000 blocks past the radius, about one per 2,000 blocks of
  circumference and at least 800 apart (starting guesses, TUNE-4), deterministic from seed + radius, computed once per
  world and cached by compile context like the anchor plan. Spots where continentalness is below −0.455 (deep ocean)
  move along the ring or are dropped.
- Each mound is round with a gently wandering shore, a top 24 to 40 blocks across, at most 6 blocks above sea level,
  and solid down to the sea floor. Outside the mounds the output equals the input exactly.
- `FiniteRadius.rule` rewrites the node's radius; the node's `radius` decodes through `FiniteRadius.CODEC`.
- The node's range is the input's widened to cover the mound's values.
- The design doc's goal, *Beyond the radius* section and decision log say what this ticket adds.

## Acceptance criteria

- [ ] Unit test: with `enabled` false, the node's output equals its input everywhere.
- [ ] Unit test: same seed + radius → same layout; every spot is inside the ring, at least the spacing apart, and
  the count matches the circumference rule.
- [ ] Unit test: the mound is never solid above sea level + 6 and never solid outside its spot's disc.
- [ ] TEST-1, on its seeds at radius 3,000 and 4,000: the edge check allows land past the radius only inside island
  discs; no island disc overlaps the main island's land; no island stands on deep ocean; every checklist biome and
  structure is still found inside the radius.
- [ ] TEST-1 with islands off: results identical to today's.
- [ ] Planning time with islands stays within today's budget for the same radius.
- [ ] `./gradlew build` passes.
- [ ] Verified in game: seed 12345, radius 3,000: a boat east from the coast reaches an island within about 1,000
  blocks; F3 shows an ocean biome on it; the map shows no other land past the coast.

## Notes

- A world saved before this ticket has no `resource_islands` node and must load as before.
- Until ISL-2, islands are made of the default block (stone) with the ocean biome's material rule on top.
