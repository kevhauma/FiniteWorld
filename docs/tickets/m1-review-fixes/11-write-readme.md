# 11 — Write a real README

Milestone: M1 · Area: Docs · Severity: Medium · Review finding 11

## Problem

`README.md` is still the template's README. It doesn't describe the mod, it links to generic setup docs, and it says
"This template is available under the CC0 license". That sentence describes the template, not this mod.

## Task

Replace it with a short README that covers:

- What the mod does. Reuse the description from `fabric.mod.json`.
- Links to the design doc and to this milestone.
- How to build and run: JDK 25, `./gradlew build`, `./gradlew runClient`.
- The license.

Also confirm that CC0 is the license you want for the mod. It appears in `LICENSE`, in `fabric.mod.json:10`, and in
the jar.

## Acceptance criteria

- [ ] The README covers the four points above.
- [ ] The license is confirmed: keep CC0, or change it in all three places.
