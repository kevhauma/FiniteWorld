# 09 · Replace array-filling lambdas with loops

**Source:** review 2026-11-24, finding 9 · **Area:** Readability · **Severity:** Low · **Type:** Refactor

## User story

As a developer, I want array writes to look like loops, so I see the side effect at a glance.

## Description

Several places fill arrays from inside `IntStream.range(...).forEach(i -> arr[i] = ...)`. That's an imperative
loop disguised as a stream; a `for` loop states it directly. Java has no `Arrays.setAll` for `float[]`.

## Current situation (as-is)

- [`AnchorField.java`](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorField.java): line 56
  (prefix sum into `start`), line 103 (`floats`).
- [`AnchorPlanner.java`](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanner.java): line
  127 (`score`), line 235 (`toArray`), line 295 (NaN outside the disc in `distances`).

## Desired result (to-be)

- Each of these is a plain `for` loop, or a real stream with no side effect where one exists (for example
  `Arrays.setAll` for `int[]`).

## Acceptance criteria

- [x] No `forEach` lambda in `src/main` writes into an array. (The five are `for` loops; grep finds none left.)
- [x] `AnchorFieldTest` and `AnchorPlannerTest` pass unchanged, including `sameClimateGivesTheSamePlan`. (Pass; the
      game-test plan hash for seed 12345 is still 1725760216.)
- [x] `./gradlew build` passes. (Unit tests and all 10 game tests.)

## Notes

- Pure refactor: plans and plan hashes must not change.
