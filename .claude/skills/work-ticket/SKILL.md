---
name: work-ticket
description: Work a ticket end-to-end — read it, plan the implementation, build it, tick each acceptance criterion as it verifiably passes, then mark the ticket done in PLAN.md. Use whenever someone says "work on a ticket", "start ticket EDGE-1", "implement this ticket", "pick up ANC-2", or points you at a ticket file to build.
---

# Work a ticket to done

Drive one ticket from "open" to "done". The ticket is the source of truth: its **Acceptance
criteria** are the contract, and once every criterion passes its row in the `PLAN.md` ticket
table is marked `done`.

Tickets live in `tickets/<PREFIX>-<N>.md`; `PLAN.md` is the index (table with priority, area,
status) plus the suggested `## Order`. The design the mod implements is in
`docs/Finite World — Minecraft worldgen mod design.md` (the **design doc**; its decision log is
the record of settled choices). **Discover the current layout at runtime; don't hardcode a
ticket list.** No `PLAN.md` yet → there are no tickets; offer the `story-ticket` skill.

On Windows use `gradlew.bat` in place of `./gradlew`. JDK 25 must be on `JAVA_HOME`.

## Running mode — in-session or delegated

Work the ticket inline for a single ticket, or when the user wants to review the plan and steer.
When the user wants **several tickets in a row**, hand each to a `general-purpose` subagent with
"Run the work-ticket skill on <ID> (tickets/<ID>.md). The user pre-approved the plan. Leave
in-game checks open." It reports back a short summary.

- **One subagent at a time. Never two in parallel**: they share one working tree and one Gradle
  daemon, so two runs would interleave edits, mix up `git diff`, and race the build.
- The agent leaves every "Verified in game: …" criterion open; **you** run those when it returns
  (ask the user first), then tick them with the evidence.
- Relay the agent's report to the user; its output isn't shown to them. Summarize per ticket:
  status, what changed, unticked criteria and why, build result.
- The agent never commits. Committing stays with you, and only when the user asks.

## Step 1 — Locate the ticket

- **If the user named a ticket** (ID like `EDGE-1` or a path), open `tickets/<ID>.md`.
- **If no ticket was named**, read the `PLAN.md` table for rows that are not `done`, follow the
  `## Order` list and priorities, check each candidate's Notes for an unmet dependency, and ask
  via `AskUserQuestion` which one to work. Present the real open tickets you found.

## Step 2 — Read and orient

Read the **whole** ticket: user story, description, as-is (follow its file links to the real
code), to-be, acceptance criteria, notes. Read the design doc sections it cites.

Load the repo skills that fit the work before planning: `fabric-worldgen-code` (density
functions, noise router, world preset, customise screen), `minecraft-world-generation` (JSON
worldgen data), `minecraft-modding` (general Fabric), `minecraft-testing` / `java-junit` (tests),
`gradle-best-practices` (build changes).

**Older tickets may have no `## Acceptance criteria` section.** Then derive checkbox criteria
from the ticket text, add them to the ticket as `## Acceptance criteria`, and include them in
the plan for approval. Add `- [ ] ./gradlew build passes.` and, for worldgen or UI work, a
`Verified in game: …` criterion.

**Restate the ticket's intent in one or two sentences** so the user can confirm you understood
it.

## Step 3 — Plan, then pause for approval

Produce a concrete plan **mapped to the acceptance criteria**: which files change under
`src/main/` (common, worldgen data in `src/main/resources/data/`) and `src/client/` (Create
World screen), which tests get added (name the cases), what the in-game check will look at.
Respect the design doc's hard rules:

- Normal (non-Finite) worlds are unaffected; everything hangs off the `finite` world preset and
  its own copy of the Overworld noise settings.
- Vanilla does terrain, biome selection and structures. The mod only adds the edge and anchor
  density functions (registered in code) and leaves the multi-noise biome source alone unless
  testing shows gaps.
- Anchors are deterministic from seed + radius and never saved separately.
- The radius and band settings travel in the density function codecs (saved with the world's
  generator settings); the radius can't change after creation and the minimum (~3,000) is
  enforced by the field.
- The checklist is read from vanilla registries at runtime, not hardcoded.
- Non-goals stay out: modded content, Nether/End, converting existing worlds, debug tools.
- Client-only classes stay in `src/client/`; mixins are a last resort over Fabric API hooks.
- A change to a design decision updates the design doc in the same change.
- Versions live in `gradle.properties`. No new dependency for a few lines of code.

Track the steps with a todo list. **Present the plan and wait for the user's go-ahead before
editing any code.**

## Step 4 — Implement

Work the plan criterion by criterion. Keep changes scoped to this ticket; if you spot
out-of-scope work, note it (or suggest a new ticket via the `story-ticket` skill) rather than
doing it.

**If the implementation diverges from an acceptance criterion, record the divergence at the
moment you pivot**: amend the criterion in the same change, striking through the superseded
wording or adding a dated note. A ticket whose boxes describe code that doesn't exist is worse
than one with open boxes.

## Step 5 — Verify, then tick each criterion

A criterion gets ticked **only when it verifiably passes**, never on hope.

1. Run `./gradlew build` (compiles, runs `test`, builds the jar) from the repo root. Fix
   failures before ticking anything.
2. For any criterion phrased **"Verified in game: …"**, ask the user first, then run
   `./gradlew runClient` in the background, create a Finite world with the ticket's seed and
   radius, and reproduce the scenario (`/locate biome …`, `/locate structure …`, `/tp` to the
   edge, F3 for coordinates and biome). Capture proof: a screenshot (`run/screenshots/`) or the
   command output from `run/logs/latest.log`. If you can't drive the game window, hand the exact
   steps to the user and wait for their result.
3. For each satisfied criterion, edit the **ticket file**: `- [ ]` → `- [x]` **and append the
   evidence in parentheses** (the file, test name, or observed behaviour). A tick with no
   evidence is not a tick.
4. Do **not** tick a criterion that fails or that you couldn't verify. If one was deliberately
   skipped (the user waived the in-game check, say), leave it `- [ ]` and append **why**.
5. **Final AC↔diff pass.** Re-read every `- [x]` against the working tree (`git diff`, the
   file, the passing test). Any box whose evidence you can't point at right now gets unticked.

## Step 6 — Mark the ticket done (only when ALL criteria are `[x]`)

In the `PLAN.md` ticket table, find the row whose link matches this ticket ID (**match on the
link, not the title**) and set its `Status` to `done`. If the ticket settled or changed a design
decision, check the design doc says so; if it changed setup, commands or requirements, update
`README.md`.

## Step 7 — Report and stop

Summarize what changed, link the ticket and `PLAN.md`, and show the verification proof (build
output + any in-game evidence). **Leave the working tree for the user to review; do not commit**
unless they ask.
