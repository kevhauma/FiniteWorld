# UI-4 · Resource islands toggle on the Create World screen and the server config

**Priority:** P3 · **Area:** Radius UI · **Type:** Feature

## User story

As a player who wants a strict one-island world, I want to turn resource islands off when I create a Finite world, so I don't have to edit any files.

## Description

Add an on/off button for resource islands to the Finite customise screen, and let dedicated servers set it in
`generator-settings`.

## Current situation (as-is)

- [FiniteRadiusScreen](../../../src/client/java/io/github/kevhauma/finiteworld/client/FiniteRadiusScreen.java) has one
  radius field; Done applies `FiniteRadius.withRadius`.
- [FiniteRadius.withConfiguredRadius](../../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java) reads
  `generator-settings={"radius":5000}`; a bad value is logged and the preset's radius kept.
- After ISL-1, the preset has `resource_islands` with `enabled: true`.
- Design doc: [Player experience](../../design.md#player-experience), decisions 10 and 14.

## Desired result (to-be)

- A toggle button: **Resource islands: On / Off**, with a one-line hint ("Small sand, dirt, gravel and clay islands
  past the coast"). The screen opens on the world's current value.
- Done writes `enabled` into the `resource_islands` node together with the radius.
- Server: `generator-settings={"radius":5000,"resource_islands":false}`; the field is optional, and `{"radius":5000}`
  alone still works. A bad value is logged and the preset kept, as today.
- Client-only code stays in `src/client/`.

## Acceptance criteria

- [ ] Unit tests for parsing: the field alone, with the radius, absent, a non-boolean value.
- [ ] A dedicated server with each `generator-settings` form creates the matching world; a bad value logs the error and
  keeps the preset (game test like `RadiusGameTest`).
- [ ] Changing the radius on the screen keeps the toggle, and the other way round.
- [ ] The label and hint are translatable (`en_us.json`).
- [ ] The design doc's *Player experience* table lists the setting and its default.
- [ ] `./gradlew build` passes.
- [ ] Verified in game: create a world on seed 12345 with resource islands off; no land past the coast on the map.
  Reopening Customize in a new world starts on On.

## Notes

- Needs ISL-1.
- UI-2 and UI-3 add controls to the same screen; whichever lands later fits its controls around the others.
