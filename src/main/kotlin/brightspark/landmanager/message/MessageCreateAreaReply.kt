package brightspark.landmanager.message

import brightspark.ksparklib.api.Message
import brightspark.ksparklib.api.extensions.readEnumValue
import brightspark.landmanager.data.areas.AddAreaResult
import brightspark.landmanager.gui.CreateAreaScreen
import brightspark.landmanager.handler.ClientEventHandler
import brightspark.landmanager.item.AreaCreateItem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.fml.network.NetworkEvent
import java.util.function.Supplier

class MessageCreateAreaReply : Message {
	private lateinit var areaName: String
	private lateinit var result: AddAreaResult

	@Suppress("unused")
	constructor()

	constructor(areaName: String, result: AddAreaResult) {
		this.areaName = areaName
		this.result = result
	}

	override fun encode(buffer: PacketBuffer): Unit = buffer.run {
		writeString(areaName)
		writeEnumValue(result)
	}

	override fun decode(buffer: PacketBuffer): Unit = buffer.run {
		areaName = readString()
		result = readEnumValue()
	}

	override fun consume(context: Supplier<NetworkEvent.Context>) {
		context.get().enqueueWork {
			val mc = Minecraft.getInstance()
			val player = mc.player!!
			val gui = mc.currentScreen
			when (result) {
				AddAreaResult.SUCCESS -> {
					sendStatusMessage(player, TextFormatting.GREEN, "message.landmanager.create.added", areaName)
					closeScreen(gui, player)
					ClientEventHandler.setRenderArea(areaName)
				}
				AddAreaResult.NAME_EXISTS -> {
					sendStatusMessage(player, TextFormatting.RED, "message.landmanager.create.name", areaName)
					clearTextField(gui)
				}
				AddAreaResult.AREA_INTERSECTS -> {
					sendStatusMessage(player, TextFormatting.RED, "message.landmanager.create.intersects")
					closeScreen(gui, player)
				}
				AddAreaResult.INVALID_NAME -> {
					sendStatusMessage(player, TextFormatting.RED, "message.landmanager.create.invalid_name")
					clearTextField(gui)
				}
				AddAreaResult.INVALID -> {
					sendStatusMessage(player, TextFormatting.RED, "message.landmanager.create.invalid")
					closeScreen(gui, player)
				}
			}
		}
	}

	private fun sendStatusMessage(
		player: PlayerEntity,
		colour: TextFormatting,
		translationKey: String,
		vararg args: String
	) =
		player.sendStatusMessage(TranslationTextComponent(translationKey, *args).mergeStyle(colour), true)

	private fun closeScreen(gui: Screen?, player: PlayerEntity) {
		if (gui is CreateAreaScreen)
			player.closeScreen()
		val stack = player.heldItemMainhand
		if (AreaCreateItem.getPos(stack) != null)
			AreaCreateItem.setPos(stack, null)
	}

	private fun clearTextField(gui: Screen?) {
		if (gui is CreateAreaScreen)
			gui.clearTextField()
	}
}
