package app.madigan.antioops;

import app.madigan.antioops.classification.TeleportCategory;
import app.madigan.antioops.classification.TeleportClassifier;
import app.madigan.antioops.classification.TeleportTarget;
import app.madigan.antioops.detection.PvpWorldDetector;
import app.madigan.antioops.detection.SafeZoneDetector;
import app.madigan.antioops.interception.InterceptionManager;
import com.google.inject.Provides;
import java.util.Arrays;
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
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "PvP Anti-Oops"
)
public class AntiOopsPlugin extends Plugin
{
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

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AntiOopsOverlay overlay;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("PvP Anti-Oops started!");
		overlayManager.add(overlay);
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
			case NONE:
			default:
				return;
		}

		// 5. Build action key and check whether to block or allow confirmation
		String actionKey = InterceptionManager.buildActionKey(
			event.getMenuOption(), event.getMenuTarget());

		if (interceptionManager.shouldBlock(actionKey))
		{
			event.consume();
			if (config.chatWarnings())
			{
				client.addChatMessage(
					ChatMessageType.GAMEMESSAGE,
					"",
					"<col=ff0000>[PvP Anti-Oops] Blocked: " + target.getName()
						+ ". Click again within " + config.confirmationTimeoutSeconds() + "s to confirm.</col>",
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
		chat("[PvP Anti-Oops] Added region to protected list: " + formatRegions(regions));
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
