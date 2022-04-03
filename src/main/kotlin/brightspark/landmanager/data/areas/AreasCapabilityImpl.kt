package brightspark.landmanager.data.areas

import brightspark.landmanager.LMConfig
import brightspark.landmanager.LandManager
import brightspark.landmanager.message.*
import brightspark.landmanager.util.runWhenOnLogicalServer
import brightspark.landmanager.util.sendToAll
import brightspark.landmanager.util.sendToPlayer
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraftforge.common.util.Constants
import java.util.*
import kotlin.math.max
import kotlin.math.min

class AreasCapabilityImpl : AreasCapability {
	private val areas = mutableMapOf<String, Area>()
	private val numAreasPerPlayer = mutableMapOf<UUID, Int>()

	override fun hasArea(areaName: String): Boolean = areas.containsKey(areaName)

	override fun getArea(areaName: String): Area? = areas[areaName]

	override fun addArea(area: Area): Boolean {
		if (hasArea(area.name))
			return false
		areas[area.name] = area
		dataChanged(area, AreaUpdateType.ADD)
		return true
	}

	override fun removeArea(areaName: String): Boolean = areas.remove(areaName)?.let {
		dataChanged(it, AreaUpdateType.DELETE)
		return@let true
	} ?: false

	override fun updateArea(area: Area) {
		areas[area.name] = area
	}

	override fun renameArea(oldName: String, newName: String): Boolean {
		val area = areas.remove(oldName) ?: return false
		area.setName(newName)
		areas[newName] = area
		runWhenOnLogicalServer { LandManager.NETWORK.sendToAll(MessageAreaRename(oldName, newName)) }
		return true
	}

	override fun setOwner(areaName: String, playerUuid: UUID): Boolean {
		val area = getArea(areaName) ?: return false
		area.owner = playerUuid
		dataChanged(area, AreaUpdateType.CHANGE)
		return true
	}

	override fun getAllAreas(): List<Area> = areas.values.toList()

	override fun getAllAreaNames(): List<String> = areas.keys.toList()

	override fun getNearbyAreas(pos: BlockPos): List<Area> = areas.values
		.filter { it.intersects(pos) || it.closestPosTo(pos).withinDistance(pos, LMConfig.showAllRadius.toDouble()) }
		.sortedByDescending { it.closestPosTo(pos).distanceSq(pos) }

	override fun intersectsAnArea(aabb: AxisAlignedBB): Boolean = areas.values.any { it.intersects(aabb) }

	override fun intersectsAnArea(area: Area): Boolean = areas.values.any {
		it.intersects(area)
	}

	override fun intersectingArea(pos: BlockPos): Area? = areas.values.find { it.intersects(pos) }

	override fun intersectingAreas(pos: Vector3d): Set<Area> = areas.values.filter { it.intersects(pos) }.toSet()

	override fun intersectingAreas(pos: BlockPos): Set<Area> = areas.values.filter { it.intersects(pos) }.toSet()

	override fun getNumAreasJoined(playerUuid: UUID): Int = numAreasPerPlayer.computeIfAbsent(playerUuid) { 0 }

	override fun canJoinArea(playerUuid: UUID): Boolean =
		LMConfig.maxAreasCanOwn < 0 || getNumAreasJoined(playerUuid) < LMConfig.maxAreasCanOwn

	override fun increasePlayerAreasNum(playerUuid: UUID) {
		numAreasPerPlayer.compute(playerUuid) { _, num ->
			num?.let { min(num + 1, LMConfig.maxAreasCanOwn) } ?: 1
		}
	}

	override fun decreasePlayerAreasNum(playerUuid: UUID) {
		numAreasPerPlayer.compute(playerUuid) { _, num ->
			num?.let { max(num - 1, 0) } ?: 0
		}
	}

	override fun dataChanged() =
		runWhenOnLogicalServer { LandManager.NETWORK.sendToAll(MessageUpdateAreasCap(serializeNBT())) }

	override fun dataChanged(area: Area, type: AreaUpdateType) = runWhenOnLogicalServer {
		LandManager.NETWORK.sendToAll(
			when (type) {
				AreaUpdateType.DELETE -> MessageAreaDelete(area.name)
				AreaUpdateType.ADD -> MessageAreaAdd(area)
				AreaUpdateType.CHANGE -> MessageAreaChange(area)
			}
		)
	}

	override fun sendDataToPlayer(player: ServerPlayerEntity) =
		LandManager.NETWORK.sendToPlayer(MessageUpdateAreasCap(serializeNBT()), player)

	override fun serializeNBT() = CompoundNBT().apply {
		put("areas", ListNBT().apply {
			areas.values.forEach { add(it.serializeNBT()) }
		})
	}

	override fun deserializeNBT(nbt: CompoundNBT) {
		areas.clear()
		numAreasPerPlayer.clear()
		nbt.getList("areas", Constants.NBT.TAG_COMPOUND).forEach { listNbt ->
			val area = Area(listNbt as CompoundNBT)
			areas[area.name] = area
			area.owner?.let { increasePlayerAreasNum(it) }
			area.members.forEach { increasePlayerAreasNum(it) }
		}
	}
}
