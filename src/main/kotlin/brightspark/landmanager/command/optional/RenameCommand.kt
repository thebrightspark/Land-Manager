package brightspark.landmanager.command.optional

import brightspark.landmanager.command.AbstractCommand
import brightspark.landmanager.command.LMCommand
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.util.canEditArea
import brightspark.landmanager.util.getWorldCapForArea
import brightspark.landmanager.util.thenArgument
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.util.text.TranslationTextComponent

object RenameCommand : AbstractCommand(
	"rename",
	{
		thenArgument(AREA, AreaArgument) {
			thenArgument(RenameCommand.NEW_NAME, StringArgumentType.word()) {
				executes { context ->
					val source = context.source
					val area = AreaArgument.get(context, AREA)
					val areaName = area.name
					if (!source.source.canEditArea(area))
						throw LMCommand.ERROR_CANT_EDIT.create(areaName)
					val newName = StringArgumentType.getString(context, RenameCommand.NEW_NAME)
					if (!Area.validateName(newName))
						throw RenameCommand.INVALID_NAME.create()
					val areas = source.server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(areaName)

					val result = areas.renameArea(areaName, newName)
					val message = if (result)
						TranslationTextComponent("lm.command.rename.success", areaName, newName)
					else
						TranslationTextComponent("lm.command.rename.invalid", newName)
					source.sendFeedback(message, true)
					return@executes if (result) 1 else 0
				}
			}
		}
	}
) {
	private const val NEW_NAME = "newAreaName"
	private val INVALID_NAME =
		SimpleCommandExceptionType(TranslationTextComponent("message.landmanager.create.invalid_name"))
}
