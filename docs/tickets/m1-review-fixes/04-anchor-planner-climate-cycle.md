# 04 — Define how the anchor planner samples unanchored climate

Milestone: M1 · Area: Design · Severity: Medium · Review finding 4

## Problem

For each biome, the planner looks for the spot whose climate is closest to that biome, then adds offsets to the
climate there. But the anchor density function is part of the climate graph, so the planner must sample the climate
**without** anchors. It also needs the seeded noise, which only exists once `RandomState` is built.

The design doesn't say where the planner runs, or how the anchor function receives the plan.

## Task

Write into the design doc:

- Where the plan is computed: once per `RandomState`, from the inputs without anchors.
- How the anchor density function gets the plan: the hook point, with no mutable statics, safe across threads.
- That the plan depends only on the seed and the radius.

## Acceptance criteria

- [ ] The design doc's *Anchors* or *Technical approach* section covers the three points above.
- [ ] The hook point is confirmed in the 26.3 vanilla source (`RandomState`, `NoiseRouter`).
