# Finite World

A Fabric mod that adds a **Finite** world type: one landmass of a chosen radius inside an endless vanilla ocean, with every vanilla Overworld land biome and structure guaranteed to appear on it at least once.

The land is vanilla terrain at vanilla scale, different for every seed. The mod only changes two things on top of it:

- **The edge.** Past the radius, land is pushed below sea level. The coastline follows the terrain: beaches and bays on lowland, sea cliffs where mountains meet the edge. The ocean, caves and underground structures continue without limit.
- **Biome anchors.** A few small, seeded nudges to vanilla's climate noise make sure no biome or structure on the checklist is missing inside the radius.

Pick **Finite** on the Create World screen and set the radius with **Customize** (default 4,000 blocks, from 3,000 to 20,000). The radius is fixed once the world is created. Normal worlds are not affected.

On a dedicated server, set `level-type=finiteworld\:finite` and `generator-settings={"radius"\:5000}` in `server.properties` before the world is first created.

> **Status:** early development. The project skeleton builds; the world type is not implemented yet.

See the full design in [docs/](docs/Finite%20World%20%E2%80%94%20Minecraft%20worldgen%20mod%20design.md).

## Requirements

| | Version |
|---|---|
| Minecraft | 26.3 |
| Fabric Loader | 0.20.1 or newer |
| Fabric API | required |
| Java | 25 |

## Development

Requires JDK 25 (`JAVA_HOME` pointing to it). Use the Gradle wrapper; no global Gradle install is needed.

```sh
./gradlew build       # build the mod jar into build/libs/ (runs unit and game tests)
./gradlew runGameTest # only the game tests
./gradlew runClient   # start Minecraft with the mod
./gradlew runServer   # start a dedicated server with the mod
```

On Windows use `gradlew.bat` in place of `./gradlew`.

`./gradlew build` also runs the guarantee check (`src/gametest/`, a Fabric server game test, about 35 seconds). For 20 fixed seeds at radius 3,000, 4,000 and 5,000 it asserts that no land rises above sea level past the radius, and it logs which checklist biomes it found inside the radius (lines starting with `[TEST-1]`).

| Path | Contents |
|---|---|
| `src/main/` | Common and server code, worldgen data |
| `src/client/` | Client-only code (Create World screen) |
| `src/gametest/` | Fabric game tests (guarantee check across seeds) |
| `docs/` | Design documents |
| `.claude/skills/` | Agent skills for Fabric modding, worldgen, Gradle and JUnit |

Versions live in `gradle.properties`; check current ones at <https://fabricmc.net/develop>. Fabric's developer docs are at <https://docs.fabricmc.net/develop/>.

## Test server

A standalone server for pre-generating whole Finite worlds and viewing them in a browser, outside the repo; see the [test server plan](docs/TEST-SERVER-PLAN.md). `mods/` holds the built `finiteworld-1.0.0.jar`, Fabric API, [Chunky](https://modrinth.com/plugin/chunky) and [BlueMap](https://modrinth.com/plugin/bluemap) (versions in [SRV-1](docs/tickets/SRV-1.md)). None of them is a dependency of the mod.

```properties
level-name=r3000
level-seed=12345
level-type=finiteworld\:finite
generator-settings={"radius"\:3000}
```

Set `accept-download: true` in `config/bluemap/core.conf`; the map is then at <http://localhost:8100>.

## License

CC0-1.0, see [LICENSE](LICENSE).
