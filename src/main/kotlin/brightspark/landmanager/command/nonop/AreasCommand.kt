package brightspark.landmanager.command.nonop

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.literal
import brightspark.ksparklib.api.thenArgument
import brightspark.landmanager.command.LMCommand.AREA_REGEX
import brightspark.landmanager.command.LMCommand.PAGE
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.util.Util
import brightspark.landmanager.util.getAreas
import brightspark.landmanager.util.getUsernameFromUuid
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.StringTextComponent
import java.util.*
import kotlin.streams.toList

object AreasCommand : Command {
	override val builder: LiteralArgumentBuilder<CommandSource> = literal("areas") {
		// areas
		executes { doCommand(it) }
		thenArgument(PAGE, IntegerArgumentType.integer(1)) {
			// areas <pageNum>
			executes { doCommand(it, getPageNum(it)) }
			thenArgument(AREA_REGEX, StringArgumentType.word()) {
				// areas <pageNum> <areaName>
				executes { doCommand(it, getPageNum(it), getAreaName(it)) }
			}
		}
		thenArgument(AREA_REGEX, StringArgumentType.word()) {
			// area <areaName>
			executes { doCommand(it, areaName = getAreaName(it)) }
		}
	}

	private fun getPageNum(context: CommandContext<CommandSource>): Int = IntegerArgumentType.getInteger(context, PAGE)

	private fun getAreaName(context: CommandContext<CommandSource>): String = StringArgumentType.getString(context, AREA_REGEX)

	private fun doCommand(context: CommandContext<CommandSource>, page: Int = 1, areaName: String = ""): Int {
		val source = context.source
		val server = source.server
		val regex = Regex(areaName)
		val areas = server.getAreas { areaName.isBlank() || regex.matches(it.name) }
			.sorted(Comparator.comparing(Area::name))
			.toList()
		source.sendFeedback(Util.createListMessage(
			source.source is PlayerEntity,
			areas,
			page,
			"lm.command.areas.title",
			{ "/lm areas $it $areaName" },
			{ area ->
				StringTextComponent(area.owner?.let { "  ${area.name} -> ${server.getUsernameFromUuid(it)}" }
					?: "  ${area.name}")
			}
		), false)
		return areas.size
	}
}
