# 01 · Search every host anchor before widening the fallback search

**Source:** review 2026-11-24, finding 1 · **Area:** Structures · **Severity:** Medium · **Type:** Bug fix

## User story

As a player, I want a missing structure's fallback placed in one of its anchored biomes, so the guarantee
puts it where the planner prepared the climate for it.

## Description

The STR-2 fallback tries a structure's host anchors one after another, and each anchor's search alone covers
the whole disc. The second and later anchors are only reached if no chunk anywhere on the island fits.

## Current situation (as-is)

- [`AnchoredStructurePlacement.positions`](../../../src/main/java/io/github/kevhauma/finiteworld/structure/AnchoredStructurePlacement.java)
  (lines 87–91) does `anchors.stream().filter(host).flatMap(anchor -> chunksOutward(anchor, radius))…findFirst()`.
- `chunksOutward` (lines 107–116) emits rings out to `2 * radius / 16 + 1` chunks, so the stream for the first
  host anchor alone visits every chunk inside the radius before the next anchor starts.
- The anchors are in hardest-biome-first order. For a structure with several host biomes, the first host is
  often a hard biome where the structure fails on terrain (an outpost on a peak), and the search then takes the
  nearest unanchored patch instead of trying the next anchor's centre.
- Affected structures (vanilla 26.3 `has_structure` tags): pillager outpost (desert, plains, savanna, snowy
  plains, taiga, `#is_mountain`, grove), village plains (plains, meadow), mansion (dark forest, pale garden),
  igloo (snowy taiga, snowy plains, snowy slopes), badlands mineshaft (`#is_badlands`), jungle temple (jungle,
  bamboo jungle).
- Design doc: [Technical approach → Structures](../../design.md) ("searching outward from the anchor of one of
  the structure's biomes").

## Desired result (to-be)

- The search visits ring 0 of every host anchor, then ring 1 of every host anchor, and so on, so the first fit
  is the one nearest to any host anchor.
- The worst case (no valid chunk anywhere) still ends after covering the disc once, not once per anchor.

## Acceptance criteria

- [ ] For a structure with several host anchors, a chunk at ring r of the second anchor is chosen over any chunk
      at ring r+1 or further of the first anchor.
- [ ] A unit test covers the interleaved order on a small synthetic case (two anchors, the first with no valid
      chunk within a few rings).
- [ ] `GuaranteeGameTest.structuresInsideRadius` still passes (all 19 structures on seeds 1–50 at 3,000 and
      4,000), and the log shows how many fallbacks moved compared with before.
- [ ] Default worlds still place no fallback and plan nothing (existing assertion).
- [ ] `./gradlew build` passes.

## Notes

- The STR-2 shortcut comment on `chunksOutward` (a full-disc scan when nothing fits) still applies; update its
  wording if the bound changes.
- Found by reading the code; not yet seen on a specific seed. The guarantee test passes today because some
  chunk always fits.
