package brightspark.landmanager.handler;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.Area;
import brightspark.landmanager.data.CapabilityAreas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID, value = Side.CLIENT)
public class ClientEventHandler
{
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random rand = new Random();
    private static boolean renderAll = false;
    private static String renderArea = "";
    private static Map<Area, Color> colourCache = new HashMap<>();

    public static void setRenderArea(String areaName)
    {
        renderAll = false;
        renderArea = areaName;
        colourCache.clear();
    }

    public static boolean toggleRenderAll()
    {
        renderAll = !renderArea.isEmpty() || !renderAll;
        renderArea = "";
        colourCache.clear();
        return renderAll;
    }

    private static Color getColour(Area area)
    {
        Color colour = colourCache.get(area);
        if(colour == null)
            colourCache.put(area, colour = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 255));
        return colour;
    }

    private static void renderBox(Area area, double partialTicks)
    {
        //Get player's actual position
        EntityPlayerSP player = mc.player;
        double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double y = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        //Render the box
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(5f);
        GlStateManager.disableTexture2D();
        GlStateManager.translate(-x, -y, -z);
        float[] rgb = getColour(area).getRGBColorComponents(null);
        AxisAlignedBB box = new AxisAlignedBB(area.getMinPos(), area.getMaxPos()).grow(0.001d);
        RenderGlobal.renderFilledBox(box, rgb[0], rgb[1], rgb[2], 0.2f);
        RenderGlobal.drawSelectionBoundingBox(box, rgb[0], rgb[1], rgb[2], 0.4f);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void areaRendering(RenderWorldLastEvent event)
    {
        if(!renderAll && renderArea.isEmpty()) return;

        CapabilityAreas cap = mc.world.getCapability(LandManager.CAPABILITY_AREAS, null);
        if(cap == null) return;

        if(renderAll)
        {
            Set<Area> areas = cap.getNearbyAreas(mc.player.getPosition());
            areas.forEach(area -> renderBox(area, event.getPartialTicks()));
        }
        else if(!renderArea.isEmpty())
        {
            Area area = cap.getArea(renderArea);
            if(area != null) renderBox(area, event.getPartialTicks());
        }
    }
}
