# ANC-4 · Plan anchors inside the island shape

**Priority:** P3 · **Area:** Anchors · **Type:** Feature

## User story

As a player of a square or rectangular Finite world, I want every biome on my island, so the shape doesn't cost me the guarantee.

## Description

The anchor planner samples and places anchors inside a disc. Make it use the island shape's inland distance, so
anchors spread over the whole square or rectangle and stay clear of the fade band.

## Current situation (as-is)

- [AnchorPlanner.plan](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanner.java) takes
  `radius` and `band`; `Grid.sample` keeps grid points with `x² + z² ≤ (radius − band)²`, and `inside` keeps anchor
  centres within `radius − band − ANCHOR_RADIUS` of the origin.
- [AnchorPlanFunction](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanFunction.java) carries
  the radius and is rewritten by `FiniteRadius.rule`.
- Design doc: [Anchors](../../design.md#land-generation), decision 18.

## Desired result (to-be)

- `anchor_plan` carries the shape (old `radius`-only nodes decode as a circle).
- The grid covers the shape's bounding box and keeps points at least `band` inland; anchor centres sit at least
  `band + ANCHOR_RADIUS` inland.
- Planning stays deterministic from seed + shape.

## Acceptance criteria

- [ ] Unit test: same seed + shape → same plan, for a circle and a rectangle.
- [ ] Unit test: a circle shape plans the same anchors as the old radius at the same size.
- [ ] Unit test: every anchor disc of a rectangle plan lies at least `band` inland.
- [ ] A shape too thin for the anchors fails with the planner's existing "No room for anchors" error, naming the shape.
- [ ] TEST-1 finds every checklist biome inside a 5,400 square and a 4,000 × 7,100 rectangle on its seeds.
- [ ] Planning time for the largest allowed square stays within the radius-20,000 budget (about 2 s).
- [ ] `./gradlew build` passes.
- [ ] Verified in game: seed 12345, a 5,400 square: `/locate biome` finds desert, jungle and mushroom fields inside the square.

## Notes

- Needs EDGE-4 (the shape type).
- Area, not radius, sets the planning cost, so the maximum area keeps the 20,000 budget.
