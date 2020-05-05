package brightspark.landmanager.message

import brightspark.ksparklib.api.Message
import brightspark.ksparklib.api.sendToPlayer
import brightspark.landmanager.LandManager
import brightspark.landmanager.data.areas.AreaUpdateType
import brightspark.landmanager.util.HomeGuiActionType
import brightspark.landmanager.util.areasCap
import brightspark.landmanager.util.canEditArea
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent
import java.util.*
import java.util.function.Supplier

class MessageHomeActionKickOrPass : Message {
	private lateinit var pos: BlockPos
	private var isPass: Boolean = false
	private lateinit var uuid: UUID

	@Suppress("unused")
	constructor()

	constructor(pos: BlockPos, isPass: Boolean, uuid: UUID) {
		this.pos = pos
		this.isPass = isPass
		this.uuid = uuid
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeBlockPos(pos)
		writeBoolean(isPass)
		writeUniqueId(uuid)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		pos = readBlockPos()
		isPass = readBoolean()
		uuid = readUniqueId()
	}

	override fun consume(context: Supplier<NetworkEvent.Context>) {
		context.get().enqueueWork {
			val player = context.get().sender ?: return@enqueueWork
			val world = player.world
			val server = world.server ?: return@enqueueWork
			val cap = world.areasCap
			val area = cap.intersectingArea(pos)
			if (!player.canEditArea(area)) {
				LandManager.NETWORK.sendToPlayer(MessageHomeActionReplyError("message.landmanager.error.noPerm"), player)
				return@enqueueWork
			}
			val profile = server.playerProfileCache.getProfileByUUID(uuid) ?: run {
				LandManager.NETWORK.sendToPlayer(MessageHomeActionReplyError("message.landmanager.error.noPlayer"), player)
				return@enqueueWork
			}

			var changed = true
			if (isPass) {
				area!!.owner?.let { area.addMember(it) }
				area.owner = uuid
				area.removeMember(uuid)
			} else {
				changed = area!!.removeMember(uuid)
				if (changed)
					cap.decreasePlayerAreasNum(uuid)
			}
			if (changed) {
				cap.dataChanged(area, AreaUpdateType.CHANGE)
				LandManager.NETWORK.sendToPlayer(MessageHomeActionReply(if (isPass) HomeGuiActionType.PASS else HomeGuiActionType.KICK, uuid, profile.name), player)
			}
		}
	}
}
