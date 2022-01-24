package brightspark.landmanager.message

import brightspark.landmanager.LMConfig
import brightspark.landmanager.util.AreaChangeType
import brightspark.landmanager.util.Message
import brightspark.landmanager.util.appendTranslation
import brightspark.landmanager.util.readEnumValue
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.Util
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.network.NetworkEvent
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Supplier

class MessageChatLog : Message {
	companion object {
		private val DATE_FORMAT = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
	}

	private var timestamp: Long = 0
	private lateinit var type: AreaChangeType
	private lateinit var areaName: String
	private lateinit var playerName: String

	@Suppress("unused")
	constructor()

	constructor(timestamp: Long, type: AreaChangeType, areaName: String, playerName: String) {
		this.timestamp = timestamp
		this.type = type
		this.areaName = areaName
		this.playerName = playerName
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeLong(timestamp)
		writeEnumValue(type)
		writeString(areaName)
		writeString(playerName)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		timestamp = readLong()
		type = readEnumValue()
		areaName = readString()
		playerName = readString()
	}

	override fun consume(context: Supplier<NetworkEvent.Context>) {
		if (!LMConfig.showChatLogs) return
		context.get().enqueueWork {
			Minecraft.getInstance().player!!.sendMessage(
				StringTextComponent(DATE_FORMAT.format(Date(timestamp)))
					.mergeStyle(TextFormatting.GRAY)
					.appendString(" ")
					.appendTranslation(type.unlocalisedName)
					.appendString(": $areaName -> $playerName"),
				Util.DUMMY_UUID
			)
		}
	}
}
