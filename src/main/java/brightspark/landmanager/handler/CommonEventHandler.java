package brightspark.landmanager.handler;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.areas.CapabilityAreasProvider;
import brightspark.landmanager.data.logs.AreaLogType;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Objects;
import java.util.Set;

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID)
public class CommonEventHandler
{
    private static final ResourceLocation AREAS_RL = new ResourceLocation(LandManager.MOD_ID, "_areas");

    private static long lastTimeHitProtectedBlock = 0L;

    private static Area getProtectedArea(EntityPlayer player, BlockPos pos)
    {
        if((LMConfig.creativeIgnoresProtection && player.isCreative()) || player.canUseCommand(2, ""))
            return null;

        //Check if in protected area
        CapabilityAreas cap = player.world.getCapability(LandManager.CAPABILITY_AREAS, null);
        if(cap == null) return null;
        Area area = cap.intersectingArea(pos);
        if(area != null && !Objects.equals(area.getAllocatedPlayer(), player.getUniqueID()))
            //Area is protected against this player
            return area;
        return null;
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
        EntityPlayer player = event.getEntityPlayer();
        Area area = getProtectedArea(player, event.getPos());
        if(area != null)
        {
            if(player.world.isRemote)
            {
                long worldTime = player.world.getTotalWorldTime();
                if(worldTime - lastTimeHitProtectedBlock > 10)
                    player.sendMessage(new TextComponentTranslation("message.protection.break"));
                lastTimeHitProtectedBlock = worldTime;
            }
            else
                LandManager.areaLog(AreaLogType.BREAK, area.getName(), (EntityPlayerMP) player);
            event.setNewSpeed(0f);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.PlaceEvent event)
    {
        //Stop players from placing block in protected areas
        EntityPlayer player = event.getPlayer();
        Area area = getProtectedArea(player, event.getPos());
        if(area != null)
        {
            player.sendMessage(new TextComponentTranslation("message.protection.place"));
            LandManager.areaLog(AreaLogType.PLACE, area.getName(), (EntityPlayerMP) player);
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

    @SubscribeEvent
    public static void onEntitySpawn(LivingSpawnEvent.CheckSpawn event)
    {
        //Stop entity spawning if it's within an area that's preventing the spawning
        CapabilityAreas cap = event.getWorld().getCapability(LandManager.CAPABILITY_AREAS, null);
        if(cap == null) return;
        Set<Area> areas = cap.intersectingAreas(new BlockPos(event.getX(), event.getY(), event.getZ()));
        boolean hostile = event.getEntityLiving().isCreatureType(EnumCreatureType.MONSTER, false);
        if(areas.stream().anyMatch(area -> !(hostile ? area.canHostileSpawn() : area.canPassiveSpawn())))
            event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event)
    {
        CapabilityAreas cap = event.getWorld().getCapability(LandManager.CAPABILITY_AREAS, null);
        if(cap == null) return;
        event.getAffectedBlocks().removeIf(pos ->
                cap.intersectingAreas(pos).stream().anyMatch(area ->
                        !area.canExplosionsCauseDamage()));
    }
}
