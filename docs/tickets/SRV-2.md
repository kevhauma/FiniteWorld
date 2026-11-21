# SRV-2 · Script that creates and pre-generates a world; generate the minimum world

**Priority:** P2 · **Area:** Test server · **Type:** Feature

## User story

As a developer, I want one script that creates a Finite world at a given radius and generates all of it, so the minimum world, and any larger one later, is made unattended instead of by hand.

## Description

A small script beside the server (not in the mod) that, for each radius it is given, writes `server.properties`, starts the server, runs the pre-generator out to radius + 512, waits for it to finish and stops the server. This ticket generates the required minimum world, `r3000`; SRV-5 reuses the script for optional larger worlds.

## Current situation (as-is)

- The radius is read from `generator-settings` only when a world is created ([FiniteRadius.withConfiguredRadius](../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java)); restarting an existing world with other settings keeps its saved radius (UI-1).
- A server holds one overworld, named by `level-name`. Nothing automates creating or generating worlds.

## Desired result (to-be)

- For each radius N: set `level-name=rN`, `level-seed=12345`, `level-type=finiteworld\:finite`, `generator-settings={"radius"\:N}`; start the server; send the pre-generator commands to its console (Chunky: `chunky world minecraft:overworld`, `chunky shape circle`, `chunky center 0 0`, `chunky radius <N+512>`, `chunky start`); wait for the completion line in the log; send `stop`.
- Skips a radius whose world is already fully generated and resumes one that was interrupted (Chunky: `chunky continue`), so a stopped run can be started again.
- Overworld only; Nether and End are not pre-generated.

## Acceptance criteria

- [x] Script takes a list of radii and does the steps above for each; where it lives (with the server, or a dev-only folder in the repo) is decided and recorded here. (Lives in the repo, dev-only and outside the mod: [tools/test-server/pregen.sh](../../tools/test-server/pregen.sh), run as `bash tools/test-server/pregen.sh <server-dir> <radius>...` from Git Bash. It drives the console through stdin, `tail -f console.in | java ...`, and also sets `pause-when-empty-seconds=-1`, because an empty server pauses and would stall Chunky.)
- [x] Run for radius 3000: world `r3000` is created, the log shows `Planned 47 anchors for radius 3000 ...`, the pre-generator reports 100 % at radius 3512 and the server stops by itself. (2026-11-21: `[ANC-2] Planned 47 anchors for radius 3000 band 500: 4809 grid points (step 64) in 243 ms, plan hash 127182501`; `Task started ... circle region centered at 0, 0 with radius 3512`; `Task finished for minecraft:overworld. Processed: 194481 chunks (100.00%), Total time: 0:15:40`; then `Stopping server` and the script prints `r3000: done`. Chunky counts the circle's bounding square (441² = 194,481); the region headers hold 151,332 chunks inside radius 3,512, which matches the circle's 151,363.)
- [x] `r3000/data/minecraft/world_gen_settings.dat` holds radius 3000 (edge, edge_cap and anchor_plan nodes), not the preset's 4,000. (Read with a small NBT reader: 35 `finiteworld:edge`, 2 `finiteworld:edge_cap` and 1559 `finiteworld:anchor_plan` nodes, every one `radius=3000.0`.)
- [x] Interrupted run: killing the server mid-generation and rerunning the script resumes and finishes `r3000` without starting over; rerunning after it finished does nothing for `r3000`. (Java force-killed at 10.08 % (19,607 chunks). On the rerun Chunky read the chunks already on disk at ~680 chunks/s and was back at 9.7 % within 4 s, then generated the rest. A rerun after it finished prints `r3000: already generated, skipped` and exits in 0.07 s. *2026-11-21 divergence:* it resumes by skipping chunks already on disk, not with `chunky continue`, because Chunky keeps one saved task per dimension (`config/chunky/tasks/`) and every world folder would share it. The script cancels any saved task and starts afresh.)
- [x] Generation time, disk size and heap used for `r3000` are recorded here, and the plan's estimate for `r3000` is replaced with them. They are the per-chunk baseline for deciding SRV-5. (See **Measured** below; [TEST-SERVER-PLAN.md](../TEST-SERVER-PLAN.md#worlds) updated.)

## Measured

`r3000`, 2026-11-21, 16-core Windows 11 machine, `-Xmx8G`, BlueMap's jar parked (`.off`) so its renderer didn't compete for CPU (SRV-3 renders and times the map separately).

| | |
| --- | --- |
| Chunks | 151,332 inside radius 3,512 (plus ~21 k partly generated chunks just outside, 172,637 saved in all) |
| Generation time | ~19 min: 3 min 15 s up to the kill at 10 %, then 15 min 40 s for the rest (Chunky's total). Server start ~20 s per run. Average **7.5 ms per chunk** (~133 chunks/s); the rate ranged from 78 to 186 chunks/s |
| Disk | 1,470,924,843 bytes (1.4 GB) for `r3000`, of which 1.43 GB is region files; **~9.7 KB per chunk**. Nether and End are empty |
| Heap | G1 peaked at 3.7 GB before a GC and 2.7 GB after one (`logs/gc-r3000.log`, resumed run); no full GC. 8 GB is plenty |

## Notes

- Needs SRV-1.
- Driving the console: feed the server's stdin from the script (a pipe, or `screen`/`tmux` on Linux), or use RCON. Pick the simplest that works on the server's OS.
