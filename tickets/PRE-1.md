# PRE-1 · Finite world preset that generates vanilla terrain

**Priority:** P1 · **Area:** World preset · **Type:** Feature

## User story

As a player, I want a **Finite** world type on the Create World screen, so I can start a world that uses the mod's generation without touching normal worlds.

## Description

Add the `finiteworld:finite` world preset and the mod's own copy of the Overworld noise settings. At this point it generates exactly like vanilla. The point is to have the hook later tickets plug into.

## Current situation (as-is)

- No worldgen data exists under `src/main/resources/data/`.
- Design doc: [Technical approach → World preset](../docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md), Player experience.

## Desired result (to-be)

- `data/finiteworld/worldgen/world_preset/finite.json`: Overworld uses `finiteworld:finite` noise settings with the vanilla multi-noise Overworld biome source. The Nether and End are copied from vanilla unchanged.
- `data/finiteworld/worldgen/noise_settings/finite.json`: a copy of the 26.3 `overworld.json`.
- **The Overworld density function subgraph is copied into `data/finiteworld/worldgen/density_function/overworld/`** (`continents`, `erosion`, `ridges`, `offset`, `factor`, `jaggedness`, `depth`, `sloped_cheese`, …), and the noise settings reference the copies. The terrain (`final_density`, via `offset`/`factor`/`jaggedness`) reads `minecraft:overworld/continents` and the others directly rather than through the router, so wrapping only the router entries would change biomes but not terrain. With the copy, EDGE-1 and ANC-3 each swap one node and both terrain and biomes follow.
- The preset is added to `data/minecraft/tags/worldgen/world_preset/normal.json` (`"replace": false`) and has a lang entry, so it shows as "Finite".

## Acceptance criteria

- [x] "Finite" can be picked on the Create World screen. (runClient → Singleplayer → World tab: the button cycles to "World Type: Finite". Data is in place: `data/minecraft/tags/worldgen/world_preset/normal.json` appends `finiteworld:finite` with `"replace": false`, `assets/finiteworld/lang/en_us.json` maps `generator.finiteworld.finite` to "Finite".)
- [x] Every node that reads continentalness, erosion or ridges goes through the `finiteworld:` copies (grep the data for `minecraft:overworld/continents|erosion|ridges`: no hits outside the copies' own leaves). (`grep -rE 'minecraft:overworld/(continents|erosion|ridges)' src/main/resources/data` gives no hits. The 12 copied nodes are continents, erosion, ridges, ridges_folded, offset, factor, jaggedness, depth, sloped_cheese, final_density, preliminary_surface_level, chunk_surface_level; the leaf copies hold the vanilla noise directly. Each copy and `noise_settings/finite.json` diffs clean against the 26.3 client jar once the namespace is swapped back. The remaining `minecraft:overworld/` references (caves, base_3d_noise, temperature, vegetation) don't read C/E/R.)
- [x] Verified in game: the same seed in Default and Finite gives identical terrain and biomes at a few named coordinates (F3). (Dedicated server, seed 12345, `level-type` minecraft:normal vs finiteworld:finite; `world_gen_settings.dat` of the Finite world names `finiteworld:finite`. Console output identical: nearest mushroom_fields [4000,145,2272], cherry_grove [-416,145,768], badlands [-2688,145,1824], village_plains [-144,~,-832], plains from (0,70,0) [-288,70,320]; world_surface Y at (0,0)=63, (1500,-2200)=63, (-3100,900)=73, (4800,4800)=63. No ERROR lines in either log.)
- [x] Default, Large Biomes, Amplified and Superflat worlds still generate as before. (Default generated on the dedicated server with no errors; the picker still lists all vanilla types. No vanilla file is replaced; the only `minecraft` data is the tag append above.)
- [x] `./gradlew build` passes. (`gradlew.bat build`: BUILD SUCCESSFUL; the repo's `validate-worldgen-json.sh --feature-registry feature` passes with 0 warnings.)

## Notes

- Take the JSON from the 26.3 server jar's `--reports` output or the client jar's `data/minecraft/worldgen/**`. Do not write it by hand.
- Amplified and Large Biomes variants of Finite are out of scope.
