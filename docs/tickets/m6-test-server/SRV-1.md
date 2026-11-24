# SRV-1 · Standalone Fabric server with the mod, a pre-generator and BlueMap

**Priority:** P2 · **Area:** Test server · **Type:** Feature

## User story

As a developer, I want a standalone server running the real mod jar with a pre-generator and a browser map, so I can generate and inspect whole Finite worlds outside the dev environment.

## Description

Set up the server the rest of the [test server plan](../../TEST-SERVER-PLAN.md) runs on. It uses the built jar, not `./gradlew runServer`, so it tests what players get, and it lives outside the repo so nothing of it is committed.

## Current situation (as-is)

- The only server is `./gradlew runServer` in `run/` (gitignored), on the dev classpath and full of single test worlds.
- Dedicated servers pick the Finite preset and radius in `server.properties` ([README.md](../../../README.md), [FiniteRadius.withConfiguredRadius](../../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java); design doc decision #14).
- Versions: Minecraft 26.3, Fabric API 0.166.2+26.3, Java 25 ([gradle.properties](../../../gradle.properties), [build.gradle](../../../build.gradle)).

## Desired result (to-be)

- A Fabric 26.3 server folder outside the repo (or a gitignored `server/`), started with Java 25 and `-Xmx8G` or more.
- `mods/` holds `build/libs/finiteworld-1.0.0.jar` from `./gradlew build`, Fabric API, BlueMap (Fabric 5.24+ supports 26.3 worlds; 5.28 lists 26.3) and a pre-generator: Chunky if it has a 26.3 Fabric build, otherwise Chunkwarm or Chunksmith (both list 26.3).
- Starts cleanly and creates a Finite world at a test radius (`level-type=finiteworld\:finite`, `generator-settings={"radius"\:3000}`).

## Acceptance criteria

- [x] Server folder is outside version control: `git status` stays clean after it is set up and run. (Folder is beside the repo; after setup and a run, `git status --short` shows only `?? logs/`, which was there before.)
- [x] Mod jar comes from `./gradlew build`; `fabric.mod.json` gains no dependency on BlueMap or the pre-generator. (`build/libs/finiteworld-1.0.0.jar` copied to `mods/` after `gradlew.bat build` passed with all 7 game tests; `depends` is still fabricloader, minecraft, java, fabric-api.)
- [x] The versions used (Fabric loader, Fabric API, BlueMap, pre-generator) and the server folder location are recorded in this ticket. (See **Setup** below.)
- [x] Server starts with all four mods loaded, no errors in `logs/latest.log`, and creates a radius 3000 world: the log shows one plan, `Planned 47 anchors for radius 3000 ...`. (2026-11-21: mod list shows bluemap 5.29, chunky 1.5.66, fabric-api 0.166.2+26.3, finiteworld 1.0.0; 0 ERROR or Exception lines; exactly one `[ANC-2] Planned 47 anchors for radius 3000 band 500: 4809 grid points (step 64) in 208 ms, plan hash 127182501`; `Done (3.456s)!`.)
- [x] BlueMap's web app answers on `http://localhost:8100` (BlueMap needs `accept-download: true` in its `core.conf` first). (After `accept-download: true` and `bluemap reload`: `WebServer started.`, `curl` returns 200 with `<title>BlueMap</title>`.)
- [x] The pre-generator's commands work (Chunky: `chunky help`). (Console prints the `Chunky Commands` list.)
- [x] README gains a short "Test server" section: the mods, the `server.properties` lines and a link to the [test server plan](../../TEST-SERVER-PLAN.md). ([README.md](../../../README.md#test-server))

## Setup

Set up 2026-11-21.

| | |
| --- | --- |
| Folder | `D:\desktop\mc-mods\finite-test-server` (beside the repo, not in it) |
| Launcher | Fabric server launcher, loader 0.20.1, installer 1.1.2 (`fabric-server-launch.jar` from meta.fabricmc.net) |
| Java | Temurin 25.0.5, `java -Xmx8G -jar fabric-server-launch.jar nogui` |
| `mods/` | `finiteworld-1.0.0.jar` (from `gradlew.bat build`), `fabric-api-0.166.2+26.3.jar`, `bluemap-5.29-fabric.jar`, `Chunky-Fabric-1.5.66.jar` (all from Modrinth) |
| `server.properties` | `level-name=r3000`, `level-seed=12345`, `level-type=finiteworld\:finite`, `generator-settings={"radius"\:3000}`, `online-mode=false`, `view-distance=6`, `spawn-protection=0` |
| BlueMap | `config/bluemap/core.conf`: `accept-download: true` |
| Console | stdin fed from a file: `tail -f console.in \| java ... > console.out`, commands appended with `echo "chunky help" >> console.in` (Git Bash) |

## Notes

- Non-goals hold: no debug tool inside the mod, no mod dependency.
- ~~Chunky had no confirmed 26.3 build on 2026-11-21.~~ Chunky-Fabric 1.5.66 (Modrinth, 2026-11-04) lists 26.3 and loads on it, so the other tickets' Chunky commands apply as written.
