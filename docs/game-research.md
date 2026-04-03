# Game Research Guide

How to investigate OSRS game mechanics for this plugin, and what still needs verification. For research *techniques* (varbit inspector, widget inspector, menu event logging), see [CONTRIBUTING.md](../CONTRIBUTING.md). This doc tracks *what* to research and *what we've found*.

---

## Open Questions

### POH Portals

- [ ] **Portal menu text** — click a directed portal. Is the option "Enter" or the destination name? What does `getMenuTarget()` return?
- [ ] **Frame style variants** — do Teak/Mahogany/Marble frames for the same destination produce different object IDs? (Frame IDs: Teak=13636, Mahogany=13637, Marble=13638 — these are unset frames)
- [ ] **"Leave House" option** — does it exist on any object or NPC inside POH?
- [ ] **Remaining 33 portal IDs** — see [ids.md](ids.md) for the blank entries. Each destination has a unique object ID that needs in-game verification.

### Jewellery Box (POH furniture)

- [ ] **Menu interaction** — click "Teleport Menu" on a jewellery box. Does it open a widget or fire `MenuOptionClicked` per destination?
- [ ] **Quick-teleport** — the last-used destination shows as a direct right-click option. Does clicking it fire `MenuOptionClicked`? What are the option/target strings?
- [ ] **Object name** — what is the exact in-game name? (for substring matching instead of enumerating 50+ object IDs across tiers)

### Jewelry Chatbox Picker

- [ ] **Does consuming "Rub" prevent the chatbox from opening?** If yes, blocking "Rub" is sufficient. If no, the chatbox destination selection needs separate interception.
- [ ] **Equipped direct destinations** — ring of dueling shows "Emir's Arena" etc. Confirm these fire `MenuOptionClicked` with destination as `getMenuOption()` and item name in `getMenuTarget()`.
- [ ] **Equipped "Rub" items** — glory, wealth, combat bracelet, skills necklace, xeric's talisman, slayer ring. Do these all use the chatbox picker pattern?

### Portal Nexus

- [ ] **Widget group/child IDs** — what widget does the nexus interface use? Needed for future widget-based interception.
- [ ] **Destination text format** — what format do destination names appear in within the nexus widget?
- [ ] **Other nexus tier/variation IDs** — are there object IDs besides 33408?

### Classifier Edge Cases

- [ ] **Spellbook "Cast" option** — is the menu option always exactly "Cast" (case-sensitive)?
- [ ] **"Operate" on equipped jewelry** — does "Operate" trigger a teleport or open the chatbox picker? Is it distinct from "Rub"?
- [ ] **Home teleport** — what is the menu option text? Should it be blocked? (Sends to Lumbridge/Edgeville on PvP worlds, which may be dangerous)

---

## Resolved Questions

| Question | Answer | Verified |
|----------|--------|----------|
| Safe zone varbit? | `PVP_AREA_CLIENT`: 0 = safe, 1 = dangerous | In-game, dev tools |
| POH varbit override? | POH reads as dangerous (varbit=1). Override with `isInInstancedRegion()` + region 8046 | In-game |
| POH region ID? | 8046, consistent across house sizes/styles | In-game |
| Exit portal ID? | 4525. Options: "Enter", "Lock" | In-game |
| Portal Nexus ID? | 33408. Options: [configured dest], "Teleport Menu", "Configuration" | In-game |
| Varrock portal ID? | 13615 | In-game |
| Falador portal ID? | 13617 | In-game |
| Ardougne portal ID? | 13619 | In-game |
| Lunar Isle portal ID? | 29339 | In-game |
| Waterbirth portal ID? | 29342 | In-game |
| Salve Graveyard portal ID? | 37586 | In-game |
| West Ardougne portal ID? | 37588 | In-game |
| Barrows portal ID? | 37591 | In-game |
| Civitas illa Fortis portal ID? | 50713 | In-game |

---

## Coverage Gaps

Teleport types not currently handled by the classifier. Organized by likely priority for future versions.

### Likely v2

| Teleport type | Interaction model | Notes |
|---------------|------------------|-------|
| **Portal Nexus widget** | Widget event (not `MenuOptionClicked`) | Currently blocked at object level as stopgap |
| **Jewellery Box** | Widget event | Multiple tiers with different object IDs |
| **Jewelry chatbox picker** | Possibly widget event | May be covered if consuming "Rub" prevents the chatbox |

### v2+ consideration

| Teleport type | Menu option | Notes |
|---------------|------------|-------|
| **Royal seed pod** | "Commune" (unverified) | One-click teleport to Grand Exchange |
| **Ectophial** | "Empty" | One-click teleport to Ectofuntus |
| **Home teleport** | Unknown | Free teleport, different from standard "Cast" spells |
| **Fairy rings** | "Configure" / "Use" | In POH and overworld. Different interaction model |
| **Spirit trees** | "Travel" | In POH and overworld |

### Out of scope

| Teleport type | Reason |
|---------------|--------|
| **Wilderness obelisks** | Player already in dangerous zone |
| **Lever teleports** (Ardougne/wilderness levers) | Player typically already in danger |
| **Minigame teleport** (grouping interface) | Widget event, low priority |
| **Canoes, charter ships** | Different interaction paradigm, low risk |
| **NPC-initiated teleports** | Edge case, not player-initiated |

---

## How to Contribute Research

1. Launch the dev client: `./gradlew run`
2. Log into a PvP world (non-Jagex account required)
3. Enable **Developer Tools** in RuneLite config
4. See [CONTRIBUTING.md](../CONTRIBUTING.md) for specific techniques:
   - **Object IDs**: "How to find object IDs" section
   - **Menu events**: "How to inspect menu events" section
   - **Varbits**: "How to check varbits" section
   - **Widgets**: "How to identify widget interactions" section
5. Record findings in this doc (mark items as `[x]`, add to Resolved Questions table)
6. Update [ids.md](ids.md) with any new IDs discovered
