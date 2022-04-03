package brightspark.landmanager.command.argumentType

import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.util.areasCap
import brightspark.landmanager.util.getArea
import brightspark.landmanager.util.getAreaNames
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.client.Minecraft
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.fml.server.ServerLifecycleHooks
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream

object AreaArgument : LMCommandArgType<Area>(Area::class) {
	private val AREA_NOT_EXISTS =
		DynamicCommandExceptionType { TranslationTextComponent("lm.command.area.not_exist", it) }

	override fun parse(reader: StringReader): Area = reader.readUnquotedString().let {
		ServerLifecycleHooks.getCurrentServer().getArea(it) ?: throw AREA_NOT_EXISTS.createWithContext(reader, it)
	}

	override fun <S : Any?> listSuggestions(
		context: CommandContext<S>,
		builder: SuggestionsBuilder
	): CompletableFuture<Suggestions> {
		val names = ServerLifecycleHooks.getCurrentServer()?.let { getAreaNamesServer() } ?: getAreaNamesClient()
		names.filter { it.startsWith(builder.remaining) }
			.sorted(Comparator.naturalOrder())
			.forEach { builder.suggest(it) }
		return builder.buildFuture()
	}

	@OnlyIn(Dist.CLIENT)
	private fun getAreaNamesClient(): Stream<String> =
		Minecraft.getInstance().world!!.areasCap.getAllAreaNames().stream()

	private fun getAreaNamesServer(): Stream<String> = ServerLifecycleHooks.getCurrentServer().getAreaNames()
}
