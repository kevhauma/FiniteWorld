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

- [x] Unit test: same sampler and radius give the same plan. (`AnchorPlannerTest.sameClimateGivesTheSamePlan`; in game, seed 12345 gives plan hash -322952148 in two game-test compiles and on the dedicated server.)
- [x] Unit test: every checklist entry gets exactly one anchor; all anchors are inside `radius - band`; minimum distances hold. (`AnchorPlannerTest.everyBiomeGetsOneAnchorInsideTheBandAndApart`: the whole 192-block anchor disc inside `radius - band`, centres at least 384 apart; `offsetFitsEachAnchorIntoItsBiome` checks each offset lands its centre in the biome's box. `AnchorPlanGameTest` asserts one anchor per real checklist biome; `GuaranteeGameTest` plans 40 worlds at radius 3,000 and 4,000 without running out of room.)
- [x] Unit test: a biome the synthetic climate already contains gets a near-zero offset. (`AnchorPlannerTest.biomeTheClimateContainsGetsANearZeroOffset`: below 0.001 per parameter; the mushroom stand-in, out of the climate's reach, gets a real continentalness pull.)
- [x] Unit test with a synthetic sampler: runtime stays within the ANC-1 budget at radius 4000. (`AnchorPlannerTest.plansWithinTheAnc1BudgetAtRadius4000`: 51 biomes, 1,001 boxes (vanilla merges to 849), best of 3 at most 100 ms; measured 57 ms. Real climate at radius 4,000: 96–106 ms per world in the game tests, 329 ms on a fresh dedicated server, world ready in 4.3 s as with ANC-1. Recorded in the design doc.)
- [x] In game, the log lists the checklist size read from the registry (no hardcoded list). (Dedicated server, `runServer`, seed 12345, new Finite world: `[ANC-2] Checklist from minecraft:overworld minus #minecraft:is_ocean: 47 biomes, 3789 boxes: [...]`, then `[ANC-2] Planned 47 anchors for radius 4000 band 500: 9401 grid points (step 64) in 329 ms`. The `anchor_plan` codec resolves the parameter list and the tag from the registries.)
- [x] `./gradlew build` passes. (`gradlew.bat build runGameTest`: BUILD SUCCESSFUL, unit tests pass, all 4 game tests passed.)

## Notes

- Needs ANC-1. Mushroom fields only exist at very low continentalness, so their anchor will be coastal or a near-shore island; it must still stay inside the radius.
- Biomes can repeat. The plan only guarantees a minimum.
