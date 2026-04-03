package app.madigan.antioops;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class AntiOopsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(AntiOopsPlugin.class);
		RuneLite.main(args);
	}
}
