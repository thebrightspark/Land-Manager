package brightspark.landmanager.data.areas

import brightspark.landmanager.LandManager
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraftforge.common.util.INBTSerializable
import java.util.*

interface AreasCapability : INBTSerializable<CompoundNBT> {
	companion object {
		val RL = ResourceLocation(LandManager.MOD_ID, "_areas")
	}

	fun hasArea(areaName: String): Boolean

	fun getArea(areaName: String): Area?

	fun addArea(area: Area): Boolean

	fun removeArea(areaName: String): Boolean

	fun updateArea(area: Area)

	fun renameArea(oldName: String, newName: String): Boolean

	fun setOwner(areaName: String, playerUuid: UUID): Boolean

	fun getAllAreas(): List<Area>

	fun getAllAreaNames(): List<String>

	fun getNearbyAreas(pos: BlockPos): List<Area>

	fun intersectsAnArea(aabb: AxisAlignedBB): Boolean

	fun intersectsAnArea(area: Area): Boolean

	fun intersectingArea(pos: BlockPos): Area?

	fun intersectingAreas(pos: Vector3d): Set<Area>

	fun intersectingAreas(pos: BlockPos): Set<Area>

	fun dataChanged()

	fun dataChanged(area: Area, type: AreaUpdateType)

	fun sendDataToPlayer(player: ServerPlayerEntity)

	fun getNumAreasJoined(playerUuid: UUID): Int

	fun canJoinArea(playerUuid: UUID): Boolean

	fun increasePlayerAreasNum(playerUuid: UUID)

	fun decreasePlayerAreasNum(playerUuid: UUID)
}
