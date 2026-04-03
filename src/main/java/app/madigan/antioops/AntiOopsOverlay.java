package app.madigan.antioops;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

@Singleton
public class AntiOopsOverlay extends Overlay
{
	private static final Duration DISPLAY_DURATION = Duration.ofSeconds(2);
	private static final int Z_OFFSET = 150;

	private final Client client;
	private final AntiOopsConfig config;

	private Instant shownAt = null;

	@Inject
	public AntiOopsOverlay(Client client, AntiOopsConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	public void trigger()
	{
		shownAt = Instant.now();
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (shownAt == null)
		{
			return null;
		}

		if (Duration.between(shownAt, Instant.now()).compareTo(DISPLAY_DURATION) > 0)
		{
			shownAt = null;
			return null;
		}

		Player local = client.getLocalPlayer();
		if (local == null)
		{
			return null;
		}

		String text = config.warningMessage();
		if (text == null || text.isEmpty())
		{
			return null;
		}

		Font boldFont = graphics.getFont().deriveFont(Font.BOLD);
		graphics.setFont(boldFont);

		net.runelite.api.Point point = local.getCanvasTextLocation(graphics, text, Z_OFFSET);
		if (point == null)
		{
			return null;
		}

		OverlayUtil.renderTextLocation(graphics, point, text, Color.RED);
		return null;
	}
}
