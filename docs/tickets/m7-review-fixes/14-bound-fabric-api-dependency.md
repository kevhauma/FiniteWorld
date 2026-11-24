# 14 — Set a lower bound on the fabric-api dependency

Milestone: M7 · Area: Build · Severity: Low · Review finding 14

## Problem

`src/main/resources/fabric.mod.json:32` declares `"fabric-api": "*"`. That accepts older Fabric API versions, which
may lack APIs the mod will use.

## Task

Change it to `"fabric-api": ">=0.166.2"`, matching `gradle.properties`.

## Acceptance criteria

- [ ] `fabric.mod.json` requires at least the Fabric API version the mod is compiled against.
- [ ] `./gradlew build` passes.
