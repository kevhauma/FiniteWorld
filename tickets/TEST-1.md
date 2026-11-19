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

- [ ] Edge rule asserted for all seeds (passes after EDGE-1).
- [ ] Biome coverage reported per seed now, asserted once ANC-3 lands.
- [ ] Runs as part of `./gradlew build`, or as a named task if it is too slow for every build (document which in the README).
- [ ] `./gradlew build` passes.

## Notes

- Needs EDGE-1. TUNE-1 uses this test to find the real minimum radius: shrink it until coverage fails.
- A grid sample can miss a biome smaller than the grid step. Use a step of 32 blocks or finer for the coverage check.
