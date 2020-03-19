package brightspark.landmanager.command

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.literal
import brightspark.ksparklib.api.thenCommand
import brightspark.ksparklib.api.thenLiteral
import brightspark.landmanager.LMConfig
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.command.nonop.*
import brightspark.landmanager.command.op.ApproveCommand
import brightspark.landmanager.command.op.DeleteCommand
import brightspark.landmanager.command.op.DisapproveCommand
import brightspark.landmanager.command.op.RequestsCommand
import brightspark.landmanager.command.optional.*
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.data.areas.AreaUpdateType
import brightspark.landmanager.util.canEditArea
import brightspark.landmanager.util.getWorldCapForArea
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.command.CommandSource
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.TranslationTextComponent

object LMCommand : Command {
	override val builder: LiteralArgumentBuilder<CommandSource> = literal("lm") {
		// Non-op
		thenCommand(AreaCommand)
		thenCommand(AreasCommand)
		thenCommand(ClaimCommand)
		thenCommand(MembersCommand)
		thenCommand(MyAreasCommand)
		thenCommand(SetOwnerCommand)
		thenCommand(ShowCommand)
		thenCommand(ShowOffCommand)

		// Op
		thenLiteral("op") {
			requires { it.hasPermissionLevel(2) }
			thenCommand(ApproveCommand)
			thenCommand(DeleteCommand)
			thenCommand(DisapproveCommand)
			thenCommand(RequestsCommand)

			// Optional
			if (!LMConfig.explosions)
				thenCommand(ExplosionsCommand)
			if (!LMConfig.hostileSpawning)
				thenCommand(HostilesCommand)
			if (!LMConfig.interactions)
				thenCommand(InteractionsCommand)
			if (!LMConfig.passiveSpawning)
				thenCommand(PassivesCommand)
			if (!LMConfig.rename)
				thenCommand(RenameCommand)
			if (!LMConfig.tool)
				thenCommand(ToolCommand)
		}

		// Optional
		if (LMConfig.explosions)
			thenCommand(ExplosionsCommand)
		if (LMConfig.hostileSpawning)
			thenCommand(HostilesCommand)
		if (LMConfig.interactions)
			thenCommand(InteractionsCommand)
		if (LMConfig.passiveSpawning)
			thenCommand(PassivesCommand)
		if (LMConfig.rename)
			thenCommand(RenameCommand)
		if (LMConfig.tool)
			thenCommand(ToolCommand)
	}

	const val AREA = "areaName"
	const val PAGE = "pageNum"
	const val AREA_REGEX = "areaNameRegex"
	const val PLAYER = "playerName"
	const val REQUEST = "requestId"

	val ERROR_CANT_EDIT = DynamicCommandExceptionType { TranslationTextComponent("lm.command.noPerm", it) }
	val ERROR_NO_AREA = DynamicCommandExceptionType { TranslationTextComponent("lm.command.none", it) }

	fun permissionCommand(context: CommandContext<CommandSource>, action: (Area) -> Unit, feedback: (Area) -> ITextComponent): Int {
		val source = context.source
		val area = AreaArgument.get(context, AREA)
		val areas = source.server.getWorldCapForArea(area) ?: throw ERROR_NO_AREA.create(area.name)
		val sender = source.source
		if (!sender.canEditArea(area))
			throw ERROR_CANT_EDIT.create(area.name)
		action(area)
		areas.dataChanged(area, AreaUpdateType.CHANGE)
		source.sendFeedback(feedback(area), true)
		return 1
	}
}
