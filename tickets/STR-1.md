# STR-1 · Measure which required structures vanilla misses

**Priority:** P1 · **Area:** Structures · **Type:** Feature

## User story

As a developer, I want data on which required structures vanilla fails to place inside the radius, so the fallback in STR-2 is built only for structures that need it.

## Description

Extend TEST-1 to check the structure checklist the way `/locate` does: enumerate each structure set's placement candidates inside the radius and test whether the structure can start there.

## Current situation (as-is)

- Structure sets are vanilla and unchanged. Design doc: [Checklist → Structures; Technical approach → Structures](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Desired result (to-be)

- For each required structure (village variants, pillager outpost, woodland mansion, desert and jungle pyramids, swamp hut, igloo, ruined portal, trail ruins, buried treasure, both mineshaft variants, stronghold, trial chambers, ancient city), TEST-1 reports a hit or a miss per seed at radius 3000 and 4000.
- A miss-rate table over about 50 seeds, saved in the STR-2 ticket Notes.

## Acceptance criteria

- [ ] TEST-1 reports per-structure coverage.
- [ ] Miss-rate table recorded in [STR-2](STR-2.md).
- [ ] Spot-check 3 reported hits with `/locate structure` in game; they match.
- [ ] `./gradlew build` passes.

## Notes

- Needs TEST-1 and ANC-3. Expect the misses to be rare, biome-bound structures such as the mansion, ancient city, trail ruins, igloo and the pyramids.
