# STR-2 · Fallback placement for missed structures

**Priority:** P1 · **Area:** Structures · **Type:** Feature

## User story

As a player, I want every required structure to exist on my island, so the guarantee covers structures as well as biomes.

## Description

For structures STR-1 shows can miss, add an extra fixed-position placement taken from the anchor plan. It is used only when vanilla's own placement found nothing inside the radius.

## Current situation (as-is)

- Vanilla structure sets only. Design doc: [Checklist → Structures; Technical approach → Structures](../../design.md).

## Desired result (to-be)

- A `finiteworld:anchored` structure placement type, registered in code and used in extra structure sets under `data/finiteworld/worldgen/structure_set/`. It places the structure at one planned chunk ~~inside its host biome's anchor~~ found by searching outward from its host biome's anchor (mansion in the dark forest, ancient city under the deep dark), and only if the vanilla candidates inside the radius all fail.
  - 2026-11-20: the anchor's 192-block disc alone was not enough. In 5 of 100 test worlds no chunk in the disc could start a desert pyramid (terrain below sea level) or an ancient city (no deep dark at y −27), and in one world (seed 21, radius 3000) none could within 1,024 blocks. The search now goes outward ring by ring from the anchor and takes the first chunk where the structure can start, out to the whole disc if needed.
- The "did vanilla miss?" check runs once per world alongside the anchor plan, not per chunk.
- The extra sets are referenced only from Finite worlds. Default worlds keep vanilla structure frequency.

## Acceptance criteria

- [x] TEST-1 asserts every required structure on every tested seed. (`GuaranteeGameTest.structuresInsideRadius` asserts all 19 checklist structures start inside the radius, from vanilla's sets or a fallback set, for seeds 1–50 at radius 3000 and 4000: passes.)
- [x] A seed where vanilla misses a structure (from the STR-1 table) gets exactly one fallback copy; a seed where vanilla hits gets none. (An anchored placement has at most one position (`AnchoredStructurePlacement.positions`). The test asserts no fallback where vanilla starts the structure, and that every miss is covered. Over the 100 worlds: 326 fallbacks, 974 skipped. Seed 1 radius 4000 misses village_desert, desert_pyramid and swamp_hut: one fallback each, the other 10 skipped.)
- [x] Verified in game on that seed: `/locate structure` finds the fallback, and it sits in its host biome. (Dedicated server, fresh world seed 1 radius 4000, over RCON. The log shows the once-per-world check finishing within a second of load: `[STR-2] … fallback at chunk [-86, 62]` for desert_pyramid, `[94, 180]` for swamp_hut and `[-84, 64]` for village_desert, and "vanilla starts one … no fallback" for the other 10. `locate structure` from the origin returns desert_pyramid −1376 992, swamp_hut 1504 2880 and village_desert −1344 1024, which are those chunks. Again after the chunks were force-loaded and generated. `locate biome` at each spot finds desert, swamp and desert 0 blocks away.)
- [x] Default worlds are unaffected. (The test builds a Default overworld structure state the same way and asserts the anchored sets place nothing and no plan is computed. `AnchoredStructurePlacement.positions` returns nothing without a `finiteworld:anchor_plan` node, and `AnchorPlanFunction.planOf` finds no node in vanilla settings without compiling one.)
- [x] `./gradlew build` passes. (`gradlew.bat build runGameTest`: BUILD SUCCESSFUL, unit tests pass, all 7 game tests passed.)

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
