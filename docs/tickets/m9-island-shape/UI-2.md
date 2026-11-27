# UI-2 · Shape choice on the Create World screen and the server config

**Priority:** P3 · **Area:** Radius UI · **Type:** Feature

## User story

As a player, I want to pick round, square or rectangle and its size when I create a Finite world, so I don't have to edit any files.

## Description

Extend the Finite customise screen with a shape button (Round, Square, Rectangle) and the size fields that shape
needs, and let dedicated servers set the shape in `generator-settings`.

## Current situation (as-is)

- [FiniteRadiusScreen](../../../src/client/java/io/github/kevhauma/finiteworld/client/FiniteRadiusScreen.java) has one
  radius field; Done is enabled only for a whole number in [`FiniteRadius.MIN`, `MAX`] and applies `FiniteRadius.withRadius`.
- [FiniteRadius.withConfiguredRadius](../../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java) reads
  `generator-settings={"radius":5000}`; a bad value is logged and the preset's radius kept.
- Design doc: [Player experience](../../design.md#player-experience), decisions 10 and 14.

## Desired result (to-be)

- A cycle button: Round (radius field), Square (side field), Rectangle (width and length fields). The screen opens on
  the world's current shape and size.
- Done is enabled only when the size passes the same limits as the codec (area, thinnest rectangle); a short hint
  under the fields names the limit that fails.
- Server: `generator-settings={"shape":"rectangle","width":4000,"length":7100}`, `{"shape":"square","side":5400}`;
  `{"radius":5000}` alone still works. A bad value is logged and the preset kept, as today.
- Client-only code stays in `src/client/`; parsing and limits live in common code shared with the server path.

## Acceptance criteria

- [ ] Unit tests for parsing: valid sizes per shape, below the minimum area, above the maximum, too thin, non-numbers, the old `radius` form.
- [ ] Switching shape keeps a sensible size (a round 4,000 becomes a square of the same area, rounded).
- [ ] A dedicated server with each `generator-settings` form creates the matching world; a bad value logs the error and keeps the preset (game test like `RadiusGameTest`).
- [ ] The screen's labels and hint are translatable (`en_us.json`).
- [ ] `./gradlew build` passes.
- [ ] Verified in game: create a Rectangle 4,000 × 7,100 world on seed 12345; the map shows a rectangular island; reopening Customize in a new world starts on Round 4,000.

## Notes

- Needs EDGE-4; the thin-rectangle limit comes from TUNE-2 (use the starting guess until then).
- M8-06 (`06-customize-screen-orelsethrow.md`) touches the same screen; land it first.
