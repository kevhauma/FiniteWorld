# TUNE-4 · Settle the island distance, spacing, size and materials

**Priority:** P3 · **Area:** Tuning · **Type:** Feature

## User story

As a player, I want a resource island within a short boat trip of any stretch of my coast, small enough that it never feels like a second continent, and with each material easy to find.

## Description

TUNE-1 settled the radius limits with a TEST-1 sweep. Do the same for resource islands: the ring's distance, the
spacing, the island size and the material weights, and confirm the default.

## Current situation (as-is)

- ISL-1 uses a ring from 250 to 1,000 past the radius, one island per 2,000 blocks of circumference, spacing 800, a
  top 24 to 40 across and at most 6 above sea level; ISL-2 uses material weights 30, 30, 25, 15. All starting guesses.
- [GuaranteeGameTest](../../../src/gametest/java/io/github/kevhauma/finiteworld/GuaranteeGameTest.java) checks the
  edge, biomes, structures and spawn; after ISL-1 it also checks the islands' limits.

## Desired result (to-be)

- TEST-1 reports per seed: island count, how many spots were dropped on deep ocean, the largest gap along the coast
  between islands, each island's top area and block count per material.
- Sweep at radius 3,000, 4,000 and 10,000, at least 20 seeds each: ring start 150, 250, 400; spacing 600, 800, 1,200.
- The chosen values: the closest ring start where no island ever reads as joined to the coast on the map check, and
  the largest spacing that keeps every point of the coast within about 1,500 blocks of an island on every seed.
- Material weights that give each material roughly its share of blocks across the sweep, clay least.
- The default (on for new worlds) is confirmed or changed, and the design doc's decision log records the values and
  the sweep table, as decisions 16 and 19 do.

## Acceptance criteria

- [ ] TEST-1 prints the island report per seed.
- [ ] The sweep table is in the decision log.
- [ ] The chosen values replace the starting guesses in ISL-1 and ISL-2's code.
- [ ] TEST-1 passes on every seed at the chosen values, islands on and off.
- [ ] Map check of seed 12345 at radius 3,000 on the test server: islands visible past the coast, none touching it.
- [ ] `./gradlew build` passes.

## Notes

- Needs ISL-1 and ISL-2.
- If the outer ring is mostly deep ocean on some seeds and too many spots drop, prefer moving the ring inward over
  allowing islands on deep ocean (monuments).
