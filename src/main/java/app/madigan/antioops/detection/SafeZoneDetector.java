package app.madigan.antioops.detection;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.WorldView;
import net.runelite.api.gameval.VarbitID;

@Singleton
public class SafeZoneDetector
{
	private static final int POH_REGION_ID = 8046;

	private final Client client;

	@Inject
	public SafeZoneDetector(Client client)
	{
		this.client = client;
	}

	/**
	 * Returns true if the player is in a protected area where outbound
	 * teleports should require confirmation.
	 *
	 * <p>This includes both game-designated safe zones (varbit = 0) and
	 * private instances like POH and boss rooms where the player is
	 * practically safe despite the varbit reading as dangerous.
	 */
	public boolean isInSafeZone()
	{
		return client.getVarbitValue(VarbitID.PVP_AREA_CLIENT) == 0 || isInPrivateInstance();
	}

	/**
	 * Returns true if the player is in a private instance (POH, boss room)
	 * that should be treated as safe even though the PvP varbit may read
	 * as dangerous.
	 */
	private boolean isInPrivateInstance()
	{
		WorldView wv = client.getTopLevelWorldView();
		if (wv == null || !wv.isInstance())
		{
			return false;
		}

		for (int region : wv.getMapRegions())
		{
			if (region == POH_REGION_ID)
			{
				return true;
			}
		}

		return false;
	}
}
