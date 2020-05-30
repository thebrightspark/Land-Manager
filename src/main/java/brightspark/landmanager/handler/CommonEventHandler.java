package brightspark.landmanager.handler;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.LandManager;
import brightspark.landmanager.data.areas.Area;
import brightspark.landmanager.data.areas.CapabilityAreas;
import brightspark.landmanager.data.areas.CapabilityAreasProvider;
import brightspark.landmanager.message.MessageMovedToArea;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
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
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static brightspark.landmanager.LandManager.log;

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID)
public class CommonEventHandler {
	private static final ResourceLocation AREAS_RL = new ResourceLocation(LandManager.MOD_ID, "_areas");

	private static long lastTimeHitProtectedBlock = 0L;
	private static Map<UUID, LastDetails> lastAreaInside = new HashMap<>();

	private static CapabilityAreas getAreas(World world) {
		return world.getCapability(LandManager.CAPABILITY_AREAS, null);
	}

	private static boolean isPlayerCreativeOrOP(EntityPlayer player) {
		return (LMConfig.creativeIgnoresProtection && player.isCreative()) || player.canUseCommand(2, "");
	}

	private static Area getArea(EntityPlayer player, BlockPos pos) {
		CapabilityAreas cap = getAreas(player.world);
		if (cap == null) {
			DimensionType dim = player.world.provider.getDimensionType();
			log("getArea: WARNING! No capability found in dimension {} ({})!", dim.getName(), dim.getId());
			return null;
		}
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
		log("onBlockStartBreak: Starting block break checks for block at {}", event.getPos());
		EntityPlayer player = event.getEntityPlayer();
		Area area = getArea(player, event.getPos());
		if (area != null && area.isMember(player.getUniqueID())) {
			log("onBlockStartBreak: Player {} is a member of area {} - block break allowed!", player.getName(), area.getName());
			return;
		} else if (isPlayerCreativeOrOP(player)) {
			log("onBlockStartBreak: Player {} is an OP or in creative (and config is enabled) - block break allowed!", player.getName());
			return;
		} else if (area == null && LMConfig.globalSettings.canPlayersBreakBlocks) {
			log("onBlockStartBreak: No area found, and global config allows block breaking - block break allowed!");
			return;
		}
		log("onBlockStartBreak: Block break cancelled!");
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
		log("onBlockPlace: Starting block place checks for block at {}", event.getPos());
		EntityPlayer player = event.getPlayer();
		Area area = getArea(player, event.getPos());
		if (area != null && area.isMember(player.getUniqueID())) {
			log("onBlockPlace: Player {} is a member of area {} - block place allowed!", player.getName(), area.getName());
			return;
		} else if (isPlayerCreativeOrOP(player)) {
			log("onBlockPlace: Player {} is an OP or in creative (and config is enabled) - block place allowed!", player.getName());
			return;
		} else if (area == null && LMConfig.globalSettings.canPlayersPlaceBlocks) {
			log("onBlockPlace: No area found, and global config allows block placing - block place allowed!");
			return;
		}
		log("onBlockPlace: Block place cancelled!");
		//Stop players from placing blocks
		player.sendMessage(new TextComponentTranslation("message.protection.place"));
		event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		log("onRightClickBlock: Starting block right click checks for block at {}", event.getPos());
		EntityPlayer player = event.getEntityPlayer();
		Area area = getArea(player, event.getPos());
		if (area != null && area.canInteract()) {
			log("onRightClickBlock: Interactions are enabled for area {} - block right click allowed!", area.getName());
			return;
		} else if (area != null && area.isMember(player.getUniqueID())) {
			log("onRightClickBlock: Player {} is a member of area {} - block right click allowed!", player.getName(), area.getName());
			return;
		} else if (isPlayerCreativeOrOP(player)) {
			log("onRightClickBlock: Player {} is an OP or in creative (and config is enabled) - block right click allowed!", player.getName());
			return;
		} else if (area == null && LMConfig.globalSettings.canPlayersInteract) {
			log("onRightClickBlock: No area found, and global config allows block interactions - block right click allowed!");
			return;
		}
		//If player is holding shift with an itemblock, then allow it for block placing checks
		if (player.isSneaking() && event.getItemStack().getItem() instanceof ItemBlock) {
			log("onRightClickBlock: Player {} is sneaking and right clicking with an ItemBlock - block right click allowed!", player.getName());
			return;
		}
		log("onRightClickBlock: Block right click cancelled!");
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
		EntityLivingBase entity = event.getEntityLiving();
		log("onEntitySpawn: Starting entity spawn checks for entity {} at {}, {}, {}", entity.getName(), event.getX(), event.getY(), event.getZ());
		//Stop entity spawning if it's within an area that's preventing the spawning
		CapabilityAreas cap = getAreas(event.getWorld());
		if (cap == null)
			return;
		Set<Area> areas = cap.intersectingAreas(new BlockPos(event.getX(), event.getY(), event.getZ()));
		boolean hostile = entity.isCreatureType(EnumCreatureType.MONSTER, false);
		log("onEntitySpawn: Is entity hostile: {}", hostile);
		if (areas.isEmpty()) {
			log("onEntitySpawn: No areas found for location where entity is spawning");
			if (!(hostile ? LMConfig.globalSettings.canHostileSpawn : LMConfig.globalSettings.canPassiveSpawn)) {
				log("onEntitySpawn: Global config does not allow entity spawning - spawn cancelled!");
				event.setResult(Event.Result.DENY);
			}
		}
		Area area = areas.stream().filter(a -> !(hostile ? a.canHostileSpawn() : a.canPassiveSpawn())).findFirst().orElse(null);
		if (area != null) {
			log("onEntitySpawn: Area {} does not allow entity spawning - spawn cancelled!", area.getName());
			event.setResult(Event.Result.DENY);
		} else
			log("onRightClickBlock: Entity spawning allowed!");
	}

	@SubscribeEvent
	public static void onExplosion(ExplosionEvent.Detonate event) {
		log("onExplosion: Starting explosion break checks");
		//Prevent blocks from being destroyed by explosions if it's an area that prevents it
		CapabilityAreas cap = getAreas(event.getWorld());
		if (cap == null)
			return;
		event.getAffectedBlocks().removeIf(pos ->
		{
			Set<Area> areas = cap.intersectingAreas(pos);
			if (areas.isEmpty()) {
				if (!LMConfig.globalSettings.canExplosionsDestroyBlocks) {
					log("onExplosion: No areas found for pos {} and global config does not allow explosions - removing affected block pos!", pos);
					return true;
				}
			} else {
				Area area = areas.stream().filter(a -> !a.canExplosionsCauseDamage()).findFirst().orElse(null);
				if (area != null) {
					log("onExplosion: Area {} at pos {} does not allow explosions - removing affected block pos!", area.getName(), pos);
					return true;
				}
			}
			log("onExplosion: Allowing affected block pos {}", pos);
			return false;
		});
	}

	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event) {
		//Send title message to client when moving into different area
		if (event.side != Side.SERVER || event.phase != TickEvent.Phase.END && event.player instanceof EntityPlayerMP)
			return;
		EntityPlayerMP player = (EntityPlayerMP) event.player;
		UUID uuid = player.getUniqueID();
		LastDetails last = lastAreaInside.get(uuid);
		if (last == null || last.updateAndCheckPlayerPos(player)) {
			Area area = getArea(player, player.getPosition());
			if (last == null || last.updateAndCheckArea(area)) {
				if (last == null) {
					last = new LastDetails(area == null ? null : area.getName(), player);
					lastAreaInside.put(uuid, last);
				}
				LandManager.NETWORK.sendTo(new MessageMovedToArea(area == null ? "" : area.getName(), area != null && area.isMember(uuid)), player);
			}
		}
	}

	static class LastDetails {
		String areaName;
		BlockPos pos;
		int dimId;

		LastDetails(String areaName, EntityPlayer player) {
			this.areaName = areaName;
			pos = player.getPosition();
			dimId = player.dimension;
		}

		boolean updateAndCheckPlayerPos(EntityPlayer player) {
			boolean result = player.getPosition().equals(pos) || player.dimension != dimId;
			pos = player.getPosition();
			dimId = player.dimension;
			return result;
		}

		boolean updateAndCheckArea(Area area) {
			boolean result = (area == null) != (areaName == null) || (area != null && !area.getName().equals(areaName));
			areaName = area == null ? null : area.getName();
			return result;
		}
	}
}
