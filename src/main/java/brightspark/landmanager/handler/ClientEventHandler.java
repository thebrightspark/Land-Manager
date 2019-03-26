package brightspark.landmanager.handler;

import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.util.BoxRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;
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
    private static Map<String, Color> colourCache = new HashMap<>();

    public static void setRenderArea(String areaName)
    {
        renderAll = false;
        renderArea = areaName;
        colourCache.clear();
    }

    public static void toggleRenderAll()
    {
        renderAll = !renderArea.isEmpty() || !renderAll;
        renderArea = "";
        colourCache.clear();
        if(renderAll)
            mc.player.sendMessage(new TextComponentTranslation("message.areas.show"));
        else
            mc.player.sendMessage(new TextComponentTranslation("message.areas.hide"));
    }

    private static Color getColour(String areaName)
    {
        Color colour = colourCache.get(areaName);
        if(colour == null)
            colourCache.put(areaName, colour = new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 255));
        return colour;
    }

    @SubscribeEvent
    public static void areaRendering(RenderWorldLastEvent event)
    {
        if(!renderAll && renderArea.isEmpty())
            return;

        CapabilityAreas cap = mc.world.getCapability(LandManager.CAPABILITY_AREAS, null);
        if(cap == null)
            return;

        if(renderAll)
        {
            Set<Area> areas = cap.getNearbyAreas(mc.player.getPosition());
            areas.forEach(area -> BoxRenderer.renderBox(area, getColour(area.getName()), event.getPartialTicks()));
        }
        else if(!renderArea.isEmpty())
        {
            Area area = cap.getArea(renderArea);
            if(area != null)
                BoxRenderer.renderBox(area, getColour(area.getName()), event.getPartialTicks());
        }
    }
}
