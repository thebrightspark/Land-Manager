package brightspark.landmanager.message

import brightspark.ksparklib.api.Message
import brightspark.ksparklib.api.extensions.readEnumValue
import brightspark.landmanager.gui.HomeScreen
import brightspark.landmanager.util.HomeGuiToggleType
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class MessageHomeToggleReply : Message {
	private lateinit var type: HomeGuiToggleType
	private var state: Boolean = false

	@Suppress("unused")
	constructor()

	constructor(type: HomeGuiToggleType, state: Boolean) {
		this.type = type
		this.state = state
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeEnumValue(type)
		writeBoolean(state)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		type = readEnumValue()
		state = readBoolean()
	}

	override fun consume(context: Supplier<NetworkEvent.Context>) {
		context.get().enqueueWork {
			val gui = Minecraft.getInstance().currentScreen
			if (gui is HomeScreen)
				gui.setToggle(type, state)
		}
	}
}
