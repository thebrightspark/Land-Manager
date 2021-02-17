package brightspark.landmanager.util

import brightspark.landmanager.LMConfig
import brightspark.landmanager.data.areas.Area
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderState
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.Direction
import net.minecraft.util.Direction.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Vector3d
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*

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

	private val noLine = RenderState.LineState(OptionalDouble.of(0.0))
	private val renderTypeAreaEdge = RenderType.makeType(
		"area_edge",
		DefaultVertexFormats.POSITION,
		GL11.GL_QUAD_STRIP,
		256,
		RenderType.State.getBuilder().line(noLine).build(false)
	)
	private val renderTypeAreaSide = RenderType.makeType(
		"area_side",
		DefaultVertexFormats.POSITION,
		GL11.GL_QUADS,
		256,
		RenderType.State.getBuilder().line(noLine).transparency(RenderState.TRANSLUCENT_TRANSPARENCY).build(false)
	)

	private fun createVecTriple(
		x1: Int,
		y1: Int,
		z1: Int,
		x2: Int,
		y2: Int,
		z2: Int,
		x3: Int,
		y3: Int,
		z3: Int
	): Triple<Vector3d, Vector3d, Vector3d> =
		Triple(
			Vector3d(x1.toDouble(), y1.toDouble(), z1.toDouble()),
			Vector3d(x2.toDouble(), y2.toDouble(), z2.toDouble()),
			Vector3d(x3.toDouble(), y3.toDouble(), z3.toDouble())
		)

	fun renderArea(area: Area, colour: Color, matrixStack: MatrixStack, buffer: IRenderTypeBuffer) {
		matrixStack.push()

		val projectedView = mc.gameRenderer.activeRenderInfo.projectedView
		matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z)

		val (r, g, b) = colour.getRGBColorComponents(null).let { Triple(it[0], it[1], it[2]) }
		val box = AxisAlignedBB(area.minPos, area.maxPos.add(1, 1, 1)).grow(0.001)
		if (LMConfig.areaBoxAlpha > 0F) {
			RenderSystem.color4f(r, g, b, LMConfig.areaBoxAlpha.toFloat())
			renderSides(box, buffer)
		}
		if (LMConfig.areaBoxEdgeThickness > 0F) {
			RenderSystem.color4f(r, g, b, 1F)
			renderBoxEdges(box, buffer)
		}
		RenderSystem.color4f(1F, 1F, 1F, 1F)
		val namePos = box.center.let {
			Vector3d(it.x, MathHelper.clamp(projectedView.y, box.minY + 0.5, box.maxY - 0.5), it.z)
		}
		renderName(area, namePos, matrixStack, buffer)

		matrixStack.pop()
	}

	private fun renderSides(box: AxisAlignedBB, buffer: IRenderTypeBuffer): Unit =
		buffer.getBuffer(renderTypeAreaSide).run {
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

	private fun renderBoxEdges(box: AxisAlignedBB, buffer: IRenderTypeBuffer) {
		val minXminYminZ = Vector3d(box.minX, box.minY, box.minZ)
		val minXminYmaxZ = Vector3d(box.minX, box.minY, box.maxZ)
		val minXmaxYminZ = Vector3d(box.minX, box.maxY, box.minZ)
		val maxXminYminZ = Vector3d(box.maxX, box.minY, box.minZ)
		val minXmaxYmaxZ = Vector3d(box.minX, box.maxY, box.maxZ)
		val maxXmaxYminZ = Vector3d(box.maxX, box.maxY, box.minZ)
		val maxXminYmaxZ = Vector3d(box.maxX, box.minY, box.maxZ)
		val maxXmaxYmaxZ = Vector3d(box.maxX, box.maxY, box.maxZ)
		renderBoxEdgesForSide(buffer, UP, minXmaxYmaxZ, maxXmaxYmaxZ, maxXmaxYminZ, minXmaxYminZ)
		renderBoxEdgesForSide(buffer, DOWN, minXminYminZ, maxXminYminZ, maxXminYmaxZ, minXminYmaxZ)
		renderBoxEdgesForSide(buffer, NORTH, maxXminYminZ, minXminYminZ, minXmaxYminZ, maxXmaxYminZ)
		renderBoxEdgesForSide(buffer, SOUTH, minXminYmaxZ, maxXminYmaxZ, maxXmaxYmaxZ, minXmaxYmaxZ)
		renderBoxEdgesForSide(buffer, EAST, maxXminYmaxZ, maxXminYminZ, maxXmaxYminZ, maxXmaxYmaxZ)
		renderBoxEdgesForSide(buffer, WEST, minXminYminZ, minXminYmaxZ, minXmaxYmaxZ, minXmaxYminZ)
	}

	private fun renderBoxEdgesForSide(buffer: IRenderTypeBuffer, side: Direction, vararg corners: Vector3d) {
		val offsetByVertex = offsets.getValue(side)

		// Outer
		buffer.getBuffer(renderTypeAreaEdge).apply {
			for (i in 0..4) {
				val actualI = if (i < 4) i else 0
				val triple: Triple<Vector3d, Vector3d, Vector3d> = offsetByVertex[actualI]
				var v = corners[actualI].add(triple.first.scale(LMConfig.areaBoxEdgeThickness))
				pos(v.x, v.y, v.z).endVertex()
				v = corners[actualI].add(triple.second.scale(LMConfig.areaBoxEdgeThickness))
				pos(v.x, v.y, v.z).endVertex()
			}
		}

		//Inner
		buffer.getBuffer(renderTypeAreaEdge).apply {
			for (i in 0..4) {
				val actualI = if (i < 4) i else 0
				val triple: Triple<Vector3d, Vector3d, Vector3d> = offsetByVertex[actualI]
				var v = corners[actualI].add(triple.third.scale(LMConfig.areaBoxEdgeThickness))
				pos(v.x, v.y, v.z).endVertex()
				v = corners[actualI].add(triple.first.scale(LMConfig.areaBoxEdgeThickness))
				pos(v.x, v.y, v.z).endVertex()
			}
		}
	}

	private fun renderName(area: Area, pos: Vector3d, matrixStack: MatrixStack, buffer: IRenderTypeBuffer) {
		matrixStack.translate(pos.x, pos.y, pos.z)
		RenderSystem.normal3f(0.0F, 1.0F, 0.0F)
		val camera = mc.gameRenderer.activeRenderInfo.viewVector
//		matrixStack.rotate(Quaternion()) // FIXME
		GlStateManager.rotatef(-camera.y, 0.0F, 1.0F, 0.0F)
		GlStateManager.rotatef(camera.x, 1.0F, 0.0F, 0.0F)
		val scale = 0.04F * LMConfig.areaNameScale.toFloat()
		matrixStack.scale(-scale, -scale, scale)

		val fr = mc.fontRenderer
		val name = area.name
		val x = -(fr.getStringWidth(name) / 2).toFloat()
		/*Tessellator.getInstance().apply {
			buffer.apply {
				begin(7, DefaultVertexFormats.POSITION_COLOR)
				pos((-i - 1).toDouble(), -1.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
				pos((-i - 1).toDouble(), 8.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
				pos((i + 1).toDouble(), 8.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
				pos((i + 1).toDouble(), -1.0, 0.0).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
			}
			draw()
		}*/
		fr.renderString(name, x, 0F, -1, false, matrixStack.last.matrix, buffer, false, 0, 15728880)
	}
}
