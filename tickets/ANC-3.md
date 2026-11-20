# ANC-3 · Anchor density function: steer climate toward each anchor

**Priority:** P1 · **Area:** Anchors · **Type:** Feature

## User story

As a player, I want anchored biomes to blend into the land around them, so the guarantee doesn't show as odd patches.

## Description

Add the `finiteworld:anchor` density function type, which adds the plan's smooth offsets to a climate parameter, and wire it into the five climate nodes.

## Current situation (as-is)

- ANC-1 added `finiteworld:anchor` (fields `input`, `plan`) as a pass-through on all five climate nodes; each references the shared `finiteworld:overworld/anchor_plan`, whose sampler (`AnchorPlanFunction.Sampler#plan()`) gives the world's plan at compile time. ANC-3 adds `parameter` and the offsets. Design doc: [Technical approach → Anchor density function, Biome source](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Desired result (to-be)

- `AnchorDensityFunction` has codec fields `input` and `parameter` (temperature, vegetation, continents, erosion, ridges), plus whatever ANC-1 decided it needs to reach the plan. It computes `input + Σ falloff(distance) × offset`.
- Hot path: a precomputed spatial grid of anchors, with no allocation or map lookups per sample.
- On continentalness it runs **before** `finiteworld:edge`, so the edge always wins and no land appears past the radius.
- The vanilla multi-noise biome source stays as it is (no custom biome source).

## Acceptance criteria

- [ ] Unit test for the falloff: full offset at the centre, zero at the anchor radius, smooth in between.
- [ ] Honest `minValue`/`maxValue`.
- [ ] TEST-1 asserts full biome coverage for all its seeds at radius 3000 and 4000.
- [ ] Verified in game (seed 12345, radius 4000): `/locate biome` finds every checklist biome inside the radius; screenshots of 3 anchors with large offsets show no hard climate seams.
- [ ] Default worlds are unaffected.
- [ ] `./gradlew build` passes.

## Notes

- Needs ANC-2 and EDGE-1.
- If testing shows gaps the multi-noise source can't close, that is the trigger for a custom biome source (design doc). Record it there before building one.
