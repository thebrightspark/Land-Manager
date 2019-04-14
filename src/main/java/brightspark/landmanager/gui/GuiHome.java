package brightspark.landmanager.gui;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.UUID;

public class GuiHome extends LMGui
{
	private Area area = null;
	private GuiTextField input;

	public GuiHome(World world, BlockPos pos)
	{
		super("gui_home", 162, 144);
		CapabilityAreas cap = world.getCapability(LandManager.CAPABILITY_AREAS, null);
		if(cap != null)
			area = cap.intersectingArea(pos);
	}

	@Override
	public void initGui()
	{
		super.initGui();

		input = new GuiTextField(0, fontRenderer, guiLeft + 99, guiTop + 15, 56, 10);
		input.setEnableBackgroundDrawing(false);

		for(int i = 0; i < 7; i++)
			addButton(new ListButton(7, 16 + (i * 12)));

		addButton(new ArrowButton(97, 29, true));
		addButton(new ArrowButton(97, 52, false));

		addButton(new ActionButton(111, 29, "Add", 0));
		addButton(new ActionButton(111, 41, "Kick", 1));
		addButton(new ActionButton(111, 53, "Pass", 2));

		for(int i = 0; i < 5; i++)
		{
			ToggleButton b = new ToggleButton(108, 70 + (i * 14));
			b.locked = mc.world.rand.nextBoolean();
			b.isOn = mc.world.rand.nextBoolean();
			addButton(b);
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	protected void drawText()
	{
		input.drawTextBox();

		int colour = 4210752;
		drawString(area.getName(), 6, 5, colour, false);

		drawString("Show Boundaries", 7, 72, colour, false);
		drawString("Interact Permission", 7, 86, colour, false);
		drawString("Passive Spawning", 7, 100, colour, false);
		drawString("Hostile Spawning", 7, 114, colour, false);
		drawString("Explosions", 7, 128, colour, false);
	}

	@Override
	public void updateScreen()
	{
		input.updateCursorCounter();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException
	{
		super.keyTyped(typedChar, keyCode);
		if(input.isFocused())
			input.textboxKeyTyped(typedChar, keyCode);
	}

	private class ListButton extends LMButton
	{
		public boolean selected = false;
		private UUID playerUuid = null;

		public ListButton(int x, int y)
		{
			super(x, y, 87, 11, 87, 144, null);
			textOffset = 1;
		}

		public void setPlayer(EntityPlayer player)
		{
			if(player == null)
			{
				displayString = null;
				playerUuid = null;
				enabled = false;
			}
			else
			{
				displayString = player.getDisplayNameString();
				playerUuid = player.getUniqueID();
				enabled = true;
			}
		}

		public UUID getPlayerUuid()
		{
			return playerUuid;
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			if(!visible || !enabled) return;
			this.hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			//Draw button
			if(selected)
			{
				mc.getTextureManager().bindTexture(image);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				drawTexturedModalRect(x, y, getIconX(), getIconY(), width, height);
			}
		}
	}

	private class ArrowButton extends LMButton
	{
		private boolean isUp;

		public ArrowButton(int x, int y, boolean isUp)
		{
			super(x, y, 10, 13, 162, 84, null);
			this.isUp = isUp;
		}

		public boolean isUp()
		{
			return isUp;
		}

		@Override
		protected int getIconY()
		{
			return isUp ? iconY : iconY + height;
		}
	}

	private class ActionButton extends LMButton
	{
		private final byte type;

		public ActionButton(int x, int y, String buttonText, int type)
		{
			super(x, y, 45, 12, 162, 0, buttonText);
			this.type = (byte) type;
			textOffset = 12;
		}

		@Override
		protected int getIconY()
		{
			return iconY + (type * height);
		}
	}

	private class ToggleButton extends LMButton
	{
		public boolean locked = false;
		public Boolean isOn = null;

		public ToggleButton(int x, int y)
		{
			super(x, y, 12, 12, 162, 36, null);
		}

		@Override
		protected int getIconY()
		{
			int y = iconY;
			if(locked)
				y += height * 2;
			if(isOn)
				y += height;
			return y;
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			if(isOn != null)
				super.drawButton(mc, mouseX, mouseY, partialTicks);
		}
	}
}
