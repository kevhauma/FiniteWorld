# UI-1 · Radius field on the Create World screen

**Priority:** P1 · **Area:** Radius UI · **Type:** Feature

## User story

As a player, I want to set the island radius when I create a Finite world, so I choose how big my world is.

## Description

Add a customise screen for the Finite preset, the same way vanilla does for Superflat and Single Biome. It writes the radius into the generator settings and enforces the minimum.

## Current situation (as-is)

- The radius is hardcoded as 4000 in the noise settings JSON (EDGE-1). [FiniteWorldClient](../../../src/client/java/io/github/kevhauma/finiteworld/client/FiniteWorldClient.java) is empty.
- Design doc: [Player experience; Technical approach → Radius UI](../../design.md), decisions #10 and #12.

## Desired result (to-be)

- A `PresetEditor` registered for `finiteworld:finite` through a client accessor mixin on vanilla's preset editor map. This is the one place a mixin is needed: vanilla offers no registry or Fabric API hook for it.
  - *2026-11-20:* in 26.3 `PresetEditor.EDITORS` is an immutable `Map.of` on an interface, so the mixin (`WorldCreationUiStateMixin`) wraps the two `EDITORS.get` calls in `WorldCreationUiState` instead. Two more mixins turned out to be needed: the spawn search (`NoiseBasedChunkGeneratorMixin`, see Notes) and the dedicated server's `generator-settings` (`WorldDimensionDataMixin`).
- The screen has one number field: default 4,000, Done disabled below the minimum (about 3,000). *2026-11-20:* minimum 3,000, maximum 20,000 (agreed with the user; planning time grows with radius²).
- On Done it rebuilds `WorldDimensions` with a direct (non-registry) noise settings holder whose `finiteworld:edge` and `finiteworld:anchor` nodes carry the new radius.
- No edit after creation. Vanilla offers no customise button on existing worlds, so this needs no extra code.

## Acceptance criteria

- [x] The Customize button appears for Finite only. (Client, 2026-11-20, confirmed by the user: Customize opens the radius screen for Finite, is greyed out for Default and Large Biomes, and Superflat keeps its own. `WorldCreationUiStateMixin` returns the Finite editor only for the `finiteworld:finite` key and vanilla's lookup for every other.)
- [x] Values below the minimum, or non-numbers, cannot be confirmed. (`FiniteRadius.parse` accepts only whole numbers in [3000, 20000]; `FiniteRadiusTest` (12 cases: "2999", "20001", "-4000", "4000.5", "4e3", "abc", "", overflow rejected; 3000, 4000, " 5000 ", 20000 accepted) passes. `FiniteRadiusScreen` sets `done.active` from `parse` on every edit, and Done applies `parse(...).orElseThrow()`.)
- [x] Verified in game: radius 5000 → land ends at about 5,000; reopen the world → still 5,000. (Client, 2026-11-20: the user made a new Finite world with Customize set to 5000; the log showed one plan, `Planned 47 anchors for radius 5000 ... plan hash 1415659355`, and the land ended at x 4753.5, z 60.3 (F3), about 4,754 blocks out, inside the 4,500–5,000 fade band and well past 4,000. After save, quit and reopen the log showed the same plan, radius 5000, hash 1415659355. Earlier on a dedicated server: dedicated server, seed 12345, `generator-settings={"radius":5000}`: one plan, `Planned 47 anchors for radius 5000 ... plan hash -679815074`; `locate biome minecraft:plains` from (4300, 0) found plains at (4588, -256), 4,595 blocks out, so land reaches past 4,000. Restarted with `generator-settings={}`: same radius-5000 plan hash, same plains, loaded in 1.1 s.)
- [x] The radius is written into the world's saved generator settings (`world_gen_settings.dat`), not only referenced by preset id (moved here from EDGE-1, 2026-11-19). (`RadiusGameTest.radiusSavedInGeneratorSettings` encodes the customised dimensions with `WorldDimensions.CODEC` (what `WorldGenSettings` saves), asserts every saved `radius` node is an edge, edge_cap or anchor_plan at 5000, decodes and re-encodes identically: passes. The server world `ui1_finite_12345_r5000/data/minecraft/world_gen_settings.dat` holds edge ×35, anchor_plan ×1489 and edge_cap ×2, all at 5000.0, and no 800 (spawn_distance stays a reference). Cost: 51 KB on disk, 3.5 MB of NBT, see the design doc.)
- [x] Every density function that takes a radius gets the new value (TEST-1 run at a non-default radius passes). (`GuaranteeGameTest` now uses `FiniteRadius.withRadius` and runs radius 5000 too: no land past the radius and all 47 checklist biomes in all 20 seeds at 3000, 4000 and 5000. `RadiusGameTest.spawnSearchPlansAtWorldRadius`: a world whose spawn search runs first plans once, and gets the same plan as the router: passes, and fails with `NoiseBasedChunkGeneratorMixin` disabled ("its plan differs from the router's").)
- [x] Client classes live only in `src/client/`. (`FiniteRadiusScreen` and `WorldCreationUiStateMixin` are in `src/client/`, registered in `finiteworld.client.mixins.json`; `grep net.minecraft.client` over `src/main` and `src/gametest` finds nothing. The server mixin is in the `server` list of `finiteworld.mixins.json`.)
- [x] `./gradlew build` passes. (`gradlew.bat build`: BUILD SUCCESSFUL, 12 `FiniteRadiusTest` cases plus the existing unit tests, all 6 game tests passed.)

## Notes

- Needs EDGE-1 and ANC-3, so one change covers every radius field.
- `finiteworld:spawn_distance` (SPAWN-1, referenced from `spawn_target`) is also a `finiteworld:edge`, but its 800/500 is a spawn ramp, not the world radius. Leave it out of the swap.
- ~~**Open question:** a dedicated server can pick `level-type=finiteworld:finite` but has no screen, so it always gets the default radius. Decide whether that's acceptable; the design doc doesn't mention servers.~~ *2026-11-20, decided with the user:* servers set it in server.properties, `generator-settings={"radius":5000}` (design doc decision #14).
- The spawn search reads `spawn_target`, which can only hold registry references (the 4,000 nodes), and it runs before any chunk. Without the `getOrigin` mixin it compiles the 4,000 anchor plan first and a customised world keeps that plan everywhere (the plan cache keeps one plan per world).
