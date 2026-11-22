# 10 — Cache Gradle in CI and stop double runs

Milestone: M1 · Area: CI · Severity: Low · Review finding 10

## Problem

`.github/workflows/build.yml:7` triggers on `[pull_request, push]`, so every push to a PR branch builds twice.
Nothing caches Gradle or the Loom Minecraft cache, so every run downloads and remaps Minecraft again.

## Task

- Replace `gradle/actions/wrapper-validation@v6` with `gradle/actions/setup-gradle@v6`, which validates the
  wrapper and also caches.
- Limit `push` to `branches: [main]`.

## Acceptance criteria

- [ ] A push to a PR branch starts one CI run.
- [ ] A second run on the same branch hits the Gradle cache and finishes faster.

Note: this only applies once the repo has a GitHub remote.
