package brightspark.landmanager.data

import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.common.util.LazyOptional

class SimpleCapabilityProvider<C : INBTSerializable<CompoundNBT>>(
	capSupplier: () -> Capability<C>,
	instanceSupplier: () -> C
) : ICapabilitySerializable<CompoundNBT> {
	private val capability: Capability<C> by lazy(capSupplier)
	private val instance = LazyOptional.of(instanceSupplier)

	override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> =
		capability.orEmpty(cap, instance)

	override fun serializeNBT(): CompoundNBT = instance.map { it.serializeNBT() }.orElse(CompoundNBT())

	override fun deserializeNBT(nbt: CompoundNBT): Unit = instance.ifPresent { it.deserializeNBT(nbt) }
}
