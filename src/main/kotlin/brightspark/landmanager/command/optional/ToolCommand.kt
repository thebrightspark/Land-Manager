package brightspark.landmanager.command.optional

import brightspark.landmanager.area_create
import brightspark.landmanager.command.AbstractCommand
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TranslationTextComponent

object ToolCommand : AbstractCommand(
	"tool",
	{
		executes {
			val player = it.source.asPlayer()
			val result = player.addItemStackToInventory(ItemStack(area_create!!))
			if (!result)
				it.source.sendFeedback(TranslationTextComponent("lm.command.tool.inventory"), false)
			return@executes if (result) 1 else 0
		}
	}
)
