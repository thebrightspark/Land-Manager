package brightspark.landmanager.command.optional

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.literal
import brightspark.ksparklib.api.thenArgument
import brightspark.landmanager.command.LMCommand
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.argumentType.AreaArgument
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.util.text.TranslationTextComponent

object ExplosionsCommand : Command {
	override val builder: LiteralArgumentBuilder<CommandSource> = literal("explosions") {
		thenArgument(AREA, AreaArgument) {
			executes { context ->
				LMCommand.permissionCommand(
					context,
					{ it.toggleExplosions() },
					{ TranslationTextComponent("lm.command.explosions.success", it.explosions, it.name) }
				)
			}
		}
	}
}
