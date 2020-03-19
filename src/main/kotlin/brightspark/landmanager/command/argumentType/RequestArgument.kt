package brightspark.landmanager.command.argumentType

import brightspark.landmanager.data.requests.Request
import brightspark.landmanager.util.requests
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.util.concurrent.CompletableFuture

object RequestArgument : LMCommandArgType<Request>(Request::class) {
	private val INVALID = DynamicCommandExceptionType { TranslationTextComponent("lm.command.requests.invalid", it) }
	private val REQUEST_NOT_EXISTS = DynamicCommandExceptionType { TranslationTextComponent("lm.command.requests.not_exist", it) }

	override fun parse(reader: StringReader): Request = reader.readUnquotedString().let {
		it.toIntOrNull() ?: throw INVALID.createWithContext(reader, it)
	}.let {
		ServerLifecycleHooks.getCurrentServer().requests.getById(it)
			?: throw REQUEST_NOT_EXISTS.createWithContext(reader, it)
	}

	override fun <S : Any?> listSuggestions(context: CommandContext<S>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
		ServerLifecycleHooks.getCurrentServer().requests.getAll().map { it.id }.sorted().forEach { builder.suggest(it) }
		return builder.buildFuture()
	}
}
