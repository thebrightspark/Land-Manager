package brightspark.landmanager.command.optional

import brightspark.landmanager.command.AbstractCommand
import brightspark.landmanager.command.LMCommand
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.util.thenArgument
import net.minecraft.util.text.TranslationTextComponent

object PassivesCommand : AbstractCommand(
	"passives",
	{
		thenArgument(AREA, AreaArgument) {
			executes { context ->
				LMCommand.permissionCommand(
					context,
					{ it.togglePassiveSpawning() },
					{ TranslationTextComponent("lm.command.passives.success", it.canPassiveSpawn, it.name) }
				)
			}
		}
	}
)
