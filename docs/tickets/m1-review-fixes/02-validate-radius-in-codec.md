# 02 — Validate the radius in the codec

Milestone: M1 · Area: Design · Severity: High · Review finding 2

## Problem

The design says the minimum radius (~3,000) is "enforced by the field" in the Create World UI. But the radius is
stored in the density function's codec, so three other paths can set it without the UI: datapacks, a hand-edited
`level.dat`, and server generator settings.

A radius below the minimum silently breaks the biome guarantee and the promise that the first stronghold ring is
on land.

## Task

Update the design doc: the codec enforces the limits (`Codec.intRange(MIN, MAX)` or `validate(...)`), and the UI
mirrors them. Once implemented, a bad radius fails loudly when the world loads.

## Acceptance criteria

- [ ] The design doc's *Player experience* and *Technical approach* sections say the codec decides the minimum and
      maximum.
- [ ] A maximum radius is decided too, or explicitly set to "none".
- [ ] Once the edge density function exists, a unit test checks that decoding a radius below the minimum fails.
