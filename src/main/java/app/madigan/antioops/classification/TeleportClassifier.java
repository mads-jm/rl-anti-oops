package app.madigan.antioops.classification;

import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.util.Text;

/**
 * Classifies menu click events to determine if they represent a teleport
 * that should be intercepted on PvP worlds.
 */
@Slf4j
@Singleton
public class TeleportClassifier
{
	// ---------- POH object IDs (confirmed in-game) ----------
	// TODO : Fill in missing entries (see ids.md)
	private static final Map<Integer, String> POH_PORTAL_IDS = Map.ofEntries(
		Map.entry(13615, "Varrock"),
		Map.entry(13617, "Falador"),
		Map.entry(13619, "Ardougne"),
		Map.entry(29339, "Lunar Isle"),
		Map.entry(29342, "Waterbirth Island"),
		Map.entry(37586, "Salve Graveyard"),
		Map.entry(37588, "West Ardougne"),
		Map.entry(37591, "Barrows"),
		Map.entry(50713, "Civitas illa Fortis")
	);

	private static final int EXIT_PORTAL_ID = 4525;
	private static final int PORTAL_NEXUS_ID = 33408;

	private static final Set<String> NEXUS_NON_TELEPORT_OPTIONS = Set.of(
		"configuration",
		"examine"
	);

	// ---------- Jewelry item name substrings (case-insensitive match against stripped target) ----------
	// Items using standard "Rub" / "Teleport" / destination menu options
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
		"xeric's talisman",
		// Standard-option items added for coverage (Rub / Teleport)
		"ring of the elements",
		"ring of shadows",
		"pharaoh's sceptre",
		"camulet",
		"ectophial",
		"chronicle",
		"amulet of the eye",
		"pendant of ates",
		"giantsoul amulet",
		"cowbell amulet",
		"explorer's ring",
		"wilderness sword"
	);

	// ---------- Charged items with non-standard menu options ----------
	// Maps item name substring → set of menu options that are teleports for that item.
	// Matched case-insensitively against stripped target and option.
	private static final Map<String, Set<String>> CHARGED_ITEM_TELEPORT_OPTIONS = Map.ofEntries(
		// Unique-option items
		Map.entry("royal seed pod", Set.of("commune")),
		Map.entry("enchanted lyre", Set.of("play")),
		Map.entry("skull sceptre", Set.of("invoke")),
		Map.entry("kharedst's memoirs", Set.of("reminisce")),
		Map.entry("quetzal whistle", Set.of("signal")),
		// Destination-name option items
		// "teleport crystal" substring covers both standard and eternal variants
		Map.entry("teleport crystal", Set.of("lletya", "prifddinas")),
		Map.entry("drakan's medallion", Set.of("ver sinhaza", "darkmeyer", "slepe")),
		// Achievement diary rewards
		Map.entry("ardougne cloak", Set.of("monastery teleport", "farm teleport")),
		Map.entry("desert amulet", Set.of("nardah", "kalphite cave")),
		Map.entry("rada's blessing", Set.of("kourend woodland", "mount karuulm")),
		// Consumable teleport items
		Map.entry("dorgesh-kaan sphere", Set.of("break")),
		Map.entry("stony basalt", Set.of("troll stronghold entrance", "troll stronghold roof")),
		Map.entry("icy basalt", Set.of("weiss"))
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

	@Inject
	public TeleportClassifier()
	{
	}

	/**
	 * Classifies a menu click event.
	 *
	 * @param menuOption the raw menu option string (e.g. "Cast", "Break", "Rub", "Varrock")
	 * @param menuTarget the raw menu target string — may contain RuneLite color tags
	 * @param itemId     the ID from the event — item ID for inventory actions, object ID for game objects
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

		result = classifyChargedItem(menuOption, menuTarget);
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

	private TeleportTarget classifySpellbook(String menuOption, String menuTarget)
	{
		if (!"Cast".equalsIgnoreCase(menuOption))
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

	private TeleportTarget classifyChargedItem(String menuOption, String menuTarget)
	{
		String optionLower = menuOption.trim().toLowerCase();
		String strippedTarget = stripTags(menuTarget).toLowerCase();

		for (Map.Entry<String, Set<String>> entry : CHARGED_ITEM_TELEPORT_OPTIONS.entrySet())
		{
			if (strippedTarget.contains(entry.getKey()) && entry.getValue().contains(optionLower))
			{
				return new TeleportTarget(TeleportCategory.CHARGED_ITEM, strippedTarget, menuOption);
			}
		}

		return null;
	}

	/**
	 * Classifies POH portal, exit portal, and Portal Nexus interactions.
	 *
	 * <p>Covers 9 confirmed portal destinations, the exit portal, and the Portal Nexus.
	 * Additional portal IDs can be added to {@code POH_PORTAL_IDS} as they are verified in-game.
	 *
	 * @param itemId for game object interactions, this is the object ID
	 */
	private TeleportTarget classifyPoh(String menuOption, String menuTarget, int itemId)
	{
		// Exit portal — sends the player out of the POH to wherever they entered from
		if (itemId == EXIT_PORTAL_ID)
		{
			return new TeleportTarget(TeleportCategory.POH_EXIT, "POH Exit Portal", "Previous location");
		}

		// Known destination portals
		String destination = POH_PORTAL_IDS.get(itemId);
		if (destination != null)
		{
			return new TeleportTarget(TeleportCategory.POH_PORTAL, destination + " portal", destination);
		}

		// Portal Nexus — block any teleport interaction, but allow configuration/examine
		if (itemId == PORTAL_NEXUS_ID)
		{
			String optionLower = menuOption.trim().toLowerCase();
			if (NEXUS_NON_TELEPORT_OPTIONS.contains(optionLower))
			{
				return null;
			}
			return new TeleportTarget(TeleportCategory.POH_PORTAL, "Portal Nexus", menuOption);
		}

		return null;
	}

	private static String stripTags(String raw)
	{
		return Text.removeTags(raw);
	}
}
