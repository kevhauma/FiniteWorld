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

- [ ] TEST-1 run at decreasing radii; the failure point is recorded and the minimum set above it.
- [ ] UI-1's minimum and the band and anchor constants are updated to match.
- [ ] Design doc's decision log and Open list are updated.
- [ ] `./gradlew build` passes.

## Notes

- Needs EDGE-2, ANC-3 and STR-2.
