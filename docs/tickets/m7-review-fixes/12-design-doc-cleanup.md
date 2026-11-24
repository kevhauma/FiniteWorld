# 12 — Fix the design doc's embed and file name

Milestone: M7 · Area: Docs · Severity: Low · Review finding 12

## Problem

- Line 90 is left over from an export: `&#91;embedded content: generation pipeline · 3 mod parts feeding vanilla\]`.
- The file name `Finite World — Minecraft worldgen mod design.md` contains spaces and an em dash. Git prints it
  octal-escaped, and every shell command has to quote it.

## Task

- Replace line 90 with a Mermaid diagram or a short list (radius + edge band + anchor planner → noise router →
  vanilla terrain, biomes and structures), or remove it.
- `git mv` the file to `docs/design.md`, and update the links to it in the review doc and this milestone's README.
- As tickets 01–04 and 06 are decided, record their outcomes in the decision log.

## Acceptance criteria

- [ ] The design doc has no broken embed.
- [ ] The file is `docs/design.md`, and no links point to the old name.
