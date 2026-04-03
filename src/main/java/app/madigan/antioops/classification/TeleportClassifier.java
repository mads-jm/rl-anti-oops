package app.madigan.antioops.classification;

import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.util.Text;

/**
 * Classifies menu click events to determine if they represent a teleport
 * that should be intercepted on PvP worlds.
 */
@Slf4j
@Singleton
public class TeleportClassifier
{
	// ---------- Jewelry item name substrings (case-insensitive match against stripped target) ----------
	private static final Set<String> JEWELRY_ITEMS = Set.of(
		"ring of dueling",
		"games necklace",
		"amulet of glory",
		"amulet of eternal glory",
		"ring of wealth",
		"combat bracelet",
		"skills necklace",
		"necklace of passage",
		"digsite pendant",
		"burning amulet",
		"slayer ring",
		"xeric's talisman"
	);

	// ---------- Menu options that are never teleport actions on jewelry ----------
	// "check" covers slayer ring + xeric's talisman; "features" covers ring of wealth;
	// "dismantle"/"uncharge" cover xeric's talisman; "master"/"partner"/"log" cover slayer ring.
	// "teleport" is intentionally absent — slayer ring's Teleport option IS a real teleport.
	private static final Set<String> JEWELRY_NON_TELEPORT_OPTIONS = Set.of(
		"wear",
		"remove",
		"wield",
		"examine",
		"drop",
		"use",
		"check",
		"features",
		"dismantle",
		"uncharge",
		"master",
		"partner",
		"log"
	);

	private final Client client;

	@Inject
	public TeleportClassifier(Client client)
	{
		this.client = client;
	}

	/**
	 * Classifies a menu click event.
	 *
	 * @param menuOption the raw menu option string (e.g. "Cast", "Break", "Rub", "Varrock")
	 * @param menuTarget the raw menu target string — may contain RuneLite color tags
	 * @param itemId     the item ID from the event (used for future POH object ID verification)
	 * @return a {@link TeleportTarget} if this is an interceptable teleport, or {@code null}
	 */
	public TeleportTarget classify(String menuOption, String menuTarget, int itemId)
	{
		if (menuOption == null || menuTarget == null)
		{
			return null;
		}

		TeleportTarget result;

		result = classifySpellbook(menuOption, menuTarget);
		if (result != null)
		{
			return result;
		}

		result = classifyJewelry(menuOption, menuTarget);
		if (result != null)
		{
			return result;
		}

		result = classifyTablet(menuOption, menuTarget);
		if (result != null)
		{
			return result;
		}

		result = classifyPoh(menuOption, menuTarget, itemId);
		if (result != null)
		{
			return result;
		}

		return null;
	}

	// -------------------------------------------------------------------------
	// TASK-05: Spellbook teleports
	// -------------------------------------------------------------------------

	private TeleportTarget classifySpellbook(String menuOption, String menuTarget)
	{
		if (!"Cast".equals(menuOption))
		{
			return null;
		}

		String stripped = stripTags(menuTarget);

		if (!isTeleportSpell(stripped))
		{
			return null;
		}

		// "Teleport to House" sends the player TO safety — never block it.
		if (stripped.toLowerCase().contains("teleport to house"))
		{
			return null;
		}

		return new TeleportTarget(TeleportCategory.SPELLBOOK, stripped, null);
	}

	/**
	 * Returns true if the stripped spell name looks like a teleport spell.
	 * Mirrors the logic in the AccidentalTeleportBlocker reference plugin.
	 */
	private boolean isTeleportSpell(String strippedTarget)
	{
		String lower = strippedTarget.toLowerCase();
		return lower.contains("teleport") || lower.contains("tele group");
	}

	// -------------------------------------------------------------------------
	// TASK-06: Jewelry teleports
	// -------------------------------------------------------------------------

	private TeleportTarget classifyJewelry(String menuOption, String menuTarget)
	{
		String strippedOption = menuOption.trim().toLowerCase();

		// Skip options that are clearly not teleports
		if (JEWELRY_NON_TELEPORT_OPTIONS.contains(strippedOption))
		{
			return null;
		}

		String strippedTarget = stripTags(menuTarget).toLowerCase();

		// Check if the target contains a known jewelry item name
		boolean isJewelry = JEWELRY_ITEMS.stream()
			.anyMatch(strippedTarget::contains);

		if (!isJewelry)
		{
			return null;
		}

		// "Rub" = left-click initiates the teleport flow; destination options are also teleports.
		// Both should be intercepted. Everything else (non-teleport options) was already excluded above.
		return new TeleportTarget(TeleportCategory.JEWELRY, strippedTarget, menuOption);
	}

	// -------------------------------------------------------------------------
	// TASK-08: Tablet teleports
	// -------------------------------------------------------------------------

	private TeleportTarget classifyTablet(String menuOption, String menuTarget)
	{
		if (!"Break".equalsIgnoreCase(menuOption))
		{
			return null;
		}

		String strippedTarget = stripTags(menuTarget);
		String lower = strippedTarget.toLowerCase();

		if (!lower.contains("teleport"))
		{
			return null;
		}

		// "Teleport to house" tablets go TO safety — never block them.
		if (lower.contains("teleport to house"))
		{
			return null;
		}

		// Redirected house tablets (e.g. "Rimmington teleport", "Watchtower teleport") are intentionally
		// NOT excluded here — they teleport AWAY from the current location, not to the house interior.
		return new TeleportTarget(TeleportCategory.TABLET, strippedTarget, null);
	}

	// -------------------------------------------------------------------------
	// TASK-07: POH portals and exit (STUB — requires in-game verification)
	// -------------------------------------------------------------------------

	/**
	 * STUB — POH portal and exit classification requires in-game verification.
	 *
	 * What needs to be verified on a live client:
	 *
	 * 1. POH PORTAL OBJECTS
	 *    - Object IDs of the portal frames inside a player-owned house.
	 *      These vary by portal destination and possibly by construction level/style.
	 *      Walk up to each portal and use the "Examine" option; note the object ID from
	 *      the "Object ID" developer plugin overlay, or log it from onGameObjectSpawned.
	 *    - Exact menu option text for entering a portal (e.g. "Enter", the destination name
	 *      like "Varrock", or "Kharyrll"). Right-click the portal and log event.getMenuOption()
	 *      and event.getMenuTarget() from onMenuOptionClicked.
	 *
	 * 2. POH EXIT PORTAL
	 *    - Object ID of the exit portal (the large portal near the house entrance/exit).
	 *    - Exact menu option — likely "Enter" or "Exit" on the exit portal object.
	 *    - Whether the target string contains any identifiable keyword like "portal" or
	 *      the house owner's name.
	 *
	 * 3. HOTSPOT / LAYOUT DIFFERENCES
	 *    - Verify whether the object IDs differ between house layouts or room styles.
	 *    - Verify what happens with a Nexus (the Jewellery Box / Portal Nexus) — it may
	 *      use different menu actions entirely (e.g. CC_OP widget interaction).
	 *
	 * Until these are confirmed, this method always returns null.
	 */
	private TeleportTarget classifyPoh(String menuOption, String menuTarget, int itemId)
	{
		// TODO: Implement once object IDs and menu text are verified in-game.
		// Log what we see to assist with verification during testing.
		log.debug("[POH stub] option='{}' target='{}' itemId={}", menuOption, stripTags(menuTarget), itemId);

		return null;
	}

	// -------------------------------------------------------------------------
	// Utility
	// -------------------------------------------------------------------------

	/**
	 * Strips RuneLite color/formatting tags (e.g. {@code <col=ff9040>}, {@code </col>})
	 * from a raw menu string. Uses the RuneLite {@link Text} utility for correctness.
	 */
	private static String stripTags(String raw)
	{
		return Text.removeTags(raw);
	}
}
