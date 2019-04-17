package brightspark.landmanager.gui;

import brightspark.landmanager.LandManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LMGui extends GuiScreen
{
	protected final ResourceLocation image;
	protected final int xSize;
	protected final int ySize;
	protected int guiLeft, guiTop;

	public LMGui(String image, int xSize, int ySize)
	{
		this.image = new ResourceLocation(LandManager.MOD_ID, "textures/gui/" + image + ".png");
		this.xSize = xSize;
		this.ySize = ySize;
	}

	@Override
	protected <T extends GuiButton> T addButton(T buttonIn)
	{
		buttonIn.x += guiLeft;
		buttonIn.y += guiTop;
		return super.addButton(buttonIn);
	}

	@Override
	public void initGui()
	{
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		//Draw GUI background
		mc.getTextureManager().bindTexture(image);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		super.drawScreen(mouseX, mouseY, partialTicks);

		drawText();

		List<String> tooltip = new ArrayList<>();
		drawTooltips(tooltip, mouseX, mouseY);
		if(!tooltip.isEmpty())
			drawHoveringText(tooltip, mouseX, mouseY);
	}

	protected void drawText() {}

	protected void drawTooltips(List<String> tooltip, int mouseX, int mouseY) {}

	protected void drawString(String text, int x, int y, int colour, boolean shadow)
	{
		fontRenderer.drawString(text, x, y, colour, shadow);
	}

	protected void drawLangString(String langKey, int x, int y, int colour, boolean shadow)
	{
		drawString(I18n.format(langKey), x, y, colour, shadow);
	}

	protected void drawStringWithMaxWidth(String text, int x, int y, int maxWidth, int colour, boolean shadow)
	{
		int textWidth = fontRenderer.getStringWidth(text);
		int ellipsisWidth = fontRenderer.getStringWidth("...");
		if(textWidth > maxWidth - 6 && textWidth > ellipsisWidth)
			text = fontRenderer.trimStringToWidth(text, maxWidth - 6 - ellipsisWidth).trim() + "...";
		drawString(text, x, y, colour, shadow);
	}

	protected class LMButton extends GuiButton
	{
		protected final int iconX, iconY;
		protected boolean hasIcon = true;
		protected boolean drawWhenDisabled = false;
		protected int textOffset = 0;

		public LMButton(int x, int y, int width, int height, int iconX, int iconY, String buttonText)
		{
			super(buttonList.size(), x, y, width, height, buttonText);
			this.iconX = iconX;
			this.iconY = iconY;
		}

		protected int getTextColour()
		{
			return enabled ? 14737632 : 10526880;
		}

		protected int getIconX()
		{
			return iconX;
		}

		protected int getIconY()
		{
			return iconY;
		}

		protected void drawText()
		{
			drawString(fontRenderer, displayString, x + textOffset, y + (height - 8) / 2, getTextColour());
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			if(!visible) return;
			//Draw button
			if(!drawWhenDisabled && !enabled)
				return;
			this.hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			if(hasIcon)
			{
				mc.getTextureManager().bindTexture(image);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				drawTexturedModalRect(x, y, getIconX(), getIconY(), width, height);
			}
			if(StringUtils.isNotBlank(displayString))
				drawText();
		}
	}
}
