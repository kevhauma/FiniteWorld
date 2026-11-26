# 11 · Readability nits

**Source:** review 2026-11-24, finding 11 · **Area:** Readability · **Severity:** Low · **Type:** Refactor

## User story

As a developer, I want small inconsistencies gone, so the code reads the same way everywhere.

## Description

Five small issues, none affecting behaviour, worth fixing in one pass.

## Current situation (as-is)

- **Duplicate grid data.** [`AnchorPlanner.Grid`](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanner.java)
  (lines 83–116) stores every sample twice, per point in `values` and per parameter in `columns`. `sample()`
  repeats the index-to-coordinate math that `x()` and `z()` already have.
- **Argument order.** [`EdgeCapDensityFunction.capped`](../../../src/main/java/io/github/kevhauma/finiteworld/edge/EdgeCapDensityFunction.java)
  (line 31) takes `(blockX, blockZ, blockY, …)`; everywhere else is x, y, z.
- **Constant placement.** `INLAND` in
  [`AnchorPlanFunction`](../../../src/main/java/io/github/kevhauma/finiteworld/anchor/AnchorPlanFunction.java)
  (line 118) sits between methods instead of with the other constants.
- **Formatting.** 38 lines under `src/` are longer than 160 characters (13 in `GuaranteeGameTest`, 4 in
  `AnchorPlanFunction`), and import order differs between files (`java.*` first in some, `com.*` first in
  others, `org.spongepowered` before `net.minecraft` in the mixins). No formatter is configured.
- **Package cycle.** [`FiniteRadius`](../../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java)
  imports the `edge` and `anchor` node types, and since M1-02 `EdgeCapDensityFunction` and `AnchorPlanFunction`
  import `FiniteRadius.CODEC`.

## Desired result (to-be)

- `Grid` keeps one copy of the samples (the per-parameter columns the hot loop needs) and one index helper.
- `capped` takes x, y, z.
- `INLAND` is declared with the other constants.
- One import order and a line-length limit, applied once. A formatter (for example Spotless) only if it costs no
  more than a few lines of build config.
- The radius limits live where neither side imports the other (for example a small `RadiusLimits` holder, or the
  codec inside the `edge` package), or the cycle is accepted with a one-line reason.

## Acceptance criteria

- [x] Each of the five items is fixed or explicitly left with a reason in this ticket. (See Outcome below.)
- [x] Plans and plan hashes are unchanged (`sameClimateGivesTheSamePlan`; seed 12345 hash in the game-test log).
      (Passes; seed 12345 plan hash 1725760216, as before.)
- [x] `./gradlew build` passes. (Unit tests and all 10 game tests.)

## Notes

- A formatter run touches most files; do it in its own commit so reviews of other changes stay readable.

## Outcome

- **Duplicate grid data:** fixed. `Grid` keeps the per-parameter `columns`, an `inside` mask and one `coordinate`
  helper; `climate(i)` reads a point's climate from the columns.
- **Argument order:** fixed. `EdgeCapDensityFunction.capped` takes x, y, z.
- **Constant placement:** fixed. `INLAND` sits with `STEP` at the top of `AnchorPlanFunction`.
- **Formatting:** fixed by hand, no formatter. Every Java file uses one import order (static, this mod, `java`, other
  libraries, Fabric, Minecraft, with a blank line between groups) and no line is longer than 160 characters. A new
  `.editorconfig` records both, so IntelliJ applies them; Spotless was skipped because it adds a build plugin.
  Done in this ticket's single commit (one commit per ticket was asked for), not a separate formatter commit.
- **Package cycle:** accepted. `FiniteRadius` holds the limits, parsing and rewrite of the one setting; splitting the
  codec out would spread them across packages. The reason is in `FiniteRadius`'s Javadoc.
