package app.madigan.antioops.interception;

import app.madigan.antioops.AntiOopsConfig;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.util.Text;

@Singleton
public class InterceptionManager
{
	private final AntiOopsConfig config;
	private final Client client;

	private PendingConfirmation pending = null;

	@Inject
	public InterceptionManager(AntiOopsConfig config, Client client)
	{
		this.config = config;
		this.client = client;
	}

	public boolean shouldBlock(String actionKey)
	{
		long now = System.nanoTime();
		int currentTick = client.getTickCount();

		if (pending != null
			&& pending.actionKey.equals(actionKey)
			&& now < pending.expiresAtNanos
			&& currentTick > pending.blockedOnTick)
		{
			pending = null;
			return false;
		}

		long timeoutNanos = 1_000_000_000L * config.confirmationTimeoutSeconds();
		pending = new PendingConfirmation(actionKey, now + timeoutNanos, currentTick);
		return true;
	}

	public void clear()
	{
		pending = null;
	}

	public static String buildActionKey(String option, String target)
	{
		return option + "|" + Text.removeTags(target);
	}

	private static class PendingConfirmation
	{
		final String actionKey;
		final long expiresAtNanos;
		final int blockedOnTick;

		PendingConfirmation(String actionKey, long expiresAtNanos, int blockedOnTick)
		{
			this.actionKey = actionKey;
			this.expiresAtNanos = expiresAtNanos;
			this.blockedOnTick = blockedOnTick;
		}
	}
}
