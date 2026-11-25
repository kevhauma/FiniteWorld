# Milestone 8 — Review fixes, 2026-11-24

Source: the full codebase review of 2026-11-24 (branch `ticket-skills`, with the M7-02 radius codec change
uncommitted). One ticket per finding; each ticket's number matches its finding's number in the review.

The review found no generation-breaking bug. Checked against the vanilla 26.3 sources and found correct: the
samplers' buffer index order, compiling `anchor_plan` children through the optimizer, structure state
registration before ring positions are computed, the client mixin's `Map.get` target, and the weighted
structure pick in `StructureStarts.generatingAt`. All 39 unit tests passed; game tests were not run.

Goal: the structure fallback uses every host anchor, failures are reported clearly, datapack input is
validated, and the code reads plainly.

| # | Ticket | Area | Severity | Status |
| --- | --- | --- | --- | --- |
| 01 | [Search every host anchor before widening the fallback search](01-fallback-search-all-anchors.md) | Structures | Medium | done |
| 02 | [Make the anchor-planning error handler reachable](02-dead-catch-anchor-planning.md) | Anchors | Low | done |
| 03 | [Stop the planner timing test from gating the build](03-flaky-planner-timing-test.md) | Build/CI | Low | done |
| 04 | [Respect vanilla exclusion zones in the fallback](04-fallback-exclusion-zones.md) | Structures | Low | done |
| 05 | [Validate the anchor plan's band against its radius](05-validate-anchor-plan-band.md) | Anchors | Low | done |
| 06 | [Don't crash the Customize screen on a replaced preset](06-customize-screen-orelsethrow.md) | Radius UI | Low | open |
| 07 | [Build the spawn ramp from vanilla density functions](07-spawn-distance-vanilla-ramp.md) | Spawn | Low | open |
| 08 | [Quiet the per-world INFO logs](08-quiet-info-logs.md) | Code | Low | open |
| 09 | [Replace array-filling lambdas with loops](09-array-filling-lambdas.md) | Readability | Low | open |
| 10 | [Replace `int[]` points with a record](10-int-array-points.md) | Readability | Low | open |
| 11 | [Readability nits](11-readability-nits.md) | Readability | Low | open |
| 12 | [Remove leftovers](12-remove-leftovers.md) | Cleanup | Low | open |

Suggested order:

1. Ticket 01.
2. Tickets 02, 03, 05 and 06, together in one small robustness PR.
3. Ticket 04, then ticket 07.
4. Tickets 08–12, together in one cleanup PR.
