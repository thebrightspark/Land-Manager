package brightspark.landmanager.data.requests

import brightspark.landmanager.LandManager
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraft.server.MinecraftServer
import net.minecraft.world.storage.WorldSavedData
import net.minecraftforge.common.util.Constants
import java.util.*
import kotlin.streams.toList

class RequestsWSD : WorldSavedData(NAME) {
	companion object {
		private const val NAME = LandManager.MOD_ID + "requests"

		fun get(server: MinecraftServer): RequestsWSD = server.worlds.first().savedData.getOrCreate(::RequestsWSD, NAME)
	}

	private var nextId = 0
	private val requestsByArea = mutableMapOf<String, MutableSet<Request>>()
	private val requests = mutableSetOf<Request>()

	private fun hasRequest(areaName: String, playerUuid: UUID): Boolean =
		requestsByArea[areaName]?.any { it.areaName == areaName && it.playerUuid == playerUuid } ?: false

	fun getById(id: Int): Request? = requests.find { it.id == id }

	fun getByAreaNameRegex(pattern: String): List<Request> {
		val regex = Regex(pattern)
		return requestsByArea.entries.stream()
			.filter { regex.matches(it.key) }
			.flatMap { it.value.stream() }
			.sorted(Comparator.comparingInt { it.id })
			.toList()
	}

	fun getByAreaName(areaName: String): MutableSet<Request> =
		requestsByArea.computeIfAbsent(areaName) { mutableSetOf() }

	fun getAll(): Set<Request> = requests.toSet()

	fun add(areaName: String, playerUuid: UUID): Int? {
		if (hasRequest(areaName, playerUuid))
			return null
		val request = Request(nextId++, areaName, playerUuid)
		requests += request
		getByAreaName(areaName) += request
		markDirty()
		return request.id
	}

	fun delete(areaName: String?, requestId: Int): Boolean {
		val request = areaName?.let { getByAreaName(areaName).find { it.id == requestId } ?: return false }
			?: requests.find { it.id == requestId }
			?: return false

		requestsByArea.entries.find { it.key == request.areaName }?.value?.removeIf { it.id == requestId }
		requests.removeIf { it.id == requestId }
		markDirty()
		return true
	}

	fun deleteAllForArea(areaName: String): Boolean {
		val removed1 = requestsByArea.remove(areaName) != null
		val removed2 = requests.removeIf { it.areaName == areaName }
		val removedAny = removed1 || removed2
		if (removedAny)
			markDirty()
		return removedAny
	}

	override fun read(nbt: CompoundNBT) = nbt.run {
		nextId = getInt("nextId")
		requests.clear()
		requestsByArea.clear()
		nbt.getList("list", Constants.NBT.TAG_COMPOUND).forEach {
			val request = Request(it as CompoundNBT)
			requests += request
			getByAreaName(request.areaName) += request
		}
	}

	override fun write(nbt: CompoundNBT) = nbt.apply {
		putInt("nextId", nextId)
		put("list", ListNBT().apply { requests.forEach { add(it.serializeNBT()) } })
	}
}
