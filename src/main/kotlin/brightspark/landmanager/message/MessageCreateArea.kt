package brightspark.landmanager.message

import brightspark.landmanager.AreaCreationEvent
import brightspark.landmanager.LandManager
import brightspark.landmanager.data.areas.AddAreaResult
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.util.AreaChangeType
import brightspark.landmanager.util.Message
import brightspark.landmanager.util.areasCap
import brightspark.landmanager.util.sendToPlayer
import net.minecraft.network.PacketBuffer
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class MessageCreateArea : Message {
	private lateinit var area: Area

	@Suppress("unused")
	constructor()

	constructor(area: Area) {
		this.area = area
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeCompoundTag(area.serializeNBT())
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		area = Area(readCompoundTag()!!)
	}

	override fun consume(context: Supplier<NetworkEvent.Context>) {
		context.get().enqueueWork {
			val player = context.get().sender ?: return@enqueueWork
			val world = player.world
			var result = AddAreaResult.INVALID
			if (area.dim == player.world.dimensionKey.location && area.minPos.y >= 0 && area.maxPos.y <= world.height) {
				val cap = world.areasCap
				when {
					!Area.validateName(area.name) -> result = AddAreaResult.INVALID_NAME
					cap.hasArea(area.name) -> result = AddAreaResult.NAME_EXISTS
					cap.intersectsAnArea(area) -> result = AddAreaResult.AREA_INTERSECTS
					!MinecraftForge.EVENT_BUS.post(AreaCreationEvent(area)) -> {
						result = if (cap.addArea(area)) AddAreaResult.SUCCESS else AddAreaResult.NAME_EXISTS
						if (result == AddAreaResult.SUCCESS)
							LandManager.areaChange(
								(world as ServerWorld).server,
								AreaChangeType.CREATE,
								area.name,
								player
							)
					}
				}
			}
			LandManager.NETWORK.sendToPlayer(MessageCreateAreaReply(area.name, result), player)
		}
	}
}
