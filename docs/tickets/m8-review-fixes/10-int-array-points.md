# 10 · Replace `int[]` points with a record

**Source:** review 2026-11-24, finding 10 · **Area:** Readability · **Severity:** Low · **Type:** Refactor

## User story

As a developer, I want points to have named fields, so `p.x()` replaces `p[0]` and a swapped index can't slip
through.

## Description

The planner refinement and the guarantee game test pass block columns around as two-element `int[]` arrays.

## Current situation (as-is)

- [`AnchorPlanner.Target.place`](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanner.java)
  (lines 150–153) builds `new int[] {x, z}` candidates and reads `p[0]`, `p[1]`.
- [`GuaranteeGameTest`](../../../src/gametest/java/io/github/kevhauma/finiteworld/GuaranteeGameTest.java) uses
  `int[]` points in `grid`, the edge sampling (`p[0]`, `p[1]`, `p[2]`), `biomesInside`, `landShares` and
  `SPAWN_SPIRAL`.

## Desired result (to-be)

- A small record (for example `Column(int x, int z)`), or an existing type such as `Spot` in the planner, replaces
  the arrays. The edge-land entries use a record with a floor height.

## Acceptance criteria

- [ ] No `int[]` used as a point in `AnchorPlanner` or `GuaranteeGameTest`.
- [ ] Unit tests pass and plans are unchanged (`sameClimateGivesTheSamePlan`, same plan hashes in the game-test
      log for seed 12345).
- [ ] Game tests pass (`runGameTest`).
- [ ] `./gradlew build` passes.

## Notes

- Keep the record private to each file unless both need the same one.
