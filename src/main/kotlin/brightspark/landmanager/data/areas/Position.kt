package brightspark.landmanager.data.areas

import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.util.INBTSerializable

class Position : INBTSerializable<CompoundNBT> {
	var dimensionId: Int = 0
	lateinit var position: BlockPos

	constructor(dimensionId: Int, position: BlockPos) {
		this.dimensionId = dimensionId
		this.position = position
	}

	constructor(nbt: CompoundNBT) {
		deserializeNBT(nbt)
	}

	override fun serializeNBT() = CompoundNBT().apply {
		putInt("dimension", dimensionId)
		putLong("position", position.toLong())
	}

	override fun deserializeNBT(nbt: CompoundNBT) {
		dimensionId = nbt.getInt("dimension")
		position = BlockPos.fromLong(nbt.getLong("position"))
	}
}
