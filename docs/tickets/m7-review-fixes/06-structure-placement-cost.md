# 06 — Estimate the cost of conditional structure placement, or simplify it

Milestone: M7 · Area: Design · Severity: Medium · Review finding 6

## Problem

The design says: "Required structures get an extra fixed-position placement … used only when vanilla placement
would miss them." To know whether vanilla would miss a structure, every placement candidate inside the radius has
to be checked, each with its own biome check.

That is cheap for mansions (spacing 80 chunks) and costs thousands of biome samples for villages and outposts. The
*Anchor search cost* risk doesn't cover it.

## Task

Pick one and write it into the design doc:

- **Simplify (recommended):** always add the fixed placement at the anchor, and accept an occasional extra copy.
  The guarantee is already "a minimum".
- **Keep the check:** add it to the *Anchor search cost* risk, with a cost estimate and a plan to sample coarsely
  first and then refine.

## Acceptance criteria

- [ ] The design doc's *Structures* section describes the chosen approach.
- [ ] The decision log has a new row.
