# Changelog

Each release's section becomes its GitHub release notes: the `release` workflow publishes the text under `## <version>`. Versions follow Minecraft: `<minecraft version>.<patch>`, so 26.3.1 is the second release for Minecraft 26.3.

## 26.3.2

Worlds made with 26.3.0 or 26.3.1 load unchanged. **Requires** Minecraft 26.3, Fabric Loader 0.20.1+, Fabric API 0.166.2+ and Java 25.

### Fixed

- **Customize no longer crashes** when a datapack replaces the Finite world type's Overworld. The button just stays on the Create World screen (M8-06).
- **Fallback outposts keep away from villages.** A pillager outpost placed by the structure guarantee now keeps vanilla's 10-chunk gap from villages, including villages the guarantee placed itself. On one tested world it had landed on top of a village (M8-04).
- **Fallback structures land in their own biomes more often.** The structure guarantee now searches around every biome a structure can use, nearest first, instead of trying one biome across the whole island before the next (M8-01).
- **Bad datapack settings fail early and clearly.** An anchor plan whose `band` leaves no room inside its `radius` is rejected when the datapack loads, naming both values. If anchor planning fails anyway, the crash log says "Anchor planning failed" with the real cause directly below it (M8-05, M8-02).

### Changed

- **Quieter logs.** The mod writes one line per world at INFO (anchors planned, time, plan hash). The biome checklist and the per-structure fallback lines are at DEBUG (M8-08).
- **For datapack authors:** `finiteworld:edge` now only accepts a `radius` from 3,000 to 20,000, like `edge_cap` and `anchor_plan`. The spawn distance term is built from vanilla density functions instead of an `edge` node; spawns are unchanged (M8-07).

### Known issues

- Along some stretches the coast still traces the circle, for example ~1,700 blocks of seed 12345's west coast at radius 3,000 (EDGE-3).

## 26.3.1

### Fixed

- A radius outside 3,000–20,000 set by a datapack or an edited `level.dat` now fails to load the world instead of being used (M7-02).

## 26.3.0

First release, first tagged as 1.0.0. **Requires** Minecraft 26.3, Fabric Loader 0.20.1+, Fabric API 0.166.2+ and Java 25.

### Added

- **Finite world type.** One landmass inside an endless vanilla ocean. Pick **Finite** under World Type on the Create World screen; normal worlds are not affected.
- **Radius setting.** Set it with **Customize**: 3,000 to 20,000 blocks, default 4,000. It's fixed once the world is created. Dedicated servers set it in `server.properties` with `level-type=finiteworld\:finite` and `generator-settings={"radius"\:5000}`.
- **Natural edge.** Past the radius, land is pushed below sea level. The coast follows the terrain: beaches and bays on lowland, sea cliffs where mountains meet the edge. The outline wanders instead of tracing the circle. Oceans, caves, ores and underground structures continue without limit.
- **Biome guarantee.** All 47 vanilla Overworld land and cave biomes appear inside the radius, steered there by small seeded climate nudges called anchors. Tested on 20 seeds at radius 3,000 and 4,000.
- **Structure guarantee.** If vanilla misses one of the 13 required structures inside the radius, one copy is placed near its host biome, and `/locate` finds it.
- **Spawn on land** near the centre of the island.
- **Warmer climate.** Temperature varies faster than in vanilla and leans warmer, so small worlds get less snow and more desert.

### Known issues

- Along some stretches the coast still traces the circle, for example ~1,700 blocks of seed 12345's west coast at radius 3,000 (EDGE-3).
- A radius set outside the Create World screen and server setting (datapack, edited `level.dat`) isn't checked against the 3,000–20,000 limits (M7-02).
