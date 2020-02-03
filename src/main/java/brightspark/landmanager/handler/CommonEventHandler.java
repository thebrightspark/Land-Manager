package brightspark.landmanager.handler;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.areas.CapabilityAreasProvider;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.Set;

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID)
public class CommonEventHandler {
	private static final ResourceLocation AREAS_RL = new ResourceLocation(LandManager.MOD_ID, "_areas");

	private static long lastTimeHitProtectedBlock = 0L;

	private static CapabilityAreas getAreas(World world) {
		return world.getCapability(LandManager.CAPABILITY_AREAS, null);
	}

	private static boolean isPlayerCreativeOrOP(EntityPlayer player) {
		return (LMConfig.creativeIgnoresProtection && player.isCreative()) || player.canUseCommand(2, "");
	}

	private static Area getArea(EntityPlayer player, BlockPos pos) {
		CapabilityAreas cap = getAreas(player.world);
		if (cap == null)
			return null;
		return cap.intersectingArea(pos);
	}

	private static void sendCapToPlayer(EntityPlayer player) {
		if (!(player instanceof EntityPlayerMP))
			return;
		CapabilityAreas cap = getAreas(player.world);
		if (cap != null)
			cap.sendDataToPlayer((EntityPlayerMP) player);
	}

	@SubscribeEvent
	public static void onBlockStartBreak(net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed event) {
		EntityPlayer player = event.getEntityPlayer();
		Area area = getArea(player, event.getPos());
		if (area != null && area.isMember(player.getUniqueID()))
			return;
		else if (isPlayerCreativeOrOP(player))
			return;
		else if (area == null && LMConfig.globalSettings.canPlayersBreakBlocks)
			return;
		//Stop players from breaking blocks
		if (player.world.isRemote) {
			long worldTime = player.world.getTotalWorldTime();
			if (worldTime - lastTimeHitProtectedBlock > 10)
				player.sendMessage(new TextComponentTranslation("message.protection.break"));
			lastTimeHitProtectedBlock = worldTime;
		}
		event.setNewSpeed(0f);
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event) {
		EntityPlayer player = event.getPlayer();
		Area area = getArea(player, event.getPos());
		if (area != null && area.isMember(player.getUniqueID()))
			return;
		else if (isPlayerCreativeOrOP(player))
			return;
		else if (area == null && LMConfig.globalSettings.canPlayersPlaceBlocks)
			return;
		//Stop players from placing blocks
		player.sendMessage(new TextComponentTranslation("message.protection.place"));
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		EntityPlayer player = event.getEntityPlayer();
		Area area = getArea(player, event.getPos());
		if (area != null && (area.canInteract() || area.isMember(player.getUniqueID())))
			return;
		else if (isPlayerCreativeOrOP(player))
			return;
		else if (area == null && LMConfig.globalSettings.canPlayersInteract)
			return;
		//If player is holding shift with an itemblock, then allow it for block placing checks
		if (player.isSneaking() && event.getItemStack().getItem() instanceof ItemBlock)
			return;
		//Stop players from right clicking blocks
		if (event.getWorld().isRemote && event.getHand() == EnumHand.MAIN_HAND)
			player.sendMessage(new TextComponentTranslation("message.protection.interact"));
		event.setUseBlock(Event.Result.DENY);
	}

	@SubscribeEvent
	public static void attachWorldCap(AttachCapabilitiesEvent<World> event) {
		World world = event.getObject();
		if (!world.hasCapability(LandManager.CAPABILITY_AREAS, null))
			event.addCapability(AREAS_RL, new CapabilityAreasProvider());
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		//Send the capability data to the client
		sendCapToPlayer(event.player);
	}

	@SubscribeEvent
	public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		//Send the capability data to the client
		sendCapToPlayer(event.player);
	}

	@SubscribeEvent
	public static void onEntitySpawn(LivingSpawnEvent.CheckSpawn event) {
		//Stop entity spawning if it's within an area that's preventing the spawning
		CapabilityAreas cap = getAreas(event.getWorld());
		if (cap == null)
			return;
		Set<Area> areas = cap.intersectingAreas(new BlockPos(event.getX(), event.getY(), event.getZ()));
		boolean hostile = event.getEntityLiving().isCreatureType(EnumCreatureType.MONSTER, false);
		if (areas.isEmpty()) {
			if (!(hostile ? LMConfig.globalSettings.canHostileSpawn : LMConfig.globalSettings.canPassiveSpawn))
				event.setResult(Event.Result.DENY);
		}
		if (areas.stream().anyMatch(area -> !(hostile ? area.canHostileSpawn() : area.canPassiveSpawn())))
			event.setResult(Event.Result.DENY);
	}

	@SubscribeEvent
	public static void onExplosion(ExplosionEvent.Detonate event) {
		//Prevent blocks from being destroyed by explosions if it's an area that prevents it
		CapabilityAreas cap = getAreas(event.getWorld());
		if (cap == null)
			return;
		event.getAffectedBlocks().removeIf(pos ->
		{
			Set<Area> areas = cap.intersectingAreas(pos);
			return areas.isEmpty() ? !LMConfig.globalSettings.canExplosionsDestroyBlocks :
				areas.stream().anyMatch(area -> !area.canExplosionsCauseDamage());
		});
	}
}
