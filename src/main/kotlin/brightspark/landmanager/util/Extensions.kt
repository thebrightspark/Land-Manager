package brightspark.landmanager.util

import brightspark.landmanager.LandManager
import brightspark.landmanager.command.AbstractCommand
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.data.areas.AreasCapability
import brightspark.landmanager.data.requests.RequestsWSD
import brightspark.landmanager.init.LMCapabilities
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.Commands
import net.minecraft.command.ICommandSource
import net.minecraft.command.arguments.ArgumentSerializer
import net.minecraft.command.arguments.ArgumentTypes
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.management.PlayerProfileCache
import net.minecraft.util.Util
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.text.*
import net.minecraft.world.World
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.common.thread.EffectiveSide
import net.minecraftforge.fml.network.PacketDistributor
import net.minecraftforge.fml.network.simple.SimpleChannel
import net.minecraftforge.registries.ForgeRegistryEntry
import java.awt.Color
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/*
 * ----------------
 *  SIMPLE CHANNEL
 * ----------------
 */

/**
 * Registers a [Message] with the given [index]
 */
@Suppress("INACCESSIBLE_TYPE")
fun <T : Message> SimpleChannel.registerMessage(messageClass: KClass<T>, index: Int) {
	this.registerMessage(
		index,
		messageClass.java,
		{ message, buffer -> message.encode(buffer) },
		{ message -> messageClass.createInstance().apply { decode(message) } },
		{ message, context ->
			message.consume(context.get())
			context.get().packetHandled = true
		}
	)
}

/**
 * Sends the [message] to the [player] client
 */
fun SimpleChannel.sendToPlayer(message: Message, player: ServerPlayerEntity): Unit =
	this.send(PacketDistributor.PLAYER.with { player }, message)

/**
 * Sends the [message] to all clients
 */
fun SimpleChannel.sendToAll(message: Message): Unit = this.send(PacketDistributor.ALL.noArg(), message)

fun PacketBuffer.readColor(): Color? {
	val array = arrayOf(this.readInt(), this.readInt(), this.readInt(), this.readInt())
	return if (array.any { it < 0 }) null else Color(array[0], array[1], array[2], array[3])
}

/*
 * -----------------
 *  TEXT COMPONENTS
 * -----------------
 */

/**
 * Adds a new [StringTextComponent] to the end of the sibling list, with the specified [obj]. Same as calling
 * [IFormattableTextComponent.appendString] and giving it the result of calling [Any.toString] on [obj].
 */
fun IFormattableTextComponent.appendString(obj: Any): IFormattableTextComponent = this.appendString(obj.toString())

/**
 * Adds a new [TranslationTextComponent] to the end of the sibling list, with the specified translation key and
 * arguments. Same as calling [IFormattableTextComponent.appendSibling] with a new [TranslationTextComponent].
 */
fun IFormattableTextComponent.appendTranslation(translationKey: String, vararg args: Any): IFormattableTextComponent =
	this.append(TranslationTextComponent(translationKey, args))

/**
 * Adds a new [StringTextComponent] to the end of the sibling list, with the specified [text] and [style].
 * Same as calling [IFormattableTextComponent.appendSibling] with a new [StringTextComponent] and calling
 * [IFormattableTextComponent.setStyle] on that.
 */
fun IFormattableTextComponent.appendStyledString(text: String, style: Style): IFormattableTextComponent =
	this.append(StringTextComponent(text).setStyle(style))

/**
 * Adds a new [StringTextComponent] to the end of the sibling list, with the specified [text] and [styles].
 * Same as calling [IFormattableTextComponent.appendSibling] with a new [StringTextComponent] and calling
 * [IFormattableTextComponent.mergeStyle] on that.
 */
fun IFormattableTextComponent.appendStyledString(
	text: String,
	vararg styles: TextFormatting
): IFormattableTextComponent = this.append(StringTextComponent(text).mergeStyle(*styles))

/*
 * ----------
 *  COMMANDS
 * ----------
 */

/**
 * Registers all [commands] to this [CommandDispatcher]
 */
fun CommandDispatcher<CommandSource>.register(vararg commands: AbstractCommand): Unit =
	commands.forEach { this.register(it.builder) }

fun <T : ArgumentBuilder<CommandSource, T>> T.thenLiteral(
	name: String,
	block: LiteralArgumentBuilder<CommandSource>.() -> Unit
): T = this.then(Commands.literal(name).apply(block))

fun <T : ArgumentBuilder<CommandSource, T>, ARG> T.thenArgument(
	argumentName: String,
	argument: ArgumentType<ARG>,
	block: RequiredArgumentBuilder<CommandSource, ARG>.() -> Unit
): T = this.then(Commands.argument(argumentName, argument).apply(block))

fun <T : ArgumentBuilder<CommandSource, T>> T.thenCommand(command: AbstractCommand, block: T.() -> Unit = {}): T =
	this.then(command.builder).apply(block)

/**
 * Registers a new [ArgumentType] with the given [id]
 * Note that this method requires the [ArgumentType] [T] to be a Kotlin Object
 */
inline fun <reified T : ArgumentType<out Any>> regCommandArgType(id: String) {
	val instance =
		requireNotNull(T::class.objectInstance) { "The argument type ${T::class.qualifiedName} must be a Kotlin Object!" }
	ArgumentTypes.register(id, T::class.java, ArgumentSerializer { instance })
}

inline fun <reified T : Enum<T>> PacketBuffer.readEnumValue(): T = this.readEnumValue(T::class.java)

/*
 * -------
 *  SIDES
 * -------
 */

/**
 * Runs the [op] when on the [side]
 */
fun runWhenOnLogical(side: LogicalSide, op: () -> Unit) {
	if (EffectiveSide.get() == side)
		op()
}

/**
 * Runs the [op] when on [LogicalSide.CLIENT]
 */
fun runWhenOnLogicalClient(op: () -> Unit): Unit = runWhenOnLogical(LogicalSide.CLIENT, op)

/**
 * Runs the [op] when on [LogicalSide.SERVER]
 */
fun runWhenOnLogicalServer(op: () -> Unit): Unit = runWhenOnLogical(LogicalSide.SERVER, op)

/*
 * --------------------
 *  CAPABILITIES / WSD
 * --------------------
 */

val World.areasCap: AreasCapability
	get() = this.getCapability(LMCapabilities.AREAS)
		.orElseThrow { RuntimeException("Areas capability not found on world") }

val MinecraftServer.allAreaNames: List<String>
	get() = mutableListOf<String>().also { list ->
		this.worlds.forEach { world ->
			list.addAll(world.areasCap.getAllAreaNames())
		}
	}

fun MinecraftServer.getAreaNames(startsWith: String): Stream<String> = Stream.builder<String>().also { stream ->
	this.worlds.forEach { world ->
		world.areasCap.getAllAreaNames().forEach {
			if (it.startsWith(startsWith))
				stream.accept(it)
		}
	}
}.build()

fun MinecraftServer.getArea(areaName: String): Area? {
	this.worlds.forEach { world -> world.areasCap.getArea(areaName)?.let { return it } }
	return null
}

fun MinecraftServer.getAreas(filter: (Area) -> Boolean = { true }): Stream<Area> =
	Stream.builder<Area>().also { stream ->
		this.worlds.forEach { world ->
			world.areasCap.getAllAreas().forEach {
				if (filter(it))
					stream.accept(it)
			}
		}
	}.build()

fun MinecraftServer.getWorldCapForArea(area: Area): AreasCapability? = this.getWorldCapForArea(area.name)

fun MinecraftServer.getWorldCapForArea(areaName: String): AreasCapability? {
	this.worlds.forEach { world ->
		world.areasCap.let {
			if (it.hasArea(areaName))
				return it
		}
	}
	return null
}

val MinecraftServer.requests: RequestsWSD
	get() = RequestsWSD.get(this)

/*
 * ------
 *  MISC
 * ------
 */

fun <T : ForgeRegistryEntry<T>> T.setRegName(name: String): T =
	this.setRegistryName(LandManager.MOD_ID, name)

/**
 * Overload for [Entity.sendMessage] which uses [Util.DUMMY_UUID] instead of an explicit UUID
 */
fun Entity.sendMessage(textComponent: ITextComponent): Unit = this.sendMessage(textComponent, Util.DUMMY_UUID)

fun AxisAlignedBB.minPos(): Vector3d = Vector3d(minX, minY, minZ)

fun AxisAlignedBB.maxPos(): Vector3d = Vector3d(maxX, maxY, maxZ)

fun BlockPos.toVec3d(): Vector3d = Vector3d(x.toDouble(), y.toDouble(), z.toDouble())

fun MinecraftServer.getUsernameFromUuid(uuid: UUID): String? = this.playerProfileCache.getProfileByUUID(uuid)?.name

fun MinecraftServer.sendToOps(message: ITextComponent, excluding: PlayerEntity? = null): Unit = this.playerList.players
	.filter { it != excluding && this.playerList.oppedPlayers.getEntry(it.gameProfile) != null }
	.mapNotNull { this.playerList.getPlayerByUUID(it.uniqueID) }
	.forEach { it.sendMessage(message, Util.DUMMY_UUID) }

fun ICommandSource.isOp(): Boolean {
	if (this !is ServerPlayerEntity)
		return false
	val server = this.world.server ?: return false
	if (this.gameProfile.name == server.serverOwner)
		return true
	return server.playerList.oppedPlayers.getEntry(this.gameProfile) != null
}

fun ICommandSource?.canEditArea(area: Area?): Boolean =
	this != null && area != null && (this is MinecraftServer || (this is PlayerEntity && (area.isOwner(this.uniqueID) || this.isOp())))

fun ServerPlayerEntity?.canEditArea(area: Area?): Boolean {
	return this != null && area != null && (area.isOwner(this.uniqueID) || this.isOp())
}

val ServerPlayerEntity.username: String
	get() = this.gameProfile.name

fun PlayerProfileCache.hasUsername(username: String): Boolean = this.usernameToProfileEntryMap.containsKey(username)

fun PlayerProfileCache.getProfileForUsername(username: String): GameProfile? =
	if (this.hasUsername(username)) this.getGameProfileForUsername(username) else null

fun CommandContext<CommandSource>.getSenderName(): String = when (val sender = this.source.source) {
	is PlayerEntity -> sender.gameProfile.name
	is Entity -> sender.name.string
	else -> this.source.server.name
}
