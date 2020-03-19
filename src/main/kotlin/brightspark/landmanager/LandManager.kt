package brightspark.landmanager

import brightspark.ksparklib.api.*
import brightspark.landmanager.block.HomeBlock
import brightspark.landmanager.command.LMCommand
import brightspark.landmanager.command.argumentType.AreaArgument
import brightspark.landmanager.command.argumentType.RequestArgument
import brightspark.landmanager.data.areas.AreasCapability
import brightspark.landmanager.data.areas.AreasCapabilityImpl
import brightspark.landmanager.data.areas.AreasCapabilityProvider
import brightspark.landmanager.item.AreaCreateItem
import brightspark.landmanager.message.*
import brightspark.landmanager.util.AreaChangeType
import brightspark.landmanager.util.getSenderName
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemStack
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityInject
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig.ModConfigEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.server.FMLServerStartingEvent

@Mod(LandManager.MOD_ID)
object LandManager {
	const val MOD_ID = "landmanager"
	val LOGGER = getLogger()

	val NETWORK = regSimpleChannel(ResourceLocation(MOD_ID, "main"), "1", messages = arrayOf(
		MessageAreaAdd::class,
		MessageAreaChange::class,
		MessageAreaDelete::class,
		MessageAreaRename::class,
		MessageChatLog::class,
		MessageCreateArea::class,
		MessageCreateAreaReply::class,
		MessageHomeActionAdd::class,
		MessageHomeActionKickOrPass::class,
		MessageHomeActionReply::class,
		MessageHomeActionReplyError::class,
		MessageHomeToggle::class,
		MessageHomeToggleReply::class,
		MessageOpenHomeGui::class,
		MessageShowArea::class,
		MessageUpdateAreasCap::class
	))

	@CapabilityInject(AreasCapability::class)
	@JvmStatic
	var CAP_AREAS: Capability<AreasCapability>? = null
	private val KEY_AREAS = ResourceLocation(MOD_ID, "_areas")

	init {
		addModListener<ModConfigEvent> { if (it.config.modId == MOD_ID) LMConfig.bake() }
		registerBlocks(MOD_ID, { _, _, props -> props.group(group) }, "home" to HomeBlock())
		registerContent<Item>(MOD_ID, "area_create" to AreaCreateItem())

		addModListener<FMLCommonSetupEvent> {
			regCapability<AreasCapability, World, AreasCapabilityProvider>(::AreasCapabilityImpl, KEY_AREAS)
		}
		addForgeListener<FMLServerStartingEvent> { it.commandDispatcher.register(LMCommand) }

		regCommandArgType<AreaArgument>("area")
		regCommandArgType<RequestArgument>("request")

		registerConfig(client = LMConfig.CLIENT_SPEC, server = LMConfig.SERVER_SPEC)
	}

	val group = object : ItemGroup(MOD_ID) {
		override fun createIcon() = ItemStack(area_create!!)
	}

	fun areaChange(context: CommandContext<CommandSource>, type: AreaChangeType, areaName: String) =
		areaChange(context.source.server, type, areaName, context.getSenderName())

	fun areaChange(server: MinecraftServer, type: AreaChangeType, areaName: String, sender: ServerPlayerEntity? = null) {
		val timestamp = System.currentTimeMillis()
		val senderName = sender?.gameProfile?.name ?: server.name
		server.playerList.oppedPlayers.keys
			.mapNotNull { server.playerList.getPlayerByUsername(it) }
			.filter { it != sender }
			.forEach { NETWORK.sendToPlayer(MessageChatLog(timestamp, type, areaName, senderName), it) }
	}

	private fun areaChange(server: MinecraftServer, type: AreaChangeType, areaName: String, name: String) {
		val timestamp = System.currentTimeMillis()
		server.playerList.oppedPlayers.keys
			.filter { !it.equals(name, true) }
			.mapNotNull { server.playerList.getPlayerByUsername(it) }
			.forEach { NETWORK.sendToPlayer(MessageChatLog(timestamp, type, areaName, name), it) }
	}
}
