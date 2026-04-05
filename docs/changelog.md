# Changelog

Notable architectural changes for returning developers. Not a git log — this captures decisions and context that would surprise someone who hasn't looked at the code recently.

For the full decision record, see [architecture.md](architecture.md).

---

## 2026-04-04

### Expanded teleport item coverage

`TeleportClassifier` now covers ~36 teleport items, up from 12. Two classification paths:

- **JEWELRY_ITEMS** (name-substring match) — expanded with 10 items that use standard "Rub"/"Teleport" options: ring of the elements, ring of shadows, pharaoh's sceptre, camulet, ectophial, chronicle, amulet of the eye, pendant of ates, giantsoul amulet, cowbell amulet, explorer's ring, wilderness sword.
- **CHARGED_ITEM_TELEPORT_OPTIONS** (new) — maps item name → known teleport menu options for items with non-standard options: royal seed pod (Commune), enchanted lyre (Play), skull sceptre (Invoke), kharedst's memoirs (Reminisce), quetzal whistle (Signal), teleport crystal (Lletya/Prifddinas), drakan's medallion, diary rewards (ardougne cloak, desert amulet, rada's blessing), consumables (stony/icy basalt, dorgesh-kaan sphere).

New `CHARGED_ITEM` category with independent `protectChargedItems` config toggle. Many menu option names sourced from wiki — in-game verification still needed.

### `::aoissue` command and config notice

Config panel now shows an "Early Release" notice section at the top explaining that some teleports may not be caught yet. `::aoissue` chat command opens the GitHub issue template (`item-request.yml`) via `LinkBrowser.browse()` for users to report missed items.

### Action key case normalization

`InterceptionManager.buildActionKey()` now lowercases the full action key. `getAllowedTeleports()` also lowercases entries when reading config. Prevents silent mismatches if Jagex changes item name capitalization or if users manually edit the allowed list.

### Status overlay

New `AntiOopsStatusOverlay` shows "TP Protection: ACTIVE/OFF" panel on PvP worlds. Toggleable via `statusOverlay` config option.

### Cleanup

- Removed `NONE` from `TeleportCategory` (dead code — never returned by classifier)
- Removed `shadowJar` task from `build.gradle`
- Fixed case sensitivity inconsistency in `classifySpellbook` (`equals` → `equalsIgnoreCase`)
- Removed unused `Client` injection from `TeleportClassifier`
- `SafeZoneDetector` now logs warnings on invalid custom region IDs
- JUnit 4.12 → 4.13.2

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
