package brightspark.landmanager

import brightspark.landmanager.data.areas.AreasCapability
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject

@CapabilityInject(AreasCapability::class)
@JvmField
val cap_areas: Capability<AreasCapability>? = null
