# TUNE-1 · Settle the open values: minimum radius, band width, anchor spacing

**Priority:** P2 · **Area:** Tuning · **Type:** Feature

## User story

As a player, I want the minimum radius and the edge and anchor settings tuned on real seeds, so small worlds still keep the guarantee and every world looks natural.

## Description

Settle the design doc's open values with data and playtesting, and write them into the decision log.

## Current situation (as-is)

- Design doc: [Risks → Open](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md) leaves the minimum anchor distance, the band width range and the exact minimum radius open.

## Desired result (to-be)

- Minimum radius: the smallest radius at which TEST-1 passes for all seeds, plus a safety margin, enforced in UI-1's field.
- Band width range and anchor spacing picked from playtest screenshots on 5 seeds.

## Acceptance criteria

- [x] TEST-1 run at decreasing radii; the failure point is recorded and the minimum set above it. (Temporary sweep of `GuaranteeGameTest` at 2000/2250/2500/2750, 20 seeds for biomes and 50 for structures, plans that throw caught and logged; reverted afterwards. 2000: no plan on 20/20 seeds (29-35 of 47 anchors fit); 2250: no plan on 19/20 (38-46 fit); 2500: all 47 biomes, no land past the radius, structures missing on 4/50 (stronghold seed 2, trail ruins seed 6, desert pyramid seeds 9 and 24); 2750: desert pyramid missing on seeds 10 and 24. 3000 and 4000 pass every seed in the normal build. Failure point 2750, minimum kept at 3000.)
- [x] UI-1's minimum and the band and anchor constants are updated to match. (No value changes: `FiniteRadius.MIN` = 3000 matches the sweep; `FiniteRadius.MAX` = 20000 kept (about 2 s to plan); edge `band` 500, `narrow_band` 200, `outline_depth` 300 (EDGE-2) give no land past the radius on any seed; anchor radius 192 and spacing 384 kept, plan band kept at 500 (800 broke the desert pyramid fallback on 2/100 worlds, see Notes). Values picked from TEST-1 data, not from playtest screenshots: the visual check stays with EDGE-2's in-game criteria.)
- [x] Design doc's decision log and Open list are updated. (Decision log rows 13 revisited, 16 minimum radius, 17 band width, 18 anchor size and spacing; Risks gains the desert pyramid gap; Open now lists only the visual check.)
- [x] `./gradlew build` passes. (`gradlew.bat build`: BUILD SUCCESSFUL, unit tests pass, all 7 game tests passed.)

## Notes

- Needs EDGE-2, ANC-3 and STR-2.
- From EDGE-2: the anchor plan's `band` (500) is smaller than the edge's `band` + `outline_depth` (800). Setting it to 800 made STR-2 miss the desert pyramid on seed 44 at 3000 and seed 39 at 4000 (every desert patch below sea level for the pyramid's footprint). Pick the anchor band together with the outline depth.
