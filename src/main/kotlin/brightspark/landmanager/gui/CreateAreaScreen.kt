package brightspark.landmanager.gui

import brightspark.landmanager.LandManager
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.message.MessageCreateArea
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.widget.button.Button
import net.minecraft.client.resources.I18n
import net.minecraft.util.math.BlockPos
import org.lwjgl.glfw.GLFW

class CreateAreaScreen(private val pos1: BlockPos, private val pos2: BlockPos) : LMScreen("Create Area", "gui_create_area", 118, 42, 130, 42) {
	private lateinit var nameInputField: TextFieldWidget
	private lateinit var extendCheckBox: ToggleButton

	private var dimId: Int = 0
	private var sentCreateMessage = false

	override fun init() {
		super.init()
		nameInputField = TextFieldWidget(font, guiLeft + 4, guiTop + 15, 110, 9, "").apply {
			setEnableBackgroundDrawing(false)
			setFocused2(true)
			this@CreateAreaScreen.focused = this
		}
		children += nameInputField
		extendCheckBox = addButton(object : ToggleButton(4, 26, 118, 0, I18n.format("gui.lm.create.checkbox")) {
			override fun getTextColour(): Int = 4210752

			override fun drawText(font: FontRenderer): Unit =
				drawString(message, x + textOffset, y + (height - 8) / 2, getTextColour(), false)
		})
		addButton(object : Button(guiLeft + 71, guiTop + 26, 43, 12, I18n.format("gui.lm.create.confirm"), { complete() }) {
			override fun renderButton(p_renderButton_1_: Int, p_renderButton_2_: Int, p_renderButton_3_: Float) =
				drawCenteredString(font, message, x + width / 2, y + 2, 14737632)
		})
	}

	override fun tick() {
		super.tick()
		nameInputField.tick()
	}

	override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
		super.render(mouseX, mouseY, partialTicks)
		nameInputField.render(mouseX, mouseY, partialTicks)
		drawLangString("gui.lm.create.area", 5 + guiLeft, 5 + guiTop, 4210752)
	}

	override fun keyPressed(keyCode: Int, p2: Int, p3: Int): Boolean {
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			complete()
			return true
		}
		return nameInputField.keyPressed(keyCode, p2, p3) || super.keyPressed(keyCode, p2, p3)
	}

	// Used by MessageCreateAreaReply when the name already exists
	fun clearTextField() {
		nameInputField.text = ""
		sentCreateMessage = false
	}

	// Sends message to the server to add the new area
	// Doesn't close the GUI - let the returned message do it if successful
	private fun complete() {
		if (sentCreateMessage)
			return
		val areaName = nameInputField.text.trim()
		if (areaName.isNotEmpty()) {
			val area = Area(areaName, dimId, pos1, pos2)
			if (extendCheckBox.isOn)
				area.extendToMinMaxY(minecraft!!.world)
			sentCreateMessage = true
			LandManager.NETWORK.sendToServer(MessageCreateArea(area))
		}
	}
}
