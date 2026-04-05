# Architecture Decisions

This document records *why* the plugin works the way it does — the design choices, trade-offs, and lessons learned. For *how to work with* the code, see [CONTRIBUTING.md](../CONTRIBUTING.md). For game IDs and raw reference data, see [ids.md](ids.md).

---

## Why a short-circuit chain?

The event pipeline in `AntiOopsPlugin.onMenuOptionClicked()` is ordered deliberately:

```
PvpWorldDetector.isPvpWorld()        → cached boolean, O(1)
SafeZoneDetector.isInSafeZone()      → live varbit read
TeleportClassifier.classify()        → string matching
Config toggle check                  → boolean lookup
InterceptionManager.shouldBlock()    → state machine transition
```

**Cheapest checks first.** `isPvpWorld()` is a cached boolean — on regular worlds (99% of sessions), nothing else runs. Safe zone detection is a single varbit read. Classification involves string operations and only runs when both prior gates pass. This means the plugin has zero measurable overhead for the vast majority of players.

**Each gate is a separate concern.** Detection, classification, configuration, and interception are independent services. This makes them individually testable and replaceable without rewiring the pipeline.

---

## Why no shift-click bypass?

The Accidental Teleport Blocker plugin (see [notes/context.md](../notes/context.md)) uses a modifier key approach: hold Shift to allow a teleport. We deliberately rejected this.

**The problem we're solving is autopilot.** A player grinding on a PvP world has muscle memory that doesn't distinguish between world types. If they've trained themselves to Shift-click teleports (as ATB users do), that habit carries to PvP worlds unchanged — the bypass becomes part of the autopilot. A confirmation gate that requires *recognizing* the warning and *re-clicking* the same action forces a moment of awareness.

This is a v1 decision. If user feedback shows the confirmation is too annoying for experienced players who want a faster escape hatch, a modifier bypass could be added in v2 behind a config toggle.

---

## Why block-then-confirm instead of a dialog?

RuneLite plugins can't inject native-feeling game dialogs. The alternatives were:

1. **Custom widget overlay** — complex, fragile across game updates, unfamiliar UX
2. **Chat message + re-click** — zero UI complexity, familiar OSRS pattern ("click again to confirm"), works with all teleport types uniformly

The re-click approach is also the cheapest possible confirmation. One extra click when you're about to make a mistake costs almost nothing. Dying with gear costs a lot.

**Single pending confirmation.** Only one teleport can be "pending" at a time. If you click a different teleport before confirming, the first pending is silently discarded and the new teleport becomes pending. This avoids race conditions with multiple confirmation slots and keeps the state machine trivial. The trade-off (rapid clicking of different teleports feels slightly janky) is acceptable — players should be reading the warning, not speed-clicking through it.

---

## Why the game tick guard?

`InterceptionManager.shouldBlock()` has a check that looks like it could be removed:

```java
if (currentTick > pending.blockedOnTick) { ... }
```

**This prevents a double-click bypass.** RuneLite can deliver two `MenuOptionClicked` events within a single game tick (~0.6s). Without this guard:

1. First click: `shouldBlock()` returns `true`, pending stored, event consumed
2. Second click (same tick): finds valid pending, returns `false`, teleport goes through

The tick guard ensures at least one game tick passes between blocking and confirming. This forces the player to *see* the chat warning before confirming. The inspection report ([notes/inspection-report.md](../notes/inspection-report.md), finding C1) identified this as the most critical safety issue.

---

## Why POH is special

The `PVP_AREA_CLIENT` varbit (0 = safe, 1 = dangerous) is the primary safe zone signal. But **POH reads as dangerous** (varbit = 1) despite being 'completely safe' from PvP.

The fix evolved through three iterations:

1. **`isInInstancedRegion()` alone** — caught POH but also caught boss rooms, minigame instances, and other areas that aren't safe
2. **Varbit + any instance = safe** — same problem, too broad
3. **Varbit + instance + region ID check** — current approach. Region 8046 is POH. Only instances in known-safe regions override the varbit

The `::aoprot` command extends this: players can stand inside any instance (e.g., a boss room they consider safe) and type `::aoprot` to add that region ID to the protected list. This is stored in config and persists across sessions. See `SafeZoneDetector.getProtectedRegions()`.

---

## Why "Teleport to House" is never blocked

Both the spellbook spell and the tablet variant are explicitly excluded from interception in `TeleportClassifier`. The destination is the player's POH — which is safe. Blocking a teleport *to* safety would be counterproductive.

**Redirected house tablets ARE blocked.** A "Rimmington teleport" tablet (created by redirecting a house teleport) sends the player directly to the overworld location, not into the house. These are dangerous on PvP worlds and must be caught. The classifier handles this by only excluding targets containing "teleport to house" (case-insensitive) — redirected tablets have the destination name instead.

---

## Why name-substring matching for jewelry

Jewelry items have many charge variants. A Ring of dueling has 8 item IDs (2552, 2554, 2556, ..., 2566). Matching by item ID would require maintaining a list of ~80+ IDs across all jewelry types, updating whenever Jagex adds a new charge variant.

**Name-substring matching is stable across charges.** The string "ring of dueling" appears in `Ring of dueling(8)`, `Ring of dueling(7)`, etc. One entry covers all variants. The trade-off is a theoretical false positive on a future item whose name contains an existing jewelry name as a substring — unlikely given OSRS naming conventions.

All item names and menu options are matched case-insensitively after stripping color tags via `Text.removeTags()`. This is consistent across the entire classifier (see `TeleportClassifier`).

---

## Why `::aoprot` and `::aoallow` exist

RuneLite config panels don't support "capture current game state" buttons. These chat commands fill that gap:

- **`::aoprot`** — adds the player's current region IDs to the protected regions list. Must be inside an instance (prevents accidentally protecting the entire overworld). Used for boss rooms or other instances the player considers safe.
- **`::aoallow`** — toggles the most recently blocked teleport action on/off the whitelist. Useful for teleports that take you close to safe instances.
- **`::aoissue`** — opens the GitHub issue template for reporting missed teleport items. Opens via `LinkBrowser.browse()`.

`::aoprot` and `::aoallow` store their data as delimited strings in RuneLite config (`customProtectedRegions` as comma-separated IDs, `allowedTeleports` as semicolon-separated `option|target` keys). This persists across sessions via RuneLite's config system.

---

## Why `buildActionKey` strips color tags

The action key used for confirmation matching is built from menu option + target text. RuneLite wraps target text in color tags like `<col=ff9040>Ring of dueling(8)</col>`. These tags can vary between clicks due to hover state, theme plugins, or interface redraws.

If the action key included raw tags, the confirmation click might not match the blocking click — the player would be unable to confirm and would see repeated "blocked" warnings. Stripping tags via `Text.removeTags()` produces a stable key. See the inspection report ([notes/inspection-report.md](../notes/inspection-report.md), finding M1) for the original analysis.

---

## Known limitations (intentional v1 scope)

These are deliberate exclusions, not bugs:

| Gap | Why excluded | Future plan |
|-----|-------------|-------------|
| **Destination-aware filtering** | One extra click is cheap; false negatives kill. v1 warns on all destinations. | v2: skip warnings for known-safe destinations (Ferox Enclave, etc.) |
| **Portal Nexus widget interception** | Nexus uses a widget UI, not menu events. Blocking the object interaction is a stopgap. | v2: intercept widget destination selection |
| **Jewellery Box** | Widget-based interaction, same problem as Nexus. | v2: research + widget interception |
| **Fairy rings, spirit trees** | Different interaction model, lower risk (players are more aware when using these). | v2+ consideration |
| **Wilderness obelisks, levers** | Player is already in a dangerous zone. Plugin only protects safe→dangerous transitions. | Out of scope |
| **Sound alerts** | Minimal viable product — chat + overhead warning covers awareness. | v2: configurable sound cue |
| **Deadman worlds** | Different safe zone rules, different audience. Needs separate research. | v2+ if demand exists |

For the full list of uncovered teleport types and research needed, see [game-research.md](game-research.md).
