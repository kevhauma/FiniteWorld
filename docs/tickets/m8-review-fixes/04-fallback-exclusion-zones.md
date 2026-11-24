# 04 · Respect vanilla exclusion zones in the fallback

**Source:** review 2026-11-24, finding 4 · **Area:** Structures · **Severity:** Low · **Type:** Bug fix

## User story

As a player, I want a fallback pillager outpost to keep vanilla's distance from villages, so a guaranteed
structure doesn't put raiders inside a village.

## Description

Vanilla keeps outposts 10 chunks away from any village. The anchored fallback set has no exclusion zone and its
position check doesn't look at one, so a fallback outpost can start inside a village.

## Current situation (as-is)

- Vanilla `data/minecraft/worldgen/structure_set/pillager_outposts.json` (26.3) has
  `"exclusion_zone": {"chunk_count": 10, "other_set": "minecraft:villages"}`.
- [`structure_set/pillager_outpost.json`](../../../src/main/resources/data/finiteworld/worldgen/structure_set/pillager_outpost.json)
  uses `finiteworld:anchored`, which has no exclusion zone:
  [`AnchoredStructurePlacement`](../../../src/main/java/io/github/kevhauma/finiteworld/structure/AnchoredStructurePlacement.java)
  passes `Optional.empty()` to the parent constructor (line 52).
- The fallback filter (line 90) checks only `inside`, `hasBiomeInColumn` and `canStart`.
- The other anchored sets have no vanilla exclusion zone, so only the outpost is affected today.

## Desired result (to-be)

- The fallback skips chunks that the vanilla set's exclusion zone forbids, for any anchored structure whose
  vanilla set has one.
- The village set's starts the check reads are vanilla's random-spread ones, plus fallback villages if
  practical; say which in the code.

## Acceptance criteria

- [ ] No fallback pillager outpost starts within 10 chunks of a village start on seeds 1–50 at radius 3,000 and
      4,000 (new assertion in `GuaranteeGameTest.structuresInsideRadius`).
- [ ] Every checklist structure still starts inside the radius on those seeds.
- [ ] Default worlds are unaffected (existing assertion).
- [ ] `./gradlew build` passes.

## Notes

- Reading the exclusion zone from the vanilla set that holds the same structure keeps it data-driven rather than
  hardcoding "villages".
- Fallback villages are computed by other anchored sets in parallel; checking them may need an order between
  sets. If not worth it, record it as a `shortcut:` comment.
