# Plan

Tickets live in [tickets/](tickets/). The design is in [docs/](docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

**Priorities:** P0 code contradicts the design doc (land past the radius, a checklist item
missing, normal worlds affected, crash) · P1 needed for a first playable Finite world · P2 polish
and tuning · P3 later options.

| Ticket | Priority | Area | Title | Status |
| --- | --- | --- | --- | --- |
| [BUILD-1](tickets/BUILD-1.md) | P1 | Build/CI | Strip template examples, add JUnit and the Java toolchain | open |
| [PRE-1](tickets/PRE-1.md) | P1 | World preset | Finite world preset that generates vanilla terrain | open |
| [EDGE-1](tickets/EDGE-1.md) | P1 | Edge | Edge density function: no land past the radius | open |
| [EDGE-2](tickets/EDGE-2.md) | P2 | Edge | Natural coastline: wandering outline and adaptive band width | open |
| [TEST-1](tickets/TEST-1.md) | P1 | Verification | Automated guarantee check across seeds | open |
| [SPAWN-1](tickets/SPAWN-1.md) | P1 | Spawn | Spawn on land near the centre | open |
| [ANC-1](tickets/ANC-1.md) | P1 | Anchors | Spike: plan anchors once per world from the seeded climate | open |
| [ANC-2](tickets/ANC-2.md) | P1 | Anchors | Anchor planner | open |
| [ANC-3](tickets/ANC-3.md) | P1 | Anchors | Anchor density function: steer climate toward each anchor | open |
| [UI-1](tickets/UI-1.md) | P1 | Radius UI | Radius field on the Create World screen | open |
| [STR-1](tickets/STR-1.md) | P1 | Structures | Measure which required structures vanilla misses | open |
| [STR-2](tickets/STR-2.md) | P1 | Structures | Fallback placement for missed structures | open |
| [TUNE-1](tickets/TUNE-1.md) | P2 | Tuning | Settle the open values: minimum radius, band width, anchor spacing | open |

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
13. TUNE-1
