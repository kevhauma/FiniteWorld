# ANC-5 · Lean the island's climate toward the chosen biome

**Priority:** P3 · **Area:** Anchors · **Type:** Feature

## User story

As a player who loves deserts (or jungles, or snowy taiga), I want more of that biome across my island, so the world feels like mine, while still having every other biome somewhere.

## Description

Let the anchor plan carry a lean biome. Add a constant offset to temperature and humidity across the whole island,
toward the middle of that biome's climate range. The planner reads the leaned climate, so every other biome's anchor is
still placed where it fits best and pulls its spot into range.

## Current situation (as-is)

- Decision 19 already leans temperature for every Finite world: `finiteworld:overworld/temperature_noise` adds 0.1.
  That is fixed in the preset's JSON, not per world.
- [AnchorDensityFunction](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorDensityFunction.java)
  computes `input + Σ falloff(distance) × offset` for each climate parameter.
- [AnchorPlanner](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanner.java) samples the climate
  without anchors on a 64-block grid and caps each offset at ±1 (`MAX_OFFSET`).
- Design doc: [Anchors](../../design.md#land-generation), decisions 19 and 20.

## Desired result (to-be)

- `anchor_plan` has an optional `lean` field, a biome id; absent means no lean and nothing changes.
- From the biome's inland boxes the plan derives one temperature and one humidity offset (toward the middle of its
  range, scaled by the lean strength, starting guess 0.15 per parameter, TUNE-3). Continentalness, erosion and
  weirdness are never leaned.
- The planner adds the lean to every climate sample before scoring, and the anchor function adds it to its input, so
  the router, spawn, the anchor plan and the debug overlay all see the same leaned climate.
- The node's value range widens by the lean, like it does for `MAX_OFFSET`.
- The codec refuses a lean biome outside the pickable list (TUNE-3; until then, the surface land biomes of the
  [milestone](README.md#which-biomes-can-be-picked)).

## Acceptance criteria

- [ ] Unit test: without `lean`, the plan and every anchor function's output are identical to today's.
- [ ] Unit test: the derived lean moves temperature and humidity toward the biome's range and is zero on the other
  three parameters.
- [ ] Unit test: same seed + radius + lean → same plan.
- [ ] TEST-1 with a lean (desert, jungle, snowy plains, cherry grove) on its seeds at radius 3,000 and 4,000: the leaned
  biome's share of land is higher than without the lean on every seed, and every checklist biome and structure is
  still found.
- [ ] Planning time with a lean stays within today's budget for the same radius.
- [ ] `./gradlew build` passes.
- [ ] Verified in game: seed 12345, radius 4,000, lean desert: the map shows clearly more desert than the same seed
  without a lean, and `/locate biome minecraft:ice_spikes` still finds ice spikes.

## Notes

- Combining with SPAWN-2: a home biome and a lean can be the same or different; the planner applies the lean first,
  then places the home anchor.
- One lean biome only in this milestone (see the milestone's open decisions).
