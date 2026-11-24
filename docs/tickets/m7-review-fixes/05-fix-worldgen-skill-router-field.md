# 05 — Fix the stale router field in the fabric-worldgen-code skill

Milestone: M7 · Area: Skill · Severity: Medium · Review finding 5 · Depends on: 01

## Problem

`.claude/skills/fabric-worldgen-code/SKILL.md:43-45` tells agents to swap `final_density` and
`initial_density_without_jaggedness`. The 26.3 overworld router has no `initial_density_without_jaggedness`. Its
fields are `chunk_surface_level`, `continents`, `depth`, `erosion`, `final_density`, `ridges`, `temperature` and
`vegetation`.

The step also implies that swapping router entries is enough. Ticket 01 shows it isn't.

## Task

- Replace the field list with the 26.3 one, or drop the list and point at the vanilla file.
- Say that terrain reads the `minecraft:overworld/*` density functions by id, so wrapping only the router changes
  biomes and not terrain. Match the approach chosen in ticket 01.
- Optional: make the Fabric API javadoc example on line 112 (`0.166.3+26.4`) match the project's version.

## Acceptance criteria

- [ ] Step 2 of the skill matches the 26.3 data and the decision in ticket 01.
