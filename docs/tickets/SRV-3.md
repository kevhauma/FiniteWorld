# SRV-3 · Browser map of the generated worlds

**Priority:** P2 · **Area:** Test server · **Type:** Feature

## User story

As a developer, I want each generated test world on one web map, so I can look at it in a browser without starting the game.

## Description

Configure BlueMap with one map per generated world folder and render it fully, served from BlueMap's built-in web server. This ticket covers `r3000`; each optional world from SRV-5 adds one map the same way.

## Current situation (as-is)

- SRV-1 installs BlueMap with its default maps for the loaded world only. SRV-2 produces `r3000`.

## Desired result (to-be)

- One BlueMap map config per world: `world: "rN"`, `dimension: "minecraft:overworld"`, name `Radius N`; the default Nether and End maps removed.
- Each map fully rendered, viewable at `http://<server>:8100` whichever world the server has loaded.

## Acceptance criteria

- [ ] A map config for `r3000` exists, and the default maps are removed.
- [ ] The web app shows `Radius 3000`, fully rendered (`/bluemap force-update`, or the BlueMap CLI on the world folder): panning to radius 3,512 in every direction shows no unrendered tiles inside the generated circle.
- [ ] Render time and map storage size for `r3000` are recorded here, so SRV-5 can count the map in its disk budget.
- [ ] How to add a world's map (config lines and render command) is written in this ticket's Notes, for SRV-5.

## Notes

- Needs SRV-2.
- With more than one world, the server loads only one at a time. BlueMap reads region files itself, so unloaded world folders should render; if the mod build cannot, use the BlueMap CLI. Prove this in SRV-5, not here.
