# ANC-2 · Anchor planner

**Priority:** P1 · **Area:** Anchors · **Type:** Feature

## User story

As a player, I want every vanilla Overworld land and cave biome to appear on my island, placed where the climate already nearly fits, so the land feels natural and nothing is missing.

## Description

Write the planner in plain Java: given a climate sampler, the radius and the checklist, pick one spot per biome and the climate offset that pulls it into range.

## Current situation (as-is)

- Nothing exists yet. Design doc: [Land generation → Anchors, Checklist; Risks → Out-of-place anchors, Mushroom fields, Deep dark](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Desired result (to-be)

- The checklist is built at runtime from the Overworld multi-noise parameter list, minus biomes in `#minecraft:is_ocean`. It includes surface biomes and cave biomes (lush caves, dripstone caves, deep dark). The planner receives it as plain records (biome id plus parameter boxes), with no Minecraft types.
- Search: sample the climate on a coarse grid across the disc (minus the edge band), then refine near the best candidates. Score each spot by climate distance to the biome's nearest parameter box **and** its neighbours' distance. Place the hardest biomes first (those with the worst best score) and keep minimum distances between anchors and from the band.
- Output: `Anchor(x, z, radius, offset per climate parameter)`.
- Cave anchors steer the 2D climate so the column below fits: deep dark needs low erosion (mountains above), dripstone needs high continentalness, lush caves need high humidity.

## Acceptance criteria

- [ ] Unit test: same sampler and radius give the same plan.
- [ ] Unit test: every checklist entry gets exactly one anchor; all anchors are inside `radius - band`; minimum distances hold.
- [ ] Unit test: a biome the synthetic climate already contains gets a near-zero offset.
- [ ] Unit test with a synthetic sampler: runtime stays within the ANC-1 budget at radius 4000.
- [ ] In game, the log lists the checklist size read from the registry (no hardcoded list).
- [ ] `./gradlew build` passes.

## Notes

- Needs ANC-1. Mushroom fields only exist at very low continentalness, so their anchor will be coastal or a near-shore island; it must still stay inside the radius.
- Biomes can repeat. The plan only guarantees a minimum.
