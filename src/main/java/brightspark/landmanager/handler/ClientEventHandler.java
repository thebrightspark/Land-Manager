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
import java.util.*;

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID, value = Side.CLIENT)
public class ClientEventHandler {
	private static final Minecraft mc = Minecraft.getMinecraft();
	private static final Random rand = new Random();
	private static boolean renderAll = false;
	private static Set<String> areasToRender = new HashSet<>();
	private static Map<String, Color> colourCache = new HashMap<>();

	public static boolean isAreaBeingRendered(String areaName) {
		return areasToRender.contains(areaName);
	}

	//Used by command
	public static void setRenderArea(String areaName) {
		renderAll = false;
		areasToRender.clear();
		areasToRender.add(areaName);
		colourCache.clear();
	}

	//Used by GUI
	public static void setRenderArea(String areaName, boolean show) {
		renderAll = false;
		if (show)
			areasToRender.add(areaName);
		else {
			areasToRender.remove(areaName);
			colourCache.remove(areaName);
		}
	}

	public static void toggleRenderAll() {
		renderAll = !areasToRender.isEmpty() || !renderAll;
		areasToRender.clear();
		colourCache.clear();
		if (renderAll)
			mc.player.sendMessage(new TextComponentTranslation("message.areas.show"));
		else
			mc.player.sendMessage(new TextComponentTranslation("message.areas.hide"));
	}

	private static float randFloat(float min) {
		return min + rand.nextFloat() * (1f - min);
	}

	private static Color getColour(String areaName) {
		Color colour = colourCache.get(areaName);
		if (colour == null)
			colourCache.put(areaName, colour = Color.getHSBColor(rand.nextFloat(), randFloat(0.3f), randFloat(0.7f)));
		return colour;
	}

	@SubscribeEvent
	public static void areaRendering(RenderWorldLastEvent event) {
		if (!renderAll && areasToRender.isEmpty())
			return;

		CapabilityAreas cap = mc.world.getCapability(LandManager.CAPABILITY_AREAS, null);
		if (cap == null)
			return;

		if (renderAll) {
			Set<Area> areas = cap.getNearbyAreas(mc.player.getPosition());
			areas.forEach(area -> BoxRenderer.renderBox(area, getColour(area.getName()), event.getPartialTicks()));
		} else if (!areasToRender.isEmpty()) {
			areasToRender.forEach(areaName ->
			{
				Area area = cap.getArea(areaName);
				if (area != null)
					BoxRenderer.renderBox(area, getColour(area.getName()), event.getPartialTicks());
			});
		}
	}
}
