# 01 — Edge and anchors must reach terrain, not just the router

Milestone: M7 · Area: Design · Severity: High · Review finding 1

## Problem

The design says the edge density function lowers continentalness, which implies wrapping the noise router's
`continents` entry. In 26.3, that entry only drives biome selection.

Terrain reads `minecraft:overworld/continents` (and `erosion`, `ridges_folded`) **by id**. The chain is
`minecraft:overworld/offset` → `depth` / `factor` / `sloped_cheese` → `final_density`. Wrapping only the router
entry puts ocean biomes on dry land at the edge. The anchors have the same problem.

## Task

Pick one approach and write it into the design doc (Technical approach and decision log):

- **Copy the graph (recommended):** copy the overworld density function graph under `finiteworld:` and point it at
  the wrapped inputs. Normal worlds stay untouched.
- **Override the vanilla ids:** only if the overrides do nothing outside the Finite preset.

## Acceptance criteria

- [ ] The design doc names the approach and lists which density functions are copied or overridden.
- [ ] The decision log has a new row that includes the rejected option.
- [ ] The approach is checked against the vanilla 26.3 data
      (`data/minecraft/worldgen/density_function/overworld/*.json`).
