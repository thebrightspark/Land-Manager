package brightspark.landmanager.handler;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.CapabilityAreas;
import brightspark.landmanager.data.CapabilityAreasProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID)
public class CommonEventHandler
{
    private static final ResourceLocation AREAS_RL = new ResourceLocation(LandManager.MOD_ID, "_areas");

    private static long lastTimeHitProtectedBlock = 0L;

    private static boolean handleProtection(EntityPlayer player, BlockPos pos)
    {
        if(!LMConfig.creativeIgnoresProtection || player.isCreative() || player.canUseCommand(2, ""))
            return false;

        //Check if in protected area
        CapabilityAreas cap = player.world.getCapability(LandManager.CAPABILITY_AREAS, null);
        return cap != null && cap.isIntersectingArea(pos);
    }

    private static void sendCapToPlayer(EntityPlayer player)
    {
        if(!(player instanceof EntityPlayerMP)) return;
        CapabilityAreas cap = player.world.getCapability(LandManager.CAPABILITY_AREAS, null);
        if(cap != null) cap.sendDataToPlayer((EntityPlayerMP) player);
    }

    @SubscribeEvent
    public static void onBlockStartBreak(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event)
    {
        //Stop players from breaking blocks in protected areas
        if(handleProtection(event.getEntityPlayer(), event.getPos()))
        {
            if(event.getEntityPlayer().world.isRemote)
            {
                long worldTime = event.getEntityPlayer().world.getTotalWorldTime();
                if(worldTime - lastTimeHitProtectedBlock > 10)
                    event.getEntityPlayer().sendMessage(new TextComponentString("Cannot break a block in someone else's protected area!"));
                lastTimeHitProtectedBlock = worldTime;
            }
            event.setNewSpeed(0f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.PlaceEvent event)
    {
        //Stop players from placing block in procteted areas
        if(handleProtection(event.getPlayer(), event.getPos()))
        {
            if(event.getPlayer().world.isRemote)
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

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        //Send the capability data to the client
        sendCapToPlayer(event.player);
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event)
    {
        //Send the capability data to the client
        sendCapToPlayer(event.player);
    }
}
