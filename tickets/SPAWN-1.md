# SPAWN-1 · Spawn on land near the centre

**Priority:** P1 · **Area:** Spawn · **Type:** Feature

## User story

As a player, I want to spawn on land near the middle of the island, so I never start at sea.

## Description

Vanilla already searches for spawn near the origin using the noise settings' `spawn_target` climate points, which exclude ocean. Check whether that is enough with the edge in place, and only add code if it isn't.

## Current situation (as-is)

- `spawn_target` is copied unchanged from vanilla in PRE-1.
- Design doc: [Technical approach → Spawn; Player experience](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Desired result (to-be)

- Spawn is on land within a few hundred blocks of the origin on every tested seed.
- If vanilla fails on some seed, use the smallest fix: tighten `spawn_target` in the noise settings first, and add a code hook only if that is not enough.

## Acceptance criteria

- [ ] Checked on 10 seeds at radius 3000: spawn is on land, within 500 blocks of (0, 0). List the seeds and spawn positions.
- [ ] Default-world spawn is unchanged.
- [ ] `./gradlew build` passes.

## Notes

- Needs EDGE-1. The radius is at least 3,000 and vanilla's spawn search stays near the origin, so the edge should not interfere. Most likely this ticket needs no code.
