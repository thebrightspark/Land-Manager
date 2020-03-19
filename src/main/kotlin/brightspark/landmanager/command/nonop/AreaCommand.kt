package brightspark.landmanager.command.nonop

import brightspark.ksparklib.api.*
import brightspark.landmanager.command.LMCommand.AREA
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.util.getUsernameFromUuid
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent

object AreaCommand : Command {
	override val builder: LiteralArgumentBuilder<CommandSource> = literal("area") {
		thenArgument(AREA, AreaArgument) {
			// area <name>
			executes { doCommand(it, AreaArgument.get(it, AREA)) }
		}
	}

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
			StringTextComponent("").applyTextStyle(TextFormatting.WHITE)
				.appendSibling(TranslationTextComponent("lm.command.area.name").applyTextStyle(TextFormatting.YELLOW)).appendText(" ${area.name}")
				.appendText("\n ").appendSibling(goldText("lm.command.area.owner")).appendText(" ").appendSibling(ownerName)
				.appendText("\n ").appendSibling(goldText("lm.command.area.members")).appendText(" ").appendSibling(members)
				.appendText("\n ").appendSibling(goldText("lm.command.area.dim")).appendText(" ${area.dimId}")
				.appendText("\n ").appendSibling(goldText("lm.command.area.posmin")).appendText(" ").appendSibling(posToText(area.minPos))
				.appendText("\n ").appendSibling(goldText("lm.command.area.posmax")).appendText(" ").appendSibling(posToText(area.maxPos))
				.appendText("\n ").appendSibling(goldText("lm.command.area.passives")).appendText(" ").appendSibling(boolToText(area.canPassiveSpawn))
				.appendText("\n ").appendSibling(goldText("lm.command.area.hostiles")).appendText(" ").appendSibling(boolToText(area.canHostileSpawn))
				.appendText("\n ").appendSibling(goldText("lm.command.area.explosions")).appendText(" ").appendSibling(boolToText(area.explosions))
				.appendText("\n ").appendSibling(goldText("lm.command.area.interactions")).appendText(" ").appendSibling(boolToText(area.interactions))
		)
		return 1
	}

	private fun goldText(langKey: String): ITextComponent =
		TranslationTextComponent(langKey).applyTextStyle(TextFormatting.GOLD)

	private fun posToText(pos: BlockPos): ITextComponent = StringTextComponent("")
		.appendStyledText("X: ", TextFormatting.YELLOW).appendText("${pos.x}, ")
		.appendStyledText("Y: ", TextFormatting.YELLOW).appendText("${pos.y}, ")
		.appendStyledText("Z: ", TextFormatting.YELLOW).appendText(pos.z)

	private fun boolToText(bool: Boolean): ITextComponent =
		TranslationTextComponent(if (bool) "message.landmanager.misc.true" else "message.landmanager.misc.false")
}
