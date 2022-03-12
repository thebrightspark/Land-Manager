package brightspark.landmanager.message

import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.util.Message
import brightspark.landmanager.util.areasCap
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent

class MessageAreaAdd : Message {
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

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			Minecraft.getInstance().world!!.areasCap.addArea(area)
		}
	}
}
