# SRV-4 · Visual check of each generated world on the map

**Priority:** P2 · **Area:** Test server · **Type:** Feature

## User story

As a developer, I want to inspect the test world on the map against the design, so the visual check the automated tests cannot do gets done.

## Description

Use SRV-3's map to check the coastline, the edge, spawn and land shape of `r3000`, the smallest and most crowded world, and write the findings where the design and EDGE-2 expect them. Each optional world from SRV-5 gets the same check.

## Current situation (as-is)

- [EDGE-2](EDGE-2.md) still has open in-game screenshots; the design doc's [Risks and open questions](../Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md) lists only the visual check as open (TUNE-1).
- [TEST-1](TEST-1.md) asserts the hard rules by sampling; nothing looks at a whole world.

## Desired result (to-be)

- A screenshot of each checked world from the map, with what was seen.
- Problems found become their own tickets in [PLAN.md](../PLAN.md).

## Acceptance criteria

- [ ] Screenshot of `r3000`'s full map (top view, whole circle) saved and linked from this ticket.
- [ ] For `r3000`: no land past the radius; the coastline wanders rather than tracing a circle (EDGE-2); spawn is on land near the centre (SPAWN-1).
- [ ] Anything that looks wrong is noted with its coordinates.
- [ ] Findings are written into EDGE-2's in-game criteria and the design doc's Open list; each problem found gets a ticket.

## Notes

- Needs SRV-3.
