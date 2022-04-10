package brightspark.landmanager.command.argumentType

import brightspark.landmanager.data.requests.Request
import brightspark.landmanager.util.requests
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.server.ServerLifecycleHooks

object RequestArgument : LMCommandArgType<Request>(Request::class) {
	private val INVALID = DynamicCommandExceptionType { TranslationTextComponent("lm.command.requests.invalid", it) }
	private val REQUEST_NOT_EXISTS =
		DynamicCommandExceptionType { TranslationTextComponent("lm.command.requests.not_exist", it) }

	override fun parse(reader: StringReader): Request = reader.readUnquotedString().let {
		it.toIntOrNull() ?: throw INVALID.createWithContext(reader, it)
	}.let {
		ServerLifecycleHooks.getCurrentServer().requests.getById(it)
			?: throw REQUEST_NOT_EXISTS.createWithContext(reader, it)
	}
}
