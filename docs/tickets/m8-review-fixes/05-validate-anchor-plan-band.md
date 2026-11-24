# 05 · Validate the anchor plan's band against its radius

**Source:** review 2026-11-24, finding 5 · **Area:** Anchors · **Severity:** Low · **Type:** Bug fix

## User story

As a datapack author, I want an anchor plan with an impossible band rejected when the datapack loads, so I get
a clear error instead of a broken world or a crash at world load.

## Description

`finiteworld:anchor_plan` only requires a positive `band`. A band that leaves no room for anchors is accepted
and fails later, or produces nonsense.

## Current situation (as-is)

- [`AnchorPlanFunction.CODEC`](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanFunction.java)
  (line 59) decodes `band` with `ExtraCodecs.POSITIVE_FLOAT` and has no `validate`.
- In [`AnchorPlanner.Grid.sample`](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanner.java)
  (lines 84–93), a band of at least the radius makes `half` negative. `n * n` is still positive, so the grid is
  built with wrong coordinates, and `inside(…, maxCentre)` with a negative `maxCentre` can accept points.
- A band above `radius - 192` fails only at world load, in `Target.of` ("No room for anchors").
- [`EdgeDensityFunction.CODEC`](../../../src/main/java/io/github/kevhauma/finiteworld/edge/EdgeDensityFunction.java)
  (lines 42–46) already validates its band against its radius; the plan node should do the same.

## Desired result (to-be)

- The codec rejects `band > radius - AnchorPlanner.ANCHOR_RADIUS` with a message naming both values.

## Acceptance criteria

- [ ] Decoding an `anchor_plan` with a band that leaves less than one anchor radius fails with a clear message.
- [ ] Unit test: one valid and one invalid band decode as expected.
- [ ] The shipped `anchor_plan.json` (radius 4,000, band 500) and every radius from 3,000 to 20,000 still decode.
- [ ] `./gradlew build` passes.

## Notes

- With the M1-02 radius limits, the default band of 500 always passes. This only guards hand-written datapacks
  and edited `level.dat` files.
