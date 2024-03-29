package brightspark.landmanager.handler

import brightspark.landmanager.LMConfig
import brightspark.landmanager.LandManager
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.util.areasCap
import brightspark.landmanager.util.sendActionBarMessage
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.text.TextFormatting
import net.minecraft.world.World
import net.minecraftforge.event.entity.living.LivingSpawnEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.BlockEvent
import net.minecraftforge.event.world.ExplosionEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = LandManager.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object CommonEventHandler {
	private var lastTimeHitProtectedBlock: Long = 0

	private fun isCreativeOrOp(player: PlayerEntity): Boolean =
		(LMConfig.creativeIgnoresProtection && player.isCreative) || player.hasPermissionLevel(2)

	private fun getArea(world: World, pos: BlockPos): Area? = world.areasCap.intersectingArea(pos)

	private fun sendCapToPlayer(player: PlayerEntity) {
		if (player is ServerPlayerEntity)
			player.world.areasCap.sendDataToPlayer(player)
	}

	@SubscribeEvent
	fun onBlockStartBreak(event: PlayerEvent.BreakSpeed) = event.run {
		val world = player.world
		val area = getArea(world, pos)
		if ((area != null && area.isMember(player.uniqueID)) ||
			isCreativeOrOp(player) ||
			(area == null && LMConfig.canPlayersBreakBlocks)
		) {
			return
		}

		// Stop players from breaking blocks
		if (world.isRemote && world.gameTime - lastTimeHitProtectedBlock > 10)
			player.sendActionBarMessage("message.landmanager.protection.break", TextFormatting.RED)
		newSpeed = 0F
		isCanceled = true
	}

	@SubscribeEvent
	fun onBlockPlace(event: BlockEvent.EntityPlaceEvent) = event.run {
		if (entity !is PlayerEntity || world !is World)
			return
		val player = event.entity as PlayerEntity
		val area = getArea(world as World, pos)
		if ((area != null && area.isMember(player.uniqueID)) ||
			isCreativeOrOp(player) ||
			(area == null && LMConfig.canPlayersBreakBlocks)
		) {
			return
		}

		// Stop players from placing blocks
		player.sendActionBarMessage("message.landmanager.protection.place", TextFormatting.RED)
		isCanceled = true
	}

	@SubscribeEvent
	fun onRightClickBlock(event: PlayerInteractEvent.RightClickBlock) = event.run {
		val area = getArea(world, pos)
		if ((area != null && (area.interactions || area.isMember(player.uniqueID))) ||
			isCreativeOrOp(player) ||
			(area == null && LMConfig.canPlayersInteract) ||
			// If player is holding shift with an itemblock, then allow it for block placing checks
			(player.isSneaking && itemStack.item is BlockItem)
		) {
			return
		}
		// If player is holding shift with an itemblock, then allow it for block placing checks

		// Stop players from right clicking blocks
		if (world.isRemote && hand == Hand.MAIN_HAND)
			player.sendActionBarMessage("message.landmanager.protection.interact", TextFormatting.RED)
		useBlock = Event.Result.DENY
	}

	@SubscribeEvent
	fun onEntitySpawn(event: LivingSpawnEvent.CheckSpawn) = event.run {
		if (world !is World)
			return
		val cap = (world as World).areasCap
		val areas = cap.intersectingAreas(Vector3d(x, y, z))
		val hostile = !entityLiving.type.classification.peacefulCreature

		// Stop entity spawning if within an area that prevents it
		if (areas.isEmpty() && !(if (hostile) LMConfig.canHostileSpawn else LMConfig.canPassiveSpawn)) {
			result = Event.Result.DENY
			return
		}
		if (areas.any { !(if (hostile) it.canHostileSpawn else it.canPassiveSpawn) })
			result = Event.Result.DENY
	}

	@SubscribeEvent
	fun onExplosion(event: ExplosionEvent.Detonate) = event.run {
		// Stop blocks from being destroyed by explosions if within an area that prevents it
		val cap = world.areasCap
		affectedBlocks.removeIf { pos ->
			cap.intersectingAreas(pos).let { areas ->
				if (areas.isEmpty())
					LMConfig.canExplosionsDestroyBlocks
				else
					areas.any { !it.explosions }
			}
		}
	}

	@SubscribeEvent
	fun onPlayerJoin(event: PlayerEvent.PlayerLoggedInEvent) = sendCapToPlayer(event.player)

	@SubscribeEvent
	fun onPlayerChangeDimension(event: PlayerEvent.PlayerChangedDimensionEvent) = sendCapToPlayer(event.player)
}
