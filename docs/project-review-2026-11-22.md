# Project review — 2026-11-22

Scope: the whole repository at `6bf58be` (branch `kevhauma/abalone`). It holds the Fabric 26.3 template skeleton,
the design doc, and the vendored agent skills. No gameplay or worldgen code exists yet, so most findings are about
the build, template leftovers, and gaps in the design doc that will bite once implementation starts.

Checked:

- `./gradlew build` passes locally (JDK 25.0.5, Loom 1.19.4, Gradle 9.8.2).
- Both template mixin targets exist in the 26.3 jars (`MinecraftServer.loadLevel()V`, `Minecraft.run()V`), so the
  game will not crash at startup.
- The vanilla 26.3 `noise_settings/overworld.json` and `density_function/overworld/offset.json` were read from the
  Loom cache to check the design's assumptions.

Severity: **High** means it will produce wrong behaviour if built as written. **Medium** means it should be fixed
before the next real feature. **Low** means cleanup.

## Summary

| # | Area | Severity | Finding |
| --- | --- | --- | --- |
| 1 | Correctness (design) | High | Replacing the router's `continents` entry does not move terrain |
| 2 | Correctness (design) | High | The minimum radius is only enforced by the UI |
| 3 | Correctness (design) | Medium | No answer for dedicated servers, which have no Create World screen |
| 4 | Correctness (design) | Medium | The anchor planner has a circular dependency on the climate it changes |
| 5 | Correctness (skill) | Medium | `fabric-worldgen-code` names a noise router field that 26.3 no longer has |
| 6 | Performance (design) | Medium | "Place a structure only when vanilla would miss it" has no cost estimate |
| 7 | Correctness (code) | Medium | The template example mixins are live, required, and do nothing |
| 8 | Build | Medium | Loom is pinned to a `-SNAPSHOT`, so builds are not reproducible |
| 9 | Build | Low | No Java toolchain; the build depends on whatever JDK runs Gradle |
| 10 | Performance (CI) | Low | CI has no Gradle cache and runs twice for every PR push |
| 11 | Documentation | Medium | The README is the template's README |
| 12 | Documentation | Low | The design doc has a broken embed and an awkward file name |
| 13 | Readability | Low | Template comments, "Hello Fabric world!", and unused publishing config |
| 14 | Build | Low | `fabric-api: "*"` accepts any version |

## Correctness

### 1. Replacing the router's `continents` entry does not move terrain — High

The design (Technical approach: *Edge density function*) says the edge function "lowers continentalness inside the
fade band". In 26.3, the noise router's `continents` field is used for biome selection, but terrain does not read
it. Terrain reads the density function **by id**: `minecraft:overworld/offset` (and through it `depth`, `factor`,
`sloped_cheese`, `final_density`) uses `"minecraft:overworld/continents"` directly as its spline coordinate.

If only the router entry is swapped, biomes turn into ocean biomes at the edge while the land stays above water.
The anchors have the same problem for erosion and ridges/weirdness.

Fix: the edge and anchor functions must reach every place that reads those ids. Either copy the overworld density
function graph under `finiteworld:` and point it at the wrapped inputs, or override the
`minecraft:overworld/continents`, `erosion` and `ridges` density functions themselves. Overriding the vanilla ids
also changes the normal world type, so it is only an option if those overrides become a no-op outside the Finite
preset. Copying the graph is safer. Write the chosen approach into the design doc.

### 2. The minimum radius is only enforced by the UI — High

The design says the minimum (~3,000) is "enforced by the field". The radius lives in the density function's codec,
so it can also arrive through a datapack, a hand-edited `level.dat`, or `server.properties` generator settings.
Each of these skips the field. A radius below the minimum silently breaks the biome guarantee and the stronghold
ring promise.

Fix: validate in the codec (`Codec.intRange(MIN, MAX)` or `validate(...)`), so any path that produces a bad radius
fails loudly. The UI then only mirrors the codec's limits.

### 3. No answer for dedicated servers — Medium

The radius UI is a Create World customise screen. A dedicated server creates its world from `server.properties`
(`level-type=finiteworld:finite`), so it never sees that screen. The design does not say which radius a server gets
or how an admin changes it. The default from the preset JSON will apply. That is acceptable, but it should be a
stated decision, with the `generator-settings` route (or a non-goal) written down.

### 4. The anchor planner depends on the climate it changes — Medium

The planner "searches for the spot whose vanilla climate is already closest" and then adds offsets to that climate.
The anchor density function is part of the climate graph, so the planner must sample the climate **without**
anchors. It also needs the seeded noise, which only exists once `RandomState` has been built. The design should
say where the planner runs (once per `RandomState`, from the unanchored inputs) and how the anchor function gets
the plan. The `fabric-worldgen-code` skill already points at this ("compute once per `RandomState`").

### 5. The skill names a router field that 26.3 removed — Medium

`.claude/skills/fabric-worldgen-code/SKILL.md:43-45` tells agents to swap `final_density` /
`initial_density_without_jaggedness`. The 26.3 overworld noise router has only `chunk_surface_level`, `continents`,
`depth`, `erosion`, `final_density`, `ridges`, `temperature` and `vegetation`. There is no
`initial_density_without_jaggedness`. Agents that follow the skill will look for a field that is not there.

The same step also suggests that swapping router entries is enough, which runs into finding 1. Update the step to
say that terrain reads the `minecraft:overworld/*` density functions by id. Line 112's Fabric API version example
(`0.166.3+26.4`) also differs from the project's `0.166.2+26.3`. That is harmless, but it can be confusing.

### 7. The template example mixins are live, required, and do nothing — Medium

`src/main/java/.../mixin/ExampleMixin.java:11` and `src/client/java/.../client/mixin/ExampleClientMixin.java:11`
inject empty methods into `MinecraftServer.loadLevel` and `Minecraft.run`. Both configs are `"required": true` with
`defaultRequire: 1` (`src/main/resources/finiteworld.mixins.json:8-10`). Today they do nothing at runtime. On a
future Minecraft version that renames either method, the mod crashes at launch because of code that has no
purpose.

Fix: delete both example classes and empty the `mixins` / `client` arrays. Keep the config files, since the radius
UI will need a client mixin (design: *Radius UI*).

## Performance

### 6. No cost estimate for the conditional structure placement — Medium

"Required structures get an extra fixed-position placement … used only when vanilla placement would miss them"
(design, *Structures*). To know whether vanilla would miss a structure, every placement candidate for that
structure set inside the disc has to be evaluated, including its biome check. That means about 4,000² / spacing²
candidates per structure set, and biome checks at each one. For rare sets (mansion: spacing 80 chunks) this is
cheap. For villages and outposts it is thousands of biome samples.

The *Anchor search cost* risk covers only the climate search. Add the structure check to that risk, or simplify
it: always add the fixed placement at the anchor, and accept that an extra copy sometimes appears. The design
already allows repeats ("the guarantee is only a minimum").

### 10. CI has no Gradle cache and runs twice per PR push — Low

`.github/workflows/build.yml:7` triggers on `[pull_request, push]`, so a push to a PR branch in this repository
runs the build twice. No step caches Gradle or the Loom Minecraft cache, so every run downloads and remaps
Minecraft again (about 1.5 min locally, even with a warm cache).

Fix: replace `gradle/actions/wrapper-validation` with `gradle/actions/setup-gradle@v6`, which validates the
wrapper and caches. Limit `push` to `branches: [main]`.

### Hot-path notes for the code to come

Not findings yet, but the design commits to them:

- The edge function runs per noise cell. "Irregular outline" noise and "adaptive width" both read other density
  functions. Wrap the edge function in `flat_cache`/`cache_2d`, since it depends only on x/z, and give it honest
  `minValue()`/`maxValue()`.
- Anchor offsets summed over ~50 anchors per sample would be costly. Use a coarse spatial grid lookup so each sample
  touches only the anchors near it.
- `org.gradle.jvmargs=-Xmx1G` (`gradle.properties:2`) is tight for Loom's `genSources` decompile. Raise it to 2–4G
  if decompiling fails with an out-of-memory error.

## Build

### 8. Loom is pinned to a `-SNAPSHOT` — Medium

`gradle.properties:10`: `loom_version=1.19-SNAPSHOT` resolved to 1.19.4 today and may resolve to something else
tomorrow, which changes the build without a commit. Pin it to `1.19.4`.

### 9. No Java toolchain — Low

`build.gradle:43-55` sets `options.release = 25` **and** `sourceCompatibility`/`targetCompatibility = 25`. The two
settings duplicate each other, and neither picks the JDK. The build ran on JDK 25 here only because `JAVA_HOME`
points at it. The repo's own skill recommends a toolchain (`fabric-worldgen-code/SKILL.md:97-99`). Replace both
blocks with `java { toolchain { languageVersion = JavaLanguageVersion.of(25) }; withSourcesJar() }`.

### 14. `fabric-api: "*"` — Low

`src/main/resources/fabric.mod.json:32` accepts any Fabric API, including older ones that lack APIs this mod will
call. Use `">=0.166.2"` to match `gradle.properties`.

## Documentation

### 11. The README is the template's README — Medium

`README.md` has no description of the mod. It links to generic Fabric setup docs, and it says "This template is
available under the CC0 license". That sentence describes the template, not this mod. Replace it with a few lines:
what the mod does (the `fabric.mod.json` description already says it), a link to the design doc, how to build and
run (`./gradlew build`, `./gradlew runClient`, JDK 25), and the license. Also confirm that CC0 is the license you
want for the mod itself. It currently comes from the template, but it is stated in `LICENSE`, `fabric.mod.json:10`
and the jar.

### 12. The design doc has a broken embed and an awkward file name — Low

- Line 90 is a leftover from an export: `&#91;embedded content: generation pipeline · 3 mod parts feeding vanilla\]`.
  Replace it with a Mermaid diagram or a short list (radius + edge band + anchor planner → noise router →
  vanilla terrain/biomes/structures), or remove it.
- The file name `Finite World — Minecraft worldgen mod design.md` contains spaces and an em dash. Git prints it
  octal-escaped, and it has to be quoted in every shell command. Rename it to `docs/design.md`.
- Findings 1–4 and 6 are design gaps. Record their outcomes in the doc's decision log once decided.

## Readability

### 13. Template leftovers

These cost nothing at runtime, but they are noise for every future reader:

- `FiniteWorld.java:13-24`: four comment lines that explain what a logger is and when `onInitialize` runs, plus
  `LOGGER.info("Hello Fabric world!")`. Remove the comments, and remove the log line or make it say something
  useful.
- `FiniteWorldClient.java:8`: the template comment in an empty entrypoint. The entrypoint is fine to keep, since the
  radius UI will need it.
- `build.gradle:6-12` (an empty `repositories` block with five comment lines) and `build.gradle:66-81` (a
  `maven-publish` publication with no repository, so nothing ever publishes). Delete both, and remove the
  `maven-publish` plugin (`build.gradle:3`). Add them back if the mod ever publishes to a Maven repository.
- `build.gradle:48-50`: a three-line comment on `withSourcesJar()`.
- `FiniteWorld.id()` (`FiniteWorld.java:27`) is unused for now. Keep it, since registering the density function
  types will use it.

## Not reviewed

- The 76 vendored third-party skill files under `.claude/skills/` other than `fabric-worldgen-code` (they come from
  upstream; see `.claude/skills/SOURCES.txt`).
- No `runClient` smoke test. The mixin targets were checked with `javap` instead.
- Tests: none exist, and none are needed yet. Add JUnit once the planner and falloff math exist, as the design and
  skill intend.
