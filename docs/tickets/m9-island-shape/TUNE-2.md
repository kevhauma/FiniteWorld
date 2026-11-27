# TUNE-2 · Settle the shape limits: thinnest rectangle, minimum and maximum area

**Priority:** P3 · **Area:** Tuning · **Type:** Feature

## User story

As a player, I want the shape limits to be the ones that actually keep the guarantee, so no square or rectangle I'm allowed to make is missing a biome or structure.

## Description

TUNE-1 measured the minimum radius with a TEST-1 sweep. Do the same for shapes: confirm the minimum area holds for
a square, and find the thinnest rectangle that still passes on every seed.

## Current situation (as-is)

- The minimum radius 3,000 and maximum 20,000 come from TUNE-1 (design doc decisions 12, 13, 16).
- [GuaranteeGameTest](../../../src/gametest/java/io/github/kevhauma/finiteworld/GuaranteeGameTest.java) sweeps seeds ×
  radii; it knows only circles.
- The starting guess for the thinnest rectangle is a width of 2,816 (see the [milestone](README.md#open-decisions)).

## Desired result (to-be)

- TEST-1 sweeps shapes as well as radii: a square at the minimum area, rectangles at the minimum area with widths
  stepping down (for example 4,000, 3,500, 3,000, 2,816, 2,500).
- The thinnest width (or largest length-to-width ratio) that passes biomes, structures (stronghold included), edge
  and spawn on every seed becomes the limit in the codec and the UI.
- The design doc's decision log records the limits and the sweep data, as decision 16 does for the radius.

## Acceptance criteria

- [ ] TEST-1 reports, per shape and seed: land past the outline, missing biomes, missing structures.
- [ ] A square of the minimum area passes on every seed (else the minimum area rises, recorded with data).
- [ ] The chosen thinnest rectangle passes on every seed; the next step thinner fails on at least one, and the data is in the decision log.
- [ ] The codec and UI use the chosen limit (EDGE-4, UI-2).
- [ ] `./gradlew build` passes.
- [ ] Verified on the test server map ([SRV-4](../m6-test-server/SRV-4.md)): seed 12345 at the thinnest allowed rectangle, coast wanders, no land past the outline.

## Notes

- Needs EDGE-4, ANC-4 and STR-3.
- The test server's outline marker ([markers.py](../../../tools/test-server/markers.py)) draws a circle; it needs the shape to check this on the map.
