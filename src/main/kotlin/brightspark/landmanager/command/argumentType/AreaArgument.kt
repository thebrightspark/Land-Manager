package brightspark.landmanager.command.argumentType

import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.util.getArea
import brightspark.landmanager.util.getAreaNames
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.util.concurrent.CompletableFuture

object AreaArgument : LMCommandArgType<Area>(Area::class) {
	private val AREA_NOT_EXISTS = DynamicCommandExceptionType { TranslationTextComponent("lm.command.area.not_exist", it) }

	override fun parse(reader: StringReader): Area = reader.readUnquotedString().let {
		ServerLifecycleHooks.getCurrentServer().getArea(it) ?: throw AREA_NOT_EXISTS.createWithContext(reader, it)
	}

	override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
		ServerLifecycleHooks.getCurrentServer().getAreaNames(builder.remaining)
			.sorted(Comparator.naturalOrder())
			.forEach { builder.suggest(it) }
		return builder.buildFuture()
	}
}
