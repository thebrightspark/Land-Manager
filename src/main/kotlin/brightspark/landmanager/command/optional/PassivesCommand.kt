package brightspark.landmanager.command.optional

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.extensions.thenArgument
import brightspark.landmanager.command.LMCommand
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.argumentType.AreaArgument
import net.minecraft.util.text.TranslationTextComponent

object PassivesCommand : Command(
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
