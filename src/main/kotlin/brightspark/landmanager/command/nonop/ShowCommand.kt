package brightspark.landmanager.command.nonop

import brightspark.landmanager.LandManager
import brightspark.landmanager.command.AbstractCommand
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.message.MessageShowArea
import brightspark.landmanager.util.sendToPlayer
import brightspark.landmanager.util.thenArgument
import net.minecraft.util.text.TranslationTextComponent

object ShowCommand : AbstractCommand(
	"show",
	{
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
)
