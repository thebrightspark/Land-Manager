package brightspark.landmanager.command.nonop

import brightspark.landmanager.LandManager
import brightspark.landmanager.command.AbstractCommand
import brightspark.landmanager.command.LMCommand
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.LMCommand.PLAYER
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.data.areas.AreaUpdateType
import brightspark.landmanager.data.areas.AreasCapability
import brightspark.landmanager.util.AreaChangeType
import brightspark.landmanager.util.canEditArea
import brightspark.landmanager.util.getWorldCapForArea
import brightspark.landmanager.util.thenArgument
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.arguments.EntityArgument
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.util.text.TranslationTextComponent

object SetOwnerCommand : AbstractCommand(
	"setowner",
	{
		thenArgument(AREA, AreaArgument) {
			requires { it.hasPermissionLevel(2) }
			// setowner <area>
			executes {
				val area = AreaArgument.get(it, AREA)
				val cap = it.source.server.getWorldCapForArea(area) ?: throw LMCommand.ERROR_NO_AREA.create(area.name)
				return@executes SetOwnerCommand.doCommand(it, cap, area)
			}
			thenArgument(PLAYER, EntityArgument.player()) {
				requires { true }
				// setowner <area> <player>
				executes {
					val area = AreaArgument.get(it, AREA)
					val player = EntityArgument.getPlayer(it, PLAYER)
					if (!player.canEditArea(area))
						throw LMCommand.ERROR_CANT_EDIT.create(area.name)
					val cap = it.source.server.getWorldCapForArea(area)
						?: throw LMCommand.ERROR_NO_AREA.create(area.name)
					return@executes SetOwnerCommand.doCommand(it, cap, area, player)
				}
			}
		}
	}
) {
	private fun doCommand(
		context: CommandContext<CommandSource>,
		cap: AreasCapability,
		area: Area,
		player: ServerPlayerEntity? = null
	): Int {
		val playerUuid = player?.uniqueID
		area.owner?.let { area.addMember(it) }
		area.owner = playerUuid
		playerUuid?.let { area.removeMember(it) }
		cap.dataChanged(area, AreaUpdateType.CHANGE)
		context.source.sendFeedback(
			TranslationTextComponent(
				"lm.command.setowner.success", area.name, player?.name?.unformattedComponentText
					?: "None"
			), true
		)
		LandManager.areaChange(context.source.server, playerUuid?.let { AreaChangeType.CLEAR_ALLOCATION }
			?: AreaChangeType.ALLOCATE, area.name)
		return 1
	}
}
