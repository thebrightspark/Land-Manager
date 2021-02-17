package brightspark.landmanager.command.op

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.extensions.thenArgument
import brightspark.landmanager.command.LMCommand.AREA_REGEX
import brightspark.landmanager.command.LMCommand.PAGE
import brightspark.landmanager.util.Util
import brightspark.landmanager.util.requests
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.text.*
import net.minecraft.util.text.event.ClickEvent
import net.minecraft.util.text.event.HoverEvent

object RequestsCommand : Command(
	"requests",
	{
		// requests
		executes { RequestsCommand.doCommand(it) }
		thenArgument(PAGE, IntegerArgumentType.integer(1)) {
			// requests <pageNum>
			executes { RequestsCommand.doCommand(it, RequestsCommand.getPageNum(it)) }
			thenArgument(AREA_REGEX, StringArgumentType.word()) {
				// requests <pageNum> <areaName>
				executes {
					RequestsCommand.doCommand(
						it,
						RequestsCommand.getPageNum(it),
						RequestsCommand.getAreaName(it)
					)
				}
			}
		}
		thenArgument(AREA_REGEX, StringArgumentType.word()) {
			// requests <areaName>
			executes { RequestsCommand.doCommand(it, areaName = RequestsCommand.getAreaName(it)) }
		}
	}
) {
	private fun getPageNum(context: CommandContext<CommandSource>): Int = IntegerArgumentType.getInteger(context, PAGE)

	private fun getAreaName(context: CommandContext<CommandSource>): String =
		StringArgumentType.getString(context, AREA_REGEX)

	private fun doCommand(context: CommandContext<CommandSource>, page: Int = 1, areaName: String = ""): Int {
		val source = context.source
		val server = source.server
		val requestsWsd = server.requests
		val requests = if (areaName.isBlank()) requestsWsd.getAll()
			.sortedBy { it.id } else requestsWsd.getByAreaNameRegex(areaName)
		source.sendFeedback(
			Util.createListMessage(
			source.source is PlayerEntity,
			requests,
			page,
			"lm.command.requests.title",
			{ "/lm op requests $it $areaName" },
			{ request ->
				val reqId = request.id
				StringTextComponent("$reqId: ")
					.append(createAction(true, reqId))
					.appendString(" ")
					.append(createAction(false, reqId))
					.appendString(" ${request.getPlayerName(server)} -> ${request.areaName} [${request.date}]")
			}
		), false)
		return requests.size
	}

	private fun createAction(approve: Boolean, requestId: Int): ITextComponent =
		StringTextComponent("[${if (approve) "/" else "X"}]").modifyStyle {
			it.color = Color.fromTextFormatting(if (approve) TextFormatting.GREEN else TextFormatting.RED)
			val actionName = if (approve) "approve" else "disapprove"
			it.hoverEvent = HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				TranslationTextComponent("lm.command.requests.$actionName", requestId)
			)
			it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/lm op $actionName $requestId")
			return@modifyStyle it
		}
}
