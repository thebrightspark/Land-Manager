package brightspark.landmanager.command.op

import brightspark.landmanager.AreaClaimApprovalEvent
import brightspark.landmanager.LandManager
import brightspark.landmanager.command.AbstractCommand
import brightspark.landmanager.command.LMCommand
import brightspark.landmanager.command.LMCommand.REQUEST
import brightspark.landmanager.command.argumentType.RequestArgument
import brightspark.landmanager.util.*
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.common.MinecraftForge

object ApproveCommand : AbstractCommand(
	"approve",
	{
		thenArgument(REQUEST, RequestArgument) {
			executes { context ->
				val request = RequestArgument.get(context, REQUEST)
				val server = context.source.server
				val requests = server.requests
				val areaName = request.areaName
				val areas = server.getWorldCapForArea(areaName) ?: run {
					requests.deleteAllForArea(areaName)
					throw LMCommand.ERROR_NO_AREA.create(areaName)
				}
				val area = areas.getArea(areaName) ?: throw LMCommand.ERROR_NO_AREA.create(areaName)
				if (MinecraftForge.EVENT_BUS.post(AreaClaimApprovalEvent(request, area, context.source)))
					return@executes 0

				// Approve request
				areas.setOwner(areaName, request.playerUuid)
				context.source.sendFeedback(
					TranslationTextComponent(
						"lm.command.approve.success",
						request.id,
						request.getPlayerName(server),
						areaName
					),
					true
				)
				LandManager.areaChange(context, AreaChangeType.CLAIM, areaName)
				// Delete all requests for the area
				requests.deleteAllForArea(areaName)
				// Notify the player if they're online
				server.playerList.getPlayerByUUID(request.playerUuid)?.sendMessage(
					TranslationTextComponent("lm.command.approve.playerMessage", areaName, context.getSenderName())
						.mergeStyle(TextFormatting.DARK_GREEN)
				)
				return@executes 1
			}
		}
	}
)
