# 07 — Delete the template example mixins

Milestone: M7 · Area: Code · Severity: Medium · Review finding 7

## Problem

`ExampleMixin` (injecting into `MinecraftServer.loadLevel`) and `ExampleClientMixin` (injecting into
`Minecraft.run`) inject empty methods. Both mixin configs set `"required": true` with `defaultRequire: 1`. If a
future Minecraft version renames either method, the mod crashes at launch, and these mixins do nothing useful.

## Task

- Delete `src/main/java/io/github/kevhauma/finiteworld/mixin/ExampleMixin.java`.
- Delete `src/client/java/io/github/kevhauma/finiteworld/client/mixin/ExampleClientMixin.java`.
- Empty the `mixins` array in `finiteworld.mixins.json` and the `client` array in `finiteworld.client.mixins.json`.
  Keep both config files, because the radius UI will need a client mixin.

## Acceptance criteria

- [ ] `./gradlew build` passes.
- [ ] `./gradlew runClient` reaches the title screen with no mixin errors in the log.
