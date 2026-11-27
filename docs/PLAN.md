# Plan

Tickets live in [tickets/](tickets/), one folder per milestone (`m<N>-<name>/`, see [Order](#order)). The design is in [the design doc](design.md).

**Priorities:** P0 code contradicts the design doc (land past the radius, a checklist item
missing, normal worlds affected, crash) · P1 needed for a first playable Finite world · P2 polish
and tuning · P3 later options.

The test server (pre-generated worlds on a browser map) has its own plan: [TEST-SERVER-PLAN.md](TEST-SERVER-PLAN.md).

Fixes from the codebase reviews are tracked per review: [2026-11-22](tickets/m7-review-fixes/README.md) and [2026-11-24](tickets/m8-review-fixes/README.md).

| Ticket | Priority | Area | Title | Status |
| --- | --- | --- | --- | --- |
| [BUILD-1](tickets/m1-finite-ocean/BUILD-1.md) | P1 | Build/CI | Strip template examples, add JUnit and the Java toolchain | done |
| [PRE-1](tickets/m1-finite-ocean/PRE-1.md) | P1 | World preset | Finite world preset that generates vanilla terrain | done |
| [EDGE-1](tickets/m1-finite-ocean/EDGE-1.md) | P1 | Edge | Edge density function: no land past the radius | done |
| [EDGE-2](tickets/m5-polish/EDGE-2.md) | P2 | Edge | Natural coastline: wandering outline and adaptive band width | open |
| [EDGE-3](tickets/m5-polish/EDGE-3.md) | P2 | Edge | Coast traces the circle along the west of seed 12345 at radius 3000 | open |
| [EDGE-4](tickets/m9-island-shape/EDGE-4.md) | P3 | Edge | Island shape in the generator settings: circle, rectangle | open |
| [ISL-1](tickets/m11-resource-islands/ISL-1.md) | P3 | Islands | Raise small islands past the radius | open |
| [ISL-2](tickets/m11-resource-islands/ISL-2.md) | P3 | Islands | Build each island from one material | open |
| [TEST-1](tickets/m1-finite-ocean/TEST-1.md) | P1 | Verification | Automated guarantee check across seeds | done |
| [SPAWN-1](tickets/m1-finite-ocean/SPAWN-1.md) | P1 | Spawn | Spawn on land near the centre | done |
| [SPAWN-2](tickets/m10-preferred-biome/SPAWN-2.md) | P3 | Spawn | Start in the chosen biome at the centre of the island | open |
| [ANC-1](tickets/m2-biome-guarantee/ANC-1.md) | P1 | Anchors | Spike: plan anchors once per world from the seeded climate | done |
| [ANC-2](tickets/m2-biome-guarantee/ANC-2.md) | P1 | Anchors | Anchor planner | done |
| [ANC-3](tickets/m2-biome-guarantee/ANC-3.md) | P1 | Anchors | Anchor density function: steer climate toward each anchor | open |
| [ANC-4](tickets/m9-island-shape/ANC-4.md) | P3 | Anchors | Plan anchors inside the island shape | open |
| [ANC-5](tickets/m10-preferred-biome/ANC-5.md) | P3 | Anchors | Lean the island's climate toward the chosen biome | open |
| [UI-1](tickets/m3-radius-setting/UI-1.md) | P1 | Radius UI | Radius field on the Create World screen | done |
| [UI-2](tickets/m9-island-shape/UI-2.md) | P3 | Radius UI | Shape choice on the Create World screen and the server config | open |
| [UI-3](tickets/m10-preferred-biome/UI-3.md) | P3 | Radius UI | Biome choices on the Create World screen and the server config | open |
| [UI-4](tickets/m11-resource-islands/UI-4.md) | P3 | Radius UI | Resource islands toggle on the Create World screen and the server config | open |
| [STR-1](tickets/m4-structure-guarantee/STR-1.md) | P1 | Structures | Measure which required structures vanilla misses | done |
| [STR-2](tickets/m4-structure-guarantee/STR-2.md) | P1 | Structures | Fallback placement for missed structures | done |
| [STR-3](tickets/m9-island-shape/STR-3.md) | P3 | Structures | Structure checks and fallback inside the island shape | open |
| [TUNE-1](tickets/m5-polish/TUNE-1.md) | P2 | Tuning | Settle the open values: minimum radius, band width, anchor spacing | done |
| [TUNE-2](tickets/m9-island-shape/TUNE-2.md) | P3 | Tuning | Settle the shape limits: thinnest rectangle, minimum and maximum area | open |
| [TUNE-3](tickets/m10-preferred-biome/TUNE-3.md) | P3 | Tuning | Settle the lean strength, the home anchor size and the pickable biomes | open |
| [TUNE-4](tickets/m11-resource-islands/TUNE-4.md) | P3 | Tuning | Settle the island distance, spacing, size and materials | open |

## Order

Milestone 1, a finite ocean world (land ends at a fixed 4,000 radius, no guarantee yet):

1. BUILD-1
2. PRE-1
3. EDGE-1
4. TEST-1
5. SPAWN-1

Milestone 2, the biome guarantee:

6. ANC-1
7. ANC-2
8. ANC-3

Milestone 3, the radius chosen per world:

9. UI-1

Milestone 4, the structure guarantee:

10. STR-1
11. STR-2

Milestone 5, polish:

12. EDGE-2
13. EDGE-3
14. TUNE-1

Milestone 6, the test server: [TEST-SERVER-PLAN.md](TEST-SERVER-PLAN.md).

Milestone 7, fixes from the 2026-11-22 review: [m7-review-fixes](tickets/m7-review-fixes/README.md).

Milestone 8, fixes from the 2026-11-24 review: [m8-review-fixes](tickets/m8-review-fixes/README.md).

Milestone 9, the island shape (round, square, rectangle; shape ideas and open decisions in [m9-island-shape](tickets/m9-island-shape/README.md)):

15. EDGE-4
16. ANC-4
17. STR-3
18. UI-2
19. TUNE-2

Milestone 10, the preferred biome (start in it, see more of it, or both; open decisions and pickable biomes in [m10-preferred-biome](tickets/m10-preferred-biome/README.md)):

20. SPAWN-2
21. ANC-5
22. UI-3
23. TUNE-3

Milestone 11, resource islands (small one-material islands past the radius; open decisions in [m11-resource-islands](tickets/m11-resource-islands/README.md)):

24. ISL-1
25. ISL-2
26. UI-4
27. TUNE-4
