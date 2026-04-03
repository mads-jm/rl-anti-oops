package app.madigan.antioops.detection;

import app.madigan.antioops.AntiOopsConfig;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.WorldView;
import net.runelite.api.gameval.VarbitID;

@Slf4j
@Singleton
public class SafeZoneDetector
{
	private static final int POH_REGION_ID = 8046;

	private final Client client;
	private final AntiOopsConfig config;

	@Inject
	public SafeZoneDetector(Client client, AntiOopsConfig config)
	{
		this.client = client;
		this.config = config;
	}

	/**
	 * Returns true if the player is in a protected area where outbound
	 * teleports should require confirmation.
	 *
	 * <p>This includes both game-designated safe zones (varbit = 0) and
	 * private instances like POH and custom user-added regions where the
	 * player is practically safe despite the varbit reading as dangerous.
	 */
	public boolean isInSafeZone()
	{
		return client.getVarbitValue(VarbitID.PVP_AREA_CLIENT) == 0 || isInPrivateInstance();
	}

	/**
	 * Returns true if the player is in a private instance (POH, boss room,
	 * or custom region) that should be treated as safe even though the PvP
	 * varbit may read as dangerous.
	 */
	private boolean isInPrivateInstance()
	{
		WorldView wv = client.getTopLevelWorldView();
		if (wv == null || !wv.isInstance())
		{
			return false;
		}

		Set<Integer> protectedRegions = getProtectedRegions();

		for (int region : wv.getMapRegions())
		{
			if (protectedRegions.contains(region))
			{
				return true;
			}
		}

		return false;
	}

	private Set<Integer> getProtectedRegions()
	{
		Set<Integer> regions = new HashSet<>();
		regions.add(POH_REGION_ID);

		String custom = config.customProtectedRegions().trim();
		if (!custom.isEmpty())
		{
			for (String s : custom.split(","))
			{
				try
				{
					regions.add(Integer.parseInt(s.trim()));
				}
				catch (NumberFormatException e)
				{
					log.warn("Invalid region ID in customProtectedRegions: '{}'", s.trim());
				}
			}
		}

		return regions;
	}
}
