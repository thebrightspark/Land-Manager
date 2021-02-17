package brightspark.landmanager.block

import brightspark.ksparklib.api.extensions.sendToPlayer
import brightspark.landmanager.LandManager
import brightspark.landmanager.message.MessageOpenHomeGui
import brightspark.landmanager.util.areasCap
import brightspark.landmanager.util.getUsernameFromUuid
import brightspark.landmanager.util.isOp
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.material.Material
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.BlockRayTraceResult
import net.minecraft.util.text.TranslationTextComponent
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld

class HomeBlock : Block(Properties.create(Material.WOOD)) {
	override fun onBlockActivated(
		state: BlockState,
		world: World,
		pos: BlockPos,
		player: PlayerEntity,
		hand: Hand,
		hit: BlockRayTraceResult
	): ActionResultType {
		if (world.isRemote || world !is ServerWorld || player !is ServerPlayerEntity || player.isSneaking || hand != Hand.MAIN_HAND)
			return ActionResultType.SUCCESS
		world.areasCap.intersectingArea(pos)?.let { area ->
			if (area.isMember(player.uniqueID) || player.isOp()) {
				val server = world.server
				val owner = area.owner?.let { uuid -> server.getUsernameFromUuid(uuid)?.let { uuid to it } }
				val members = area.members.mapNotNull { uuid -> server.getUsernameFromUuid(uuid)?.let { uuid to it } }
				LandManager.NETWORK.sendToPlayer(MessageOpenHomeGui(pos, player.isOp(), owner, members), player)
			} else
				player.sendStatusMessage(TranslationTextComponent("message.landmanager.home.notMember"), true)
		} ?: run {
			player.sendStatusMessage(TranslationTextComponent("message.landmanager.home.none"), true)
		}
		return ActionResultType.SUCCESS
	}
}
