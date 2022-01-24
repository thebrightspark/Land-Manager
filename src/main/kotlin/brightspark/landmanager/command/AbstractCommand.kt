package brightspark.landmanager.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands

abstract class AbstractCommand(
	name: String,
	builderBlock: LiteralArgumentBuilder<CommandSource>.() -> Unit
) {
	/**
	 * The command builder which will actually be registered to vanilla's [CommandDispatcher.register]
	 */
	val builder: LiteralArgumentBuilder<CommandSource> = Commands.literal(name).apply(builderBlock)
}
