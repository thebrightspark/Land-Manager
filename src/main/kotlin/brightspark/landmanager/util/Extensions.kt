package brightspark.landmanager.util

import brightspark.ksparklib.api.extensions.get
import brightspark.landmanager.LandManager
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.data.areas.AreasCapability
import brightspark.landmanager.data.requests.RequestsWSD
import com.mojang.authlib.GameProfile
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.command.ICommandSource
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.server.MinecraftServer
import net.minecraft.server.management.PlayerProfileCache
import net.minecraft.util.Util
import net.minecraft.util.text.ITextComponent
import net.minecraft.world.World
import java.util.*
import java.util.stream.Stream

val World.areasCap: AreasCapability
	get() = LandManager.CAP_AREAS!!.get(this)

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

fun MinecraftServer.getUsernameFromUuid(uuid: UUID): String? = this.playerProfileCache.getProfileByUUID(uuid)?.name

fun MinecraftServer.sendToOps(message: ITextComponent, excluding: PlayerEntity? = null): Unit = this.playerList.players
	.filter { it != excluding && this.playerList.oppedPlayers.getEntry(it.gameProfile) != null }
	.mapNotNull { this.playerList.getPlayerByUUID(it.uniqueID) }
	.forEach { it.sendMessage(message, Util.DUMMY_UUID) }

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
