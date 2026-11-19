# UI-1 · Radius field on the Create World screen

**Priority:** P1 · **Area:** Radius UI · **Type:** Feature

## User story

As a player, I want to set the island radius when I create a Finite world, so I choose how big my world is.

## Description

Add a customise screen for the Finite preset, the same way vanilla does for Superflat and Single Biome. It writes the radius into the generator settings and enforces the minimum.

## Current situation (as-is)

- The radius is hardcoded as 4000 in the noise settings JSON (EDGE-1). [FiniteWorldClient](../src/client/java/io/github/kevhauma/finiteworld/client/FiniteWorldClient.java) is empty.
- Design doc: [Player experience; Technical approach → Radius UI](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md), decisions #10 and #12.

## Desired result (to-be)

- A `PresetEditor` registered for `finiteworld:finite` through a client accessor mixin on vanilla's preset editor map. This is the one place a mixin is needed: vanilla offers no registry or Fabric API hook for it.
- The screen has one number field: default 4,000, Done disabled below the minimum (about 3,000).
- On Done it rebuilds `WorldDimensions` with a direct (non-registry) noise settings holder whose `finiteworld:edge` and `finiteworld:anchor` nodes carry the new radius.
- No edit after creation. Vanilla offers no customise button on existing worlds, so this needs no extra code.

## Acceptance criteria

- [ ] The Customize button appears for Finite only.
- [ ] Values below the minimum, or non-numbers, cannot be confirmed.
- [ ] Verified in game: radius 5000 → land ends at about 5,000; reopen the world → still 5,000.
- [ ] The radius is written into the world's saved generator settings (`world_gen_settings.dat`), not only referenced by preset id (moved here from EDGE-1, 2026-11-19).
- [ ] Every density function that takes a radius gets the new value (TEST-1 run at a non-default radius passes).
- [ ] Client classes live only in `src/client/`.
- [ ] `./gradlew build` passes.

## Notes

- Needs EDGE-1 and ANC-3, so one change covers every radius field.
- **Open question:** a dedicated server can pick `level-type=finiteworld:finite` but has no screen, so it always gets the default radius. Decide whether that's acceptable; the design doc doesn't mention servers.
