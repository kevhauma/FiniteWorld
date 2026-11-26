# 07 · Build the spawn ramp from vanilla density functions

**Source:** review 2026-11-24, finding 7 · **Area:** Spawn · **Severity:** Low · **Type:** Refactor

## User story

As a developer, I want `finiteworld:edge` to mean only "the island edge", so the radius rewrite needs no special
case and every edge radius can be validated.

## Description

`finiteworld:spawn_distance` reuses the edge function as a distance ramp. That forces an exception in the radius
rewrite and keeps `edge`'s radius from using `FiniteRadius.CODEC`. Vanilla 26.3 likely has the parts to build the
ramp without it.

## Current situation (as-is)

- [`spawn_distance.json`](../../../src/main/resources/data/finiteworld/worldgen/density_function/spawn_distance.json)
  is a `finiteworld:edge` with `input: 1.0`, `radius: 800`, `band: 500`: 1 inside 300 blocks, falling to
  `EdgeFalloff.ocean(1.0) = -0.22` at 800.
- [`FiniteRadius`](../../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java) keeps a
  `SPAWN_DISTANCE` id (line 41) and skips it in `rule` (lines 126–128), so the world radius doesn't overwrite the
  ramp's 800.
- [`EdgeDensityFunction.CODEC`](../../../src/main/java/io/github/kevhauma/finiteworld/edge/EdgeDensityFunction.java)
  (line 31) keeps `ExtraCodecs.POSITIVE_FLOAT` for `radius`, because 800 is below the 3,000 minimum.
- Vanilla 26.3 registers `minecraft:distance_to_point` (point, metric; 3D), `minecraft:slice` (removes an axis),
  and `minecraft:clamp` / `minecraft:spline`.
- Design doc: [Spawn](../../design.md) (SPAWN-1).

## Desired result (to-be)

- `spawn_distance` is built from vanilla functions (for example a clamp or spline over `distance_to_point` with
  the Y axis sliced away) and gives the same ramp within rounding.
- The `SPAWN_DISTANCE` special case is gone from `FiniteRadius`.
- `finiteworld:edge` decodes `radius` with `FiniteRadius.CODEC`, like `edge_cap` and `anchor_plan`.

## Acceptance criteria

- [x] The new `spawn_distance` matches the old one to within 0.01 at distances 0, 300, 550, 800 and 2,000
      (unit or game test). (`GuaranteeGameTest.spawnDistanceMatchesTheOldRamp`, at y -64, 64 and 200. A cubic spline
      from (300, 1) to (800, -0.22) with zero slopes is exactly the old smoothstep.)
- [x] `GuaranteeGameTest.spawnOnLandNearCentre` passes (spawn on land within 500 blocks, seeds 1–20). (Passes; all 20
      `[SPAWN-1]` spawns are the same as before.)
- [x] `RadiusGameTest` passes: every saved radius node is at the chosen radius. (Passes.)
- [x] Decoding an `edge` with radius below 3,000 fails. (`EdgeDensityFunction` decodes `radius` with
      `FiniteRadius.CODEC`; the same game test decodes radius 800 and expects an error.)
- [x] The design doc's Radius UI paragraph no longer says `edge` keeps any positive radius. (Updated; the Spawn
      paragraph describes the vanilla ramp.)
- [x] `./gradlew build` passes. (Unit tests and all 10 game tests.)

## Notes

- Check first whether the spawn search samples spawn targets at a fixed y; if it does, the Y slice may not be
  needed. If no vanilla combination reproduces the ramp, close this ticket with that finding.
- Worlds saved by 1.0.0 hold the `edge`-based node; they must still load, so keep the old form decodable or
  confirm that saved worlds only reference it by id (spawn targets stay references).

## Outcome

`spawn_distance` is a `minecraft:spline` over `minecraft:slice` (axis y, coordinate 0) of `minecraft:distance_to_point`
at the origin. `FiniteRadius` no longer special-cases it. Worlds saved by 26.3.0/26.3.1 keep `spawn_target` as
references by id, so they load the new ramp; no saved world holds an `edge` below 3,000 inline.
