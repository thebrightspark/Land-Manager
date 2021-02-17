package brightspark.landmanager.command.nonop

import brightspark.ksparklib.api.Command
import brightspark.ksparklib.api.extensions.appendString
import brightspark.ksparklib.api.extensions.appendStyledString
import brightspark.ksparklib.api.extensions.sendMessage
import brightspark.ksparklib.api.extensions.thenArgument
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.util.getUsernameFromUuid
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent

object AreaCommand : Command(
	"area",
	{
		thenArgument(AREA, AreaArgument) {
			// area <name>
			executes { AreaCommand.doCommand(it, AreaArgument.get(it, AREA)) }
		}
	}
) {
	private fun doCommand(context: CommandContext<CommandSource>, area: Area): Int {
		val server = context.source.server
		val player = context.source.asPlayer()

		val ownerName = area.owner?.let { ownerUuid ->
			server.getUsernameFromUuid(ownerUuid)?.let { StringTextComponent(it) }
		} ?: TranslationTextComponent("lm.command.area.none")

		val members = area.members.mapNotNull { server.getUsernameFromUuid(it) }.sorted().let {
			if (it.isEmpty())
				TranslationTextComponent("lm.command.area.none")
			else
				StringTextComponent(it.joinToString(", "))
		}

		player.sendMessage(
			StringTextComponent("").mergeStyle(TextFormatting.WHITE)
				.append(TranslationTextComponent("lm.command.area.name").mergeStyle(TextFormatting.YELLOW))
				.appendString(" ${area.name}")
				.appendString("\n ").append(goldText("lm.command.area.owner")).appendString(" ").append(ownerName)
				.appendString("\n ").append(goldText("lm.command.area.members")).appendString(" ").append(members)
				.appendString("\n ").append(goldText("lm.command.area.dim")).appendString(" ${area.dim}")
				.appendString("\n ").append(goldText("lm.command.area.posmin")).appendString(" ")
				.append(posToText(area.minPos))
				.appendString("\n ").append(goldText("lm.command.area.posmax")).appendString(" ")
				.append(posToText(area.maxPos))
				.appendString("\n ").append(goldText("lm.command.area.passives")).appendString(" ")
				.append(boolToText(area.canPassiveSpawn))
				.appendString("\n ").append(goldText("lm.command.area.hostiles")).appendString(" ")
				.append(boolToText(area.canHostileSpawn))
				.appendString("\n ").append(goldText("lm.command.area.explosions")).appendString(" ")
				.append(boolToText(area.explosions))
				.appendString("\n ").append(goldText("lm.command.area.interactions")).appendString(" ")
				.append(boolToText(area.interactions))
		)
		return 1
	}

	private fun goldText(langKey: String): ITextComponent =
		TranslationTextComponent(langKey).mergeStyle(TextFormatting.GOLD)

	private fun posToText(pos: BlockPos): ITextComponent = StringTextComponent("")
		.appendStyledString("X: ", TextFormatting.YELLOW).appendString("${pos.x}, ")
		.appendStyledString("Y: ", TextFormatting.YELLOW).appendString("${pos.y}, ")
		.appendStyledString("Z: ", TextFormatting.YELLOW).appendString(pos.z)

	private fun boolToText(bool: Boolean): ITextComponent =
		TranslationTextComponent(if (bool) "message.landmanager.misc.true" else "message.landmanager.misc.false")
}
