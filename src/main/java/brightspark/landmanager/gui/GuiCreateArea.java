package brightspark.landmanager.gui;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.Position;
import brightspark.landmanager.item.ItemAdmin;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.client.config.GuiCheckBox;

import java.io.IOException;

public class GuiCreateArea extends GuiScreen
{
    private static final ResourceLocation bgImage = new ResourceLocation(LandManager.MOD_ID, "textures/gui/createAreaGui.png");
    private static int xSize = 100;
    private static int ySize = 50;
    private int guiLeft, guiTop;

    private GuiCheckBox extendCheck;
    private GuiTextField nameInput;

    private BlockPos pos1, pos2;
    private int dimId;

    public GuiCreateArea(EntityPlayer player, BlockPos pos2)
    {
        Position position = ItemAdmin.getPos(player.getHeldItemMainhand());
        if(position == null)
            mc.player.closeScreen();
        else
        {
            this.dimId = position.dimensionId;
            this.pos1 = position.position;
            this.pos2 = pos2;
        }
    }

    public void clearTextField()
    {
        nameInput.setText("");
    }

    @Override
    public void initGui()
    {
        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize) / 2;

        addButton(new GuiButton(0, 100, 100, 30, 10, "Confirm"));
        extendCheck = addButton(new GuiCheckBox(1, 100, 150, "Extend to min/max height", false));
        nameInput = new GuiTextField(2, fontRenderer, 100, 60, 100, 50);
        nameInput.setFocused(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        //Draw GUI background
        mc.getTextureManager().bindTexture(bgImage);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        super.drawScreen(mouseX, mouseY, partialTicks);

        nameInput.drawTextBox();

        //TODO: Draw text
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        if(button.id == 0)
        {
            //TODO: When confirm button clicked
            //Send packet to server to add new area
            //Don't close window - let the return packet do it if it was successful
        }
        super.actionPerformed(button);
    }
}
