package brightspark.landmanager.command.argumentType

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import kotlin.reflect.KClass

abstract class LMCommandArgType<T : Any>(kClass: KClass<T>) : ArgumentType<T> {
	private val type = kClass.java

	fun get(context: CommandContext<*>, name: String): T = context.getArgument(name, type)
}
