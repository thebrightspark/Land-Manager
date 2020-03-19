package brightspark.landmanager.command.nonop

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.literal
import brightspark.ksparklib.api.thenArgument
import brightspark.landmanager.command.LMCommand.AREA_REGEX
import brightspark.landmanager.command.LMCommand.PAGE
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.util.Util
import brightspark.landmanager.util.getAreas
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.util.text.StringTextComponent
import java.util.*
import kotlin.streams.toList

object MyAreasCommand : Command {
	override val builder: LiteralArgumentBuilder<CommandSource> = literal("myareas") {
		// myareas
		executes { doCommand(it) }
		thenArgument(PAGE, IntegerArgumentType.integer(1)) {
			// myareas <page>
			executes { doCommand(it, IntegerArgumentType.getInteger(it, PAGE)) }
			thenArgument(AREA_REGEX, StringArgumentType.word()) {
				// myareas <page> <areaName>
				executes { doCommand(it, IntegerArgumentType.getInteger(it, PAGE), StringArgumentType.getString(it, AREA_REGEX)) }
			}
		}
		thenArgument(AREA_REGEX, StringArgumentType.word()) {
			// myareas <areaName>
			executes { doCommand(it, areaName = StringArgumentType.getString(it, AREA_REGEX)) }
		}
	}

	private fun doCommand(context: CommandContext<CommandSource>, page: Int = 1, areaName: String = ""): Int {
		val source = context.source
		val uuid = source.asPlayer().uniqueID
		val regex = Regex(areaName)
		val areas = source.server.getAreas { it.isOwner(uuid) && (areaName.isBlank() || regex.matches(it.name)) }
			.sorted(Comparator.comparing(Area::name))
			.toList()
		val message = Util.createListMessage(
			true,
			areas,
			page,
			"lm.command.myareas.title",
			{ "/lm myareas $it $areaName" },
			{ StringTextComponent("  ${it.name}") }
		)
		source.sendFeedback(message, false)
		return areas.size
	}
}
