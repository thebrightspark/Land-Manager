package brightspark.landmanager.message

import brightspark.ksparklib.api.Message
import brightspark.ksparklib.api.readEnumValue
import brightspark.landmanager.data.areas.AddAreaResult
import brightspark.landmanager.gui.CreateAreaScreen
import brightspark.landmanager.handler.ClientEventHandler
import brightspark.landmanager.item.AreaCreateItem
import net.minecraft.client.Minecraft
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
			val player = mc.player
			val gui = mc.currentScreen
			when (result) {
				AddAreaResult.SUCCESS -> {
					player.sendStatusMessage(TranslationTextComponent("message.landmanager.create.added", areaName).applyTextStyle(TextFormatting.GREEN), true)
					if (gui is CreateAreaScreen)
						player.closeScreen()
					resetItem(player)
					ClientEventHandler.setRenderArea(areaName)
				}
				AddAreaResult.NAME_EXISTS -> {
					player.sendStatusMessage(TranslationTextComponent("message.landmanager.create.name", areaName).applyTextStyle(TextFormatting.RED), true)
					if (gui is CreateAreaScreen)
						gui.clearTextField()
				}
				AddAreaResult.AREA_INTERSECTS -> {
					player.sendStatusMessage(TranslationTextComponent("message.landmanager.create.intersects").applyTextStyle(TextFormatting.RED), true)
					if (gui is CreateAreaScreen)
						player.closeScreen()
					resetItem(player)
				}
				AddAreaResult.INVALID_NAME -> {
					player.sendStatusMessage(TranslationTextComponent("message.landmanager.create.invalid_name").applyTextStyle(TextFormatting.RED), true)
					if (gui is CreateAreaScreen)
						gui.clearTextField()
				}
				AddAreaResult.INVALID -> {
					player.sendStatusMessage(TranslationTextComponent("message.landmanager.create.invalid").applyTextStyle(TextFormatting.RED), true)
					if (gui is CreateAreaScreen)
						player.closeScreen()
					resetItem(player)
				}
			}
		}
	}

	private fun resetItem(player: PlayerEntity) {
		val stack = player.heldItemMainhand
		if (AreaCreateItem.getPos(stack) != null)
			AreaCreateItem.setPos(stack, null)
	}
}
