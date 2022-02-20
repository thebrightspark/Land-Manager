package brightspark.landmanager.util

import brightspark.landmanager.LMConfig
import brightspark.landmanager.data.areas.Area
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.IVertexBuilder
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.IRenderTypeBuffer
import net.minecraft.client.renderer.RenderState
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.Direction
import net.minecraft.util.Direction.*
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.vector.Matrix4f
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.util.math.vector.Vector3f
import org.lwjgl.opengl.GL11
import java.awt.Color

object AreaRenderer {
	private val mc = Minecraft.getInstance()
	private val offsets = mapOf(
		// First vector is the inside offset
		// Second is the outside offset
		// Third is the other inside offset for the internal faces
		UP to listOf(
			createVecTriple(1, 1, -1, -1, 1, 1, 1, -1, -1),    // South West -> -X, +Z
			createVecTriple(-1, 1, -1, 1, 1, 1, -1, -1, -1),   // South East -> +X, +Z
			createVecTriple(-1, 1, 1, 1, 1, -1, -1, -1, 1),    // North East -> +X, -Z
			createVecTriple(1, 1, 1, -1, 1, -1, 1, -1, 1)      // North West -> -X, -Z
		),
		DOWN to listOf(
			createVecTriple(1, -1, 1, -1, -1, -1, 1, 1, 1),    // North West -> -X, -Z
			createVecTriple(-1, -1, 1, 1, -1, -1, -1, 1, 1),   // North East -> +X, -Z
			createVecTriple(-1, -1, -1, 1, -1, 1, -1, 1, -1),  // South East -> +X, +Z
			createVecTriple(1, -1, -1, -1, -1, 1, 1, 1, -1)    // South West -> -X, +Z
		),
		NORTH to listOf(
			createVecTriple(-1, 1, -1, 1, -1, -1, -1, 1, 1),   // Down East -> +X, -Y
			createVecTriple(1, 1, -1, -1, -1, -1, 1, 1, 1),    // Down West -> -X, -Y
			createVecTriple(1, -1, -1, -1, 1, -1, 1, -1, 1),   // Up West -> -X, +Y
			createVecTriple(-1, -1, -1, 1, 1, -1, -1, -1, 1)   // Up East -> +X, +Y
		),
		SOUTH to listOf(
			createVecTriple(1, 1, 1, -1, -1, 1, 1, 1, -1),     // Down West -> -X, -Y
			createVecTriple(-1, 1, 1, 1, -1, 1, -1, 1, -1),    // Down East -> +X, -Y
			createVecTriple(-1, -1, 1, 1, 1, 1, -1, -1, -1),   // Up East -> +X, +Y
			createVecTriple(1, -1, 1, -1, 1, 1, 1, -1, -1)     // Up West -> -X, +Y
		),
		EAST to listOf(
			createVecTriple(1, 1, -1, 1, -1, 1, -1, 1, -1),    // Down South -> +Z, -Y
			createVecTriple(1, 1, 1, 1, -1, -1, -1, 1, 1),     // Down North -> -Z, -Y
			createVecTriple(1, -1, 1, 1, 1, -1, -1, -1, 1),    // Up North -> -Z, +Y
			createVecTriple(1, -1, -1, 1, 1, 1, -1, -1, -1)    // Up South -> +Z, +Y
		),
		WEST to listOf(
			createVecTriple(-1, 1, 1, -1, -1, -1, 1, 1, 1),    // Down North -> -Z, -Y
			createVecTriple(-1, 1, -1, -1, -1, 1, 1, 1, -1),   // Down South -> +Z, -Y
			createVecTriple(-1, -1, -1, -1, 1, 1, 1, -1, -1),  // Up South -> +Z, +Y
			createVecTriple(-1, -1, 1, -1, 1, -1, 1, -1, 1)    // Up North -> -Z, +Y
		)
	)

	private val renderTypeAreaEdge = createRenderType(
		"area_edge",
		GL11.GL_QUAD_STRIP,
		RenderType.State.getBuilder()
			.layer(RenderState.field_239235_M_)
			.cull(RenderState.CULL_DISABLED)
			.writeMask(RenderState.COLOR_WRITE)
			.build(true)
	)
	private val renderTypeAreaSide = createRenderType(
		"area_side",
		GL11.GL_QUADS,
		RenderType.State.getBuilder()
			.layer(RenderState.field_239235_M_)
			.transparency(RenderState.TRANSLUCENT_TRANSPARENCY)
			.cull(RenderState.CULL_DISABLED)
			.writeMask(RenderState.COLOR_WRITE)
			.build(true)
	)
	private val renderTypeNameBg = createRenderType(
		"name_bg",
		GL11.GL_QUADS,
		RenderType.State.getBuilder().transparency(RenderState.TRANSLUCENT_TRANSPARENCY).build(false)
	)

	private fun createRenderType(name: String, drawMode: Int, state: RenderType.State): RenderType.Type =
		RenderType.makeType(name, DefaultVertexFormats.POSITION_COLOR, drawMode, 256, state)

	private fun createVecTriple(
		x1: Int, y1: Int, z1: Int,
		x2: Int, y2: Int, z2: Int,
		x3: Int, y3: Int, z3: Int
	): Triple<Vector3f, Vector3f, Vector3f> = Triple(
		Vector3f(x1.toFloat(), y1.toFloat(), z1.toFloat()),
		Vector3f(x2.toFloat(), y2.toFloat(), z2.toFloat()),
		Vector3f(x3.toFloat(), y3.toFloat(), z3.toFloat())
	)

	private fun renderWithType(
		renderType: RenderType.Type,
		matrixStack: MatrixStack,
		render: IVertexBuilder.(Matrix4f) -> Unit
	) {
		RenderSystem.color4f(1F, 1F, 1F, 1F)
//		val buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().buffer)
		val buffer = mc.renderTypeBuffers.bufferSource
		render(buffer.getBuffer(renderType), matrixStack.last.matrix)
		buffer.finish()
	}

	fun renderArea(matrixStack: MatrixStack, view: Vector3d, area: Area, colour: Color, renderSides: Boolean) {
		matrixStack.push()

		val (r, g, b) = colour.getRGBColorComponents(null).let { Triple(it[0], it[1], it[2]) }
		val box = area.displayAabb.get()
		val xSize = box.xSize.toFloat()
		val ySize = box.ySize.toFloat()
		val zSize = box.zSize.toFloat()

		matrixStack.translate(box.minX - view.x, box.minY - view.y, box.minZ - view.z)
		if (renderSides)
			renderSides(matrixStack, xSize, ySize, zSize, r, g, b)
		renderBoxEdges(matrixStack, xSize, ySize, zSize, r, g, b)

		val eyePos = mc.player!!.getEyePosition(mc.renderPartialTicks)
		val boxCenter = box.center
		val nameY = Vector3d(boxCenter.x, MathHelper.clamp(eyePos.y, box.minY + 0.5, box.maxY - 0.5), boxCenter.z)
		renderName(matrixStack, area.name, nameY)

		matrixStack.pop()
	}

	private fun renderSides(
		matrixStack: MatrixStack,
		xSize: Float,
		ySize: Float,
		zSize: Float,
		r: Float,
		g: Float,
		b: Float
	) {
		val a = LMConfig.areaBoxAlpha.toFloat()
		if (a <= 0F)
			return

		renderWithType(renderTypeAreaSide, matrixStack) { matrix ->
			//North
			pos(matrix, xSize, 0F, 0F).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, 0F).color(r, g, b, a).endVertex()
			pos(matrix, 0F, ySize, 0F).color(r, g, b, a).endVertex()
			pos(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex()

			//South
			pos(matrix, 0F, 0F, zSize).color(r, g, b, a).endVertex()
			pos(matrix, 0F, ySize, zSize).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, zSize).color(r, g, b, a).endVertex()
			pos(matrix, xSize, 0F, zSize).color(r, g, b, a).endVertex()

			//East
			pos(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex()
			pos(matrix, 0F, ySize, 0F).color(r, g, b, a).endVertex()
			pos(matrix, 0F, ySize, zSize).color(r, g, b, a).endVertex()
			pos(matrix, 0F, 0F, zSize).color(r, g, b, a).endVertex()

			//West
			pos(matrix, xSize, 0F, zSize).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, zSize).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, 0F).color(r, g, b, a).endVertex()
			pos(matrix, xSize, 0F, 0F).color(r, g, b, a).endVertex()

			//Down
			pos(matrix, 0F, 0F, zSize).color(r, g, b, a).endVertex()
			pos(matrix, xSize, 0F, zSize).color(r, g, b, a).endVertex()
			pos(matrix, xSize, 0F, 0F).color(r, g, b, a).endVertex()
			pos(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex()

			//Up
			pos(matrix, 0F, ySize, 0F).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, 0F).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, zSize).color(r, g, b, a).endVertex()
			pos(matrix, 0F, ySize, zSize).color(r, g, b, a).endVertex()

			/*
			//North outside
			pos(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex()
			pos(matrix, 0F, ySize, 0F).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, 0F).color(r, g, b, a).endVertex()
			pos(matrix, xSize, 0F, 0F).color(r, g, b, a).endVertex()

			//South outside
			pos(matrix, xSize, 0F, zSize).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, zSize).color(r, g, b, a).endVertex()
			pos(matrix, 0F, ySize, zSize).color(r, g, b, a).endVertex()
			pos(matrix, 0F, 0F, zSize).color(r, g, b, a).endVertex()

			//East outside
			pos(matrix, 0F, 0F, zSize).color(r, g, b, a).endVertex()
			pos(matrix, 0F, ySize, zSize).color(r, g, b, a).endVertex()
			pos(matrix, 0F, ySize, 0F).color(r, g, b, a).endVertex()
			pos(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex()

			//West outside
			pos(matrix, xSize, 0F, 0F).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, 0F).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, zSize).color(r, g, b, a).endVertex()
			pos(matrix, xSize, 0F, zSize).color(r, g, b, a).endVertex()

			//Down outside
			pos(matrix, 0F, 0F, 0F).color(r, g, b, a).endVertex()
			pos(matrix, xSize, 0F, 0F).color(r, g, b, a).endVertex()
			pos(matrix, xSize, 0F, zSize).color(r, g, b, a).endVertex()
			pos(matrix, 0F, 0F, zSize).color(r, g, b, a).endVertex()

			//Up outside
			pos(matrix, 0F, ySize, zSize).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, zSize).color(r, g, b, a).endVertex()
			pos(matrix, xSize, ySize, 0F).color(r, g, b, a).endVertex()
			pos(matrix, 0F, ySize, 0F).color(r, g, b, a).endVertex()
			*/
		}
	}

	private fun renderBoxEdges(
		matrixStack: MatrixStack,
		xSize: Float,
		ySize: Float,
		zSize: Float,
		r: Float,
		g: Float,
		b: Float
	) {
		if (LMConfig.areaBoxEdgeThickness <= 0.0)
			return

		val minXminYminZ = Vector3f(0F, 0F, 0F)
		val minXminYmaxZ = Vector3f(0F, 0F, zSize)
		val minXmaxYminZ = Vector3f(0F, ySize, 0F)
		val maxXminYminZ = Vector3f(xSize, 0F, 0F)
		val minXmaxYmaxZ = Vector3f(0F, ySize, zSize)
		val maxXmaxYminZ = Vector3f(xSize, ySize, 0F)
		val maxXminYmaxZ = Vector3f(xSize, 0F, zSize)
		val maxXmaxYmaxZ = Vector3f(xSize, ySize, zSize)
		renderBoxEdgesForSide(matrixStack, UP, r, g, b, minXmaxYmaxZ, maxXmaxYmaxZ, maxXmaxYminZ, minXmaxYminZ)
		renderBoxEdgesForSide(matrixStack, DOWN, r, g, b, minXminYminZ, maxXminYminZ, maxXminYmaxZ, minXminYmaxZ)
		renderBoxEdgesForSide(matrixStack, NORTH, r, g, b, maxXminYminZ, minXminYminZ, minXmaxYminZ, maxXmaxYminZ)
		renderBoxEdgesForSide(matrixStack, SOUTH, r, g, b, minXminYmaxZ, maxXminYmaxZ, maxXmaxYmaxZ, minXmaxYmaxZ)
		renderBoxEdgesForSide(matrixStack, EAST, r, g, b, maxXminYmaxZ, maxXminYminZ, maxXmaxYminZ, maxXmaxYmaxZ)
		renderBoxEdgesForSide(matrixStack, WEST, r, g, b, minXminYminZ, minXminYmaxZ, minXmaxYmaxZ, minXmaxYminZ)
	}

	private fun renderBoxEdgesForSide(
		matrixStack: MatrixStack,
		side: Direction,
		r: Float,
		g: Float,
		b: Float,
		vararg corners: Vector3f
	) {
		val offsetByVertex = offsets.getValue(side)
		val thickness = LMConfig.areaBoxEdgeThickness.toFloat()

		// Outer
		renderWithType(renderTypeAreaEdge, matrixStack) { matrix ->
			for (i in 0..4) {
				val actualI = if (i < 4) i else 0
				val triple: Triple<Vector3f, Vector3f, Vector3f> = offsetByVertex[actualI]
				var v = corners[actualI].copy().also { corner ->
					corner.add(triple.first.copy().also { it.mul(thickness) })
				}
				pos(matrix, v.x, v.y, v.z).color(r, g, b, 1F).endVertex()
				v = corners[actualI].copy().also { corner ->
					corner.add(triple.second.copy().also { it.mul(thickness) })
				}
				pos(matrix, v.x, v.y, v.z).color(r, g, b, 1F).endVertex()
			}
		}

		//Inner
		renderWithType(renderTypeAreaEdge, matrixStack) { matrix ->
			for (i in 0..4) {
				val actualI = if (i < 4) i else 0
				val triple: Triple<Vector3f, Vector3f, Vector3f> = offsetByVertex[actualI]
				var v = corners[actualI].copy().also { corner ->
					corner.add(triple.third.copy().also { it.mul(thickness) })
				}
				pos(matrix, v.x, v.y, v.z).color(r, g, b, 1F).endVertex()
				v = corners[actualI].copy().also { corner ->
					corner.add(triple.first.copy().also { it.mul(thickness) })
				}
				pos(matrix, v.x, v.y, v.z).color(r, g, b, 1F).endVertex()
			}
		}
	}

	// FIXME: This isn't rendering properly atm
	private fun renderName(matrixStack: MatrixStack, name: String, pos: Vector3d) {
		matrixStack.push()
		matrixStack.translate(pos.x, pos.y, pos.z)
//		RenderSystem.normal3f(0.0F, 1.0F, 0.0F)
		matrixStack.rotate(mc.renderManager.cameraOrientation)
		val scale = 0.04F * LMConfig.areaNameScale.toFloat()
		matrixStack.scale(-scale, -scale, scale)

		val fr = mc.fontRenderer
		val width = -(fr.getStringWidth(name) / 2).toFloat()

		renderWithType(renderTypeNameBg, matrixStack) { matrix ->
			pos(matrix, -width - 1F, -1F, 0F).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
			pos(matrix, -width - 1F, 8F, 0F).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
			pos(matrix, width + 1F, 8F, 0F).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
			pos(matrix, width + 1F, -1F, 0F).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex()
		}
		val buffer = IRenderTypeBuffer.getImpl(Tessellator.getInstance().buffer)
		fr.renderString(name, width, 0F, -1, false, matrixStack.last.matrix, buffer, false, 0, 15728880)
//		val bgColour = (mc.gameSettings.getTextBackgroundColor(0.25F) * 255F).toInt() shl 24
//		fr.func_243247_a(StringTextComponent(name), width, 0F, -1, false, matrixStack.last.matrix, buffer, false, bgColour, 15728880)
		buffer.finish()
		matrixStack.pop()
	}
}
