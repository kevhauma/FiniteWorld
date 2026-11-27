# ISL-2 · Build each island from one material

**Priority:** P3 · **Area:** Islands · **Type:** Feature

## User story

As a player, I want each resource island to be made of one bulk block (sand, dirt, gravel or clay), so I know which island to sail to for what I need.

## Description

Give every island in the ISL-1 layout one material and add a rule in front of the Finite material rule that builds the
whole island from it, with a fitting top layer.

## Current situation (as-is)

- ISL-1's islands are the default block (stone) under the ocean biome's material rule.
- `finiteworld:finite` holds a copy of vanilla's Overworld `material_rule`; the mod registers no material condition or
  material rule of its own.
- [AnchorPlanFunction.planOf](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanFunction.java)
  finds the world's plan from a `RandomState` and the noise settings; the same lookup can find the island layout.
- Design doc: [Beyond the radius](../../design.md#beyond-the-radius).

## Desired result (to-be)

- Each spot in the layout carries a material, picked deterministically from seed + spot: sand, dirt, gravel or clay,
  clay rarer (starting weights 30, 30, 25, 15; TUNE-4). Every world with islands has each material at least once.
- A custom material condition, `finiteworld:resource_island`, is true inside an island with a given material. The
  copied `material_rule` starts with one sequence: for each material, if the block is inside such an island, use it.
- The whole island, down to the sea floor, is its material. Dirt islands have grass on the top block where it is dry;
  the others have no top layer.
- Outside the islands the material rule picks exactly the same blocks as today.

## Acceptance criteria

- [ ] Unit test: same seed + radius → same material per spot; every layout with at least four spots has all four
  materials.
- [ ] Unit test: the material weights over many seeds are within a few percent of the configured weights.
- [ ] Game test: on seed 12345 at radius 3,000, every block of each island from the sea floor up is its material
  (grass allowed on a dirt island's top block), and a sample of columns outside the islands matches a world without
  islands block for block.
- [ ] `./gradlew build` passes.
- [ ] Verified in game: seed 12345, radius 3,000: visit one island of each material; digging down from the top reaches
  the sea floor without leaving the material.

## Notes

- Needs ISL-1.
- One material per island in this milestone; mixed islands and more materials (red sand, mud, coarse dirt) are later
  options (see the milestone's open decisions).
