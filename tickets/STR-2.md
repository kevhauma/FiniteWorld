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

  Vanilla misses over seeds 1–50 (`GuaranteeGameTest.structuresInsideRadius`, structure start chunk centre inside the radius, each candidate checked as `/locate` does):

  | Structure | Misses at 3000 | Misses at 4000 |
  | --- | --- | --- |
  | village_plains | 1 | 0 |
  | village_desert | 27 | 19 |
  | village_savanna | 12 | 4 |
  | village_snowy | 20 | 9 |
  | village_taiga | 8 | 4 |
  | pillager_outpost | 2 | 0 |
  | mansion | 37 | 24 |
  | desert_pyramid | 32 | 24 |
  | jungle_pyramid | 12 | 2 |
  | swamp_hut | 25 | 17 |
  | igloo | 13 | 5 |
  | ruined_portal | 0 | 0 |
  | trail_ruins | 0 | 0 |
  | buried_treasure | 0 | 0 |
  | mineshaft | 0 | 0 |
  | mineshaft_mesa | 15 | 11 |
  | stronghold | 0 | 0 |
  | trial_chambers | 0 | 0 |
  | ancient_city | 3 | 0 |

  13 of 19 structures can miss, far more than expected: each village type, pyramid, hut, igloo and the mansion needs its biome at a vanilla candidate, and the anchored biome is one 192-block disc. 26.x also adds abandoned camps (one per forest-type biome), which the design's checklist does not list.
