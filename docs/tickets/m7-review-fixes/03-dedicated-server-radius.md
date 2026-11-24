# 03 — Decide the radius for dedicated servers

Milestone: M7 · Area: Design · Severity: Medium · Review finding 3

## Problem

The radius UI is on the Create World screen. A dedicated server creates its world from `server.properties`
(`level-type=finiteworld:finite`) and never sees that screen. The design doesn't say which radius a server gets,
or how an admin changes it.

## Task

Pick one and write it into the design doc:

- Servers get the preset's default radius, and changing it is a non-goal.
- Servers set it through `generator-settings` in `server.properties`. Document the JSON shape.

## Acceptance criteria

- [ ] The design doc states how servers behave, as a goal or a non-goal.
- [ ] The decision log has a new row.
