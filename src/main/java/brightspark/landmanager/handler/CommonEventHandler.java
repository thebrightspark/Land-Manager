package brightspark.landmanager.handler;

import brightspark.landmanager.Config;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.CapabilityAreas;
import brightspark.landmanager.data.CapabilityAreasProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID)
public class CommonEventHandler
{
    private static final ResourceLocation AREAS_RL = new ResourceLocation(LandManager.MOD_ID, "_areas");

    private static boolean handleEvent(Event event, EntityPlayer player, BlockPos pos)
    {
        if(player.world.isRemote || (Config.creativeIgnoresProtection || player.isCreative()) || player.canUseCommand(2, ""))
            return false;

        //Check if in protected area
        CapabilityAreas cap = player.world.getCapability(LandManager.CAPABILITY_AREAS, null);
        return cap != null && cap.isIntersectingArea(pos);
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

    @SubscribeEvent
    public static void attachWorldCap(AttachCapabilitiesEvent<World> event)
    {
        World world = event.getObject();
        if(!world.hasCapability(LandManager.CAPABILITY_AREAS, null))
            event.addCapability(AREAS_RL, new CapabilityAreasProvider());
    }
}