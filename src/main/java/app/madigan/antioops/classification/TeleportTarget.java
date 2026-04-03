package app.madigan.antioops.classification;

import lombok.Value;

@Value
public class TeleportTarget
{
	TeleportCategory category;
	String name;
	String destination;
}
