package brightspark.landmanager.gui

import brightspark.landmanager.LandManager
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.message.MessageCreateArea
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.widget.button.Button
import net.minecraft.client.gui.widget.button.CheckboxButton
import net.minecraft.client.resources.I18n
import net.minecraft.util.math.BlockPos
import org.lwjgl.glfw.GLFW

class CreateAreaScreen(private val pos1: BlockPos, private val pos2: BlockPos) : LMScreen("Create Area", "gui_create_area", 113, 46) {
	private lateinit var nameInputField: TextFieldWidget
	private lateinit var extendCheckBox: CheckboxButton

	private var dimId: Int = 0
	private var sentCreateMessage = false

	override fun init() {
		super.init()
		nameInputField = TextFieldWidget(font, guiLeft + 5, guiTop + 16, guiWidth - 10, font.FONT_HEIGHT + 2, "")
		nameInputField.setCanLoseFocus(false)
		nameInputField.changeFocus(true)
		children += nameInputField
		extendCheckBox = addButton(CheckboxButton(guiLeft + 5, guiTop + 31, 150, 20, I18n.format("gui.landmanager.create.checkbox"), false))
		addButton(object : Button(guiLeft + 68, guiTop + 31, 40, font.FONT_HEIGHT + 2, I18n.format("gui.landmanager.create.confirm"), { complete() }) {
			override fun renderButton(p_renderButton_1_: Int, p_renderButton_2_: Int, p_renderButton_3_: Float) {
				minecraft!!.textureManager.bindTexture(imageResLoc)
				GlStateManager.color3f(1F, 1F, 1F)
				GlStateManager.enableBlend()
				GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
				blit(x, y, 0, height + if (isHovered) 11 else 0, width, height)
				drawCenteredString(font, message, x + width / 2, y + 2, 14737632)
			}
		})
	}

	override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
		super.render(mouseX, mouseY, partialTicks)
		nameInputField.render(mouseX, mouseY, partialTicks)
		drawLangString("gui.landmanager.create.area", 5 + guiLeft, 5 + guiTop, 14737632)
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
			if (extendCheckBox.isChecked)
				area.extendToMinMaxY(minecraft!!.world)
			sentCreateMessage = true
			LandManager.NETWORK.sendToServer(MessageCreateArea(area))
		}
	}
}
