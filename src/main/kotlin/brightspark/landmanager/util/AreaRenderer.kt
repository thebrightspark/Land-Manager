package brightspark.landmanager.util

import brightspark.landmanager.LMConfig
import brightspark.landmanager.data.areas.Area
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.Direction
import net.minecraft.util.Direction.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import java.awt.Color

object AreaRenderer {
	private val mc = Minecraft.getInstance()
	private val offsets = mapOf(
		// First vector is the inside offset
		// Second is the outside offset
		// Third is the other inside offset for the internal faces
		UP to listOf(
			createVecTriple(1, 1, -1, -1, 1, 1, 1, -1, -1),        // South West -> -X, +Z
			createVecTriple(-1, 1, -1, 1, 1, 1, -1, -1, -1),    // South East -> +X, +Z
			createVecTriple(-1, 1, 1, 1, 1, -1, -1, -1, 1),     // North East -> +X, -Z
			createVecTriple(1, 1, 1, -1, 1, -1, 1, -1, 1)        // North West -> -X, -Z
		),
		DOWN to listOf(
			createVecTriple(1, -1, 1, -1, -1, -1, 1, 1, 1),     // North West -> -X, -Z
			createVecTriple(-1, -1, 1, 1, -1, -1, -1, 1, 1),    // North East -> +X, -Z
			createVecTriple(-1, -1, -1, 1, -1, 1, -1, 1, -1),   // South East -> +X, +Z
			createVecTriple(1, -1, -1, -1, -1, 1, 1, 1, -1)    // South West -> -X, +Z
		),
		NORTH to listOf(
			createVecTriple(-1, 1, -1, 1, -1, -1, -1, 1, 1),    // Down East -> +X, -Y
			createVecTriple(1, 1, -1, -1, -1, -1, 1, 1, 1),     // Down West -> -X, -Y
			createVecTriple(1, -1, -1, -1, 1, -1, 1, -1, 1),    // Up West -> -X, +Y
			createVecTriple(-1, -1, -1, 1, 1, -1, -1, -1, 1)    // Up East -> +X, +Y
		),
		SOUTH to listOf(
			createVecTriple(1, 1, 1, -1, -1, 1, 1, 1, -1),      // Down West -> -X, -Y
			createVecTriple(-1, 1, 1, 1, -1, 1, -1, 1, -1),     // Down East -> +X, -Y
			createVecTriple(-1, -1, 1, 1, 1, 1, -1, -1, -1),    // Up East -> +X, +Y
			createVecTriple(1, -1, 1, -1, 1, 1, 1, -1, -1)    // Up West -> -X, +Y
		),
		EAST to listOf(
			createVecTriple(1, 1, -1, 1, -1, 1, -1, 1, -1),     // Down South -> +Z, -Y
			createVecTriple(1, 1, 1, 1, -1, -1, -1, 1, 1),      // Down North -> -Z, -Y
			createVecTriple(1, -1, 1, 1, 1, -1, -1, -1, 1),     // Up North -> -Z, +Y
			createVecTriple(1, -1, -1, 1, 1, 1, -1, -1, -1)    // Up South -> +Z, +Y
		),
		WEST to listOf(
			createVecTriple(-1, 1, 1, -1, -1, -1, 1, 1, 1),     // Down North -> -Z, -Y
			createVecTriple(-1, 1, -1, -1, -1, 1, 1, 1, -1),    // Down South -> +Z, -Y
			createVecTriple(-1, -1, -1, -1, 1, 1, 1, -1, -1),   // Up South -> +Z, +Y
			createVecTriple(-1, -1, 1, -1, 1, -1, 1, -1, 1)    // Up North -> -Z, +Y
		)
	)

	private fun createVecTriple(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int, x3: Int, y3: Int, z3: Int): Triple<Vec3d, Vec3d, Vec3d> =
		Triple(Vec3d(x1.toDouble(), y1.toDouble(), z1.toDouble()), Vec3d(x2.toDouble(), y2.toDouble(), z2.toDouble()), Vec3d(x3.toDouble(), y3.toDouble(), z3.toDouble()))

	fun renderArea(area: Area, colour: Color, partialTicks: Float) {
		val player = mc.player
		val pos = player.interpolatePos(partialTicks)

		GlStateManager.pushMatrix()
		GlStateManager.enableAlphaTest()
		GlStateManager.enableBlend()
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO)
		GlStateManager.disableTexture()
		GlStateManager.disableLighting()
		GlStateManager.translated(-pos.x, -pos.y, -pos.z)

		val (r, g, b) = colour.getRGBColorComponents(null).let { Triple(it[0], it[1], it[2]) }
		val box = AxisAlignedBB(area.minPos, area.maxPos.add(1, 1, 1)).grow(0.001)
		if (LMConfig.areaBoxAlpha > 0F) {
			GlStateManager.enableDepthTest()
			GlStateManager.color4f(r, g, b, LMConfig.areaBoxAlpha.toFloat())
			renderSides(box)
			GlStateManager.disableDepthTest()
		}
		if (LMConfig.areaBoxEdgeThickness > 0F) {
			GlStateManager.color4f(r, g, b, 1F)
			renderBoxEdges(box)
		}
		GlStateManager.color3f(1F, 1F, 1F)
		val eyePos = player.getEyePosition(partialTicks)
		val nameRenderPos = box.center.let { Vec3d(it.x, MathHelper.clamp(eyePos.y, box.minY + 0.5, box.minY - 0.5), it.z) }
		renderName(area, nameRenderPos)

		GlStateManager.enableDepthTest()
		GlStateManager.enableLighting()
		GlStateManager.enableTexture()
		GlStateManager.popMatrix()
	}

	private fun renderSides(box: AxisAlignedBB) = Tessellator.getInstance().apply {
		buffer.apply {
			begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)

			//North inside
			pos(box.maxX, box.minY, box.minZ).endVertex()
			pos(box.maxX, box.maxY, box.minZ).endVertex()
			pos(box.minX, box.maxY, box.minZ).endVertex()
			pos(box.minX, box.minY, box.minZ).endVertex()

			//South inside
			pos(box.minX, box.minY, box.maxZ).endVertex()
			pos(box.minX, box.maxY, box.maxZ).endVertex()
			pos(box.maxX, box.maxY, box.maxZ).endVertex()
			pos(box.maxX, box.minY, box.maxZ).endVertex()

			//East inside
			pos(box.minX, box.minY, box.minZ).endVertex()
			pos(box.minX, box.maxY, box.minZ).endVertex()
			pos(box.minX, box.maxY, box.maxZ).endVertex()
			pos(box.minX, box.minY, box.maxZ).endVertex()

			//West inside
			pos(box.maxX, box.minY, box.maxZ).endVertex()
			pos(box.maxX, box.maxY, box.maxZ).endVertex()
			pos(box.maxX, box.maxY, box.minZ).endVertex()
			pos(box.maxX, box.minY, box.minZ).endVertex()

			//Down inside
			pos(box.minX, box.minY, box.maxZ).endVertex()
			pos(box.maxX, box.minY, box.maxZ).endVertex()
			pos(box.maxX, box.minY, box.minZ).endVertex()
			pos(box.minX, box.minY, box.minZ).endVertex()

			//Up inside
			pos(box.minX, box.maxY, box.minZ).endVertex()
			pos(box.maxX, box.maxY, box.minZ).endVertex()
			pos(box.maxX, box.maxY, box.maxZ).endVertex()
			pos(box.minX, box.maxY, box.maxZ).endVertex()

			//North outside
			pos(box.minX, box.minY, box.minZ).endVertex()
			pos(box.minX, box.maxY, box.minZ).endVertex()
			pos(box.maxX, box.maxY, box.minZ).endVertex()
			pos(box.maxX, box.minY, box.minZ).endVertex()

			//South outside
			pos(box.maxX, box.minY, box.maxZ).endVertex()
			pos(box.maxX, box.maxY, box.maxZ).endVertex()
			pos(box.minX, box.maxY, box.maxZ).endVertex()
			pos(box.minX, box.minY, box.maxZ).endVertex()

			//East outside
			pos(box.minX, box.minY, box.maxZ).endVertex()
			pos(box.minX, box.maxY, box.maxZ).endVertex()
			pos(box.minX, box.maxY, box.minZ).endVertex()
			pos(box.minX, box.minY, box.minZ).endVertex()

			//West outside
			pos(box.maxX, box.minY, box.minZ).endVertex()
			pos(box.maxX, box.maxY, box.minZ).endVertex()
			pos(box.maxX, box.maxY, box.maxZ).endVertex()
			pos(box.maxX, box.minY, box.maxZ).endVertex()

			//Down outside
			pos(box.minX, box.minY, box.minZ).endVertex()
			pos(box.maxX, box.minY, box.minZ).endVertex()
			pos(box.maxX, box.minY, box.maxZ).endVertex()
			pos(box.minX, box.minY, box.maxZ).endVertex()

			//Up outside
			pos(box.minX, box.maxY, box.maxZ).endVertex()
			pos(box.maxX, box.maxY, box.maxZ).endVertex()
			pos(box.maxX, box.maxY, box.minZ).endVertex()
			pos(box.minX, box.maxY, box.minZ).endVertex()
		}
		draw()
	}

	private fun renderBoxEdges(box: AxisAlignedBB) {
		val minXminYminZ = Vec3d(box.minX, box.minY, box.minZ)
		val minXminYmaxZ = Vec3d(box.minX, box.minY, box.maxZ)
		val minXmaxYminZ = Vec3d(box.minX, box.maxY, box.minZ)
		val maxXminYminZ = Vec3d(box.maxX, box.minY, box.minZ)
		val minXmaxYmaxZ = Vec3d(box.minX, box.maxY, box.maxZ)
		val maxXmaxYminZ = Vec3d(box.maxX, box.maxY, box.minZ)
		val maxXminYmaxZ = Vec3d(box.maxX, box.minY, box.maxZ)
		val maxXmaxYmaxZ = Vec3d(box.maxX, box.maxY, box.maxZ)
		renderBoxEdgesForSide(UP, minXmaxYmaxZ, maxXmaxYmaxZ, maxXmaxYminZ, minXmaxYminZ)
		renderBoxEdgesForSide(DOWN, minXminYminZ, maxXminYminZ, maxXminYmaxZ, minXminYmaxZ)
		renderBoxEdgesForSide(NORTH, maxXminYminZ, minXminYminZ, minXmaxYminZ, maxXmaxYminZ)
		renderBoxEdgesForSide(SOUTH, minXminYmaxZ, maxXminYmaxZ, maxXmaxYmaxZ, minXmaxYmaxZ)
		renderBoxEdgesForSide(EAST, maxXminYmaxZ, maxXminYminZ, maxXmaxYminZ, maxXmaxYmaxZ)
		renderBoxEdgesForSide(WEST, minXminYminZ, minXminYmaxZ, minXmaxYmaxZ, minXmaxYminZ)
	}

	private fun renderBoxEdgesForSide(side: Direction, vararg corners: Vec3d) = Tessellator.getInstance().apply {
		val offsetByVertex = offsets.getValue(side)
		buffer.apply {
			// Outer
			begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION)
			for (i in 0..4) {
				val actualI = if (i < 4) i else 0
				val triple: Triple<Vec3d, Vec3d, Vec3d> = offsetByVertex[actualI]
				var v = corners[actualI].add(triple.first.scale(LMConfig.areaBoxEdgeThickness))
				pos(v.x, v.y, v.z).endVertex()
				v = corners[actualI].add(triple.second.scale(LMConfig.areaBoxEdgeThickness))
				pos(v.x, v.y, v.z).endVertex()
			}
			draw()

			//Inner
			begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION)
			for (i in 0..4) {
				val actualI = if (i < 4) i else 0
				val triple: Triple<Vec3d, Vec3d, Vec3d> = offsetByVertex[actualI]
				var v = corners[actualI].add(triple.third.scale(LMConfig.areaBoxEdgeThickness))
				pos(v.x, v.y, v.z).endVertex()
				v = corners[actualI].add(triple.first.scale(LMConfig.areaBoxEdgeThickness))
				pos(v.x, v.y, v.z).endVertex()
			}
			draw()
		}
	}

	private fun renderName(area: Area, pos: Vec3d) {
		val rm = mc.renderManager
		val viewerYaw = rm.playerViewY
		val viewerPitch = rm.playerViewX
		val isThirdPersonFrontal = rm.options.thirdPersonView == 2

		GlStateManager.translated(pos.x, pos.y, pos.z)
		GlStateManager.normal3f(0.0F, 1.0F, 0.0F)
		GlStateManager.rotatef(-viewerYaw, 0.0F, 1.0F, 0.0F)
		GlStateManager.rotatef((if (isThirdPersonFrontal) -1F else 1F) * viewerPitch, 1.0F, 0.0F, 0.0F)
		val scale = 0.04F * LMConfig.areaNameScale.toFloat()
		GlStateManager.scalef(-scale, -scale, scale)
		GlStateManager.disableTexture()

		val fr = mc.fontRenderer
		val name = area.name
		val i = fr.getStringWidth(name) / 2
		Tessellator.getInstance().apply {
			buffer.apply {
				begin(7, DefaultVertexFormats.POSITION_COLOR)
				pos((-i - 1).toDouble(), -1.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
				pos((-i - 1).toDouble(), 8.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
				pos((i + 1).toDouble(), 8.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
				pos((i + 1).toDouble(), -1.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
			}
			draw()
		}
		GlStateManager.enableTexture()
		fr.drawString(name, -i.toFloat(), 0F, -1)
		GlStateManager.disableBlend()
	}
}
