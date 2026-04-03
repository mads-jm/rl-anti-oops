# Contributing to PvP Anti-Oops

## Requirements

- [Java 11](https://adoptium.net/temurin/releases/?version=11) (can also be installed via IntelliJ)
- [IntelliJ IDEA Community Edition](https://www.jetbrains.com/idea/download/) or [VSCode](https://code.visualstudio.com/) / [Cursor](https://cursor.com/)
- An OSRS account (non-Jagex / basic RuneScape account for the dev client)

## Getting started

### Clone and build

```bash
git clone <repo-url>
cd pvp-world-protector
./gradlew build
```

### Run the dev client

```bash
./gradlew run
```

This launches RuneLite in developer mode with `--safe-mode` (no external plugins loaded). Login with a **non-Jagex account** (basic RuneScape account). The plugin appears as "PvP Anti-Oops" in the RuneLite config panel.

You can also run `AntiOopsPluginTest.java` directly from your IDE:
- IntelliJ: add `-ea --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED` to JVM args
- VSCode/Cursor: click "Run" above the `main` method

### Verify it works

1. Log in on a **PvP world**
2. Open the config panel and confirm all toggles are visible
3. Try a teleport (e.g., rub a ring of dueling) — it should be blocked with a chat warning
4. Click the same teleport again within 3 seconds — it should go through

On a regular world, the plugin should be completely silent.

## Project structure

```
app.madigan.antioops/
├── AntiOopsPlugin.java            Main plugin — event wiring and short-circuit chain
├── AntiOopsConfig.java            RuneLite config interface — all user settings
├── detection/
│   ├── PvpWorldDetector.java      Cached PvP/High Risk world detection
│   └── SafeZoneDetector.java      Varbit + instance-based safe zone detection
├── classification/
│   ├── TeleportClassifier.java    Examines menu events, returns TeleportTarget or null
│   ├── TeleportTarget.java        Value object: category + name + destination
│   └── TeleportCategory.java      Enum: JEWELRY, SPELLBOOK, POH_PORTAL, POH_EXIT, TABLET, NONE
└── interception/
    └── InterceptionManager.java   Click-again-to-confirm state machine
```

### How the plugin works

Every `MenuOptionClicked` event passes through a short-circuit chain in `AntiOopsPlugin`:

```
PvpWorldDetector.isPvpWorld()        -> skip if not PvP world
SafeZoneDetector.isInSafeZone()      -> skip if not in a safe zone
TeleportClassifier.classify()        -> skip if not a teleport we care about
Config toggle check                  -> skip if that category is disabled
InterceptionManager.shouldBlock()    -> block first click, allow confirmed re-click
```

If everything passes, `event.consume()` eats the click and a chat warning is shown. The player clicks the same action again within the timeout to confirm.

### Key design decisions

- **Zero overhead on non-PvP worlds.** The world type check is the very first gate — cached boolean, no API calls.
- **No shift-click bypass.** The whole point is catching autopilot. A modifier key would defeat the purpose.
- **Warn on all destinations.** v1 does not distinguish safe from dangerous destinations. One extra confirmation click is cheap; dying with gear is not.

## How to contribute

### In-game research (highest impact right now)

The plugin needs real game data to complete several features. See [`docs/ids.md`](docs/ids.md) for the full reference and [`docs/game-research.md`](docs/game-research.md) for what still needs confirming.

Priority items:

1. **POH portal object IDs** — each directed portal has a unique object ID. Many are still unrecorded.
2. **Jewellery Box interaction** — does "Teleport Menu" open a widget or fire MenuOptionClicked?
3. **Jewelry chatbox picker** — does blocking "Rub" prevent the chatbox from opening?
4. **Portal Nexus widget IDs** — what widget group/child IDs does the nexus interface use?

### How to find object IDs

1. Launch the dev client: `./gradlew run`
2. Log into a PvP world
3. Enable **Developer Tools** in the RuneLite plugin config panel
4. Open **Developer Tools -> Object Inspector** (or enable the "Object ID" overlay via Settings -> Overlays)
5. Right-click the object you want to identify — the overlay shows the object ID on hover
6. Record the ID in `docs/ids.md` alongside the destination name

For POH portals specifically:
- Enter your house in building mode
- Walk to each portal room and hover over directed portals
- Each destination (Varrock, Falador, etc.) has a different object ID
- Unset portal frames have their own IDs (Teak=13636, Mahogany=13637, Marble=13638)

### How to inspect menu events

To see exactly what `getMenuOption()`, `getMenuTarget()`, and `getMenuAction()` return for any click:

1. Add a temporary log line in `AntiOopsPlugin.onMenuOptionClicked()`:
   ```java
   log.debug("MENU: option='{}' target='{}' action={} id={}",
       event.getMenuOption(), event.getMenuTarget(), event.getMenuAction(), event.getId());
   ```
2. Run the dev client and perform the action
3. Check the console output — debug logging is enabled for `app.madigan.antioops` in `logback-test.xml`
4. Note: `getMenuTarget()` contains color tags like `<col=ff9040>Ring of dueling(8)</col>` — the classifier strips these with `Text.removeTags()` before matching

### How to check varbits

1. Enable **Developer Tools** in RuneLite
2. Open **Developer Tools -> Varbit Inspector**
3. Filter by "changed" to see only varbits that change as you move/interact
4. Walk between safe and dangerous zones and note which varbits flip
5. The primary safe zone varbit is `PVP_AREA_CLIENT` — 0 for safe, 1 for dangerous

### How to identify widget interactions

Some game interfaces (Portal Nexus, jewellery box "Teleport Menu", jewelry chatbox pickers) use widgets instead of standard menu clicks. To investigate:

1. Enable **Developer Tools** in RuneLite
2. Open **Developer Tools -> Widget Inspector**
3. Interact with the object/interface — the widget inspector highlights the active widget
4. Note the **widget group ID** and **child ID** from the inspector
5. To check if `onMenuOptionClicked` fires for a widget interaction, add a log line (see "How to inspect menu events" above). If nothing logs, the interaction is widget-only and needs a `@Subscribe` handler for `WidgetLoaded` or `ScriptCallbackEvent`

### Adding a new teleport category

1. Add the enum value to `TeleportCategory.java`
2. Add a private classifier method in `TeleportClassifier.java` (follow the pattern of `classifyJewelry`, `classifySpellbook`, etc.)
3. Call it from the `classify()` method in the try-each-category chain
4. Add a config toggle in `AntiOopsConfig.java` if the category should be independently disableable
5. Add the config check to the switch in `AntiOopsPlugin.onMenuOptionClicked()`

### Adding new items to an existing category

To add a new jewelry item, for example:
1. Add the item name (lowercase) to `JEWELRY_ITEMS` in `TeleportClassifier.java`
2. If the item has unusual menu options that aren't covered by the existing exclusion set (`JEWELRY_NON_TELEPORT_OPTIONS`), update that set too
3. Test on a live client to confirm the menu text matches

### Code style

- Follow existing RuneLite plugin conventions (Guice DI, `@Subscribe` for events, Lombok where appropriate)
- `@Singleton` for all service classes, constructor injection via `@Inject`
- Keep classification data (item names, spell names) as `static final Set` or `List` at the top of the classifier
- Strip color tags from all menu text before matching — use `Text.removeTags()` from `net.runelite.client.util.Text`
- Case-insensitive matching everywhere
- No regex in the hot path (`onMenuOptionClicked` fires very frequently)

### Testing

There are no automated tests for the classification logic yet — this is a known gap. Manual testing on a live PvP world is the primary verification method for now.

When testing changes:

1. `./gradlew build` — must compile clean
2. `./gradlew run` — launch the dev client
3. Test on a PvP world: confirm blocking works for your change
4. Test on a regular world: confirm zero interference
5. Test config toggles: disable the relevant category, confirm the teleport is no longer blocked

### Debugging

The plugin uses `@Slf4j` (Lombok) for logging. Debug logging is enabled for `app.madigan.antioops` in `src/test/resources/logback-test.xml`.

```java
log.debug("classifyJewelry: option='{}' target='{}'", option, strippedTarget);
```

This appears in the RuneLite dev client console when running via `./gradlew run`.

## RuneLite plugin basics

- Plugins extend `net.runelite.client.plugins.Plugin`
- `@PluginDescriptor` defines the display name
- `@Subscribe` methods receive game events from the RuneLite event bus
- `@Inject` fields are provided by Guice dependency injection
- `@Provides` methods register config interfaces with RuneLite's config system
- `ConfigGroup` + `ConfigItem` annotations define the settings UI automatically
- `net.runelite.api.gameval.VarbitID` is the newer API for varbit constants (preferred over the older `Varbits` enum)

### Key RuneLite APIs used

| API | What we use it for |
|---|---|
| `client.getWorldType()` | Detect PvP / High Risk worlds |
| `MenuOptionClicked` event | Intercept teleport actions |
| `event.consume()` | Block an action from executing |
| `client.addChatMessage()` | Show the "[PvP Anti-Oops] Blocked" warning |
| `GameStateChanged` event | Detect login, logout, world hops |
| `Text.removeTags()` | Strip `<col=...>` tags from menu text |
| `VarbitID.PVP_AREA_CLIENT` | Safe zone detection varbit |
| `client.isInInstancedRegion()` | Detect POH instances |
| `client.getVarbitValue()` | Read varbit values |

### Related plugins worth reading

- **Accidental Teleport Blocker** — cloned at `../accidental-teleport-blocker/`. Spellbook-only, uses modifier key bypass. Good reference for `MenuOptionClicked` patterns.
- **Goal Tracker** — cloned at `../rl-goal-tracker/`. Mature plugin with full docs and test suite. Good reference for project structure.

## Publishing to Plugin Hub

When the plugin is ready for release:

1. Ensure the repo is public on GitHub
2. `runelite-plugin.properties` has correct metadata
3. Submit a PR to [runelite/plugin-hub](https://github.com/runelite/plugin-hub) adding a `plugins/pvp-anti-oops` file with the repo URL and commit hash
4. Reviewers will likely ask about overlap with Accidental Teleport Blocker. The answer: different problem (PvP-world-specific vs. general spellbook), different scope (jewelry + POH + tablets), different UX (confirmation gate vs. modifier key bypass)
