# STR-3 · Structure checks and fallback inside the island shape

**Priority:** P3 · **Area:** Structures · **Type:** Feature

## User story

As a player of a square or rectangular Finite world, I want every checklist structure under or on my island, so I can find a stronghold without leaving the land.

## Description

The structure fallback checks vanilla's starts inside a disc and searches outward to the radius. Make both use the
island shape, and make sure a thin rectangle still has a stronghold under its land.

## Current situation (as-is)

- [StructureStarts.inside](../../../src/main/java/io/github/kevhauma/finiteworld/structure/StructureStarts.java) tests
  a chunk's middle against `radius²`; `startChunks` sizes its region scan from `radius`.
- [AnchoredStructurePlacement](../../../src/main/java/io/github/kevhauma/finiteworld/structure/AnchoredStructurePlacement.java)
  reads the radius with `FiniteRadius.of` and bounds `chunksOutward` by it.
- Strongholds have no fallback structure set (`src/main/resources/data/finiteworld/worldgen/structure_set/`); the design
  relies on vanilla's first ring (1,280–2,816 blocks) lying inside radius 3,000. A thin rectangle can leave all three
  first-ring strongholds outside the land.
- Design doc: [Structures](../../design.md#technical-approach), decision 15, [Beyond the radius](../../design.md#beyond-the-radius) (strongholds).

## Desired result (to-be)

- `inside` asks the shape (inland distance ≥ 0 at the chunk's middle); region scans and the outward search use the
  shape's bounding half-extent.
- A stronghold under the land is guaranteed: either TUNE-2's width limit keeps a first-ring stronghold inside every
  allowed rectangle (and a test proves it), or a stronghold fallback set is added like the others.

## Acceptance criteria

- [ ] Unit test: `inside` matches the old disc test for a circle, and is right at a rectangle's centre, edge and corner chunks.
- [ ] TEST-1 finds every checklist structure, stronghold included, inside a 5,400 square and the thinnest allowed rectangle on its seeds.
- [ ] `/locate structure minecraft:stronghold` from the centre of the thinnest allowed rectangle reports one inside the land.
- [ ] Normal worlds are unaffected: the anchored placement places nothing there (existing check still passes).
- [ ] `./gradlew build` passes.
- [ ] Verified in game: seed 12345, a 4,000 × 7,100 rectangle: the stronghold `/locate` finds is under the land.

## Notes

- Needs EDGE-4; the stronghold rule depends on TUNE-2's width limit.
- Strongholds past the coast stay reachable by digging, as on round worlds; only the checklist needs one under land.
