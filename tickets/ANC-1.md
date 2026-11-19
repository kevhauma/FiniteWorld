# ANC-1 · Spike: plan anchors once per world from the seeded climate

**Priority:** P1 · **Area:** Anchors · **Type:** Feature

## User story

As a developer, I want a proven way for the anchor density function to get a per-world anchor plan, so ANC-2 and ANC-3 build on a mechanism that is deterministic, fast and mixin-free.

## Description

Density functions never see the world seed directly, but the anchor plan depends on the seeded vanilla climate. Settle how the plan is computed once per `RandomState` and shared by the five climate parameters, and record the answer in the design doc.

## Current situation (as-is)

- Nothing exists yet. Design doc: [Land generation → Anchors; Technical approach → Anchor density function; Risks → Anchor search cost](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Desired result (to-be)

- Preferred option: the anchor function holds the vanilla climate functions as children. After `RandomState` runs `mapAll`, those children are seeded, so the planner can sample them; it runs lazily on first use with thread-safe initialisation. The five parameter instances share one plan, keyed on the seeded inputs (or one shared holder function they all reference).
- Fallback if that can't work: a narrow mixin at `RandomState` construction that builds the plan from the seed.
- A short note in the design doc's Technical approach naming the chosen mechanism.

## Acceptance criteria

- [ ] Proof of concept: a stub plan is computed exactly once per world load (log line), and never for Default worlds.
- [ ] Same seed gives the same stub plan across restarts and across client and dedicated server.
- [ ] Planning on a coarse grid across a 4,000-radius disc adds a measured, acceptable time to world creation. Record the number.
- [ ] Design doc updated with the mechanism.
- [ ] `./gradlew build` passes.

## Notes

- Needs PRE-1. Read `RandomState`, `DensityFunction.Visitor` and `NoiseRouter` in the 26.3 sources first (fabric-worldgen-code skill).
- The planner has to sample the climate **without** the anchor offsets, or it would read its own output.
