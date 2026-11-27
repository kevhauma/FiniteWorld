# EDGE-4 · Island shape in the generator settings: circle, rectangle

**Priority:** P3 · **Area:** Edge · **Type:** Feature

## User story

As a player, I want my Finite island to be round, square or rectangular, so the world's outline fits how I want to play.

## Description

Replace the radius in the edge nodes with an island shape: a circle (radius) or a rectangle (width and length; a
square is a rectangle with equal sides). The edge works on how far inland a block is, so the coast stays natural for
every shape and round worlds generate exactly as before.

## Current situation (as-is)

- The outline is a circle everywhere: [EdgeDensityFunction](../../../src/main/java/io/github/kevhauma/finiteworld/edge/EdgeDensityFunction.java)
  computes `sqrt(x² + z²)` and [EdgeFalloff.apply](../../../src/main/java/io/github/kevhauma/finiteworld/edge/EdgeFalloff.java)
  compares it with `radius` across the band `[radius − band, radius]`.
- [EdgeCapDensityFunction.capped](../../../src/main/java/io/github/kevhauma/finiteworld/edge/EdgeCapDensityFunction.java)
  caps the final density where `x² + z² > radius²`.
- [FiniteRadius](../../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java) holds the radius limits and
  codec, reads the radius back from `edge_cap` (`FiniteRadius.of`) and rewrites every `edge`, `edge_cap` and
  `anchor_plan` node to a new radius (`rule`), skipping `spawn_distance`.
- Design doc: [The edge](../../design.md#the-edge), [Beyond the radius](../../design.md#beyond-the-radius).

## Desired result (to-be)

- An island shape type with two variants, circle (`radius`) and rectangle (`width`, `length`), and one method: the
  signed distance inland from the outline at `(x, z)`. Circle: `radius − sqrt(x² + z²)`. Rectangle: a rounded-box
  distance centred on the origin, corner radius at least the fade band.
- `edge` and `edge_cap` take a `shape` field. A node with only `radius` (every existing world and the shipped
  preset) still decodes as a circle and saves the way it did.
- `EdgeFalloff` works on inland distance: weight 0 deeper than `band` inland, 1 at and past the outline. For a circle
  the result is identical to today's.
- The cap applies where the inland distance is below 0.
- `FiniteRadius` becomes the shape's home (limits, read back, rewrite) or is replaced by an equivalent; `spawn_distance`
  keeps its circle (it's the spawn ramp, not the island).
- Area limits from the [milestone](README.md): at least π × 3,000², at most π × 20,000², enforced by the codec like the
  radius today.

## Acceptance criteria

- [ ] A saved world or datapack with the old `radius`-only `edge` / `edge_cap` loads and generates the same terrain (same density at sampled points).
- [ ] A circle shape gives the same density as the old radius node at the same size (unit test over sampled points, band edge and past the outline).
- [ ] Unit tests for the rectangle's inland distance: centre, mid-edge, corner, outside, and width ≠ length.
- [ ] The codec rejects a shape below the minimum area or above the maximum, with a message naming the limit.
- [ ] Normal (non-Finite) worlds are unaffected: their noise settings hold no Finite nodes and decode as before.
- [ ] TEST-1 (`GuaranteeGameTest`) finds no land past the outline for a 5,400 square and a 4,000 × 7,100 rectangle on its seeds.
- [ ] The design doc's edge and decision log sections describe the shape.
- [ ] `./gradlew build` passes.
- [ ] Verified in game: seed 12345, a 5,400 square, flown around the coast: beaches and cliffs, no straight-ruled stretch along a side.

## Notes

- Needs EDGE-2 and EDGE-3 landed or rebased onto (same classes).
- The `volume` early-out in `EdgeDensityFunction.Sampler.sampleVolume` uses the farthest corner's distance; it needs the shape's equivalent (the smallest inland distance in the volume).
- No rotation in this ticket (see the milestone's open decisions).
