# STR-2 · Fallback placement for missed structures

**Priority:** P1 · **Area:** Structures · **Type:** Feature

## User story

As a player, I want every required structure to exist on my island, so the guarantee covers structures as well as biomes.

## Description

For structures STR-1 shows can miss, add an extra fixed-position placement taken from the anchor plan. It is used only when vanilla's own placement found nothing inside the radius.

## Current situation (as-is)

- Vanilla structure sets only. Design doc: [Checklist → Structures; Technical approach → Structures](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Desired result (to-be)

- A `finiteworld:anchored` structure placement type, registered in code and used in extra structure sets under `data/finiteworld/worldgen/structure_set/`. It places the structure at one planned chunk inside its host biome's anchor (mansion in the dark forest, ancient city under the deep dark), and only if the vanilla candidates inside the radius all fail.
- The "did vanilla miss?" check runs once per world alongside the anchor plan, not per chunk.
- The extra sets are referenced only from Finite worlds. Default worlds keep vanilla structure frequency.

## Acceptance criteria

- [ ] TEST-1 asserts every required structure on every tested seed.
- [ ] A seed where vanilla misses a structure (from the STR-1 table) gets exactly one fallback copy; a seed where vanilla hits gets none.
- [ ] Verified in game on that seed: `/locate structure` finds the fallback, and it sits in its host biome.
- [ ] Default worlds are unaffected.
- [ ] `./gradlew build` passes.

## Notes

- Needs STR-1. Build only for structures with a non-zero miss rate.
- Structure sets are global data, not per world preset. Check in the 26.3 sources how to keep the extra sets out of Default worlds (for example, the placement returns nothing unless a Finite anchor plan exists) before building.
- Miss-rate table (filled in by STR-1):
