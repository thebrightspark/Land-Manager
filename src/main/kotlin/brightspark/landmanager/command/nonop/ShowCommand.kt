package brightspark.landmanager.command.nonop

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.literal
import brightspark.ksparklib.api.sendToPlayer
import brightspark.ksparklib.api.thenArgument
import brightspark.landmanager.LandManager
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.message.MessageShowArea
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.util.text.TranslationTextComponent

object ShowCommand : Command {
	override val builder: LiteralArgumentBuilder<CommandSource> = literal("show") {
		// show
		executes {
			LandManager.NETWORK.sendToPlayer(MessageShowArea(null), it.source.asPlayer())
			return@executes 1
		}
		thenArgument(AREA, AreaArgument) {
			// show <area>
			executes {
				val areaName = AreaArgument.get(it, AREA).name
				LandManager.NETWORK.sendToPlayer(MessageShowArea(areaName), it.source.asPlayer())
				it.source.sendFeedback(TranslationTextComponent("lm.command.show.showing", areaName), false)
				return@executes 1
			}
		}
	}
}
