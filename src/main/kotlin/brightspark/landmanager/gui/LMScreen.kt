package brightspark.landmanager.gui

import brightspark.landmanager.LandManager
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.button.Button
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent

open class LMScreen(
	title: String,
	imageName: String,
	protected val guiWidth: Int,
	protected val guiHeight: Int,
	protected val imageWidth: Int = 256,
	protected val imageHeight: Int = 256
) : Screen(StringTextComponent(title)) {
	protected val imageResLoc = ResourceLocation(LandManager.MOD_ID, "textures/gui/$imageName.png")
	protected var guiLeft = 0
	protected var guiTop = 0

	override fun init() {
		super.init()
		guiLeft = (width - guiWidth) / 2
		guiTop = (height - guiHeight) / 2
	}

	override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
		renderBackground()
		GlStateManager.color3f(1F, 1F, 1F)
		minecraft!!.textureManager.bindTexture(imageResLoc)
		blit(guiLeft, guiTop, 0F, 0F, guiWidth, guiHeight, imageWidth, imageHeight)
		super.render(mouseX, mouseY, partialTicks)
		buttons.firstOrNull { it is LMButton && it.isHovered && it.tooltip.isNotEmpty() }?.let { button ->
			renderTooltip((button as LMButton).tooltip.map { it.formattedText }, mouseX, mouseY, font)
		}
	}

	fun drawString(text: String, x: Int, y: Int, colour: Int = 4210752, shadow: Boolean = false) {
		if (shadow)
			font.drawStringWithShadow(text, x.toFloat(), y.toFloat(), colour)
		else
			font.drawString(text, x.toFloat(), y.toFloat(), colour)
	}

	fun drawLangString(key: String, x: Int, y: Int, colour: Int = 4210752, shadow: Boolean = false) {
		drawString(I18n.format(key), x, y, colour, shadow)
	}

	fun drawStringWithMaxWidth(text: String, x: Int, y: Int, maxWidth: Int, colour: Int = 4210752, shadow: Boolean = false) {
		val textWidth = font.getStringWidth(text)
		val ellipsisWidth = font.getStringWidth("...")
		var textToDraw = text
		if (textWidth > maxWidth - 6 && textWidth > ellipsisWidth)
			textToDraw = font.trimStringToWidth(text, maxWidth - 6 - ellipsisWidth).trim() + "..."
		drawString(textToDraw, x, y, colour, shadow)
	}

	protected open inner class LMButton(
		x: Int,
		y: Int,
		width: Int,
		height: Int,
		protected val iconX: Int,
		@get:JvmName("_getIconY")
		protected val iconY: Int,
		text: String,
		onPress: (Button) -> Unit
	) : Button(guiLeft + x, guiTop + y, width, height, text, onPress) {
		protected var hasIcon = true
		protected var drawWhenDisabled = false
		protected var textOffset = 0
		val tooltip: MutableList<ITextComponent> = mutableListOf()

		protected open fun getIconY(): Int = iconY

		protected open fun getTextColour(): Int = if (active) 14737632 else 10526880

		protected fun setTooltip(vararg textComponents: ITextComponent) {
			tooltip.clear()
			textComponents.forEach { tooltip.add(it) }
		}

		protected open fun drawText(font: FontRenderer): Unit =
			drawString(message, x + textOffset, y + (height - 8) / 2, getTextColour(), true)

		override fun renderButton(p_renderButton_1_: Int, p_renderButton_2_: Int, p_renderButton_3_: Float) {
			if (!visible || (!drawWhenDisabled && !active))
				return
			val mc = Minecraft.getInstance()
			if (hasIcon) {
				mc.textureManager.bindTexture(imageResLoc)
				GlStateManager.color3f(1F, 1F, 1F)
				blit(x, y, iconX.toFloat(), getIconY().toFloat(), width, height, imageWidth, imageHeight)
			}
			if (message.isNotBlank())
				drawText(mc.fontRenderer)
		}
	}

	protected open inner class ToggleButton(x: Int, y: Int, iconX: Int, iconY: Int, text: String, onPress: (Button) -> Unit = {})
		: LMButton(x, y, 12, 12, iconX, iconY, text, onPress) {
		var isOn: Boolean = false

		init {
			textOffset = width + 2
		}

		override fun drawText(font: FontRenderer): Unit =
			drawString(message, x + textOffset, y + (height - 8) / 2, getTextColour(), false)

		override fun getIconY(): Int = if (isOn) iconY + height else iconY

		override fun onPress() {
			isOn = !isOn
			super.onPress()
		}
	}
}
