# 13 — Remove template leftovers

Milestone: M7 · Area: Readability · Severity: Low · Review finding 13

## Task

- `FiniteWorld.java:13-24`: remove the comments that explain the logger and the initializer. Remove
  `LOGGER.info("Hello Fabric world!")`, or change it to log something useful.
- `FiniteWorldClient.java:8`: remove the template comment, but keep the entrypoint.
- `build.gradle:6-12`: delete the empty `repositories` block.
- `build.gradle:66-81` and `build.gradle:3`: delete the `publishing` block and the `maven-publish` plugin. Nothing
  publishes today; add them back if that changes.
- `build.gradle:48-50`: remove the comment on `withSourcesJar()`.
- Keep `FiniteWorld.id()`, because registering the density functions will need it.

## Acceptance criteria

- [ ] `./gradlew build` passes.
- [ ] No template comments are left in `src/` or `build.gradle`.
