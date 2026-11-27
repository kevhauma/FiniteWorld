# Milestone 11 — Resource islands

Small islands out in the ocean, past your main island, each made mostly of one material such as sand, clay, dirt or
gravel. They're a place to stock up on bulk blocks once those start running low at home.

- **Optional:** turn them off if you want a strict one-island world.
- **Small and low:** they won't feel like a second continent.
- **Guarantee unchanged:** every biome and structure still appears on your main island. The resource islands don't
  count toward it.
- **Still undecided:** whether they're on or off by default, whether each island is one material or a mix, and how far
  out and how often they appear (see [Open decisions](#open-decisions)).

Goal: a Finite world can have resource islands past the radius; the whole guarantee holds on the main island (every
biome, every structure, spawn on land), the only land past the radius is the islands themselves, and worlds without
the setting load and generate exactly as before.

## How it fits the code

Past the radius, two things keep land away today: `finiteworld:edge` pushes continentalness into the ocean range, and
`finiteworld:edge_cap` makes nothing at or above sea level solid. Resource islands leave both alone and add on top.

- **Layout.** A list of island spots, deterministic from the seed and radius, in a ring just past the radius. It is
  computed once per world and cached by the compile context, the way the anchor plan is (`AnchorPlanFunction`), so
  islands need no extra save data. Spots on deep ocean (continentalness below −0.455) are skipped, which is where
  ocean monuments form, so an island never buries one.
- **Shape.** A new density function, `finiteworld:resource_islands`, wraps the capped final density and returns
  `max(input, mound)`: a low, round mound at each island spot, from the sea floor up to a few blocks above sea level,
  and the input unchanged everywhere else. Because it sits outside the cap, the islands are the one exception to it.
- **Material.** The copied `material_rule` in `finiteworld:finite` gets one rule in front: inside an island, every
  block is that island's material. The rule's condition is a custom material condition registered in code; it finds
  the layout the same way `AnchorPlanFunction.planOf` finds the plan.
- **Biome.** Nothing changes: an island stands in whatever ocean biome is there, so no land structure or tree can
  form on it, and the biome checklist on the main island is untouched.
- **Saved with the world.** The node carries the radius and the on/off choice, and `FiniteRadius.withRadius` rewrites
  its radius like the other radius nodes. A world saved without the node has no islands.

## Open decisions

| Question | Recommendation | Why |
| --- | --- | --- |
| On or off by default? | On for new worlds, off for worlds saved before this milestone. The preset JSON has the node; a world without it has no islands. | Running out of sand and gravel is the problem this solves, and most players won't open Customize to find it. Old worlds must not grow islands next to chunks already generated. |
| One material or a mix per island? | One material per island, with a thin top layer that fits it (grass on dirt). Each of the four materials appears at least once per world. | "The clay island" is easy to find again and easy to explain. A mix spreads each material thin, so you sail between islands to fill one chest. |
| Which materials? | Sand, dirt, gravel and clay. Clay islands are rarer, since one clay island holds more clay than most players use. | The bulk blocks a finite island runs out of first. Red sand, mud and coarse dirt are easy additions later (one entry each). |
| How far out? | From 250 to 1,000 blocks past the radius (starting guess, TUNE-4). | Close enough for a short boat trip and inside TEST-1's edge check (1,024 past the radius); far enough that the coast never reads as continuing land. |
| How often? | About one island per 2,000 blocks of circumference, at least 800 blocks apart: about 9 at radius 3,000, 13 at 4,000, 63 at 20,000 (starting guess, TUNE-4). | Scales with the coast, so every stretch of shore has one within reach; never fewer than the four materials. |
| How big? | A top about 24 to 40 blocks across, at most 6 blocks above sea level. Below the water, the island is the same material down to the sea floor. | Small and low on the map; the underwater part is where most of the blocks are, for players who dig. |

## Heads-up

- **The design doc's first goal changes.** "No new land past the chosen radius" becomes "no land past the radius
  except the resource islands, when on". ISL-1 updates the goal, *Beyond the radius* and the decision log.
- **Ocean structures can touch an island.** A shipwreck or ocean ruin may end up beached on one. That is fine; only
  monuments are kept clear (deep ocean is skipped).
- **Frozen oceans.** Vanilla's freeze-top-layer step runs everywhere, so islands in frozen oceans get snow on top.
- **Island shape (milestone 9):** the ring sits past the outline, so it follows the shape through the same inland
  distance once EDGE-4 lands. Whichever lands second uses the other's distance.

## Tickets

| Ticket | Area | Title | Status |
| --- | --- | --- | --- |
| [ISL-1](ISL-1.md) | Islands | Raise small islands past the radius | open |
| [ISL-2](ISL-2.md) | Islands | Build each island from one material | open |
| [UI-4](UI-4.md) | Radius UI | Resource islands toggle on the Create World screen and the server config | open |
| [TUNE-4](TUNE-4.md) | Tuning | Settle the island distance, spacing, size and materials | open |

Order: ISL-1, ISL-2, UI-4, TUNE-4. ISL-2 needs ISL-1's layout; UI-4 needs ISL-1 and uses the starting guesses until
TUNE-4 lands.
