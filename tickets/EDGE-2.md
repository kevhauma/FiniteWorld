# EDGE-2 · Natural coastline: wandering outline and adaptive band width

**Priority:** P2 · **Area:** Edge · **Type:** Feature

## User story

As a player, I want the coast to look natural, with beaches on lowland and cliffs where mountains meet the sea, so I only notice the radius on a map.

## Description

Make the fade band's inner edge wander with low-frequency noise, and make the band narrow under rugged terrain and wide on flat terrain.

## Current situation (as-is)

- After EDGE-1, `EdgeDensityFunction` uses a fixed band on a perfect circle.
- Design doc: [The edge → Adaptive width, Irregular outline; Risks → Cliff look](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Desired result (to-be)

- Outline: a seeded `NoiseHolder` (a new `data/finiteworld/worldgen/noise/edge_outline.json`, wired through `mapAll`) moves the band's inner edge **inward only**, so no land goes past the radius.
- Width: the function takes erosion and ridges as extra inputs. Low erosion with high peaks-and-valleys gives a narrow band, flat terrain gives a wide one.
- The math stays in `EdgeFalloff` with unit tests.

## Acceptance criteria

- [ ] Unit tests: the outline offset never pushes land past the radius; band width is monotonic in ruggedness.
- [ ] TEST-1 still passes (no land past the radius on any tested seed).
- [ ] Verified in game on 3 seeds at radius 4000: the coast on a map is not a circle; a mountain range reaching the edge ends in a sea cliff, lowland ends in beach or bay. Screenshots of both.
- [ ] No floating overhangs at cliff edges in those screenshots.
- [ ] `./gradlew build` passes.

## Notes

- Needs EDGE-1. TUNE-1 settles the band width range.
