package brightspark.landmanager.message

import brightspark.ksparklib.api.Message
import brightspark.landmanager.gui.HomeScreen
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.network.NetworkEvent
import java.util.*
import java.util.function.Supplier

class MessageOpenHomeGui : Message {
	private lateinit var pos: BlockPos
	private var isOp: Boolean = false
	private var owner: Pair<UUID, String>? = null
	private lateinit var members: List<Pair<UUID, String>>

	@Suppress("unused")
	constructor()

	constructor(pos: BlockPos, isOp: Boolean, owner: Pair<UUID, String>?, members: List<Pair<UUID, String>>) {
		this.pos = pos
		this.isOp = isOp
		this.owner = owner
		this.members = members
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeBlockPos(pos)
		writeBoolean(isOp)
		writeBoolean(owner != null)
		owner?.let {
			writeUniqueId(it.first)
			writeString(it.second)
		}
		writeInt(members.size)
		members.forEach {
			writeUniqueId(it.first)
			writeString(it.second)
		}
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		pos = readBlockPos()
		isOp = readBoolean()
		if (readBoolean())
			owner = readUniqueId() to readString()
		members = Array<Pair<UUID, String>>(readInt()) {
			readUniqueId() to readString()
		}.toList()
	}

	override fun consume(context: Supplier<NetworkEvent.Context>) {
		context.get().enqueueWork {
			val mc = Minecraft.getInstance()
			mc.displayGuiScreen(HomeScreen(mc.player, pos).apply {
				setMembersData(owner, members)
				if (isOp)
					setClientIsOp()
			})
		}
	}
}
