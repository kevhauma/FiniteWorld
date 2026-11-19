# TEST-1 · Automated guarantee check across seeds

**Priority:** P1 · **Area:** Verification · **Type:** Feature

## User story

As a developer, I want a test that generates the Finite climate for many seeds and checks the design's hard rules, so I know the guarantee holds without flying around by hand.

## Description

A developer test (not an in-game tool, which is a non-goal) that builds the Finite `RandomState` for a set of seeds and samples it. It grows with the project: the edge now, biomes after ANC-3, structures in STR-1.

## Current situation (as-is)

- No tests exist. Design doc: [Goals, Checklist, Risks → Open](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Desired result (to-be)

- A Fabric game test (or JUnit with Minecraft bootstrapped, see the minecraft-testing skill) that, for about 20 fixed seeds at radius 3000 and 4000:
  - samples `NoiseBasedChunkGenerator.getBaseHeight` on a grid past the radius and asserts it is at most sea level;
  - samples the biome source on a grid inside the radius (surface and cave depths) and reports which checklist biomes were found. After ANC-3 it asserts all of them.
- The checklist comes from vanilla's registries, never a hardcoded list.

## Acceptance criteria

- [x] Edge rule asserted for all seeds (passes after EDGE-1). (`GuaranteeGameTest`: all 40 seed/radius runs log `edge 0 land`. Fixed 2026-11-19 by `finiteworld:edge_cap` (`EdgeCapDensityFunction`, wrapping `overworld/final_density.json`): past the radius nothing at or above sea level is solid, as the design's "Beyond the radius" requires. First run before the fix: 37 of 40 seed/radius runs have ocean floor at y=64-70 past the radius, 1-16 of 5,492/7,084 columns per run, e.g. seed 14 radius 4000 at (-2688, 3136) floor y=70. Continentalness there is already inside `EdgeFalloff`'s ocean range (-0.22 to -0.46), so the edge function is wired correctly; vanilla still raises small islands from shallow-ocean continentalness.)
- [x] Biome coverage reported per seed now, asserted once ANC-3 lands. (`GuaranteeGameTest.biomesInside` logs `[TEST-1] seed N radius R: ... biomes found/47 missing [...]` per run and the union of misses per radius; checklist is the `finite` preset's overworld biome source `possibleBiomes()` minus `#minecraft:is_ocean`, 32-block grid, surface plus 8 cave heights.)
- [x] Runs as part of `./gradlew build`, or as a named task if it is too slow for every build (document which in the README). (Fabric server game test via Loom `fabricApi.configureTests`; `check` depends on `runGameTest`, about 35 s; documented in README.md Development.)
- [x] `./gradlew build` passes. (`gradlew.bat build`: BUILD SUCCESSFUL in 40s, JUnit and `runGameTest` included.)

## Notes

- Needs EDGE-1. TUNE-1 uses this test to find the real minimum radius: shrink it until coverage fails.
- A grid sample can miss a biome smaller than the grid step. Use a step of 32 blocks or finer for the coverage check.
