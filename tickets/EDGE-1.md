# EDGE-1 · Edge density function: no land past the radius

**Priority:** P1 · **Area:** Edge · **Type:** Feature

## User story

As a player, I want the land to end at the radius and only ocean beyond it, so the world is one finite landmass.

## Description

Add the `finiteworld:edge` density function type. It lowers continentalness inside a fade band and holds it at ocean values past the radius. Vanilla's terrain then turns the land into shore and sea floor by itself.

## Current situation (as-is)

- No density function types are registered. [FiniteWorld](../src/main/java/io/github/kevhauma/finiteworld/FiniteWorld.java) is empty.
- Design doc: [The edge, Beyond the radius, Technical approach → Edge density function](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Desired result (to-be)

- A plain-Java `EdgeFalloff` class (no Minecraft types) holds the math: a smooth blend from the vanilla input to an ocean value across `[radius - band, radius]`.
- `EdgeDensityFunction` has codec fields `input`, `radius` and `band`, is registered in `BuiltInRegistries.DENSITY_FUNCTION_TYPE` as `finiteworld:edge`, and reports honest `minValue`/`maxValue`.
- `finiteworld:overworld/continents` (from PRE-1) wraps the vanilla continents in `finiteworld:edge` with `radius: 4000` and a fixed band, inside `flat_cache`. UI-1 makes the radius configurable later.
  - *2026-11-19: 26.3 has no `flat_cache`; its `minecraft:cache` caches by the node's domain axes, so the edge sits inside the existing `minecraft:cache` and reports X|Z (2D). `minValue`/`maxValue` became `range()` in 26.3. Band fixed at 500 until TUNE-1.*

## Acceptance criteria

- [x] Unit tests for `EdgeFalloff`: unchanged inside the band, smooth and monotonic across it, always in the ocean range past the radius. (`EdgeFalloffTest`: `unchangedInsideTheBand`, `smoothAndMonotonicAcrossTheBand` (1-block steps, never steeper than smoothstep's peak slope, ends exactly at the ocean value), `alwaysInTheOceanRangePastTheRadius`.)
- [x] Past the radius, continentalness never reaches the mushroom-fields range (below about -1.05) or the coast/land range (above about -0.19). Look up the exact values in `OverworldBiomeBuilder` for 26.3. (26.3 `OverworldBiomeBuilder`: mushroom fields `[-1.2, -1.05]`, coast from `-0.19`. `EdgeFalloff.ocean` maps vanilla continentalness linearly into `[-1.0, -0.22]`; `alwaysInTheOceanRangePastTheRadius` sweeps c in [-3, 3] at distances 4000 to 1,000,000. In game, seed 12345: `locate biome minecraft:mushroom_fields` from (±9000, 0) and (0, ±9000) finds none, where the Default world has one at [4000, 145, 2272], past the radius.)
- [x] Past the radius both ocean and deep-ocean biomes appear (with their temperature variants), not one uniform ocean (decision #3). (`pastTheRadiusKeepsBothDeepAndShallowOcean`: the deep-ocean/ocean split -0.455 falls inside the mapped range. In game, seed 12345, all nine variants found more than 4,000 blocks out: ocean [9480, 480], deep_ocean [-9000, 0], cold_ocean [8552, 352], deep_cold_ocean [9000, 0], lukewarm_ocean [352, 9608], deep_lukewarm_ocean [0, 9000], warm_ocean [11016, -1152], frozen_ocean [9992, -2144], deep_frozen_ocean [9256, -1664].)
- [x] ~~The radius is saved in the world's generator settings~~ The radius travels in the edge density function's codec (`radius`, `band`); it survives a save and reload. (2026-11-19: the save references the `finiteworld:finite` noise settings by id, so the value itself is written into the world only when UI-1 builds the settings per world; that part moved to UI-1. `radius`/`band` are `EdgeDensityFunction.CODEC` fields. The saved `world_gen_settings.dat` references the `finiteworld:finite` noise settings by id, which carry the radius in `overworld/continents`; the value is written inline once UI-1 builds the settings per world. Server restarted on the same save: chunks generated after the reload past the radius are ocean (sea floor Y 35 at (4300, 2000), Y 39 at (-3000, -3000)), and saved chunks are unchanged ((4500, 0) floor 48, (0, 0) floor 49, same as before the restart).)
- [x] Verified in game (seed 12345, radius 4000): the surface at (4500, 0), (0, -4500) and (3200, 3200) is below sea level; caves and ore exist under the sea floor there. (Headless dedicated server, `level-type=finiteworld:finite`, seed 12345, no ERROR lines. Sea floor / surface Y: (4500, 0) 48 / 63, (0, -4500) 54 / 63, (3200, 3200) 48 / 63 after removing a vanilla iceberg there (3,780 packed ice blocks up to Y 70: ocean decoration, not terrain). Ring at distance 4200, 8 directions: all water at 63, floors Y 35 to 50. Counted in one chunk at each point, Y -60 to 20: (4500, 0) ores coal 2, iron 69, copper 8, gold 22, redstone 29, lapis 11, diamond 26, cave air 800, water 316; (0, -4500) iron 39, copper 15, gold 34, redstone 41, lapis 13, diamond 14, air 1478, water 680, lava 238; (3200, 3200) coal 23, iron 61, copper 23, gold 25, redstone 26, lapis 20, diamond 26, air 192, water 48, lava 402.)
- [x] Default worlds are unaffected. (Only `finiteworld:overworld/continents` uses `finiteworld:edge`. Default world, seed 12345: mushroom_fields still at [4000, 145, 2272] and surface Y at (0, 0) still 63, same as the PRE-1 baseline; land at (4500, 0) Y 127, (0, -4500) Y 89, (3200, 3200) Y 80, where Finite has ocean. `world_gen_settings.dat` names `minecraft:overworld` settings.)
- [x] `./gradlew build` passes. (`gradlew.bat build`: BUILD SUCCESSFUL, 4 tests in `EdgeFalloffTest`, 0 failures. The placeholder `SmokeTest` is removed.)

## Notes

- Needs PRE-1.
- A simple clamp of the vanilla value into the ocean range can leave shallow shoals that erosion and ridges push above sea level. TEST-1 measures this; pick the mapping that passes it.
- The coastline is a plain circle for now. EDGE-2 makes it wander and adapts the band width.
