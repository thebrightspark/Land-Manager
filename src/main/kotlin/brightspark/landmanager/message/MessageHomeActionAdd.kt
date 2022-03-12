package brightspark.landmanager.message

import brightspark.landmanager.LandManager
import brightspark.landmanager.data.areas.AreaUpdateType
import brightspark.landmanager.util.*
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent

class MessageHomeActionAdd : Message {
	private lateinit var pos: BlockPos
	private lateinit var name: String

	@Suppress("unused")
	constructor()

	constructor(pos: BlockPos, name: String) {
		this.pos = pos
		this.name = name
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeBlockPos(pos)
		writeString(name)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		pos = readBlockPos()
		name = readString()
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val player = context.sender ?: return@enqueueWork
			val world = player.world
			val server = world.server ?: return@enqueueWork
			val cap = world.areasCap
			val area = cap.intersectingArea(pos)
			if (!player.canEditArea(area)) {
				sendError(player, "message.landmanager.error.noPerm")
				return@enqueueWork
			}
			val profile = server.playerProfileCache.getProfileForUsername(name) ?: run {
				sendError(player, "message.landmanager.error.noPlayerName", name)
				return@enqueueWork
			}
			val uuid = profile.id
			if (!cap.canJoinArea(uuid)) {
				sendError(player, "message.landmanager.error.cantJoin", name)
				return@enqueueWork
			}
			if (area!!.addMember(uuid)) {
				cap.increasePlayerAreasNum(uuid)
				cap.dataChanged(area, AreaUpdateType.CHANGE)
				LandManager.NETWORK.sendToPlayer(MessageHomeActionReply(HomeGuiActionType.ADD, uuid, profile.name), player)
			} else
				sendError(player, "message.landmanager.error.alreadyMember", name)
		}
	}

	private fun sendError(player: ServerPlayerEntity, langKey: String, vararg args: String) =
		LandManager.NETWORK.sendToPlayer(MessageHomeActionReplyError(langKey, *args), player)
}
