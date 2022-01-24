package brightspark.landmanager.init

import brightspark.landmanager.block.HomeBlock
import brightspark.landmanager.util.setRegName
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraftforge.event.RegistryEvent
import thedarkcolour.kotlinforforge.forge.objectHolder

object LMBlocks {
	val HOME: Block by objectHolder("home")

	fun register(event: RegistryEvent.Register<Block>) = event.registry.register(
		HomeBlock(props(Material.WOOD)).setRegName("home")
	)

	private fun props(material: Material): AbstractBlock.Properties = AbstractBlock.Properties.create(material)
}
