package brightspark.landmanager.message

import brightspark.ksparklib.api.Message
import brightspark.landmanager.handler.ClientEventHandler
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class MessageShowArea : Message {
	private var toggleShowAll: Boolean = false
	private var showArea: String? = null

	@Suppress("unused")
	constructor()

	constructor(showArea: String?) {
		toggleShowAll = showArea == null
		this.showArea = showArea
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeBoolean(toggleShowAll)
		if (!toggleShowAll)
			writeString(showArea!!)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		toggleShowAll = readBoolean()
		if (!toggleShowAll)
			showArea = readString()
	}

	override fun consume(context: Supplier<NetworkEvent.Context>) {
		if (toggleShowAll)
			ClientEventHandler.toggleRenderAll()
		else
			ClientEventHandler.setRenderArea(showArea!!)
	}
}
