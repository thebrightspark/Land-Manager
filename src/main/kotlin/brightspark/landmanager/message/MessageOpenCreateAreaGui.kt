package brightspark.landmanager.message

import brightspark.ksparklib.api.Message
import brightspark.landmanager.gui.CreateAreaScreen
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class MessageOpenCreateAreaGui : Message {
	private lateinit var pos1: BlockPos
	private lateinit var pos2: BlockPos

	@Suppress("unused")
	constructor()

	constructor(pos1: BlockPos, pos2: BlockPos) {
		this.pos1 = pos1
		this.pos2 = pos2
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeBlockPos(pos1)
		writeBlockPos(pos2)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		pos1 = readBlockPos()
		pos2 = readBlockPos()
	}

	override fun consume(context: Supplier<NetworkEvent.Context>) {
		context.get().enqueueWork {
			val mc = Minecraft.getInstance()
			mc.displayGuiScreen(CreateAreaScreen(pos1, pos2))
		}
	}
}