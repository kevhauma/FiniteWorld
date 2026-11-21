# SRV-1 · Standalone Fabric server with the mod, a pre-generator and BlueMap

**Priority:** P2 · **Area:** Test server · **Type:** Feature

## User story

As a developer, I want a standalone server running the real mod jar with a pre-generator and a browser map, so I can generate and inspect whole Finite worlds outside the dev environment.

## Description

Set up the server the rest of the [test server plan](../TEST-SERVER-PLAN.md) runs on. It uses the built jar, not `./gradlew runServer`, so it tests what players get, and it lives outside the repo so nothing of it is committed.

## Current situation (as-is)

- The only server is `./gradlew runServer` in `run/` (gitignored), on the dev classpath and full of single test worlds.
- Dedicated servers pick the Finite preset and radius in `server.properties` ([README.md](../../README.md), [FiniteRadius.withConfiguredRadius](../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java); design doc decision #14).
- Versions: Minecraft 26.3, Fabric API 0.166.2+26.3, Java 25 ([gradle.properties](../../gradle.properties), [build.gradle](../../build.gradle)).

## Desired result (to-be)

- A Fabric 26.3 server folder outside the repo (or a gitignored `server/`), started with Java 25 and `-Xmx8G` or more.
- `mods/` holds `build/libs/finiteworld-1.0.0.jar` from `./gradlew build`, Fabric API, BlueMap (Fabric 5.24+ supports 26.3 worlds; 5.28 lists 26.3) and a pre-generator: Chunky if it has a 26.3 Fabric build, otherwise Chunkwarm or Chunksmith (both list 26.3).
- Starts cleanly and creates a Finite world at a test radius (`level-type=finiteworld\:finite`, `generator-settings={"radius"\:3000}`).

## Acceptance criteria

- [ ] Server folder is outside version control: `git status` stays clean after it is set up and run.
- [ ] Mod jar comes from `./gradlew build`; `fabric.mod.json` gains no dependency on BlueMap or the pre-generator.
- [ ] The versions used (Fabric loader, Fabric API, BlueMap, pre-generator) and the server folder location are recorded in this ticket.
- [ ] Server starts with all four mods loaded, no errors in `logs/latest.log`, and creates a radius 3000 world: the log shows one plan, `Planned 47 anchors for radius 3000 ...`.
- [ ] BlueMap's web app answers on `http://localhost:8100` (BlueMap needs `accept-download: true` in its `core.conf` first).
- [ ] The pre-generator's commands work (Chunky: `chunky help`).
- [ ] README gains a short "Test server" section: the mods, the `server.properties` lines and a link to the [test server plan](../TEST-SERVER-PLAN.md).

## Notes

- Non-goals hold: no debug tool inside the mod, no mod dependency.
- Chunky had no confirmed 26.3 build on 2026-11-21. The other tickets give Chunky's commands; translate them if another pre-generator is used.
