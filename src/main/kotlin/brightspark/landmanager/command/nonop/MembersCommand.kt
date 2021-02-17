package brightspark.landmanager.command.nonop

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.extensions.thenArgument
import brightspark.ksparklib.api.extensions.thenLiteral
import brightspark.landmanager.command.LMCommand
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.LMCommand.PLAYER
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.data.areas.AreaUpdateType
import brightspark.landmanager.util.canEditArea
import brightspark.landmanager.util.getWorldCapForArea
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.command.arguments.EntityArgument
import net.minecraft.util.text.TranslationTextComponent

object MembersCommand : Command(
	"members",
	{
		thenLiteral("add") {
			thenArgument(AREA, AreaArgument) {
				thenArgument(PLAYER, EntityArgument.player()) {
					// members add <area> <player>
					executes { context ->
						val area = AreaArgument.get(context, AREA)
						val player = EntityArgument.getPlayer(context, PLAYER)
						if (!player.canEditArea(area))
							throw LMCommand.ERROR_CANT_EDIT.create(area.name)

						val server = context.source.server
						val cap = server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(area.name)
						val uuid = player.uniqueID
						if (!cap.canJoinArea(uuid))
							throw MembersCommand.CANT_JOIN.create(cap.getNumAreasJoined(uuid))

						if (area.addMember(uuid)) {
							cap.increasePlayerAreasNum(uuid)
							cap.dataChanged(area, AreaUpdateType.CHANGE)
							context.source.sendFeedback(TranslationTextComponent("lm.command.members.add.success", player.displayName, area.name), true)
							return@executes 1
						}
						context.source.sendFeedback(TranslationTextComponent("lm.command.members.add.already", player.displayName, area.name), true)
						return@executes 0
					}
				}
			}
		}
		thenLiteral("remove") {
			thenArgument(AREA, AreaArgument) {
				thenArgument(PLAYER, EntityArgument.player()) {
					// members remove <area> <player>
					executes { context ->
						val area = AreaArgument.get(context, AREA)
						val player = EntityArgument.getPlayer(context, PLAYER)
						if (!player.canEditArea(area))
							throw LMCommand.ERROR_CANT_EDIT.create(area.name)

						val server = context.source.server
						val cap = server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(area.name)
						val uuid = player.uniqueID
						if (!cap.canJoinArea(uuid))
							throw MembersCommand.CANT_JOIN.create(cap.getNumAreasJoined(uuid))

						if (area.removeMember(uuid)) {
							cap.decreasePlayerAreasNum(uuid)
							cap.dataChanged(area, AreaUpdateType.CHANGE)
							context.source.sendFeedback(
								TranslationTextComponent(
									"lm.command.members.remove.success",
									player.displayName,
									area.name
								), true
							)
							return@executes 1
						}
						context.source.sendFeedback(
							TranslationTextComponent(
								"lm.command.members.remove.already",
								player.displayName,
								area.name
							), true
						)
						return@executes 0
					}
				}
			}
		}
	}
) {
	private val CANT_JOIN =
		DynamicCommandExceptionType { TranslationTextComponent("message.landmanager.error.maxJoined", it) }
}
