package brightspark.landmanager.init

import brightspark.landmanager.data.DelegatingCapabilityStorage
import brightspark.landmanager.data.SimpleCapabilityProvider
import brightspark.landmanager.data.areas.AreasCapability
import brightspark.landmanager.data.areas.AreasCapabilityImpl
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent

object LMCapabilities {
	@set:CapabilityInject(AreasCapability::class)
	@JvmStatic
	lateinit var AREAS: Capability<AreasCapability>

	fun register() = regCap<AreasCapability>(::AreasCapabilityImpl)

	fun attach(event: AttachCapabilitiesEvent<World>) =
		event.addCapability(AreasCapability.RL, SimpleCapabilityProvider(::AREAS, ::AreasCapabilityImpl))

	private fun sendData(player: ServerPlayerEntity) =
		player.getCapability(AREAS).ifPresent { it.sendDataToPlayer(player) }

	fun playerLoggedIn(event: PlayerEvent.PlayerLoggedInEvent) = sendData(event.player as ServerPlayerEntity)

	fun playerChangedDimension(event: PlayerEvent.PlayerChangedDimensionEvent) =
		sendData(event.player as ServerPlayerEntity)

	fun playerRespawn(event: PlayerEvent.PlayerRespawnEvent) = sendData(event.player as ServerPlayerEntity)

	private inline fun <reified C : INBTSerializable<CompoundNBT>> regCap(noinline capInstanceSupplier: () -> C) =
		CapabilityManager.INSTANCE.register(C::class.java, DelegatingCapabilityStorage<C>(), capInstanceSupplier)
}
