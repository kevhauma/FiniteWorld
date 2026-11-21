# ANC-3 · Anchor density function: steer climate toward each anchor

**Priority:** P1 · **Area:** Anchors · **Type:** Feature

## User story

As a player, I want anchored biomes to blend into the land around them, so the guarantee doesn't show as odd patches.

## Description

Add the `finiteworld:anchor` density function type, which adds the plan's smooth offsets to a climate parameter, and wire it into the five climate nodes.

## Current situation (as-is)

- ANC-1 added `finiteworld:anchor` (fields `input`, `plan`) as a pass-through on all five climate nodes; each references the shared `finiteworld:overworld/anchor_plan`, whose sampler (`AnchorPlanFunction.Sampler#plan()`) gives the world's plan at compile time. ANC-3 adds `parameter` and the offsets. Design doc: [Technical approach → Anchor density function, Biome source](../Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Desired result (to-be)

- `AnchorDensityFunction` has codec fields `input` and `parameter` (temperature, vegetation, continents, erosion, ridges), plus whatever ANC-1 decided it needs to reach the plan. It computes `input + Σ falloff(distance) × offset`.
- Hot path: a precomputed spatial grid of anchors, with no allocation or map lookups per sample.
- On continentalness it runs **before** `finiteworld:edge`, so the edge always wins and no land appears past the radius.
- The vanilla multi-noise biome source stays as it is (no custom biome source).

## Acceptance criteria

- [x] Unit test for the falloff: full offset at the centre, zero at the anchor radius, smooth in between. (`AnchorFieldTest.falloffIsFullAtTheCentreZeroAtTheRadiusAndSmoothBetween`: 1 at d=0, 0 at and past r, flat at both ends, monotonic, per-block change within the curve's peak slope; `fullOffsetAtTheCentreAndNoneFromTheRadiusOn` and `gridMatchesTheSumOverEveryAnchor` check the bucketed `AnchorField` against a brute-force sum.)
- [x] Honest `minValue`/`maxValue`. (`AnchorDensityFunction.range()` = input range ± `AnchorPlanner.MAX_OFFSET` (1.0). It holds for any plan: the planner caps every offset at ±1 (`AnchorPlannerTest.offsetIsCappedAtMaxOffset`), and centres at least 2 × radius apart mean at most one anchor reaches a point (`AnchorFieldTest.touchingDiscsNeverAddUpPastMaxOffset`). Largest real offset over the 40 TEST-1 worlds: 0.73, so the cap never bites.)
- [x] TEST-1 asserts full biome coverage for all its seeds at radius 3000 and 4000. (`GuaranteeGameTest.guaranteeHoldsAcrossSeeds` samples the preset's multi-noise biome source with each world's anchored climate sampler at every anchor centre and on the 32-block grid, at the surface and 8 cave heights, and asserts all 47 checklist biomes for seeds 1–20 at both radii: passes. Before ANC-3 the same sampling missed 31 biomes in some seed at 3000 and 15 at 4000. The 32-block grid alone, without the centre samples, also finds all 47 in every world.)
- [ ] Verified in game (seed 12345, radius 4000): `/locate biome` finds every checklist biome inside the radius; screenshots of 3 anchors with large offsets show no hard climate seams. (Half done. `/locate biome`: dedicated server, fresh world seed 12345 radius 4000 (plan hash -322952148), `locate biome` over RCON for each of the 47 biomes in the `[ANC-2] Checklist` log line: 47/47 found, farthest badlands at 3,246 blocks, all inside the radius. Open: screenshots of 3 strongly pulled anchors, which need the client window.)
- [x] Default worlds are unaffected. (`AnchorPlanGameTest.planOncePerWorldNeverForDefault` compiles every router function of vanilla `minecraft:overworld` noise settings and asserts no plan is computed; an anchor node compiles its plan first, so none is reachable from Default. The mod only adds files under `data/finiteworld/` plus the preset tag.)
- [x] `./gradlew build` passes. (`gradlew.bat build runGameTest`: BUILD SUCCESSFUL, unit tests pass, all 4 game tests passed.)

## Notes

- Needs ANC-2 and EDGE-1.
- If testing shows gaps the multi-noise source can't close, that is the trigger for a custom biome source (design doc). Record it there before building one.
