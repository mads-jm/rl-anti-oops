package app.madigan.antioops;

import app.madigan.antioops.detection.PvpWorldDetector;
import app.madigan.antioops.detection.SafeZoneDetector;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

@Singleton
public class AntiOopsStatusOverlay extends OverlayPanel
{
	private final AntiOopsConfig config;
	private final PvpWorldDetector pvpWorldDetector;
	private final SafeZoneDetector safeZoneDetector;

	@Inject
	public AntiOopsStatusOverlay(AntiOopsConfig config, PvpWorldDetector pvpWorldDetector, SafeZoneDetector safeZoneDetector)
	{
		this.config = config;
		this.pvpWorldDetector = pvpWorldDetector;
		this.safeZoneDetector = safeZoneDetector;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.statusOverlay())
		{
			return null;
		}

		if (!pvpWorldDetector.isPvpWorld(config.includeHighRisk()))
		{
			return null;
		}

		if (config.pvpWorldReminder())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("PvP World")
				.leftColor(Color.RED)
				.build());
		}

		boolean safe = safeZoneDetector.isInSafeZone();

		panelComponent.getChildren().add(LineComponent.builder()
			.left("TP Protection:")
			.right(safe ? "ACTIVE" : "OFF")
			.rightColor(safe ? Color.GREEN : Color.RED)
			.build());

		return super.render(graphics);
	}
}
