# Finite World

A Fabric mod that adds a **Finite** world type: one landmass of a chosen radius inside an endless vanilla ocean, with every vanilla Overworld land biome and structure guaranteed to appear on it at least once.

The land is vanilla terrain at vanilla scale, different for every seed. The mod only changes two things on top of it:

- **The edge.** Past the radius, land is pushed below sea level. The coastline follows the terrain: beaches and bays on lowland, sea cliffs where mountains meet the edge. The ocean, caves and underground structures continue without limit.
- **Biome anchors.** A few small, seeded nudges to vanilla's climate noise make sure no biome or structure on the checklist is missing inside the radius.

Pick **Finite** on the Create World screen and set the radius (default 4,000 blocks, minimum about 3,000). The radius is fixed once the world is created. Normal worlds are not affected.

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
./gradlew build       # build the mod jar into build/libs/
./gradlew runClient   # start Minecraft with the mod
./gradlew runServer   # start a dedicated server with the mod
```

On Windows use `gradlew.bat` in place of `./gradlew`.

| Path | Contents |
|---|---|
| `src/main/` | Common and server code, worldgen data |
| `src/client/` | Client-only code (Create World screen) |
| `docs/` | Design documents |
| `.claude/skills/` | Agent skills for Fabric modding, worldgen, Gradle and JUnit |

Versions live in `gradle.properties`; check current ones at <https://fabricmc.net/develop>. Fabric's developer docs are at <https://docs.fabricmc.net/develop/>.

## License

CC0-1.0, see [LICENSE](LICENSE).
