# 08 · Quiet the per-world INFO logs

**Source:** review 2026-11-24, finding 8 · **Area:** Code · **Severity:** Low · **Type:** Refactor

## User story

As a server owner, I want one short line from the mod per world load, so its output doesn't bury other logs.

## Description

Each Finite world load logs the full biome checklist and one line per anchored structure set at INFO, all tagged
with ticket ids.

## Current situation (as-is)

- [`AnchorPlanFunction.plan`](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanFunction.java)
  (lines 107–112) logs `[ANC-2] Checklist …` with all 47 biome ids, then `[ANC-2] Planned …`.
- [`AnchoredStructurePlacement.positions`](../../../src/main/java/io/github/kevhauma/finiteworld/structure/AnchoredStructurePlacement.java)
  (lines 84 and 92) logs one `[STR-2]` line per anchored set: 13 per world.
- [`tools/test-server/markers.py`](../../../tools/test-server/markers.py) reads `[SRV]` lines from the game-test
  log, not these.

## Desired result (to-be)

- At INFO, one line per world: anchors planned, time, plan hash, and how many fallbacks were placed.
- The checklist and per-structure lines move to DEBUG.
- Production log messages drop ticket ids; game-test log lines may keep them.

## Acceptance criteria

- [ ] A dedicated server creating a Finite world logs at most two INFO lines from the mod after "Finite World
      initialized".
- [ ] With DEBUG enabled for `finiteworld`, the checklist and per-structure fallback lines still appear.
- [ ] Nothing in `tools/` or the docs depends on the removed INFO lines (grep `[ANC-2]`, `[STR-2]`).
- [ ] `./gradlew build` passes.

## Notes

- STR-2's in-game verification quotes `[STR-2]` lines; that's history, no update needed.
