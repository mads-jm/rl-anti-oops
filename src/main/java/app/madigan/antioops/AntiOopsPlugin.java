package app.madigan.antioops;

import app.madigan.antioops.classification.TeleportCategory;
import app.madigan.antioops.classification.TeleportClassifier;
import app.madigan.antioops.classification.TeleportTarget;
import app.madigan.antioops.detection.PvpWorldDetector;
import app.madigan.antioops.detection.SafeZoneDetector;
import app.madigan.antioops.interception.InterceptionManager;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

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

	@Override
	protected void startUp() throws Exception
	{
		log.debug("PvP Anti-Oops started!");
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			pvpWorldDetector.updateWorldType();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("PvP Anti-Oops stopped!");
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
			safeZoneDetector.clear();
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
					"[PvP Anti-Oops] Blocked: " + target.getName()
						+ ". Click again within " + config.confirmationTimeoutSeconds() + "s to confirm.",
					null);
			}
		}
	}

	@Provides
	AntiOopsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AntiOopsConfig.class);
	}
}
