# SPAWN-1 · Spawn on land near the centre

**Priority:** P1 · **Area:** Spawn · **Type:** Feature

## User story

As a player, I want to spawn on land near the middle of the island, so I never start at sea.

## Description

Vanilla already searches for spawn near the origin using the noise settings' `spawn_target` climate points, which exclude ocean. Check whether that is enough with the edge in place, and only add code if it isn't.

## Current situation (as-is)

- `spawn_target` is copied unchanged from vanilla in PRE-1.
- Design doc: [Technical approach → Spawn; Player experience](../Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Desired result (to-be)

- Spawn is on land within a few hundred blocks of the origin on every tested seed.
- If vanilla fails on some seed, use the smallest fix: tighten `spawn_target` in the noise settings first, and add a code hook only if that is not enough.
- **Outcome (2026-11-20):** vanilla's spawn is on land for every seed but lands 516 and 568 blocks out on seeds 6 and 7 (vanilla's own spawn there too; the edge is not involved). `spawn_target` is tightened, no code: both points gain `finiteworld:spawn_distance` = `[1.0, 1.0]`, a `finiteworld:edge` over the constant 1.0 with radius 800 and band 500. It is 1.0 within 300 blocks and ramps down to -0.22 at 800, so the climate search keeps vanilla's choice within 300 blocks and otherwise trades a little climate fit for distance. Dropping the ridges constraint or capping continents at 360 blocks were tried first and made some seeds worse (up to 1,540 blocks), because vanilla's search samples coarse 512-block rings before refining.

## Acceptance criteria

- [x] Checked on 10 seeds at radius 3000: spawn is on land, within 500 blocks of (0, 0). List the seeds and spawn positions. (`GuaranteeGameTest.spawnOnLandNearCentre`, seeds 1-20, asserts the spawn's column is dry with feet above sea level (y > 63) and within 500 blocks. It reproduces `MinecraftServer.setInitialSpawn` on generated terrain: `NoiseBasedChunkGenerator.getOrigin`, then the 11 x 11 chunk spiral for the first column without fluid on top. Checked against real servers: pure vanilla 26.3 server seeds 6 and 7 spawn at (-64, 72, 512) and (208, 64, 528), exactly the test's Default values; a Finite server (radius 4000) seed 7 spawns at (112, 66, 320), the test's Finite value. Table below; max 339 blocks.)
- [x] Default-world spawn is unchanged. (The change only touches `finiteworld:finite` noise settings and adds `finiteworld:spawn_distance`; `minecraft:overworld` and spawn code are untouched, no mixins. Seed 7 Default world: modded `runServer` (`level-type=minecraft:normal`) spawn (208, 64, 528) = pure vanilla server (no mod) (208, 64, 528) = the test's Default column.)
- [x] `./gradlew build` passes. (`gradlew.bat build`: BUILD SUCCESSFUL, both game tests pass.)

Spawn per seed, radius 3000 (`[SPAWN-1]` lines of `gradlew.bat build`; y is the feet position on bare terrain, a tree can raise it in game):

| Seed | Finite spawn | Distance | Default-world spawn |
| --- | --- | --- | --- |
| 1 | 144, 71, 160 | 215 | 160, 70, 160 |
| 2 | -32, 65, -32 | 44 | -32, 66, 0 |
| 3 | 0, 71, 0 | 0 | 0, 71, 0 |
| 4 | -112, 66, 64 | 128 | -128, 71, 16 |
| 5 | 0, 65, 0 | 0 | 0, 65, 0 |
| 6 | -128, 130, -240 | 271 | -64, 72, 512 (516) |
| 7 | 112, 66, 320 | 339 | 208, 64, 528 (568) |
| 8 | 0, 109, 0 | 0 | 0, 109, 0 |
| 9 | 0, 71, 0 | 0 | 0, 71, 0 |
| 10 | 0, 88, 0 | 0 | 0, 88, 0 |
| 11 | 16, 66, 80 | 82 | 64, 67, 48 |
| 12 | -32, 72, -32 | 44 | -16, 71, -48 |
| 13 | 0, 71, 0 | 0 | 0, 71, 0 |
| 14 | 112, 118, -160 | 195 | 368, 66, 400 |
| 15 | 0, 119, 0 | 0 | 0, 119, 0 |
| 16 | -32, 76, 272 | 274 | -176, 72, -304 |
| 17 | -32, 64, -64 | 70 | 32, 70, -32 |
| 18 | -48, 72, 144 | 152 | -80, 75, 144 |
| 19 | 0, 126, 0 | 0 | 0, 126, 0 |
| 20 | 0, 91, 0 | 0 | 0, 91, 0 |

## Notes

- Needs EDGE-1. The radius is at least 3,000 and vanilla's spawn search stays near the origin, so the edge should not interfere. Most likely this ticket needs no code.
- `finiteworld:spawn_distance` is a `finiteworld:edge` used as a radial ramp, not the world radius: UI-1's radius swap must leave it alone.
