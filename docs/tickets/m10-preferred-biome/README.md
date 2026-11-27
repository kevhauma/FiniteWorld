# Milestone 10 — Preferred biome

Pick a favourite biome and the world leans toward it. Every other biome is still guaranteed to appear at least
once. There are two ways this could work:

- **Start there:** you spawn in your chosen biome, in the middle of the island.
- **See more of it:** your chosen biome shows up more often across the whole island.
- **Still undecided:** one of these or both, and whether you can pick more than one biome (see
  [Open decisions](#open-decisions)).

Goal: a Finite world can start in a chosen biome, have more of a chosen biome, or both; the whole guarantee holds
(no land past the radius, every biome, every structure, spawn on land), and worlds without a choice load and
generate exactly as before.

## How it fits the code

Both options build on what the anchor planner already does.

- **Start there** is one extra anchor with a fixed spot: the chosen biome's anchor goes to the centre of the island
  first, and the other anchors are planned around it with the usual spacing. It counts as that biome's guarantee
  anchor, so the biome doesn't get a second one. Spawn already stays within about 300 blocks of the centre
  (SPAWN-1), so the home anchor has to cover that area; its reach is larger than a normal anchor's 192 blocks.
- **See more of it** is a lean: a small constant added to the climate across the whole island, toward the chosen
  biome's climate, the way decision 19 already adds 0.1 to temperature. The planner reads the leaned climate, so the
  anchors for every other biome are still placed and still pull their spot into range.
- Both choices are saved with the world, next to the radius, in the `finiteworld:anchor_plan` node and rewritten by
  the same rule as the radius (`FiniteRadius.withRadius`). A node without them means "no choice", so existing
  worlds are unchanged. The plan stays deterministic from seed + radius + choices.

## Which biomes can be picked

| Biome group | Start there | See more of it | Why |
| --- | --- | --- | --- |
| Surface land biomes (plains, forests, taiga, desert, savanna, jungle, badlands, snowy, cherry grove, …) | Yes | Yes, if the lean helps it (TUNE-3) | The normal case. |
| Peaks and slopes (jagged peaks, frozen peaks, grove, …) | Yes | Probably not | You spawn on a mountain, which is fine. They are picked by erosion and continentalness, which also shape the terrain, so a lean would change the land, not just the biome. |
| Mushroom fields | No (later idea) | No | Only forms at ocean-level continentalness; spawn needs inland continentalness (−0.11 and up). A mushroom island in the middle, ringed by its own sea, is a fun later option. |
| Beaches, stony shore, rivers | No | No | Coast lines and thin strips; they can't sit inland at the centre, and a lean can't make more coast. |
| Cave biomes (deep dark, lush caves, dripstone, sulfur caves) | No | No | Underground; spawn is on the surface. A lean on erosion would flatten or raise the whole island. |

TUNE-3 turns the "See more of it" column into a fixed list from data: a biome is pickable only if the lean clearly
raises its share of land.

## Open decisions

| Question | Recommendation | Why |
| --- | --- | --- |
| One option or both? | Both, as two independent settings: **Start in** (a biome, or anywhere) and **More of** (a biome, or none). They can name the same biome or different ones. | They are separate pieces of code (a fixed anchor, a climate lean) that don't interfere, and each is useful alone: a jungle start with a normal mix, or a world full of cherry groves where you spawn anywhere. |
| More than one biome? | Start in: exactly one (there is one spawn). More of: one in this milestone. | Two leans can cancel (desert pulls hot and dry, snowy plains cold) or push the climate past what the anchors can correct. Later, allow a second biome when it shares a climate side with the first (jungle and bamboo jungle, the snowy biomes). |
| How does "more of" work? | A climate lean on temperature and humidity only. | Looks natural: the island gets warmer or wetter as a whole, so the extra biome comes in vanilla-shaped regions with matching neighbours. Extra anchors (several 192-block discs of the biome) would look like polka dots, and leaning erosion or continentalness changes the terrain and the edge. |
| How strong is the lean? | One strength, set by TEST-1 data (TUNE-3): roughly double the biome's share of land on a typical seed. A strength slider is later. | One value to test and explain. The guarantee has to hold at the strongest lean anyone can pick, so a slider means testing a range instead of one point. |
| How big is the home area? | A home anchor with a reach of about 384 blocks, and the spawn search kept inside its core. Starting guess, TUNE-3 confirms. | Spawn picks within about 300 blocks of the centre; a 192 reach would leave spawn in the neighbouring biome half the time. |

## Heads-up

- **Neighbours change too.** A desert lean also brings more savanna and badlands and fewer snowy biomes; that's the
  point of it looking natural, but the "more of" description in the UI should say so.
- **Out-of-place anchors get likelier.** With a strong hot lean, the ice spikes anchor has to pull harder, so it is
  more likely to read as an odd patch (design doc risk "Out-of-place anchors"). TUNE-3 picks the strength with this in
  mind.
- **Island shape (milestone 9):** every planned shape has its centre at the origin, so "start there" works unchanged.
  The later donut and crescent shapes need the home anchor to move with the spawn.

## Tickets

| Ticket | Area | Title | Status |
| --- | --- | --- | --- |
| [SPAWN-2](SPAWN-2.md) | Spawn | Start in the chosen biome at the centre of the island | open |
| [ANC-5](ANC-5.md) | Anchors | Lean the island's climate toward the chosen biome | open |
| [UI-3](UI-3.md) | Radius UI | Biome choices on the Create World screen and the server config | open |
| [TUNE-3](TUNE-3.md) | Tuning | Settle the lean strength, the home anchor size and the pickable biomes | open |

Order: SPAWN-2, ANC-5, UI-3, TUNE-3. SPAWN-2 and ANC-5 are independent; UI-3 needs both, and uses the starting
guesses until TUNE-3 lands. ANC-5 touches the same planner code as ANC-4 (milestone 9); land whichever comes first and
rebase the other.
