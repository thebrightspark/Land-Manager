package brightspark.landmanager.gui

import brightspark.landmanager.LandManager
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
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

	override fun render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
		renderBackground(matrixStack)
		RenderSystem.color3f(1F, 1F, 1F)
		minecraft!!.textureManager.bindTexture(imageResLoc)
		blit(matrixStack, guiLeft, guiTop, guiWidth, guiHeight, imageWidth, imageHeight)
		super.render(matrixStack, mouseX, mouseY, partialTicks)
		buttons.firstOrNull { it is LMButton && it.isHovered && it.tooltip.isNotEmpty() }?.let { button ->
			renderToolTip(
				matrixStack,
				(button as LMButton).tooltip.flatMap { font.trimStringToWidth(it, 100) },
				mouseX,
				mouseY,
				font
			)
		}
	}

	fun drawString(
		matrixStack: MatrixStack,
		text: String,
		x: Int,
		y: Int,
		colour: Int = 4210752,
		shadow: Boolean = false
	) {
		if (shadow)
			font.drawStringWithShadow(matrixStack, text, x.toFloat(), y.toFloat(), colour)
		else
			font.drawString(matrixStack, text, x.toFloat(), y.toFloat(), colour)
	}

	fun drawLangString(
		matrixStack: MatrixStack,
		key: String,
		x: Int,
		y: Int,
		colour: Int = 4210752,
		shadow: Boolean = false
	): Unit = drawString(matrixStack, I18n.format(key), x, y, colour, shadow)

	fun drawStringWithMaxWidth(
		matrixStack: MatrixStack,
		text: ITextComponent,
		x: Int,
		y: Int,
		maxWidth: Int,
		colour: Int = 4210752,
		shadow: Boolean = false
	) {
//		val textWidth = font.getStringWidth(text)
//		val ellipsisWidth = font.getStringWidth("...")
//		var textToDraw = text
//		if (textWidth > maxWidth - 6 && textWidth > ellipsisWidth)
//			textToDraw = font.trimStringToWidth(text, maxWidth - 6 - ellipsisWidth).trim() + "..."
		val matrix = matrixStack.last.matrix
		font.trimStringToWidth(text, maxWidth).forEach {
			font.func_238415_a_(it, x.toFloat(), y.toFloat(), colour, matrix, shadow)
		}
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
	) : Button(guiLeft + x, guiTop + y, width, height, StringTextComponent(text), onPress) {
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

		protected open fun drawText(matrixStack: MatrixStack, font: FontRenderer): Unit =
			drawString(matrixStack, font, message, x + textOffset, y + (height - 8) / 2, getTextColour())

		override fun renderButton(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
			if (!visible || (!drawWhenDisabled && !active))
				return
			val mc = Minecraft.getInstance()
			if (hasIcon) {
				mc.textureManager.bindTexture(imageResLoc)
				RenderSystem.color3f(1F, 1F, 1F)
				blit(matrixStack, x, y, iconX.toFloat(), getIconY().toFloat(), width, height, imageWidth, imageHeight)
			}
			if (message.unformattedComponentText.isNotBlank())
				drawText(matrixStack, mc.fontRenderer)
		}
	}

	protected open inner class ToggleButton(
		x: Int,
		y: Int,
		iconX: Int,
		iconY: Int,
		text: String,
		onPress: (Button) -> Unit = {}
	) : LMButton(x, y, 12, 12, iconX, iconY, text, onPress) {
		var isOn: Boolean = false

		init {
			textOffset = width + 2
		}

		override fun drawText(matrixStack: MatrixStack, font: FontRenderer): Unit =
			drawString(matrixStack, font, message, x + textOffset, y + (height - 8) / 2, getTextColour())

		override fun getIconY(): Int = if (isOn) iconY + height else iconY

		override fun onPress() {
			isOn = !isOn
			super.onPress()
		}
	}
}
