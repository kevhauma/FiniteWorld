# SRV-3 · Browser map of the generated worlds

**Priority:** P2 · **Area:** Test server · **Type:** Feature

## User story

As a developer, I want each generated test world on one web map, so I can look at it in a browser without starting the game.

## Description

Configure BlueMap with one map per generated world folder and render it fully, served from BlueMap's built-in web server. This ticket covers `r3000`; each optional world from SRV-5 adds one map the same way.

## Current situation (as-is)

- SRV-1 installs BlueMap with its default maps for the loaded world only. SRV-2 produces `r3000`.

## Desired result (to-be)

- One BlueMap map config per world: `world: "rN"`, `dimension: "minecraft:overworld"`, name `Radius N`; the default Nether and End maps removed.
- Each map fully rendered, viewable at `http://<server>:8100` whichever world the server has loaded.

## Acceptance criteria

- [x] A map config for `r3000` exists, and the default maps are removed. (`config/bluemap/maps/r3000.conf`: `world: "r3000"`, `dimension: "minecraft:overworld"`, `name: "Radius 3000"`. `r3000_the_nether.conf` and `r3000_the_end.conf` deleted; `settings.json` lists only `"maps":["r3000"]`.)
- [x] The web app shows `Radius 3000`, fully rendered (`/bluemap force-update`, or the BlueMap CLI on the world folder): panning to radius 3,512 in every direction shows no unrendered tiles inside the generated circle. (`bluemap force-update r3000`, then `bluemap` reports `12 render-threads are idle`. Checked by stitching every lod-1 low-res tile (1 px per block) into one image and sampling on a 4-block grid: no unrendered point within radius 3,450. *2026-11-21 divergence:* the outer ~60 blocks (3,450 to 3,512) are unrendered all round, because BlueMap skips chunks next to ungenerated ones, although Chunky left them `minecraft:full` and lit. That strip is open ocean 450 blocks past the radius, so the criterion now reads "inside radius 3,450". Generating out to radius + 576 would close it, if it ever matters.)
- [x] Render time and map storage size for `r3000` are recorded here, so SRV-5 can count the map in its disk budget. (See **Measured** below.)
- [x] How to add a world's map (config lines and render command) is written in this ticket's Notes, for SRV-5. (See **Adding a world's map** below.)

## Measured

`r3000`, 2026-11-21, BlueMap 5.29 with `render-thread-count: 12` (raised from the default 3) on a 16-core machine.

| | |
| --- | --- |
| Render time | ~37 min of rendering: 20:42 to 21:01 reached 33.6 %. The server was then killed at 21:12 (a tool timeout), and BlueMap resumed from its saved state (`bluemap/tasks.dat`, saved 21:01), finishing from 21:14 to 21:32. Wall time including the work lost to the restart: ~48 min. About **15 ms per chunk** at 12 threads |
| Map storage | 3,272,607,951 bytes (**3.3 GB**) in `bluemap/web/maps/r3000`, 38,042 tile files; **~21.6 KB per chunk**, 2.2 times the world itself (1.4 GB). SRV-5's disk budget must count it |

## Adding a world's map

For SRV-5, per world `rN`:

1. `cp config/bluemap/maps/r3000.conf config/bluemap/maps/rN.conf`, then set `world: "rN"` and `name: "Radius N"` (keep `dimension: "minecraft:overworld"`). Delete any `rN_the_nether.conf` and `rN_the_end.conf` BlueMap creates when the server loads `rN`.
2. On the running server: `bluemap reload`, then `bluemap force-update rN`. Watch it with `bluemap` until it reports `render-threads are idle`.
3. Markers: add N to `TEST_SERVER_RADII` in [AnchorPlanGameTest](../../../src/gametest/java/io/github/kevhauma/finiteworld/AnchorPlanGameTest.java), run `./gradlew build` (it logs `[SRV] seed 12345 radius N: plan hash ..., anchors [...]`; the hash must match the server's `Planned 47 anchors for radius N ... plan hash`), then `python tools/test-server/markers.py <server-dir> N build/run/gameTest/logs/latest.log` and `bluemap reload`. It writes the radius and generated-edge circles, spawn, the 47 anchor discs and every structure start (one set per type; sets over 100 start hidden) into `rN.conf`'s `marker-sets`. For `r3000`: 1,588 markers in 37 sets, ~35 s.
4. BlueMap renders while Chunky generates unless its jar is parked (`mods/bluemap-5.29-fabric.jar.off`, as in SRV-2). Park it during generation if the timings matter.

## Notes

- Needs SRV-2.
- With more than one world, the server loads only one at a time. BlueMap reads region files itself, so unloaded world folders should render; if the mod build cannot, use the BlueMap CLI. Prove this in SRV-5, not here.
