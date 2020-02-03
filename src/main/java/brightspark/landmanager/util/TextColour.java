package brightspark.landmanager.util;

import net.minecraft.util.text.TextFormatting;

public enum TextColour {
	BLACK(TextFormatting.BLACK),
	DARK_BLUE(TextFormatting.DARK_BLUE),
	DARK_GREEN(TextFormatting.DARK_GREEN),
	DARK_AQUA(TextFormatting.DARK_AQUA),
	DARK_RED(TextFormatting.DARK_RED),
	DARK_PURPLE(TextFormatting.DARK_PURPLE),
	GOLD(TextFormatting.GOLD),
	GRAY(TextFormatting.GRAY),
	DARK_GRAY(TextFormatting.DARK_GRAY),
	BLUE(TextFormatting.BLUE),
	GREEN(TextFormatting.GREEN),
	AQUA(TextFormatting.AQUA),
	RED(TextFormatting.RED),
	LIGHT_PURPLE(TextFormatting.LIGHT_PURPLE),
	YELLOW(TextFormatting.YELLOW),
	WHITE(TextFormatting.WHITE);

	public final TextFormatting colour;

	TextColour(TextFormatting colour) {
		this.colour = colour;
	}
}
