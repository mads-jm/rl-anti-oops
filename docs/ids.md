# ID Reference

Authoritative reference for all game IDs used by the plugin. Sourced from OSRS Wiki + in-game verification. For research status and open questions, see [game-research.md](game-research.md).

---

## Safe Zone Detection

| Varbit | Safe | Dangerous | Notes |
|--------|------|-----------|-------|
| `PVP_AREA_CLIENT` | 0 | 1 | Primary detection method |
| `INSIDE_WILDERNESS` | 0 | 1 | Also flips at boundaries |

POH reads as dangerous (varbit = 1) despite being safe. Override via `isInInstancedRegion()` + region 8046.

---

## Jewelry Items

The classifier uses **name-substring matching**, not item IDs. IDs here are for reference. See [architecture.md — Why name-substring matching](architecture.md#why-name-substring-matching-for-jewelry).

### Ring of Dueling (2552-2566, evens)
| (8) | (7) | (6) | (5) | (4) | (3) | (2) | (1) |
|-----|-----|-----|-----|-----|-----|-----|-----|
| 2552 | 2554 | 2556 | 2558 | 2560 | 2562 | 2564 | 2566 |

Destinations: Emir's Arena, Castle Wars, Ferox Enclave, Fortis Colosseum

### Games Necklace (3853-3867, odds)
| (8) | (7) | (6) | (5) | (4) | (3) | (2) | (1) |
|-----|-----|-----|-----|-----|-----|-----|-----|
| 3853 | 3855 | 3857 | 3859 | 3861 | 3863 | 3865 | 3867 |

Destinations: Burthorpe, Barbarian Outpost, Corporeal Beast, Tears of Guthix, Wintertodt Camp

### Amulet of Glory (1704-1712 + 11976-11978)
| (0) | (1) | (2) | (3) | (4) | (5) | (6) |
|-----|-----|-----|-----|-----|-----|-----|
| 1704 | 1706 | 1708 | 1710 | 1712 | 11976 | 11978 |

Destinations: Edgeville, Karamja, Draynor Village, Al Kharid

### Ring of Wealth (2572 + 11980-11988)
| (0) | (1) | (2) | (3) | (4) | (5) |
|-----|-----|-----|-----|-----|-----|
| 2572 | 11988 | 11986 | 11984 | 11982 | 11980 |

Destinations: Miscellania, Grand Exchange, Falador Park, Dondakan

### Combat Bracelet (11118-11126 + 11972-11974)
| (0) | (1) | (2) | (3) | (4) | (5) | (6) |
|-----|-----|-----|-----|-----|-----|-----|
| 11126 | 11124 | 11122 | 11120 | 11118 | 11974 | 11972 |

Destinations: Warriors' Guild, Champions' Guild, Edgeville Monastery, Ranging Guild

### Skills Necklace (11105-11113 + 11968-11970)
| (0) | (1) | (2) | (3) | (4) | (5) | (6) |
|-----|-----|-----|-----|-----|-----|-----|
| 11113 | 11111 | 11109 | 11107 | 11105 | 11970 | 11968 |

Destinations: Fishing Guild, Mining Guild, Crafting Guild, Cooks' Guild, Woodcutting Guild, Farming Guild

### Necklace of Passage (21146-21155)
| (5) | (4) | (3) | (2) | (1) |
|-----|-----|-----|-----|-----|
| 21146 | 21149 | 21151 | 21153 | 21155 |

Destinations: Wizards' Tower, The Outpost, Eagles' Eyrie

### Digsite Pendant (11190-11194)
| (1) | (2) | (3) | (4) | (5) |
|-----|-----|-----|-----|-----|
| 11190 | 11191 | 11192 | 11193 | 11194 |

Destinations: Digsite, Fossil Island, Lithkren Dungeon

### Burning Amulet (21166-21175)
| (5) | (4) | (3) | (2) | (1) |
|-----|-----|-----|-----|-----|
| 21166 | 21169 | 21171 | 21173 | 21175 |

Destinations: Chaos Temple, Bandit Camp, Lava Maze (all Wilderness)

### Slayer Ring (11866-11873)
| (8) | (7) | (6) | (5) | (4) | (3) | (2) | (1) |
|-----|-----|-----|-----|-----|-----|-----|-----|
| 11866 | 11867 | 11868 | 11869 | 11870 | 11871 | 11872 | 11873 |

Destinations: Stronghold Slayer Cave, Slayer Tower, Fremennik Slayer Dungeon, Tarn's Lair, Dark Beasts, Haunted Mine

### Xeric's Talisman
| Inert | Charged |
|-------|---------|
| 13392 | 13393 |

Destinations: Xeric's Lookout, Xeric's Glade, Xeric's Inferno, Xeric's Heart, Xeric's Honour

---

## Additional Covered Items — JEWELRY category (name-substring via `JEWELRY_ITEMS`)

Items matched by name-substring in the menu target. Use standard "Rub" / "Teleport" menu options. IDs are for reference only — the classifier matches by name, not ID.

| Item | Menu Option | Destinations | Item IDs | Charges |
|------|------------|--------------|----------|---------|
| Ring of the elements | Rub | Air Altar, Water Altar, Earth Altar, Fire Altar | 26818 (uncharged), 26820 (charged) | Rechargeable |
| Ring of shadows | Teleport | Ancient Vault, Ghorrock Dungeon, The Scar, Lassar Undercity, Stranglewood | 28673 (uncharged), 28675 (charged) | Rechargeable |
| Pharaoh's sceptre | Teleport | Jalsavrah, Jaleustrophos, Jaldraocht, Jaltevas | 9044-9050 (charged), 9045 (uncharged) | 3-100 (diary-dependent) |
| Camulet | Teleport | Enakhra's Temple | 9032 | 4 (rechargeable, upgradeable to unlimited) |
| Ectophial | Teleport | Ectofuntus | 4251 | Unlimited |
| Chronicle | Teleport | Champions' Guild | 20674 | 1000 (charges via teleport cards) |
| Amulet of the eye | Teleport | Temple of the Eye | `___` | Unlimited |
| Pendant of Ates | Rub | The Darkfrost, Twilight Temple, Ralos' Rise, North Aldarin, North of Kastori, Nemus Retreat | `___` | 1000 (rechargeable via frozen tears) |
| Giantsoul amulet | Rub | Bryophyta's lair, Obor's lair, Royal Titans' lair | 30637 (uncharged), 30638 (charged) | 16000 (rechargeable) |
| Cowbell amulet | Teleport | Lumbridge cow field | 33103 (uncharged), 33104 (charged) | 1000 (rechargeable) |
| Explorer's ring | Teleport | Cabbage patch (Falador) | `___` | Tier 3+ diary reward |
| Wilderness sword | Teleport | Fountain of Rune | `___` | Tier 4 diary reward |

---

## Additional Covered Items — CHARGED_ITEM category (via `CHARGED_ITEM_TELEPORT_OPTIONS`)

Items with non-standard menu options. Matched by item name substring in target + specific menu option. Menu options sourced from wiki — many need in-game verification.

### Unique-option items

| Item | Menu Option | Destinations | Item IDs | Charges |
|------|------------|--------------|----------|---------|
| Royal seed pod | Commune | Grand Tree | 19564 | Unlimited |
| Enchanted lyre | Play | Rellekka, Waterbirth Island, Jatizso, Neitiznot | 3690 (uncharged), 3691(1)-3695(5) | 1-5 (rechargeable) |
| Skull sceptre | Invoke | Barbarian Village (Stronghold entrance) | 21276 (standard), 21279 (i) | 10-26 (diary-dependent) |
| Kharedst's memoirs | Reminisce | Hosidius, Piscarilius, Shayzien, Lovakengj, Arceuus | 21756 | 100 (rechargeable) |
| Quetzal whistle | Signal | Quetzal Transport landing sites | `___` (basic/enhanced/perfected/perfected(i)) | 50 (rechargeable; perfected(i) unlimited) |

### Destination-name option items

| Item | Menu Options (teleport) | Destinations | Item IDs | Charges |
|------|------------------------|--------------|----------|---------|
| Teleport crystal | Lletya / Prifddinas | Lletya, Prifddinas | 23776(1)-23779(4), `___`(5) | 1-5 (rechargeable) |
| Eternal teleport crystal | Lletya / Prifddinas | Lletya, Prifddinas | 23901 | Unlimited |
| Drakan's medallion | Ver Sinhaza / Darkmeyer / Slepe | Ver Sinhaza, Darkmeyer, Slepe | 22400 | Unlimited |

Note: "teleport crystal" substring covers both standard and eternal variants in the classifier.

### Achievement diary rewards

| Item | Menu Options (teleport) | Destinations | Tiers with Teleport |
|------|------------------------|--------------|---------------------|
| Ardougne cloak | Monastery Teleport / Farm Teleport | Ardougne Monastery, Ardougne farm | All tiers (1-4); farm from tier 2+ |
| Desert amulet | Nardah / Kalphite Cave | Nardah, Kalphite Cave | Tier 2+ for Nardah; tier 4 for Kalphite |
| Rada's blessing | Kourend Woodland / Mount Karuulm | Kourend Woodland, Mount Karuulm | Tier 3+ |

### Consumable items

| Item | Menu Options (teleport) | Destinations | Notes |
|------|------------------------|--------------|-------|
| Dorgesh-kaan sphere | Break | Random location in Dorgesh-Kaan | Consumed on use |
| Stony basalt | Troll Stronghold entrance / Troll Stronghold roof | Troll Stronghold | Consumed on use; roof requires 73 Agility + Hard Fremennik Diary |
| Icy basalt | Weiss | Weiss | Consumed on use; requires Making Friends with My Arm |

---

## Intentionally Excluded

| Item | Reason |
|------|--------|
| Ring of returning | Teleports TO respawn point — safe. Same logic as "Teleport to House" exclusion. |

---

## Verification Status

Many menu option names for CHARGED_ITEM items are sourced from wiki, not verified in-game. If an item's menu option doesn't match what the wiki says, the classifier will miss it. Use `::aoissue` to report missed teleports.

---

## POH Object IDs

### Portal Room — Directed Portals

Each destination has a unique object ID. Unset frame IDs: Teak=13636, Mahogany=13637, Marble=13638.

| Destination | Object ID | Source |
|-------------|-----------|--------|
| Varrock | 13615 | confirmed |
| Falador | 13617 | confirmed |
| Ardougne | 13619 | confirmed |
| Lumbridge | `___` | |
| Camelot | `___` | |
| Yanille/Watchtower | `___` | |
| Kharyrll (Canifis) | `___` | |
| Senntisten | `___` | |
| Kourend Castle | `___` | |
| Lunar Isle | 29339 | confirmed |
| Waterbirth Island | 29342 | confirmed |
| Salve Graveyard | 37586 | confirmed |
| West Ardougne | 37588 | confirmed |
| Barrows | 37591 | confirmed |
| Civitas illa Fortis | 50713 | confirmed |
| Draynor Manor | `___` | |
| Al-Kharid | `___` | |
| Arceuus Library | `___` | |
| Mind Altar | `___` | |
| Fenkenstrain's Castle | `___` | |
| Paddewwa | `___` | |
| Trollheim | `___` | |
| Marim (Ape Atoll) | `___` | |
| Harmony Island | `___` | |
| Forgotten Cemetery | `___` | |
| Ourania Cave | `___` | |
| Lassar | `___` | |
| Barbarian Outpost | `___` | |
| Dareeyak | `___` | |
| Port Khazard | `___` | |
| Carrallanger | `___` | |
| Fishing Guild | `___` | |
| Catherby | `___` | |
| Ice Plateau | `___` | |
| Annakarl | `___` | |
| Ape Atoll Dungeon | `___` | |
| Ghorrock | `___` | |
| Troll Stronghold | `___` | |
| Weiss | `___` | |
| Battlefront | `___` | |
| Respawn Point | `___` | |
| Teleport to Boat | `___` | |

### Exit Portal

| Object | ID | Menu options |
|--------|----|-------------|
| Exit portal | 4525 | Enter, Lock |

### Portal Nexus

| Object | ID | Menu options |
|--------|----|-------------|
| Portal Nexus | 33408 | [Configured destination], Teleport Menu, Configuration |

### Jewellery Box

Each tier has many object IDs (one per combination of stored jewelry).

| Tier | Object IDs | Added destinations |
|------|-----------|-------------------|
| Basic | 37492-37500, 50710 | Ring of dueling + Games necklace destinations |
| Fancy | 37501-37519, 50711 | + Combat bracelet + Skills necklace destinations |
| Ornate | 37520-37546, 50712 | + Ring of wealth + Amulet of glory destinations |

---

## Portal Nexus Destinations (41 total)

Uses a widget interface, not individual menu entries. Currently blocked at object level.

Arceuus Library, Draynor Manor, Battlefront, Varrock/GE, Mind Altar, Lumbridge, Respawn Point, Falador, Salve Graveyard, Camelot/Seers', Kourend Castle, Fenkenstrain's Castle, East Ardougne, Civitas illa Fortis, Paddewwa, Watchtower/Yanille, Senntisten, West Ardougne, Trollheim, Marim, Harmony Island, Kharyrll, Teleport to Boat, Lunar Isle, Forgotten Cemetery, Ourania Cave, Waterbirth Island, Lassar, Barbarian Outpost, Port Khazard, Dareeyak, Barrows, Carrallanger, Fishing Guild, Catherby, Ice Plateau, Annakarl, Ape Atoll Dungeon, Ghorrock, Troll Stronghold, Weiss
