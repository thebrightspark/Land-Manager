package brightspark.landmanager.data.requests

import brightspark.landmanager.util.getUsernameFromUuid
import net.minecraft.nbt.CompoundNBT
import net.minecraft.server.MinecraftServer
import net.minecraftforge.common.util.INBTSerializable
import java.text.SimpleDateFormat
import java.util.*

class Request : INBTSerializable<CompoundNBT> {
	companion object {
		private val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy HH:mm")
	}

	var id: Int = 0
		private set
	lateinit var areaName: String
		private set
	lateinit var playerUuid: UUID
		private set
	var timestamp: Long = 0
	val date: String
		get() = DATE_FORMAT.format(Date(timestamp))

	constructor(id: Int, areaName: String, playerUuid: UUID) {
		this.id = id
		this.areaName = areaName
		this.playerUuid = playerUuid
		timestamp = System.currentTimeMillis()
	}

	constructor(nbt: CompoundNBT) {
		deserializeNBT(nbt)
	}

	fun getPlayerName(server: MinecraftServer): String? = server.getUsernameFromUuid(playerUuid)

	override fun serializeNBT() = CompoundNBT().apply {
		putInt("id", this@Request.id)
		putString("areaName", areaName)
		putUniqueId("player", playerUuid)
		putLong("timestamp", timestamp)
	}

	override fun deserializeNBT(nbt: CompoundNBT) = nbt.run {
		this@Request.id = getInt("id")
		areaName = getString("areaName")
		playerUuid = getUniqueId("player")
		timestamp = getLong("timestamp")
	}
}
