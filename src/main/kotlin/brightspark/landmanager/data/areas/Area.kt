package brightspark.landmanager.data.areas

import brightspark.ksparklib.api.extensions.toVec3d
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.ListNBT
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.INBTSerializable
import org.apache.commons.lang3.builder.EqualsBuilder
import java.util.*
import kotlin.math.max
import kotlin.math.min

class Area : INBTSerializable<CompoundNBT> {
	companion object {
		private val AREA_NAME = Regex("^\\w+\$")

		fun validateName(areaName: String): Boolean = AREA_NAME.matches(areaName)
	}

	@set:JvmName("_setName")
	lateinit var name: String
		private set
	lateinit var dim: ResourceLocation
		private set
	lateinit var minPos: BlockPos
		private set
	lateinit var maxPos: BlockPos
		private set
	var owner: UUID? = null
	val members = mutableSetOf<UUID>()

	// TODO: Have defaults configurable?
	var canPassiveSpawn = true
	var canHostileSpawn = true
	var explosions = true
	var interactions = false
	private var cachedAABB: AxisAlignedBB? = null

	constructor(name: String, dimension: ResourceLocation, position1: BlockPos, position2: BlockPos) {
		this.name = name
		dim = dimension
		minPos = BlockPos(
			min(position1.x, position2.x),
			min(position1.y, position2.y),
			min(position1.z, position2.z)
		)
		maxPos = BlockPos(
			max(position1.x, position2.x),
			max(position1.y, position2.y),
			max(position1.z, position2.z)
		)
	}

	constructor(nbt: CompoundNBT) {
		deserializeNBT(nbt)
	}

	fun setName(name: String): Boolean = if (validateName(name)) {
		this.name = name
		true
	} else {
		false
	}

	fun isOwner(playerUuid: UUID): Boolean = owner == playerUuid

	fun addMember(playerUuid: UUID): Boolean = members.add(playerUuid)

	fun removeMember(playerUuid: UUID): Boolean = members.remove(playerUuid)

	fun isMember(playerUuid: UUID): Boolean = isOwner(playerUuid) || members.contains(playerUuid)

	fun togglePassiveSpawning() {
		canPassiveSpawn = canPassiveSpawn.not()
	}

	fun toggleHostileSpawning() {
		canHostileSpawn = canHostileSpawn.not()
	}

	fun toggleExplosions() {
		explosions = explosions.not()
	}

	fun toggleInteractions() {
		interactions = interactions.not()
	}

	fun asAABB(): AxisAlignedBB {
		if (cachedAABB == null)
			cachedAABB = AxisAlignedBB(minPos.toVec3d().add(0.4, 0.4, 0.4), maxPos.toVec3d().add(0.6, 0.6, 0.6))
		return cachedAABB!!
	}

	fun intersects(area: Area): Boolean = asAABB().intersects(area.asAABB())

	fun intersects(pos: Vector3d): Boolean = asAABB().contains(pos)

	fun intersects(pos: BlockPos): Boolean = intersects(pos.toVec3d().add(0.5, 0.5, 0.5))

	fun extendToMinMaxY(world: World) {
		minPos = BlockPos(minPos.x, 0, minPos.z)
		maxPos = BlockPos(maxPos.x, world.height, maxPos.z)
		cachedAABB = null
	}

	override fun serializeNBT(): CompoundNBT = CompoundNBT().apply {
		putString("name", name)
		putString("dimension", dim.toString())
		putLong("position1", minPos.toLong())
		putLong("position2", maxPos.toLong())
		owner?.let { putUniqueId("player", it) }
		if (members.isNotEmpty()) {
			put("members", ListNBT().apply {
				members.forEach {
					add(CompoundNBT().apply { putUniqueId("uuid", it) })
				}
			})
		}
		putBoolean("passive", canPassiveSpawn)
		putBoolean("hostile", canHostileSpawn)
		putBoolean("explosions", explosions)
		putBoolean("interact", interactions)
	}

	override fun deserializeNBT(nbt: CompoundNBT) {
		name = nbt.getString("name")
		dim = ResourceLocation(nbt.getString("dimension"))
		minPos = BlockPos.fromLong(nbt.getLong("position1"))
		maxPos = BlockPos.fromLong(nbt.getLong("position2"))
		cachedAABB = null
		if (nbt.hasUniqueId("player"))
			owner = nbt.getUniqueId("player")
		members.clear()
		if (nbt.contains("members")) {
			nbt.getList("members", Constants.NBT.TAG_COMPOUND).forEach {
				members.add((it as CompoundNBT).getUniqueId("uuid"))
			}
		}
		canPassiveSpawn = nbt.getBoolean("passive")
		canHostileSpawn = nbt.getBoolean("hostile")
		explosions = nbt.getBoolean("explosions")
		interactions = nbt.getBoolean("interact")
	}

	override fun equals(other: Any?): Boolean {
		if (other == null) return false
		if (other === this) return true
		if (other !is Area) return false
		return EqualsBuilder()
			.append(name, other.name)
			.append(dim, other.dim)
			.append(minPos, other.minPos)
			.append(maxPos, other.maxPos)
			.isEquals
	}

	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + dim.hashCode()
		return result
	}
}
