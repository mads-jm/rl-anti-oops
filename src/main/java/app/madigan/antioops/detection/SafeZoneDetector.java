package app.madigan.antioops.detection;

import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.gameval.VarbitID;

@Singleton
public class SafeZoneDetector
{
	private final Client client;

	@Inject
	public SafeZoneDetector(Client client)
	{
		this.client = client;
	}

	/**
	 * Returns true if the player is currently in a PvP safe zone.
	 *
	 * <p>Uses {@code VarbitID.PVP_AREA_CLIENT} (varbit 8121):
	 * <ul>
	 *   <li>0 = safe zone (banks, non-PvP areas)</li>
	 *   <li>1 = dangerous (PvP area active)</li>
	 * </ul>
	 *
	 * <p>POH exception: instanced regions read as dangerous (varbit = 1) despite
	 * being safe. {@link Client#isInInstancedRegion()} covers this case.
	 */
	public boolean isInSafeZone()
	{
		return client.getVarbitValue(VarbitID.PVP_AREA_CLIENT) == 0 || client.isInInstancedRegion();
	}

	/**
	 * Reset cached state on logout or world hop.
	 * No-op: varbit is read live on each call to {@link #isInSafeZone()}.
	 */
	public void clear()
	{
		// No cached state to clear — varbit is read live each call.
	}
}
