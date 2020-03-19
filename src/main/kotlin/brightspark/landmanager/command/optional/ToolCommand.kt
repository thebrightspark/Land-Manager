package brightspark.landmanager.command.optional

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.literal
import brightspark.landmanager.area_create
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.item.ItemStack
import net.minecraft.util.text.TranslationTextComponent

object ToolCommand : Command {
	override val builder: LiteralArgumentBuilder<CommandSource> = literal("tool") {
		executes {
			val player = it.source.asPlayer()
			val result = player.addItemStackToInventory(ItemStack(area_create!!))
			if (!result)
				it.source.sendFeedback(TranslationTextComponent("lm.command.tool.inventory"), false)
			return@executes if (result) 1 else 0
		}
	}
}
