# 03 · Stop the planner timing test from gating the build

**Source:** review 2026-11-24, finding 3 · **Area:** Build/CI · **Severity:** Low · **Type:** Refactor

## User story

As the maintainer, I want releases not to fail because a CI runner was slow, so a tag push publishes the build
whenever the code is correct.

## Description

A unit test asserts wall-clock time, and it runs in `./gradlew build`, which both CI and the release workflow
run.

## Current situation (as-is)

- [`AnchorPlannerTest.plansWithinTheAnc1BudgetAtRadius4000`](../../../src/test/java/io/github/kevhauma/finiteworld/anchor/AnchorPlannerTest.java)
  (lines 106–115) fails if the best of three plans takes more than 100 ms.
- [`.github/workflows/release.yml`](../../../.github/workflows/release.yml) runs `./gradlew build` before
  `gh release create`, so a slow shared runner blocks the release.

## Desired result (to-be)

- The timing check no longer fails the build on a slow machine. Either it is tagged and excluded from `test`
  (run on demand), or its bound is loose enough to catch a real regression only (for example 10× the measured
  time), or it only logs the time.

## Acceptance criteria

- [ ] `./gradlew build` does not fail on timing alone.
- [ ] The planner's measured time is still visible somewhere (test output, a tagged test, or the game-test log).
- [ ] `./gradlew build` passes.

## Notes

- The design doc's planning-time figures (ANC-1, ANC-2) come from game tests and the dedicated server, not from
  this test, so it can change without a design doc update.
