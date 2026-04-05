package app.madigan.antioops;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup("antioops")
public interface AntiOopsConfig extends Config
{
	@ConfigItem(
		keyName = "notice",
		name = "Notice",
		description = "Type ::aoissue in-game to report a missed teleport",
		position = -1
	)
	default String notice()
	{
		return "Early release — some teleports may slip through. Type ::aoissue to report.";
	}

	@ConfigSection(
		name = "Protection Toggles",
		description = "Which teleport categories to intercept",
		position = 0
	)
	String protectionToggles = "protectionToggles";

	@ConfigSection(
		name = "Custom Zones & Exceptions",
		description = "Custom protected regions and allowed teleports",
		position = 5
	)
	String customSection = "customSection";

	@ConfigSection(
		name = "Behavior",
		description = "How the plugin responds to blocked teleports",
		position = 10
	)
	String behavior = "behavior";

	@ConfigItem(
		keyName = "protectJewelry",
		name = "Protect Jewelry",
		description = "Block jewelry teleports (ring of dueling, games necklace, glory, etc.)",
		position = 1,
		section = protectionToggles
	)
	default boolean protectJewelry()
	{
		return true;
	}

	@ConfigItem(
		keyName = "protectSpellbook",
		name = "Protect Spellbook",
		description = "Block spellbook teleport casts",
		position = 2,
		section = protectionToggles
	)
	default boolean protectSpellbook()
	{
		return true;
	}

	@ConfigItem(
		keyName = "protectPoh",
		name = "Protect POH",
		description = "Block POH portal and house exit actions",
		position = 3,
		section = protectionToggles
	)
	default boolean protectPoh()
	{
		return true;
	}

	@ConfigItem(
		keyName = "protectTablets",
		name = "Protect Tablets",
		description = "Block teleport tablet usage",
		position = 4,
		section = protectionToggles
	)
	default boolean protectTablets()
	{
		return true;
	}

	@ConfigItem(
		keyName = "protectChargedItems",
		name = "Protect Charged Items",
		description = "Block teleports from charged items (lyre, seed pod, sceptres, diary rewards, etc.)",
		position = 5,
		section = protectionToggles
	)
	default boolean protectChargedItems()
	{
		return true;
	}

	@ConfigItem(
		keyName = "customProtectedRegions",
		name = "Custom Protected Regions",
		description = "Region IDs treated as safe. Type ::aoprot in-game to add your current region. HIGHLY recommend doing outside of a PVP world :)",
		position = 6,
		section = customSection
	)
	default String customProtectedRegions()
	{
		return "";
	}

	@ConfigItem(
		keyName = "allowedTeleports",
		name = "Allowed Teleports",
		description = "Teleports that skip confirmation. Type ::aoallow after a blocked teleport to toggle.",
		position = 7,
		section = customSection
	)
	default String allowedTeleports()
	{
		return "";
	}

	@Range(min = 1, max = 10)
	@ConfigItem(
		keyName = "confirmationTimeoutSeconds",
		name = "Confirmation Timeout",
		description = "Seconds to re-click to confirm a blocked teleport",
		position = 11,
		section = behavior
	)
	default int confirmationTimeoutSeconds()
	{
		return 3;
	}

	@ConfigItem(
		keyName = "chatWarnings",
		name = "Chat Warnings",
		description = "Show warning messages in chat when a teleport is blocked",
		position = 12,
		section = behavior
	)
	default boolean chatWarnings()
	{
		return true;
	}

	@ConfigItem(
		keyName = "includeHighRisk",
		name = "Include High-Risk Worlds",
		description = "Also protect on high-risk worlds (not just standard PvP worlds)",
		position = 13,
		section = behavior
	)
	default boolean includeHighRisk()
	{
		return true;
	}

	@ConfigItem(
		keyName = "overheadWarning",
		name = "Overhead Warning",
		description = "Show bold overhead text when a teleport is blocked",
		position = 14,
		section = behavior
	)
	default boolean overheadWarning()
	{
		return true;
	}

	@ConfigItem(
		keyName = "warningMessage",
		name = "Warning Message",
		description = "Text shown in overhead warning",
		position = 15,
		section = behavior
	)
	default String warningMessage()
	{
		return "BLOCKED!";
	}

	@ConfigItem(
		keyName = "statusOverlay",
		name = "Status Overlay",
		description = "Show a small overlay with teleport protection status on PvP worlds",
		position = 16,
		section = behavior
	)
	default boolean statusOverlay()
	{
		return true;
	}
}
