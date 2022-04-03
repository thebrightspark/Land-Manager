package brightspark.landmanager.message

import brightspark.landmanager.data.areas.AddAreaResult
import brightspark.landmanager.gui.CreateAreaScreen
import brightspark.landmanager.handler.ClientEventHandler
import brightspark.landmanager.item.AreaCreateItem
import brightspark.landmanager.util.Message
import brightspark.landmanager.util.readEnumValue
import brightspark.landmanager.util.sendActionBarMessage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.network.PacketBuffer
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.network.NetworkEvent

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

	override fun consume(context: NetworkEvent.Context) {
		context.enqueueWork {
			val mc = Minecraft.getInstance()
			val player = mc.player!!
			val gui = mc.currentScreen
			when (result) {
				AddAreaResult.SUCCESS -> {
					player.sendActionBarMessage("message.landmanager.create.added", TextFormatting.GREEN, areaName)
					closeScreen(gui, player)
					ClientEventHandler.setRenderArea(areaName)
				}
				AddAreaResult.NAME_EXISTS -> {
					player.sendActionBarMessage("message.landmanager.create.name", TextFormatting.RED, areaName)
					clearTextField(gui)
				}
				AddAreaResult.AREA_INTERSECTS -> {
					player.sendActionBarMessage("message.landmanager.create.intersects", TextFormatting.RED)
					closeScreen(gui, player)
				}
				AddAreaResult.INVALID_NAME -> {
					player.sendActionBarMessage("message.landmanager.create.invalid_name", TextFormatting.RED)
					clearTextField(gui)
				}
				AddAreaResult.INVALID -> {
					player.sendActionBarMessage("message.landmanager.create.invalid", TextFormatting.RED)
					closeScreen(gui, player)
				}
			}
		}
	}

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
