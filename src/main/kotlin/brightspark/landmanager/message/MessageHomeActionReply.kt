package brightspark.landmanager.message

import brightspark.landmanager.gui.HomeScreen
import brightspark.landmanager.util.HomeGuiActionType
import brightspark.landmanager.util.Message
import brightspark.landmanager.util.readEnumValue
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.*

class MessageHomeActionReply : Message {
	private lateinit var type: HomeGuiActionType
	private lateinit var uuid: UUID
	private lateinit var name: String

	@Suppress("unused")
	constructor()

	constructor(type: HomeGuiActionType, uuid: UUID, name: String) {
		this.type = type
		this.uuid = uuid
		this.name = name
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeEnumValue(type)
		writeUniqueId(uuid)
		writeString(name)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		type = readEnumValue()
		uuid = readUniqueId()
		name = readString()
	}

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val mc = Minecraft.getInstance()
			val gui = mc.currentScreen
			if (gui !is HomeScreen)
				return@enqueueWork
			when (type) {
				HomeGuiActionType.ADD -> {
					gui.addMember(uuid, name)
					gui.clearInput()
				}
				HomeGuiActionType.KICK -> {
					gui.removeMember(uuid)
					gui.clearSelection()
				}
				HomeGuiActionType.PASS -> {
					val player = mc.player!!
					if (player.uniqueID == uuid)
						player.closeScreen()
				}
			}
		}
	}
}
