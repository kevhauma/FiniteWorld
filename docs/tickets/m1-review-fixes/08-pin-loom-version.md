# 08 — Pin Loom to a release version

Milestone: M1 · Area: Build · Severity: Medium · Review finding 8

## Problem

`gradle.properties:10` has `loom_version=1.19-SNAPSHOT`. It resolved to 1.19.4 on 2026-11-22, but it can resolve to
a different version later and change the build without any commit.

## Task

Set `loom_version=1.19.4`, or the latest 1.19.x release at the time.

## Acceptance criteria

- [ ] `gradle.properties` contains no `-SNAPSHOT` version.
- [ ] `./gradlew build` passes.
