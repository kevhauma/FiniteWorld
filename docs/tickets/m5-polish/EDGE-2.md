# EDGE-2 · Natural coastline: wandering outline and adaptive band width

**Priority:** P2 · **Area:** Edge · **Type:** Feature

## User story

As a player, I want the coast to look natural, with beaches on lowland and cliffs where mountains meet the sea, so I only notice the radius on a map.

## Description

Make the fade band's inner edge wander with low-frequency noise, and make the band narrow under rugged terrain and wide on flat terrain.

## Current situation (as-is)

- After EDGE-1, `EdgeDensityFunction` uses a fixed band on a perfect circle.
- Design doc: [The edge → Adaptive width, Irregular outline; Risks → Cliff look](../../design.md).

## Desired result (to-be)

- Outline: ~~a seeded `NoiseHolder` (a new `data/finiteworld/worldgen/noise/edge_outline.json`, wired through `mapAll`)~~ a new `data/finiteworld/worldgen/noise/edge_outline.json`, read by a `minecraft:noise` node in the edge's `outline` input, moves the band's inner edge **inward only**, so no land goes past the radius. *(2026-11-21: 26.3's density function API has no `NoiseHolder`/`mapAll`; vanilla's `minecraft:noise` already seeds a noise per world, so the edge takes the outline as an ordinary density function input. The whole band moves inward by `inset ∈ [0, outline_depth]`, so its outer edge never passes the radius either.)*
- Width: the function takes erosion and ridges as extra inputs. Low erosion with high peaks-and-valleys gives a narrow band, flat terrain gives a wide one.
- The math stays in `EdgeFalloff` with unit tests.

## Acceptance criteria

- [x] Unit tests: the outline offset never pushes land past the radius; band width is monotonic in ruggedness. (`EdgeFalloffTest.outlineNeverPushesLandPastTheRadius`: for outline values -5..5 the inset stays in [0, outline_depth], and at and past the radius every band from narrow to wide gives ocean continentalness; a high outline really pulls the full-ocean point inward. `bandWidthIsMonotonicInRuggedness`: wide at 0, narrow at 1, never growing between. `ruggednessRisesWithLowErosionAndPeaks`: 0 on flat (high-erosion) terrain, non-decreasing as erosion falls, peaks ≥ valleys.)
- [x] TEST-1 still passes (no land past the radius on any tested seed). (`gradlew.bat build`: all 7 game tests passed; `[TEST-1]` logs `edge 0 land` for all 20 seeds at radius 3000, 4000 and 5000, and all 47 checklist biomes are still covered.)
- [ ] Verified in game on 3 seeds at radius 4000: the coast on a map is not a circle; a mountain range reaching the edge ends in a sea cliff, lowland ends in beach or bay. Screenshots of both. (Open: needs the client window and the user's go-ahead to run it. 2026-11-21, [SRV-4](../m6-test-server/SRV-4.md) on the test server map, seed 12345 at radius **3000**: the coast is not a circle except one ~1,700-block stretch on the west that follows it, now [EDGE-3](EDGE-3.md). Lowland ends in beaches. Almost no mountains meet the edge on that seed, so it shows no sea cliff, and the 4,000 seeds are still needed.)
- [ ] No floating overhangs at cliff edges in those screenshots. (Open: same in-game run. SRV-4's top-down map can't show overhangs.)
- [x] `./gradlew build` passes. (`gradlew.bat build`: BUILD SUCCESSFUL, unit tests 31/31, game tests 7/7.)

## Notes

- Needs EDGE-1. TUNE-1 settles the band width range.
- Starting values in `overworld/continents.json`: `band` 500 (flat), `narrow_band` 200 (peaks), `outline_depth` 300. All new `finiteworld:edge` fields are optional, so `spawn_distance` and worlds saved before EDGE-2 keep the fixed circle.
- The anchor plan's `band` stays 500, so the outermost anchor discs can reach up to 300 blocks into the wandered band; TEST-1's biome coverage still passes. Raising it to 800 (band + outline depth) moved desert anchors onto low ground: on seed 44 at 3000 and seed 39 at 4000 no desert spot was high enough for a pyramid, and STR-2's fallback found none. Left for TUNE-1.
