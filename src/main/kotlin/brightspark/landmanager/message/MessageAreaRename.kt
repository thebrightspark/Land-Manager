package brightspark.landmanager.message

import brightspark.landmanager.util.Message
import brightspark.landmanager.util.areasCap
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class MessageAreaRename : Message {
	private lateinit var oldName: String
	private lateinit var newName: String

	@Suppress("unused")
	constructor()

	constructor(oldName: String, newName: String) {
		this.oldName = oldName
		this.newName = newName
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeString(oldName)
		writeString(newName)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		oldName = readString()
		newName = readString()
	}

	override fun consume(context: Supplier<NetworkEvent.Context>) {
		context.get().enqueueWork {
			Minecraft.getInstance().world!!.areasCap.renameArea(oldName, newName)
		}
	}
}
