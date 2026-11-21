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

- [ ] Script takes a list of radii and does the steps above for each; where it lives (with the server, or a dev-only folder in the repo) is decided and recorded here.
- [ ] Run for radius 3000: world `r3000` is created, the log shows `Planned 47 anchors for radius 3000 ...`, the pre-generator reports 100 % at radius 3512 and the server stops by itself.
- [ ] `r3000/data/minecraft/world_gen_settings.dat` holds radius 3000 (edge, edge_cap and anchor_plan nodes), not the preset's 4,000.
- [ ] Interrupted run: killing the server mid-generation and rerunning the script resumes and finishes `r3000` without starting over; rerunning after it finished does nothing for `r3000`.
- [ ] Generation time, disk size and heap used for `r3000` are recorded here, and the plan's estimate for `r3000` is replaced with them. They are the per-chunk baseline for deciding SRV-5.

## Notes

- Needs SRV-1.
- Driving the console: feed the server's stdin from the script (a pipe, or `screen`/`tmux` on Linux), or use RCON. Pick the simplest that works on the server's OS.
