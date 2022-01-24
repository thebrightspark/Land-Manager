package brightspark.landmanager.command.optional

import brightspark.landmanager.command.AbstractCommand
import brightspark.landmanager.command.LMCommand
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.util.thenArgument
import net.minecraft.util.text.TranslationTextComponent

object HostilesCommand : AbstractCommand(
	"hostiles",
	{
		thenArgument(AREA, AreaArgument) {
			executes { context ->
				LMCommand.permissionCommand(
					context,
					{ it.toggleHostileSpawning() },
					{ TranslationTextComponent("lm.command.hostiles.success", it.canHostileSpawn, it.name) }
				)
			}
		}
	}
)
