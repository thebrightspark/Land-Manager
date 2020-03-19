package brightspark.landmanager.command.nonop

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.literal
import brightspark.ksparklib.api.sendToPlayer
import brightspark.landmanager.LandManager
import brightspark.landmanager.message.MessageShowArea
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.util.text.TranslationTextComponent

object ShowOffCommand : Command {
	override val builder: LiteralArgumentBuilder<CommandSource> = literal("showoff") {
		// showoff
		executes {
			LandManager.NETWORK.sendToPlayer(MessageShowArea(""), it.source.asPlayer())
			it.source.sendFeedback(TranslationTextComponent("lm.command.showoff"), false)
			return@executes 1
		}
	}
}
