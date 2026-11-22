# Milestone M1 — Review fixes

Source: [project review 2026-11-22](../../project-review-2026-11-22.md). One ticket per finding. Each ticket's
number matches its finding's number in the review.

Goal: the skeleton is clean and builds the same way every time, and the design doc has no known gaps before any
worldgen code is written.

| # | Ticket | Area | Severity | Status |
| --- | --- | --- | --- | --- |
| 01 | [Edge and anchors must reach terrain, not just the router](01-edge-must-reach-terrain.md) | Design | High | Open |
| 02 | [Validate the radius in the codec](02-validate-radius-in-codec.md) | Design | High | Open |
| 03 | [Decide the radius for dedicated servers](03-dedicated-server-radius.md) | Design | Medium | Open |
| 04 | [Define how the anchor planner samples unanchored climate](04-anchor-planner-climate-cycle.md) | Design | Medium | Open |
| 05 | [Fix the stale router field in the fabric-worldgen-code skill](05-fix-worldgen-skill-router-field.md) | Skill | Medium | Open |
| 06 | [Estimate the cost of conditional structure placement, or simplify it](06-structure-placement-cost.md) | Design | Medium | Open |
| 07 | [Delete the template example mixins](07-delete-example-mixins.md) | Code | Medium | Done |
| 08 | [Pin Loom to a release version](08-pin-loom-version.md) | Build | Medium | Done |
| 09 | [Use a Java toolchain](09-java-toolchain.md) | Build | Low | Done |
| 10 | [Cache Gradle in CI and stop double runs](10-ci-cache-and-triggers.md) | CI | Low | Open |
| 11 | [Write a real README](11-write-readme.md) | Docs | Medium | Open |
| 12 | [Fix the design doc's embed and file name](12-design-doc-cleanup.md) | Docs | Low | Open |
| 13 | [Remove template leftovers](13-remove-template-leftovers.md) | Readability | Low | Done |
| 14 | [Set a lower bound on the fabric-api dependency](14-bound-fabric-api-dependency.md) | Build | Low | Open |

Suggested order:

1. Tickets 07, 08, 09, 13 and 14, together in one cleanup PR.
2. Tickets 10 and 11.
3. The design tickets 01–04 and 06. Record each outcome in the design doc's decision log, which also covers part
   of ticket 12.
4. Ticket 05, once ticket 01 is decided.
