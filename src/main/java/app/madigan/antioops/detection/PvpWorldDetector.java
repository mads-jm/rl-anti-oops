package app.madigan.antioops.detection;

import java.util.EnumSet;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.WorldType;

@Singleton
public class PvpWorldDetector
{
	private final Client client;

	private boolean pvpWorld = false;
	private boolean highRiskWorld = false;

	@Inject
	public PvpWorldDetector(Client client)
	{
		this.client = client;
	}

	public void updateWorldType()
	{
		EnumSet<WorldType> types = client.getWorldType();
		pvpWorld = types.contains(WorldType.PVP);
		highRiskWorld = types.contains(WorldType.HIGH_RISK);
	}

	public boolean isPvpWorld(boolean includeHighRisk)
	{
		return pvpWorld || (includeHighRisk && highRiskWorld);
	}

	public void clear()
	{
		pvpWorld = false;
		highRiskWorld = false;
	}
}
