package brightspark.landmanager.command.nonop

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.extensions.sendToPlayer
import brightspark.landmanager.LandManager
import brightspark.landmanager.message.MessageShowArea
import net.minecraft.util.text.TranslationTextComponent

object ShowOffCommand : Command(
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
