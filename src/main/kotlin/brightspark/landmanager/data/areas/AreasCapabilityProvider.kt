package brightspark.landmanager.data.areas

import brightspark.landmanager.LandManager
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.LazyOptional

class AreasCapabilityProvider : ICapabilitySerializable<CompoundNBT> {
	private var areas: LazyOptional<AreasCapability> = LazyOptional.of { AreasCapabilityImpl() }

	@Suppress("UNCHECKED_CAST")
	override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> =
		LandManager.CAP_AREAS!!.orEmpty(cap, areas)

	override fun deserializeNBT(nbt: CompoundNBT) = areas.ifPresent { it.deserializeNBT(nbt) }

	override fun serializeNBT(): CompoundNBT = areas.map { it.serializeNBT() }.orElse(CompoundNBT())
}
