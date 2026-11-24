# STR-1 · Measure which required structures vanilla misses

**Priority:** P1 · **Area:** Structures · **Type:** Feature

## User story

As a developer, I want data on which required structures vanilla fails to place inside the radius, so the fallback in STR-2 is built only for structures that need it.

## Description

Extend TEST-1 to check the structure checklist the way `/locate` does: enumerate each structure set's placement candidates inside the radius and test whether the structure can start there.

## Current situation (as-is)

- Structure sets are vanilla and unchanged. Design doc: [Checklist → Structures; Technical approach → Structures](../../design.md).

## Desired result (to-be)

- For each required structure (village variants, pillager outpost, woodland mansion, desert and jungle pyramids, swamp hut, igloo, ruined portal, trail ruins, buried treasure, both mineshaft variants, stronghold, trial chambers, ancient city), TEST-1 reports a hit or a miss per seed at radius 3000 and 4000.
- A miss-rate table over about 50 seeds, saved in the STR-2 ticket Notes.

## Acceptance criteria

- [x] TEST-1 reports per-structure coverage. (`GuaranteeGameTest.structuresInsideRadius`, seeds 1–50 at radius 3000 and 4000: per world it logs vanilla's misses and the nearest start per structure (`[STR-1] seed …`), then misses per structure over all seeds (`[STR-1] radius …`). Each set's candidates inside the radius pass the placement's frequency and exclusion checks, then the set's weighted pick, each pick checked with `Structure.findValidGenerationPoint` as `/locate` does. The shared code is `structure/StructureStarts`, which STR-2 also uses.)
- [x] Miss-rate table recorded in [STR-2](STR-2.md). (STR-2 Notes: 13 of 19 structures miss in some world.)
- [x] Spot-check 3 reported hits with `/locate structure` in game; they match. (Dedicated server, fresh world seed 1 radius 4000, `execute positioned 0 64 0 run locate structure …` over RCON: mansion 1888 240, village_plains 640 816, trail_ruins −992 −992 and jungle_pyramid 64 1776 match the test's nearest starts. The igloo the test reports at −144 −944 exists too (`locate` from beside it: 11 blocks away), but `locate` from the origin returns −880 −1024: both start chunks sit in the same ring of vanilla's region search, and vanilla returns the first hit in a ring, not the nearest.)
- [x] `./gradlew build` passes. (`gradlew.bat build runGameTest`: BUILD SUCCESSFUL, all 7 game tests passed.)

## Notes

- Needs TEST-1 and ANC-3. Expect the misses to be rare, biome-bound structures such as the mansion, ancient city, trail ruins, igloo and the pyramids.
