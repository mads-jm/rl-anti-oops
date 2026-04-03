# PvP Anti-Oops

A RuneLite plugin that prevents you from accidentally leaving a safe zone on a PvP world.

Ironmen use PvP worlds for blighted supplies, typically grinding bosses in safe areas like POH or instances. One autopilot teleport and you're skulled in Lumbridge with your best gear. This plugin adds a click-again-to-confirm gate on any action that would take you from safety into danger.

## How it works

When you're on a PvP world (or High Risk world) and inside a safe zone, the plugin intercepts teleport actions and blocks the first click. A chat warning tells you what was blocked — click the same action again within the timeout window to confirm and teleport normally.

On regular worlds, or when you're already in a dangerous area, the plugin does nothing.

"Teleport to House" (spell, tab, or scroll) is never blocked — it takes you *to* safety.

## Supported teleport types

| Category | Examples | Status |
|---|---|---|
| Teleport jewelry | Ring of dueling, games necklace, glory, etc. | Working |
| Spellbook teleports | All 4 spellbooks, standard + tele group variants | Working |
| Teleport tablets | Varrock teleport, redirected house tabs, etc. | Working |
| POH portals & exit | Portal rooms, exit portal | Partial (recording object IDs) |
| Portal Nexus | All 41 destinations | Planned (widget interception needed) |
| Jewellery Box | Basic, Fancy, Ornate tiers | Planned |

## Configuration

All settings are in the RuneLite config panel under "PvP Anti-Oops":

| Setting | Default | Description |
|---|---|---|
| Protect Jewelry | On | Block jewelry teleports |
| Protect Spellbook | On | Block spellbook casts |
| Protect POH | On | Block POH portal/exit actions |
| Protect Tablets | On | Block teleport tablet usage |
| Confirmation Timeout | 3s | Window to re-click and confirm (1-10s) |
| Chat Warnings | On | Show "[PvP Anti-Oops] Blocked: ..." messages |
| Include High-Risk | On | Also protect on High Risk worlds |

## Safe zone detection

The plugin uses the `PVP_AREA_CLIENT` varbit to detect safe zones (0 = safe, 1 = dangerous). POH instances are always treated as safe via `client.isInInstancedRegion()`, since the varbit incorrectly reads as "dangerous" inside a house.

## Development

See [CONTRIBUTING.md](CONTRIBUTING.md) for setup, architecture, and how to contribute. The highest-impact contributions right now are **recording POH portal object IDs** and **verifying menu interaction patterns** — see [`ids.md`](ids.md) and [`verification.md`](verification.md).

### Roadmap

- Complete POH portal/exit interception (object ID recording in progress)
- Portal Nexus widget interception
- Jewellery Box interception
- Jewelry chatbox destination picker verification
- Instance exit warnings
- Destination-aware filtering (skip warnings for known-safe destinations)
