# UI-3 · Biome choices on the Create World screen and the server config

**Priority:** P3 · **Area:** Radius UI · **Type:** Feature

## User story

As a player, I want to pick my start biome and the biome I want more of when I create a Finite world, so I don't have to edit any files.

## Description

Extend the Finite customise screen with two biome choices, **Start in** and **More of**, and let dedicated servers set
them in `generator-settings`.

## Current situation (as-is)

- [FiniteRadiusScreen](../../../src/client/java/io/github/kevhauma/finiteworld/client/FiniteRadiusScreen.java) has one
  radius field; Done applies `FiniteRadius.withRadius`.
- [FiniteRadius.withConfiguredRadius](../../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java) reads
  `generator-settings={"radius":5000}`; a bad value is logged and the preset's radius kept.
- Design doc: [Player experience](../../design.md#player-experience), decisions 10 and 14.

## Desired result (to-be)

- Two cycle buttons (or a searchable list if the cycle gets long): **Start in:** Anywhere or a biome from the start
  list; **More of:** None or a biome from the lean list. Biome names use the game's own translated names. The screen
  opens on the world's current choices.
- A one-line hint under **More of** says that neighbouring biomes shift too (for example "Warmer and drier: more
  desert, savanna and badlands").
- Done writes the choices into `anchor_plan` together with the radius.
- Server: `generator-settings={"radius":5000,"start_in":"minecraft:cherry_grove","more_of":"minecraft:desert"}`; each
  field is optional, and `{"radius":5000}` alone still works. A bad value is logged and the preset kept, as today.
- The pickable lists live in common code shared with the codec, so the screen, the server and a datapack accept the
  same biomes. Client-only code stays in `src/client/`.

## Acceptance criteria

- [ ] Unit tests for parsing: each field alone, both, neither, an unknown biome, a biome that can't be picked
  (ocean, deep dark), a non-string value.
- [ ] A dedicated server with each `generator-settings` form creates the matching world; a bad value logs the error and
  keeps the preset (game test like `RadiusGameTest`).
- [ ] Changing the radius on the screen keeps the biome choices, and the other way round.
- [ ] The screen's labels and hints are translatable (`en_us.json`).
- [ ] `./gradlew build` passes.
- [ ] Verified in game: create a world on seed 12345 with Start in Jungle and More of Snowy Plains; you spawn in a
  jungle and the map shows more snow than the same seed without choices; reopening Customize in a new world starts on
  Anywhere and None.

## Notes

- Needs SPAWN-2 and ANC-5; the lean list comes from TUNE-3 (use the milestone's surface land biomes until then).
- UI-2 (milestone 9) adds the shape choice to the same screen; whichever lands second fits its controls around the
  other's.
