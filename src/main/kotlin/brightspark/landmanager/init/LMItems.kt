package brightspark.landmanager.init

import brightspark.landmanager.LandManager
import brightspark.landmanager.item.AreaCreateItem
import brightspark.landmanager.util.setRegName
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraftforge.event.RegistryEvent

object LMItems {
	fun register(event: RegistryEvent.Register<Item>) = event.registry.registerAll(
		AreaCreateItem(props().maxStackSize(1)).setRegName("area_create"),
		blockItem(LMBlocks.HOME)
	)

	private fun props(): Item.Properties = Item.Properties().apply { group(LandManager.group) }

	private fun blockItem(block: Block): Item = BlockItem(block, props()).setRegName(block.registryName!!.path)
}
