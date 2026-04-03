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
