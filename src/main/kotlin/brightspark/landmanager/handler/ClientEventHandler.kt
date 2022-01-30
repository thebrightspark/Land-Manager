package brightspark.landmanager.handler

import brightspark.landmanager.LMConfig
import brightspark.landmanager.LandManager
import brightspark.landmanager.data.areas.Area
import brightspark.landmanager.util.AreaRenderer
import brightspark.landmanager.util.areasCap
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.common.Mod
import java.awt.Color
import kotlin.random.Random

@Mod.EventBusSubscriber(Dist.CLIENT, modid = LandManager.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
object ClientEventHandler {
	private val mc = Minecraft.getInstance()
	private val rand = Random.Default
	private var renderAll = false
	private val areasToRender = mutableSetOf<String>()
	private val colourCache = mutableMapOf<String, Color>()
	private var lastAreaInside: LastDetails? = null

	fun isAreaBeingRendered(areaName: String): Boolean = areasToRender.contains(areaName)

	// Used by command
	fun setRenderArea(areaName: String) {
		renderAll = false
		areasToRender.clear()
		areasToRender += areaName
		colourCache.clear()
	}

	// Used by GUI
	fun setRenderArea(areaName: String, show: Boolean) {
		renderAll = false
		if (show)
			areasToRender += areaName
		else {
			areasToRender -= areaName
			colourCache -= areaName
		}
	}

	fun toggleRenderAll() {
		renderAll = areasToRender.isNotEmpty() || !renderAll
		areasToRender.clear()
		colourCache.clear()
		mc.player!!.sendStatusMessage(
			TranslationTextComponent("message.landmanager.areas.${if (renderAll) "show" else "hide"}")
				.mergeStyle(TextFormatting.GREEN),
			true
		)
	}

	private fun randFloat(min: Float) = min + rand.nextFloat() * (1F - min)

	private fun getColour(areaName: String): Color =
		colourCache.computeIfAbsent(areaName) { Color.getHSBColor(rand.nextFloat(), randFloat(0.3F), randFloat(0.7F)) }

	@SubscribeEvent
	fun renderAreas(event: RenderWorldLastEvent) {
		if (!renderAll && areasToRender.isEmpty())
			return

		val cap = mc.world!!.areasCap
		val matrixStack = event.matrixStack
		val view = mc.gameRenderer.activeRenderInfo.projectedView
		RenderSystem.color4f(1F, 1F, 1F, 1F)

		if (renderAll)
			cap.getNearbyAreas(mc.player!!.position)
				.forEach { AreaRenderer.renderArea(matrixStack, view, it, getColour(it.name)) }
		else
			areasToRender.stream()
				.map { cap.getArea(it) }
				.filter { it != null }
				.forEach { AreaRenderer.renderArea(matrixStack, view, it!!, getColour(it.name)) }
	}

	@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
	@SubscribeEvent
	fun playerTick(event: TickEvent.PlayerTickEvent): Unit = event.run {
		// Send title message to client when moving into different area
		if (!LMConfig.titleOnAreaChange || side != LogicalSide.CLIENT || phase != TickEvent.Phase.END)
			return

		val cap = player.world.areasCap
		val pos = player.position
		lastAreaInside?.let {
			// If still inside the same area, then no need to check further
			// Worth noting that this will only work while areas aren't allowed to overlap
			val area = it.area ?: return@let
			if (area.intersects(pos) && cap.hasArea(area.name))
				return
		}

		if (lastAreaInside == null || lastAreaInside!!.updateAndCheckPlayerPos(player)) {
			val area = cap.intersectingArea(pos)
			if (lastAreaInside == null || lastAreaInside!!.updateAndCheckArea(area)) {
				if (lastAreaInside == null)
					lastAreaInside = LastDetails(area, player)

				// Display area change message
				val text = area?.let { StringTextComponent(it.name) }
					?: TranslationTextComponent("misc.landmanager.wilderness")
				text.mergeStyle(
					when {
						area == null -> LMConfig.titleColourWilderness
						area.isMember(player.uniqueID) -> LMConfig.titleColourAreaMember
						else -> LMConfig.titleColourAreaOutsider
					}
				)
				mc.ingameGUI.run {
					// Set area name as sub-title
					func_238452_a_(null, text, 0, 0, 0) // displayTitle
					// Display empty title so that sub-title is shown
					func_238452_a_(StringTextComponent(""), null, 0, 0, 0) // displayTitle
				}
			}
		}
	}

	class LastDetails(var area: Area?, player: PlayerEntity) {
		var pos: BlockPos = player.position
		var dim: ResourceLocation = player.world.dimensionKey.location

		fun updateAndCheckPlayerPos(player: PlayerEntity): Boolean {
			val result = player.position != pos || player.world.dimensionKey.location != dim
			pos = player.position
			dim = player.world.dimensionKey.location
			return result
		}

		fun updateAndCheckArea(area: Area?): Boolean {
			val result = area == null != (this.area == null) || area != null && area.name != this.area?.name
			this.area = area
			return result
		}
	}
}
