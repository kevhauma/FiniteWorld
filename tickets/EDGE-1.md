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

## Acceptance criteria

- [ ] Unit tests for `EdgeFalloff`: unchanged inside the band, smooth and monotonic across it, always in the ocean range past the radius.
- [ ] Past the radius, continentalness never reaches the mushroom-fields range (below about -1.05) or the coast/land range (above about -0.19). Look up the exact values in `OverworldBiomeBuilder` for 26.3.
- [ ] Past the radius both ocean and deep-ocean biomes appear (with their temperature variants), not one uniform ocean (decision #3).
- [ ] The radius is saved in the world's generator settings: it survives a save and reload.
- [ ] Verified in game (seed 12345, radius 4000): the surface at (4500, 0), (0, -4500) and (3200, 3200) is below sea level; caves and ore exist under the sea floor there.
- [ ] Default worlds are unaffected.
- [ ] `./gradlew build` passes.

## Notes

- Needs PRE-1.
- A simple clamp of the vanilla value into the ocean range can leave shallow shoals that erosion and ridges push above sea level. TEST-1 measures this; pick the mapping that passes it.
- The coastline is a plain circle for now. EDGE-2 makes it wander and adapts the band width.
