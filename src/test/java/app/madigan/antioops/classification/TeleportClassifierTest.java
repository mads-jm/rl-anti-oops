package app.madigan.antioops.classification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class TeleportClassifierTest
{
	private TeleportClassifier classifier;

	@Before
	public void setUp()
	{
		classifier = new TeleportClassifier(null);
	}

	// ---- Spellbook ----

	@Test
	public void classifySpellbookTeleport()
	{
		TeleportTarget target = classifier.classify("Cast", "<col=00ff00>Varrock Teleport</col>", -1);
		assertNotNull(target);
		assertEquals(TeleportCategory.SPELLBOOK, target.getCategory());
	}

	@Test
	public void classifySpellbookTeleGroup()
	{
		TeleportTarget target = classifier.classify("Cast", "<col=00ff00>Varrock Tele Group</col>", -1);
		assertNotNull(target);
		assertEquals(TeleportCategory.SPELLBOOK, target.getCategory());
	}

	@Test
	public void spellbookTeleportToHouseExcluded()
	{
		TeleportTarget target = classifier.classify("Cast", "<col=00ff00>Teleport to House</col>", -1);
		assertNull(target);
	}

	@Test
	public void spellbookNonCastOptionReturnsNull()
	{
		TeleportTarget target = classifier.classify("Examine", "Varrock Teleport", -1);
		assertNull(target);
	}

	@Test
	public void spellbookNonTeleportSpellReturnsNull()
	{
		TeleportTarget target = classifier.classify("Cast", "High Level Alchemy", -1);
		assertNull(target);
	}

	// ---- Jewelry ----

	@Test
	public void classifyJewelryRub()
	{
		TeleportTarget target = classifier.classify("Rub", "<col=ff9040>Ring of dueling(8)</col>", -1);
		assertNotNull(target);
		assertEquals(TeleportCategory.JEWELRY, target.getCategory());
	}

	@Test
	public void classifyJewelryDestinationOption()
	{
		TeleportTarget target = classifier.classify("Duel Arena", "<col=ff9040>Ring of dueling(8)</col>", -1);
		assertNotNull(target);
		assertEquals(TeleportCategory.JEWELRY, target.getCategory());
	}

	@Test
	public void jewelryWearReturnsNull()
	{
		TeleportTarget target = classifier.classify("Wear", "<col=ff9040>Ring of dueling(8)</col>", -1);
		assertNull(target);
	}

	@Test
	public void jewelryDropReturnsNull()
	{
		TeleportTarget target = classifier.classify("Drop", "<col=ff9040>Amulet of glory(6)</col>", -1);
		assertNull(target);
	}

	@Test
	public void jewelryExamineReturnsNull()
	{
		TeleportTarget target = classifier.classify("Examine", "<col=ff9040>Games necklace(8)</col>", -1);
		assertNull(target);
	}

	@Test
	public void unknownItemNotClassifiedAsJewelry()
	{
		TeleportTarget target = classifier.classify("Rub", "Dragon scimitar", -1);
		assertNull(target);
	}

	// ---- Tablets ----

	@Test
	public void classifyTabletBreak()
	{
		TeleportTarget target = classifier.classify("Break", "<col=ff9040>Varrock teleport</col>", -1);
		assertNotNull(target);
		assertEquals(TeleportCategory.TABLET, target.getCategory());
	}

	@Test
	public void tabletTeleportToHouseExcluded()
	{
		TeleportTarget target = classifier.classify("Break", "<col=ff9040>Teleport to house</col>", -1);
		assertNull(target);
	}

	@Test
	public void tabletNonBreakOptionReturnsNull()
	{
		TeleportTarget target = classifier.classify("Use", "Varrock teleport", -1);
		assertNull(target);
	}

	@Test
	public void tabletNonTeleportReturnsNull()
	{
		TeleportTarget target = classifier.classify("Break", "Some random item", -1);
		assertNull(target);
	}

	// ---- POH ----

	@Test
	public void classifyPohPortalById()
	{
		TeleportTarget target = classifier.classify("Enter", "Portal", 13615);
		assertNotNull(target);
		assertEquals(TeleportCategory.POH_PORTAL, target.getCategory());
		assertEquals("Varrock", target.getDestination());
	}

	@Test
	public void classifyPohExitPortal()
	{
		TeleportTarget target = classifier.classify("Enter", "Portal", 4525);
		assertNotNull(target);
		assertEquals(TeleportCategory.POH_EXIT, target.getCategory());
	}

	@Test
	public void classifyPortalNexusTeleport()
	{
		TeleportTarget target = classifier.classify("Varrock", "Portal Nexus", 33408);
		assertNotNull(target);
		assertEquals(TeleportCategory.POH_PORTAL, target.getCategory());
	}

	@Test
	public void portalNexusConfigurationAllowed()
	{
		TeleportTarget target = classifier.classify("Configuration", "Portal Nexus", 33408);
		assertNull(target);
	}

	@Test
	public void portalNexusExamineAllowed()
	{
		TeleportTarget target = classifier.classify("Examine", "Portal Nexus", 33408);
		assertNull(target);
	}

	@Test
	public void unknownObjectIdReturnsNull()
	{
		TeleportTarget target = classifier.classify("Enter", "Some object", 99999);
		assertNull(target);
	}

	// ---- Edge cases ----

	@Test
	public void nullMenuOptionReturnsNull()
	{
		TeleportTarget target = classifier.classify(null, "Varrock Teleport", -1);
		assertNull(target);
	}

	@Test
	public void nullMenuTargetReturnsNull()
	{
		TeleportTarget target = classifier.classify("Cast", null, -1);
		assertNull(target);
	}

	@Test
	public void redirectedHouseTabletIsBlocked()
	{
		// "Rimmington teleport" is a redirected house tablet — teleports AWAY, should be blocked
		TeleportTarget target = classifier.classify("Break", "Rimmington teleport", -1);
		assertNotNull(target);
		assertEquals(TeleportCategory.TABLET, target.getCategory());
	}
}
