# Changelog

Notable architectural changes for returning developers. Not a git log — this captures decisions and context that would surprise someone who hasn't looked at the code recently.

For the full decision record, see [architecture.md](architecture.md).

---

## 2026-04-03

### Game tick guard added to InterceptionManager

Added `currentTick > pending.blockedOnTick` check to prevent double-click bypass within a single game tick. Without this, two rapid clicks in the same tick (~0.6s) would block the first and allow the second, defeating the entire protection. See [architecture.md — Why the game tick guard?](architecture.md#why-the-game-tick-guard)

### SafeZoneDetector narrowed to POH + custom regions

Previously `isInInstancedRegion()` treated *all* instances as safe, including boss rooms. Now checks against region ID 8046 (POH) and a configurable set of custom regions. Custom regions are added in-game via `::aoprot`.

### `::aoprot` and `::aoallow` commands added

Chat commands for runtime customization. `::aoprot` captures current region IDs as protected. `::aoallow` toggles the most recently blocked teleport on/off the whitelist. Both persist via RuneLite config.

### Overhead warning overlay added

`AntiOopsOverlay` renders configurable text above the player's head for 2 seconds when a teleport is blocked. Controlled by `overheadWarning` and `warningMessage` config options.

### Action key now strips color tags

`InterceptionManager.buildActionKey()` uses `Text.removeTags()` to strip `<col=...>` tags before building the confirmation key. Prevents intermittent confirmation failures when RuneLite delivers slightly different color tags between clicks.

### Allowed teleports config

New `allowedTeleports` config field stores semicolon-separated `option|target` keys. Teleports on this list bypass interception. Managed via `::aoallow` command.
