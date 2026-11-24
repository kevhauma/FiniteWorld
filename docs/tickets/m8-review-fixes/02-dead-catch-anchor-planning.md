# 02 · Make the anchor-planning error handler reachable

**Source:** review 2026-11-24, finding 2 · **Area:** Anchors · **Severity:** Low · **Type:** Bug fix

## User story

As a developer reading a crash log, I want an anchor-planning failure reported as such, so I see the real cause
without unwrapping a Guava exception.

## Description

The `catch` around the plan cache never runs for the planner's own failures, so its message never appears.

## Current situation (as-is)

- [`AnchorPlanFunction.compileSampler`](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanFunction.java)
  (lines 92–96) calls `PLANS.get(context, () -> this.plan(context))` and catches `ExecutionException`.
- Guava's `Cache.get(key, loader)` throws `ExecutionException` only for checked exceptions. Unchecked ones are
  wrapped in `UncheckedExecutionException`.
- The planner only throws unchecked exceptions: `IllegalStateException("No room for anchors…")` and
  `IllegalStateException("No room for the … anchor")` in
  [`AnchorPlanner`](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanner.java) (lines 132
  and 145). So the catch is dead and "Anchor planning failed" never shows up.

## Desired result (to-be)

- A planner failure reaches the log as `IllegalStateException("Anchor planning failed", cause)` with the
  planner's message as the cause, or the try is removed and the planner's exception propagates unwrapped.

## Acceptance criteria

- [ ] A planner exception thrown while compiling the plan node surfaces with the planner's message directly
      under the top-level exception.
- [ ] A test triggers a planning failure (for example a radius minus band below the anchor radius, built in code)
      and asserts the exception type and message.
- [ ] `AnchorPlanFunction.plansComputed()` still counts one plan per world (`AnchorPlanGameTest` passes).
- [ ] `./gradlew build` passes.

## Notes

- If the cache call changes (for example to `PLANS.asMap().computeIfAbsent`), check that `recordStats()` still
  counts loads, since `plansComputed()` and two game tests depend on it.
