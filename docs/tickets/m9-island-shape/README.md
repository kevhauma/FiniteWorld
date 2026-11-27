# Milestone 9 — Island shape

Choose the outline of your land: round (like now), square or rectangle. The coast still looks natural, with
beaches, bays and cliffs, so a square island is squarish rather than ruler-straight.

- **Size:** round islands use a radius, square islands a side length, rectangles a width and a length.
- **Minimum size:** the same minimum area as today's smallest round island (radius 3,000, so π × 3,000² ≈
  28.3 million blocks²). That makes a square at least about 5,300 blocks across (√28.3 M ≈ 5,317). Very thin
  rectangles get a limit so every biome still fits (TUNE-2 measures it).
- **Maximum size:** the same maximum area as radius 20,000 (≈ 1.26 billion blocks², a square of about 35,400),
  since planning time grows with area.
- **Heads-up:** on a long, thin rectangle, some of the nearest strongholds can end up under the sea floor. You
  can still dig down to them. The checklist still needs at least one stronghold under the land (STR-3).

Goal: a Finite world can be round, square or rectangular; every shape keeps the whole guarantee (no land past
the outline, every biome, every structure, spawn on land), and existing round worlds load unchanged.

## How it fits the code

Everything that knows the island's outline asks one question today: how far is this block from the origin,
compared to the radius. A shape answers a slightly different one: **how far inland is this block** (its distance
to the outline, positive inside). For a circle that is `radius − distance`, so round worlds behave exactly as
before. A rectangle uses a rounded-box distance; the corners are rounded by at least the fade band so the inner
band edge has no sharp corner for the coast to trace. Everything else (fade band, adaptive width, wandering
outline, the cap past the outline) keeps working on that one number.

## Open decisions

| Question | Recommendation | Why |
| --- | --- | --- |
| Which shapes come first? | Circle, square, rectangle. Square is a rectangle with equal sides, so it is one shape type with a UI preset, not a second implementation. | Smallest step that still feels new; the inland-distance function makes each later shape a small addition. |
| Can you rotate them? | Not in this milestone. A rectangle already picks east–west or north–south by swapping width and length. | The coast wanders anyway, so a free angle changes little you can see. Rotating the coordinates later is a few lines and doesn't change saved worlds. |
| Thinnest rectangle? | Decide from data (TUNE-2). Starting guess: width at least 2,816. | Anchor discs need 692 blocks of depth (band 500 + anchor 192), and vanilla's first stronghold ring (1,280–2,816) keeps one stronghold within 1,408 blocks of the long axis, so a width of 2,816 keeps it under land. |
| Separate square type? | No (see first row). | One codec, one distance function. |

## Shape ideas for later

Each of these is an inland-distance function plus its own size limits. The hard rule for any shape: every part of
the land must be wide enough for anchors (more than 2 × 692 blocks), the total area must reach the minimum, and the
centre must be land or the spawn ramp moves.

| Shape | What you get | Effort | Catch |
| --- | --- | --- | --- |
| Oval (ellipse) | A stretched round island. | Low | Same thin-shape limit as the rectangle. |
| Squircle (superellipse) | One slider from round to square. | Low | Replaces circle and square with one shape; the slider is extra UI. |
| Hexagon / triangle / regular polygon | Faceted coasts, blunt capes at the corners. | Low | Corners rounded like the rectangle's. |
| Donut / atoll | A ring of land around a sheltered inland sea, with real ocean biomes and a second coastline inside. | Medium | The centre is water, so spawn has to target the ring (the spawn ramp is radial today). Ring width needs the thin-shape limit. Vanilla's first stronghold ring (1,280–2,816) can sit right under the land ring. |
| Crescent / horseshoe | A big protected bay on one side. | Medium | Circle minus an offset circle; spawn moves off-centre. |
| Figure-eight / dumbbell | Two round lobes joined by an isthmus, a natural chokepoint. | Medium | The isthmus needs the minimum width or the anchors and structures split badly. |
| Plus / star | Long peninsulas; lots of coast and capes. | Medium | Each arm is a thin rectangle with its own width limit. |
| Spiral | One arm winding out from the centre, channels of sea between the turns; a long journey across a small area, with the next turn visible across the water. | High | The spiral's distance function is approximate; the arm needs the thin-shape limit, so a spiral needs a large area; strongholds fall in the channels. |
| Archipelago | Several islands of the minimum width, sea between them. The guarantee counts over all islands together. | High | Boats needed; spawn island must be one of them; anchors spread over islands. |
| Custom mask | A black-and-white image as the outline: draw your own or trace a real country. | High | Upload or datapack UX, scaling, and a generic width check by sampling instead of per-shape rules. Covers every shape above. |

A generic width check (sample the inland distance on a grid and try packing the anchor discs) would let any of
these shapes refuse a size it can't fill, instead of each shape carrying its own rules.

## Tickets

| Ticket | Area | Title | Status |
| --- | --- | --- | --- |
| [EDGE-4](EDGE-4.md) | Edge | Island shape in the generator settings: circle, rectangle | open |
| [ANC-4](ANC-4.md) | Anchors | Plan anchors inside the island shape | open |
| [STR-3](STR-3.md) | Structures | Structure checks and fallback inside the island shape | open |
| [UI-2](UI-2.md) | Radius UI | Shape choice on the Create World screen and the server config | open |
| [TUNE-2](TUNE-2.md) | Tuning | Settle the shape limits: thinnest rectangle, minimum and maximum area | open |

Order: EDGE-4, ANC-4, STR-3, UI-2, TUNE-2. EDGE-4 touches the same edge code as EDGE-2 and EDGE-3 (milestone 5);
land those first or rebase onto them.
