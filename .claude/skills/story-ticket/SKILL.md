---
name: story-ticket
description: Create a new ticket (user story + description, as-is, to-be, detailed acceptance criteria) when the user asks for a bug, refactor, or feature. Use whenever someone says "add a user story", "write a ticket", "create a story for this bug/refactor/feature", or describes work that should be captured as a ticket.
---

# Create a new ticket

When the user asks to capture a **bug**, **refactor**, or **feature**, produce two linked pieces:

1. A detailed **ticket** file in `docs/tickets/`, carrying its own **user story** plus description,
   as-is, to-be, and detailed acceptance criteria.
2. One **row** in the ticket table of a plan file, linking to the ticket. `docs/PLAN.md` is the
   mod's plan; other `docs/*PLAN.md` files (e.g. `docs/TEST-SERVER-PLAN.md`) plan work outside
   the mod. The row goes in the plan whose areas the ticket belongs to.

**Never assume which areas or prefixes exist; they change.** Read the plan files and list `docs/tickets/`
at runtime and mirror what you find, but always create both pieces.

The design is in `docs/design.md` (the **design doc**).
Link it from tickets as
`../design.md`.

## Step 0 — Bootstrap (only if `docs/PLAN.md` or `docs/tickets/` is missing)

Create `docs/tickets/` and a `docs/PLAN.md` with this skeleton, then continue:

```markdown
# Plan

Tickets live in [tickets/](tickets/). The design is in [the design doc](design.md).

**Priorities:** P0 code contradicts the design doc (land past the radius, a checklist item
missing, normal worlds affected, crash) · P1 needed for a first playable Finite world · P2 polish
and tuning · P3 later options.

| Ticket | Priority | Area | Title | Status |
| --- | --- | --- | --- | --- |

## Order

1. <ticket id>
```

## Step 1 — Resolve the area, and with it the prefix

**This is the first and most important question. Do not guess.** Tickets are named by an
**area prefix**, and a prefix maps to exactly one area (the `Area` column of the plan
table). Derive it, don't recite it:

1. Read the ticket tables in `docs/*PLAN.md` to see the areas and prefixes in use right now.
2. Determine the story's area from what it touches. The design's parts are: world preset and
   noise settings, edge density function, anchor planner / anchor density function, structures,
   spawn, the Create World radius screen (`src/client/`), and build/CI (Gradle, `gradle.properties`).
3. **Reuse the existing prefix** for that area.
4. **If it fits no existing area** (always true for the first tickets), ask the user (via
   `AskUserQuestion`) which area it belongs to, presenting the areas you found plus the design
   part it touches. Only if they confirm it's genuinely new: mint a short prefix (2–4 uppercase
   letters, unambiguous against existing ones, e.g. `EDGE`, `ANC`, `UI`).

Then find the **highest existing number for that prefix** in `docs/tickets/` and use the next one.
Filename: `docs/tickets/<PREFIX>-<N>.md` (no zero padding).

Also confirm the **type** if ambiguous: `Bug fix`, `Refactor`, or `Feature`, and the
**priority** (definitions at the top of `docs/PLAN.md`).

## Step 2 — Open a neighbour for formatting

Open an existing ticket of the same area in `docs/tickets/` (any ticket if the area is new) to see the
header line and tone, and mirror it. Older tickets may lack the sections below; new tickets
always get all of them.

## Step 3 — Write the ticket

```markdown
# <PREFIX>-<N> · <Title>

**Priority:** <P0–P3> · **Area:** <Area> · **Type:** <Bug fix | Refactor | Feature>

## User story

As a <player / server owner / developer>, I want <capability>, so <benefit>.

## Description

<1–3 sentences: what this delivers and why, in plain language.>

## Current situation (as-is)

- <How it works today. Link real files with clickable relative paths (`../../src/main/...`). For
  bugs, pinpoint the root cause. Cite the design doc section this ticket implements.>

## Desired result (to-be)

- <The target behaviour after this ticket lands.>

## Acceptance criteria

- [ ] <Specific, testable outcome.>
- [ ] <Tests cover: … (name the cases).>
- [ ] <`./gradlew build` passes.>
- [ ] <Verified in game: … (seed, radius, what was seen).>

## Notes

- <Edge cases, design trade-offs, related tickets ("needs EDGE-1"), or scope caveats.>
```

Writing rules:

- **User story** is the classic `As a <role>, I want <capability>, so <benefit>` line.
- **As-is** must reference actual code with clickable relative links and, for bugs, name the
  exact root cause (method, condition, wrong comparison). Grep the codebase to find the real
  files; don't invent paths. If the code doesn't exist yet, say so and cite the design doc.
- **Acceptance criteria** are checkboxes, each independently verifiable. Fold in the design
  doc's hard rules where relevant:
  - Normal (non-Finite) worlds are unaffected. Anything touching shared generation code gets a
    criterion proving that.
  - Vanilla does terrain, biomes and structures; the mod only steers climate noise (edge and
    anchor density functions). No custom biome source unless testing shows gaps.
  - Anchors are deterministic from seed + radius and never saved separately. Planner logic gets
    a unit test: same seed + radius → same plan.
  - The radius lives in the generator settings (density function codec), can't change after
    creation, and the minimum (~3,000) is enforced by the field.
  - The biome checklist is read from vanilla registries at runtime, not hardcoded.
  - Non-goals stay out: modded content, Nether/End changes, converting existing worlds, new
    debug tools.
  - Client-only code goes in `src/client/`, common code in `src/main/`.
  - A change to a design decision updates the design doc (decision log or the matching section).
  - Worldgen or UI changes get an in-game check (`./gradlew runClient`, a named seed and radius).
- Match the concise, traceable style of the existing tickets; no filler.

## Step 4 — Add the ticket to its plan file

Append one row to the ticket table, after the last row of the same area:

`| [<PREFIX>-<N>](tickets/<PREFIX>-<N>.md) | <Priority> | <Area> | <Title> | open |`

**Dependencies go in the ticket's Notes, not in the table.** If the ticket changes what should
be worked next, adjust that plan's `## Order` list too.

## Step 5 — Report back

Tell the user the **area**, **priority**, and link the new ticket file and its plan file. Do not run
the build; these are docs, not code.
