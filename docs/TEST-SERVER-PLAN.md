# Test server plan

A dedicated server with a fully pre-generated Finite world at the minimum radius, viewable on a browser map. Larger worlds, up to the maximum radius, are added later only if time and disk allow. It is a developer tool outside the mod: the mod gains no code and no dependency. Tickets live in [tickets/](tickets/); the mod's own plan is [PLAN.md](PLAN.md), the design is in [the design doc](Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

**Priorities:** as in [PLAN.md](PLAN.md). The minimum world is P2: it feeds the visual check, it does not block a playable world. Larger worlds are P3.

## Shape

- **Server:** Fabric 26.3, Java 25, outside version control, running the built `finiteworld` jar, Fabric API, a pre-generator (Chunky) and BlueMap, in `D:\desktop\mc-mods\finite-test-server` ([SRV-1](tickets/SRV-1.md) has the versions).
- **Worlds:** one folder per radius, all seed 12345, so the radius is the only variable. A server holds one overworld, so each world is created on its own (`level-name=rN`, `generator-settings={"radius":N}`) and the server switches between them by restart.
- **Full world:** a circle at 0,0 out to radius + 512: all land, the edge band and a ring of open ocean.
- **Map:** one BlueMap map per world folder, all in one web app on port 8100.

## Worlds

**Required:** `r3000`, radius 3,000 (MIN): 151 k chunks, measured 1.4 GB (~9.7 KB/chunk) and ~19 min to generate (~7.5 ms/chunk) ([SRV-2](tickets/SRV-2.md#measured)).

**Optional ([SRV-5](tickets/SRV-5.md)), in this order, each only if `r3000`'s measured cost says it fits:**

| Folder | Radius | Chunks (circle, radius + 512) | Disk at ~8 KB/chunk |
| --- | --- | --- | --- |
| `r20000` | 20,000 (MAX) | 5.16 M | ~41 GB |
| `r8000` | 8,000 | 889 k | ~7.1 GB |
| `r5000` | 5,000 | 373 k | ~3.0 GB |
| `r12000` | 12,000 | 1.92 M | ~15 GB |
| `r4000` | 4,000 | 250 k | ~2.0 GB |
| `r6000` | 6,000 | 520 k | ~4.2 GB |
| `r10000` | 10,000 | 1.36 M | ~11 GB |
| `r14000` | 14,000 | 2.58 M | ~21 GB |
| `r17000` | 17,000 | 3.76 M | ~30 GB |

The order covers the range coarsely first: MAX, then the middle, then the gaps. All nine together are ~16.8 M chunks and ~134 GB plus map tiles. Estimates at ~8 KB/chunk; SRV-2 measured `r3000` at ~9.7 KB and ~7.5 ms per chunk, so scale the disk column by 1.2 (MAX: ~50 GB, ~11 h).

| Ticket | Priority | Area | Title | Status |
| --- | --- | --- | --- | --- |
| [SRV-1](tickets/SRV-1.md) | P2 | Test server | Standalone Fabric server with the mod, a pre-generator and BlueMap | done |
| [SRV-2](tickets/SRV-2.md) | P2 | Test server | Script that creates and pre-generates a world; generate the minimum world | | done |
| [SRV-3](tickets/SRV-3.md) | P2 | Test server | Browser map of the generated worlds | open |
| [SRV-4](tickets/SRV-4.md) | P2 | Test server | Visual check of each generated world on the map | open |
| [SRV-5](tickets/SRV-5.md) | P3 | Test server | Optional larger worlds up to the maximum radius, as time and disk allow | open |

## Order

1. SRV-1
2. SRV-2
3. SRV-3
4. SRV-4
5. SRV-5 (optional; each world added goes through SRV-3's map and SRV-4's check)
