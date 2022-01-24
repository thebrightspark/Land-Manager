package brightspark.landmanager.command.nonop

import brightspark.landmanager.LandManager
import brightspark.landmanager.command.AbstractCommand
import brightspark.landmanager.message.MessageShowArea
import brightspark.landmanager.util.sendToPlayer
import net.minecraft.util.text.TranslationTextComponent

object ShowOffCommand : AbstractCommand(
	"showoff",
	{
		// showoff
		executes {
			LandManager.NETWORK.sendToPlayer(MessageShowArea(""), it.source.asPlayer())
			it.source.sendFeedback(TranslationTextComponent("lm.command.showoff"), false)
			return@executes 1
		}
	}
)
