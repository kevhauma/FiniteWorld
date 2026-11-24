# 09 — Use a Java toolchain

Milestone: M7 · Area: Build · Severity: Low · Review finding 9

## Problem

`build.gradle:43-55` sets both `options.release = 25` and `sourceCompatibility`/`targetCompatibility = 25`. The two
settings duplicate each other, and neither picks the JDK. The build only works because `JAVA_HOME` points at JDK
25. The repo's own skill (`fabric-worldgen-code/SKILL.md:97-99`) recommends a toolchain.

## Task

Replace both blocks with:

```groovy
java {
	toolchain { languageVersion = JavaLanguageVersion.of(25) }
	withSourcesJar()
}
```

## Acceptance criteria

- [ ] `./gradlew build` passes with `JAVA_HOME` pointing at a JDK other than 25, because the toolchain picks 25.
- [ ] CI still passes.
