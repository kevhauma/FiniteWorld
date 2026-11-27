# 06 · Don't crash the Customize screen on a replaced preset

**Source:** review 2026-11-24, finding 6 · **Area:** Radius UI · **Severity:** Low · **Type:** Bug fix

## User story

As a player with a datapack that changes the Finite preset, I want the Customize button not to crash the game,
so I can still create a world.

## Description

The Customize screen reads the current radius with `orElseThrow()`. If the selected Finite preset's overworld
has no `finiteworld:edge_cap` final density, the client crashes.

## Current situation (as-is)

- [`FiniteRadiusScreen.EDITOR`](../../../src/client/java/io/github/kevhauma/finiteworld/client/FiniteRadiusScreen.java)
  (line 25) calls `FiniteRadius.of(settings.selectedDimensions()).orElseThrow()`.
- [`FiniteRadius.of`](../../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java) (lines 57–65)
  is empty when the overworld isn't a noise generator or its final density isn't an `edge_cap`. A datapack that
  overrides the `finiteworld:finite` world preset or noise settings can cause that.
- The dedicated-server path (`withConfiguredRadius`) already returns early when `of` is empty.

## Desired result (to-be)

- Without a Finite radius, the editor doesn't open the screen (or opens it with the 4,000 default and Done does
  nothing harmful). It never throws.

## Acceptance criteria

- [x] With the Finite preset's overworld replaced by a non-Finite generator, clicking Customize doesn't crash.
      (2026-11-27, `runClient` with the preset's overworld settings switched to `minecraft:overworld`, a local edit
      standing in for a datapack, never committed: Customize leaves the Create World screen up, the game keeps
      running, no error in the log.)
- [x] With the shipped preset, the screen still opens with the current radius and Done still rewrites it.
      (2026-11-27, `runClient`: it opened at 4000; after Done at 5000, reopening it showed 5000.)
- [x] Client-only code stays in `src/client/`. (The only change is in `FiniteRadiusScreen`.)
- [x] `./gradlew build` passes. (Unit tests and all 9 game tests.)
- [x] Verified in game (`./gradlew runClient`): Create World → Finite → Customize opens, Done at 5,000 creates a
      5,000 world. (2026-11-27, seed 12345: the server logged "Planned 47 anchors for radius 5000 … plan hash
      -850451181", the same as the game test's seed 12345 at 5,000. The player spawned on land, and all 1,596
      `radius` tags in the saved `world_gen_settings.dat` are 5000.)

## Notes

- Applying the rewrite to a non-Finite overworld would fail in `FiniteRadius.withRadius` (it casts to
  `NoiseBasedChunkGenerator`), so not opening the screen is the simpler fix.
