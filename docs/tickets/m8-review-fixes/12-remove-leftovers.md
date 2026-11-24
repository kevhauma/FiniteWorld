# 12 · Remove leftovers

**Source:** review 2026-11-24, finding 12 · **Area:** Cleanup · **Severity:** Low · **Type:** Refactor

## User story

As a developer, I want no dead code or stray files, so everything in the repo has a reason to be there.

## Description

An empty client entrypoint, an untracked log folder and a template comment are left over.

## Current situation (as-is)

- [`FiniteWorldClient`](../../../src/client/java/io/github/kevhauma/finiteworld/client/FiniteWorldClient.java) has
  an empty `onInitializeClient`. It was kept for the radius UI, but UI-1 was built with a client mixin and never
  used it. It is registered in [`fabric.mod.json`](../../../src/main/resources/fabric.mod.json) under
  `entrypoints.client`.
- `logs/` at the repo root holds build and pre-generation logs (`build-climate-*.log`, `pregen-r5000.log`, …) and
  is not in [`.gitignore`](../../../.gitignore).
- [`gradle.properties`](../../../gradle.properties) line 1: the template comment "Done to increase the memory
  available to gradle."

## Desired result (to-be)

- `FiniteWorldClient` and its entrypoint are deleted; the client mixin config stays.
- `logs/` is ignored.
- The `jvmargs` comment is removed or says why 1 GB is enough.

## Acceptance criteria

- [ ] `./gradlew build` passes.
- [ ] `./gradlew runClient` reaches the title screen, and Create World → Finite → Customize still opens the radius
      screen.
- [ ] `git status` no longer lists `logs/`.

## Notes

- Check nothing else references `FiniteWorldClient` (grep) before deleting.
