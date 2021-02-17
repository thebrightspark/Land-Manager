package brightspark.landmanager.command.op

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.extensions.sendMessage
import brightspark.ksparklib.api.extensions.thenArgument
import brightspark.landmanager.AreaDeletedEvent
import brightspark.landmanager.LandManager
import brightspark.landmanager.command.LMCommand
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.util.AreaChangeType
import brightspark.landmanager.util.getWorldCapForArea
import brightspark.landmanager.util.requests
import net.minecraft.server.MinecraftServer
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.common.MinecraftForge
import java.util.*

object DeleteCommand : Command(
	"delete",
	{
		thenArgument(AREA, AreaArgument) {
			executes { context ->
				val server = context.source.server
				val area = AreaArgument.get(context, AREA)
				val areaName = area.name
				val areas = server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(areaName)
				if (areas.removeArea(areaName)) {
					MinecraftForge.EVENT_BUS.post(AreaDeletedEvent(area))
					server.requests.deleteAllForArea(areaName)
					context.source.sendFeedback(TranslationTextComponent("lm.command.delete.deleted", areaName), true)
					// Notify all area members that the area was deleted
					area.owner?.let { DeleteCommand.notifyPlayer(server, it, areaName) }
					area.members.forEach { DeleteCommand.notifyPlayer(server, it, areaName) }
					// Send chat message to OPs
					LandManager.areaChange(context, AreaChangeType.DELETE, areaName)
					return@executes 1
				}
				context.source.sendFeedback(TranslationTextComponent("lm.command.delete.failed", areaName), true)
				return@executes 0
			}
		}
	}
) {
	private fun notifyPlayer(server: MinecraftServer, uuid: UUID, areaName: String) =
		server.playerList.getPlayerByUUID(uuid)
			?.sendMessage(TranslationTextComponent("lm.command.delete.notify", areaName))
}
