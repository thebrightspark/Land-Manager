package brightspark.landmanager;

import brightspark.landmanager.data.AreasWorldSavedData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class EventHandler
{
    private static boolean handleEvent(Event event, EntityPlayer player, BlockPos pos)
    {
        if(player.world.isRemote || (Config.creativeIgnoresProtection || player.isCreative()) || player.canUseCommand(2, ""))
            return false;

        //Check if in protected area
        AreasWorldSavedData wsd = AreasWorldSavedData.get(player.world);
        return wsd != null && wsd.isIntersectingArea(pos);
    }

    @SubscribeEvent
    public static void onBlockStartBreak(PlayerEvent.BreakSpeed event)
    {
        //Stop players from breaking blocks in protected areas
        if(handleEvent(event, event.getEntityPlayer(), event.getPos()))
        {
            event.getEntityPlayer().sendMessage(new TextComponentString("Cannot break a block in someone else's protected area!"));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.PlaceEvent event)
    {
        //Stop players from placing block in procteted areas
        if(handleEvent(event, event.getPlayer(), event.getPos()))
        {
            event.getPlayer().sendMessage(new TextComponentString("Cannot place a block in someone else's protected area!"));
            event.setCanceled(true);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void areaRendering(RenderGameOverlayEvent event)
    {
        //TODO: Render areas
    }
}
