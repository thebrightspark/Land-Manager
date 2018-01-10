package brightspark.landmanager.gui;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.Position;
import brightspark.landmanager.item.ItemAdmin;
import net.minecraft.client.Minecraft;
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
    private static final ResourceLocation bgImage = new ResourceLocation(LandManager.MOD_ID, "textures/gui/gui_create_area.png");
    private static int xSize = 113;
    private static int ySize = 35;
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

        nameInput = new GuiTextField(0, fontRenderer, guiLeft + 5, guiTop + 5, xSize - 10, fontRenderer.FONT_HEIGHT + 2);
        nameInput.setFocused(true);

        extendCheck = addButton(new GuiCheckBox(1, guiLeft + 5, guiTop + 20, "Min/Max Y", false));

        String text = "Confirm";
        addButton(new GuiButton(2, guiLeft + 68, guiTop + 20, 40, fontRenderer.FONT_HEIGHT + 2, text)
        {
            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
            {
                mc.getTextureManager().bindTexture(bgImage);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                drawTexturedModalRect(x, y, 0, ySize + (hovered ? 11 : 0), width, height);
                drawCenteredString(mc.fontRenderer, text, x + width / 2, y + 2, 14737632);
            }
        });
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
        if(button.id == 2)
        {
            //TODO: When confirm button clicked
            //Send packet to server to add new area
            //Don't close window - let the return packet do it if it was successful
        }
        super.actionPerformed(button);
    }
}
