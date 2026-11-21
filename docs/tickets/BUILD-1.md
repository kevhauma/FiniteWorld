# BUILD-1 · Strip template examples, add JUnit and the Java toolchain

**Priority:** P1 · **Area:** Build/CI · **Type:** Refactor

## User story

As a developer, I want a clean skeleton with a working test setup, so every later ticket can add code and tests without template noise.

## Description

Remove the Fabric template's example code and set up JUnit 5, so the pure-Java math in EDGE-1 and ANC-2 can be unit tested.

## Current situation (as-is)

- The template example mixins are still present: [ExampleMixin](../../src/main/java/io/github/kevhauma/finiteworld/mixin/ExampleMixin.java) and [ExampleClientMixin](../../src/client/java/io/github/kevhauma/finiteworld/client/mixin/ExampleClientMixin.java), listed in [finiteworld.mixins.json](../../src/main/resources/finiteworld.mixins.json) and [finiteworld.client.mixins.json](../../src/client/resources/finiteworld.client.mixins.json).
- [FiniteWorld](../../src/main/java/io/github/kevhauma/finiteworld/FiniteWorld.java) logs "Hello Fabric world!".
- [build.gradle](../../build.gradle) has no test dependencies and no Java toolchain, so the Gradle JVM is whatever `JAVA_HOME` points at.

## Desired result (to-be)

- Example mixins and their config entries are gone. The mixin config files stay with empty lists, because UI-1 needs a client mixin.
- JUnit 5 is a test dependency and `test` uses the JUnit Platform.
- `java { toolchain { languageVersion = JavaLanguageVersion.of(25) } }` pins JDK 25.

## Acceptance criteria

- [x] No `Example*` classes or mixin entries are left. (ExampleMixin and ExampleClientMixin deleted; both mixin configs now have empty lists; no `Example` match under `src/`; built jar contains only FiniteWorld and FiniteWorldClient classes)
- [x] One trivial test runs under `./gradlew test` (replace it with real tests in EDGE-1). (`src/test/java/io/github/kevhauma/finiteworld/SmokeTest.java`, 1 test, 0 failures in `build/test-results/test`)
- [x] `./gradlew build` passes. (`gradlew.bat build`: BUILD SUCCESSFUL, JUnit 5.14.2 via BOM, toolchain pinned to 25)
- [x] `./gradlew runClient` reaches the title screen with the mod loaded. (run/logs/latest.log: 52 mods loaded incl. finiteworld, "Finite World initialized", resource reload finished and atlases created; only error is the expected dev-account 401)

## Notes

- Versions go in `gradle.properties`. Check the gradle-best-practices skill for the test setup.
