# PvP Anti-Oops — Verification Checklist

**Setup:** PvP world, RuneLite Developer Tools enabled, `./gradlew run` with `--safe-mode`.

---

## Safe Zone Detection — RESOLVED

- [x] Varbit `PVP_AREA_CLIENT`: 0 = safe, 1 = dangerous
- [x] POH reads as dangerous (varbit=1), use `client.isInInstancedRegion()` as override
- [x] `isInInstancedRegion()` returns true inside POH
- [x] POH region ID: 8046 — may need this if `isInInstancedRegion()` catches non-POH instances too

**Open question:** Is region 8046 consistent across all POH sizes/styles? Does it change?

---

## POH Portals

- [x] Exit portal: ID 4525, options: Enter, Lock
- [x] Portal Nexus: ID 33408, options: [configured dest], Teleport Menu, Configuration
- [ ] **Portal menu text** — click a directed portal. Is the option "Enter" or the destination name? What's in `getMenuTarget()`?
- [ ] **"Leave House" option** — does it exist on any object/NPC?
- [ ] **Frame style variants** — same destination, different teak/mahogany/marble frame = different object ID?
- [ ] **Remaining portal IDs** — see `ids.md` for the 33 still blank

---

## Jewellery Box — NEW

- [ ] **Menu interaction** — click "Teleport Menu" on a jewellery box. Does it open a widget or fire MenuOptionClicked per destination?
- [ ] **Quick-teleport** — the last-used destination shows as a direct right-click option. Does clicking it fire MenuOptionClicked? What are the option/target strings?
- [ ] **Object name** — what is the exact in-game object name? (for substring matching instead of enumerating 50+ IDs)

---

## Jewelry Chatbox Picker

- [ ] **Does consuming "Rub" prevent the chatbox from opening?** If yes, blocking "Rub" is sufficient
- [ ] **Equipped direct destinations** — ring of dueling shows "Emir's Arena" etc. Confirm these fire MenuOptionClicked with destination as `getMenuOption()` and item name in `getMenuTarget()`
- [ ] **Equipped "Rub" items** — glory/wealth/combat bracelet/skills necklace. Same chatbox question

---

## Classifier Verification

### Spellbook
- [ ] Option is always "Cast"?
- [ ] Stripped target contains "teleport" or "tele group"?
- [ ] "Teleport to House" is NOT blocked?

### Jewelry
- [ ] Stripped target contains item name for all 12 types?
- [ ] Non-teleport options excluded? Need to add: Features, Dismantle, Uncharge, Master, Partner, Log

### Tablets
- [ ] Option is "Break"?
- [ ] Stripped target contains "teleport"?
- [ ] "Teleport to house" tablet NOT blocked?
- [ ] Redirected house tablets ARE blocked?

---
