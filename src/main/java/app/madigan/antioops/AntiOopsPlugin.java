package app.madigan.antioops;

import app.madigan.antioops.classification.TeleportCategory;
import app.madigan.antioops.classification.TeleportClassifier;
import app.madigan.antioops.classification.TeleportTarget;
import app.madigan.antioops.detection.PvpWorldDetector;
import app.madigan.antioops.detection.SafeZoneDetector;
import app.madigan.antioops.interception.InterceptionManager;
import com.google.inject.Provides;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.WorldView;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.LinkBrowser;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "PvP Anti-Oops"
)
public class AntiOopsPlugin extends Plugin
{
	private static final String ISSUES_URL = "https://github.com/mads-jm/rl-anti-oops/issues/new?template=item-request.yml";

	@Inject
	private Client client;

	@Inject
	private AntiOopsConfig config;

	@Inject
	private PvpWorldDetector pvpWorldDetector;

	@Inject
	private SafeZoneDetector safeZoneDetector;

	@Inject
	private TeleportClassifier teleportClassifier;

	@Inject
	private InterceptionManager interceptionManager;

	@Inject
	private ConfigManager configManager;

	private String lastBlockedActionKey;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AntiOopsOverlay overlay;

	@Inject
	private AntiOopsStatusOverlay statusOverlay;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("PvP Anti-Oops started!");
		overlayManager.add(overlay);
		overlayManager.add(statusOverlay);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			pvpWorldDetector.updateWorldType();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("PvP Anti-Oops stopped!");
		overlayManager.remove(overlay);
		overlayManager.remove(statusOverlay);
		interceptionManager.clear();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGGED_IN)
		{
			pvpWorldDetector.updateWorldType();
		}
		else if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			pvpWorldDetector.clear();
			interceptionManager.clear();
		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		// 1. Only act on PvP (or high-risk) worlds
		if (!pvpWorldDetector.isPvpWorld(config.includeHighRisk()))
		{
			return;
		}

		// 2. Only block when in a safe zone — prevent accidentally leaving safety
		if (!safeZoneDetector.isInSafeZone())
		{
			return;
		}

		// 3. Classify the click
		TeleportTarget target = teleportClassifier.classify(
			event.getMenuOption(), event.getMenuTarget(), event.getId());
		if (target == null)
		{
			return;
		}

		// 4. Per-category config toggle
		switch (target.getCategory())
		{
			case JEWELRY:
				if (!config.protectJewelry())
				{
					return;
				}
				break;
			case SPELLBOOK:
				if (!config.protectSpellbook())
				{
					return;
				}
				break;
			case POH_PORTAL:
			case POH_EXIT:
				if (!config.protectPoh())
				{
					return;
				}
				break;
			case TABLET:
				if (!config.protectTablets())
				{
					return;
				}
				break;
			case CHARGED_ITEM:
				if (!config.protectChargedItems())
				{
					return;
				}
				break;
			default:
				return;
		}

		// 5. Build action key and check whether to block or allow confirmation
		String actionKey = InterceptionManager.buildActionKey(
			event.getMenuOption(), event.getMenuTarget());

		// 6. Skip if this teleport is on the allowed list
		if (isAllowedTeleport(actionKey))
		{
			return;
		}

		if (interceptionManager.shouldBlock(actionKey))
		{
			lastBlockedActionKey = actionKey;
			event.consume();
			if (config.chatWarnings())
			{
				client.addChatMessage(
					ChatMessageType.GAMEMESSAGE,
					"",
					"<col=ffaa00>[PvP Anti-Oops]</col> <col=ff0000>Blocked:</col> <col=ffffff>"
						+ target.getName() + "</col>. Click again within "
						+ config.confirmationTimeoutSeconds()
						+ "s to confirm. Type <col=00ff00>::aoallow</col> to whitelist.",
					null);
			}
			if (config.overheadWarning())
			{
				overlay.trigger();
			}
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted event)
	{
		if ("aoallow".equals(event.getCommand()))
		{
			handleAoallow();
			return;
		}

		if ("aoissue".equals(event.getCommand()))
		{
			LinkBrowser.browse(ISSUES_URL);
			return;
		}

		if (!"aoprot".equals(event.getCommand()))
		{
			return;
		}

		WorldView wv = client.getTopLevelWorldView();
		if (wv == null)
		{
			return;
		}

		if (!wv.isInstance())
		{
			chat("[PvP Anti-Oops] You are not in an instance. Region IDs: "
				+ formatRegions(wv.getMapRegions()));
			return;
		}

		int[] regions = wv.getMapRegions();
		String existing = config.customProtectedRegions().trim();
		java.util.Set<String> regionSet = new java.util.LinkedHashSet<>();
		if (!existing.isEmpty())
		{
			regionSet.addAll(Arrays.asList(existing.split(",")));
		}

		boolean added = false;
		for (int region : regions)
		{
			if (regionSet.add(String.valueOf(region)))
			{
				added = true;
			}
		}

		if (!added)
		{
			chat("[PvP Anti-Oops] Region already protected: " + formatRegions(regions));
			return;
		}

		String updated = String.join(",", regionSet);
		configManager.setConfiguration("antioops", "customProtectedRegions", updated);
		chat("[PvP Anti-Oops] Added region to protected list: " + formatRegions(regions)
			+ " (" + regionSet.size() + " total protected regions)");
	}

	private void handleAoallow()
	{
		if (lastBlockedActionKey == null)
		{
			chat("<col=ffaa00>[PvP Anti-Oops]</col> No recently blocked teleport. Block a teleport first, then type ::aoallow.");
			return;
		}

		Set<String> allowed = getAllowedTeleports();
		String key = lastBlockedActionKey;
		String displayName = formatActionKey(key);

		if (allowed.remove(key))
		{
			String updated = String.join(";", allowed);
			configManager.setConfiguration("antioops", "allowedTeleports", updated);
			chat("<col=ffaa00>[PvP Anti-Oops]</col> <col=ff0000>Removed</col> from allowed list: <col=ffffff>"
				+ displayName + "</col>. This teleport will be blocked again.");
		}
		else
		{
			allowed.add(key);
			String updated = String.join(";", allowed);
			configManager.setConfiguration("antioops", "allowedTeleports", updated);
			chat("<col=ffaa00>[PvP Anti-Oops]</col> <col=00ff00>Allowed</col>: <col=ffffff>"
				+ displayName + "</col>. This teleport will no longer be blocked. Type ::aoallow to undo.");
		}
	}

	/**
	 * Converts an internal action key like "rub|ring of dueling(8)" into
	 * a readable display string like "Rub — Ring of dueling(8)".
	 */
	private static String formatActionKey(String actionKey)
	{
		int sep = actionKey.indexOf('|');
		if (sep < 0)
		{
			return actionKey;
		}
		String option = actionKey.substring(0, sep);
		String target = actionKey.substring(sep + 1);
		// Title-case the option, leave target as-is (already readable)
		String displayOption = option.isEmpty() ? "" : Character.toUpperCase(option.charAt(0)) + option.substring(1);
		return displayOption + " \u2014 " + target;
	}

	private boolean isAllowedTeleport(String actionKey)
	{
		return getAllowedTeleports().contains(actionKey);
	}

	private Set<String> getAllowedTeleports()
	{
		String raw = config.allowedTeleports().trim();
		Set<String> set = new LinkedHashSet<>();
		if (!raw.isEmpty())
		{
			for (String entry : raw.split(";"))
			{
				String trimmed = entry.trim().toLowerCase();
				if (!trimmed.isEmpty())
				{
					set.add(trimmed);
				}
			}
		}
		return set;
	}

	private void chat(String message)
	{
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", message, null);
	}

	private static String formatRegions(int[] regions)
	{
		return Arrays.stream(regions)
			.mapToObj(Integer::toString)
			.collect(Collectors.joining(", "));
	}

	@Provides
	AntiOopsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AntiOopsConfig.class);
	}
}
