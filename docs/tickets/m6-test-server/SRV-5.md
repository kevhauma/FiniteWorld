# SRV-5 · Optional larger worlds up to the maximum radius, as time and disk allow

**Priority:** P3 · **Area:** Test server · **Type:** Feature

## User story

As a developer, I want larger test worlds up to the maximum radius when I have the time and disk for them, so I can see how the mod behaves beyond the minimum size.

## Description

Optional. Using `r3000`'s measured cost (SRV-2) and the free disk, pick how many of the plan's optional worlds fit, generate them with SRV-2's script in the plan's order, and add each to the map (SRV-3) and the visual check (SRV-4). Stopping after any world is a valid end state.

## Current situation (as-is)

- Radius bounds are `FiniteRadius.MIN` = 3000 and `FiniteRadius.MAX` = 20000 ([FiniteRadius](../../../src/main/java/io/github/kevhauma/finiteworld/FiniteRadius.java), TUNE-1). Planning at 20,000 takes about 2 s.
- No world above 5,000 has been generated in full. The [test server plan](../../TEST-SERVER-PLAN.md) lists the optional worlds with estimated costs: `r20000` alone is ~5.16 M chunks and ~41 GB.

## Desired result (to-be)

- A recorded decision of which worlds to generate, made from measured numbers.
- Each chosen world generated, mapped and checked like `r3000`.

## Acceptance criteria

- [ ] Budget written here before generating: `r3000`'s seconds and bytes per chunk (SRV-2, map size from SRV-3) scaled to each optional world, against the free disk and the time available. The worlds chosen, in the plan's order, are listed.
- [ ] Each chosen world: the log shows one plan at its radius (`Planned 47 anchors for radius N ...`), its `world_gen_settings.dat` holds that radius, and the pre-generator reports 100 % at radius + 512.
- [ ] No crash or out-of-memory; the heap used for the largest world is recorded.
- [ ] Each chosen world has a fully rendered map, including worlds the server has not loaded (or the BlueMap CLI was used, recorded here).
- [ ] Each chosen world passes SRV-4's check, and its screenshot is linked from SRV-4.
- [ ] The plan's table has real numbers for each world generated.

## Notes

- Needs SRV-4.
- If only one world fits, take `r20000` (MAX) so both ends of the range are seen; if it doesn't fit, take the largest that does.
- Closing this ticket with no larger world, because time or disk ran out, is fine: record why and set it done.
