package brightspark.landmanager.util;

import brightspark.landmanager.LMConfig;
import brightspark.landmanager.data.areas.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoxRenderer
{
	private static final Minecraft MC = Minecraft.getMinecraft();
	private static final Map<EnumFacing, List<Triple<Vec3d, Vec3d, Vec3d>>> OFFSETS = new HashMap<>();

	static
	{
		// https://minecraft.gamepedia.com/File:Minecraft_axes.png
		/*
		From UP:
					North (-Z)
		West (-X)				East (+X)
					South (+Z)

		From DOWN:
					South (+Z)
		West (-X)				East (+X)
					North (-Z)

		From NORTH:
					UP (+Y)
		East (+X)				West (-X)
					DOWN(-Y)

		From SOUTH:
					UP (+Y)
		West (-X)				East (+X)
					DOWN(-Y)

		From EAST:
					UP (+Y)
		South (+Z)				North (-Z)
					DOWN(-Y)

		From WEST:
					UP (+Y)
		North (-Z)				South (+Z)
					DOWN(-Y)
		*/
		//First vector is the inside offset
		//Second is the outside offset
		//Third is the other inside offset for the internal faces
		OFFSETS.put(EnumFacing.UP, Arrays.asList(
			createVecTriple(1, 1, -1, -1, 1, 1, 1, -1, -1),     // South West -> -X, +Z
			createVecTriple(-1, 1, -1, 1, 1, 1, -1, -1, -1),    // South East -> +X, +Z
			createVecTriple(-1, 1, 1, 1, 1, -1, -1, -1, 1),     // North East -> +X, -Z
			createVecTriple(1, 1, 1, -1, 1, -1, 1, -1, 1)));    // North West -> -X, -Z
		OFFSETS.put(EnumFacing.DOWN, Arrays.asList(
			createVecTriple(1, -1, 1, -1, -1, -1, 1, 1, 1),     // North West -> -X, -Z
			createVecTriple(-1, -1, 1, 1, -1, -1, -1, 1, 1),    // North East -> +X, -Z
			createVecTriple(-1, -1, -1, 1, -1, 1, -1, 1, -1),   // South East -> +X, +Z
			createVecTriple(1, -1, -1, -1, -1, 1, 1, 1, -1)));  // South West -> -X, +Z
		OFFSETS.put(EnumFacing.NORTH, Arrays.asList(
			createVecTriple(-1, 1, -1, 1, -1, -1, -1, 1, 1),    // Down East -> +X, -Y
			createVecTriple(1, 1, -1, -1, -1, -1, 1, 1, 1),     // Down West -> -X, -Y
			createVecTriple(1, -1, -1, -1, 1, -1, 1, -1, 1),    // Up West -> -X, +Y
			createVecTriple(-1, -1, -1, 1, 1, -1, -1, -1, 1))); // Up East -> +X, +Y
		OFFSETS.put(EnumFacing.SOUTH, Arrays.asList(
			createVecTriple(1, 1, 1, -1, -1, 1, 1, 1, -1),      // Down West -> -X, -Y
			createVecTriple(-1, 1, 1, 1, -1, 1, -1, 1, -1),     // Down East -> +X, -Y
			createVecTriple(-1, -1, 1, 1, 1, 1, -1, -1, -1),    // Up East -> +X, +Y
			createVecTriple(1, -1, 1, -1, 1, 1, 1, -1, -1)));   // Up West -> -X, +Y
		OFFSETS.put(EnumFacing.EAST, Arrays.asList(
			createVecTriple(1, 1, -1, 1, -1, 1, -1, 1, -1),     // Down South -> +Z, -Y
			createVecTriple(1, 1, 1, 1, -1, -1, -1, 1, 1),      // Down North -> -Z, -Y
			createVecTriple(1, -1, 1, 1, 1, -1, -1, -1, 1),     // Up North -> -Z, +Y
			createVecTriple(1, -1, -1, 1, 1, 1, -1, -1, -1)));  // Up South -> +Z, +Y
		OFFSETS.put(EnumFacing.WEST, Arrays.asList(
			createVecTriple(-1, 1, 1, -1, -1, -1, 1, 1, 1),     // Down North -> -Z, -Y
			createVecTriple(-1, 1, -1, -1, -1, 1, 1, 1, -1),    // Down South -> +Z, -Y
			createVecTriple(-1, -1, -1, -1, 1, 1, 1, -1, -1),   // Up South -> +Z, +Y
			createVecTriple(-1, -1, 1, -1, 1, -1, 1, -1, 1)));  // Up North -> -Z, +Y
	}

	private static Triple<Vec3d, Vec3d, Vec3d> createVecTriple(int x1, int y1, int z1, int x2, int y2, int z2, int x3, int y3, int z3)
	{
		return new ImmutableTriple<>(new Vec3d(x1, y1, z1), new Vec3d(x2, y2, z2), new Vec3d(x3, y3, z3));
	}

	public static void renderBox(Area area, Color colour, double partialTicks)
	{
		//Get player's actual position
		EntityPlayerSP player = MC.player;
		double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
		double y = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
		double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
		//Render the box
		GlStateManager.pushMatrix();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableTexture2D();
		GlStateManager.disableLighting();
		GlStateManager.translate(-x, -y, -z);
		float[] rgb = colour.getRGBColorComponents(null);
		AxisAlignedBB box = new AxisAlignedBB(area.getMinPos(), area.getMaxPos().add(1, 1, 1)).grow(0.001d);
		if(LMConfig.client.areaBoxAlpha > 0f)
		{
			GlStateManager.enableDepth();
			GlStateManager.color(rgb[0], rgb[1], rgb[2], LMConfig.client.areaBoxAlpha);
			renderSides(box);
			GlStateManager.disableDepth();
		}
		if(LMConfig.client.areaBoxEdgeThickness > 0f)
		{
			GlStateManager.color(rgb[0], rgb[1], rgb[2], 1f);
			renderBoxEdges(box);
		}
		GlStateManager.color(1f, 1f, 1f);
		Vec3d playerPos = player.getPositionEyes((float) partialTicks);
		Vec3d nameRenderPos = box.getCenter();
		nameRenderPos = new Vec3d(nameRenderPos.x, MathHelper.clamp(playerPos.y, box.minY + 0.5d, box.maxY - 0.5d), nameRenderPos.z);
		renderName(area, nameRenderPos);
		GlStateManager.enableTexture2D();
		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
		GlStateManager.popMatrix();
	}

	private static void renderSides(AxisAlignedBB box)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

		//North inside
		buffer.pos(box.maxX, box.minY, box.minZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
		buffer.pos(box.minX, box.maxY, box.minZ).endVertex();
		buffer.pos(box.minX, box.minY, box.minZ).endVertex();

		//South inside
		buffer.pos(box.minX, box.minY, box.maxZ).endVertex();
		buffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
		buffer.pos(box.maxX, box.minY, box.maxZ).endVertex();

		//East inside
		buffer.pos(box.minX, box.minY, box.minZ).endVertex();
		buffer.pos(box.minX, box.maxY, box.minZ).endVertex();
		buffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
		buffer.pos(box.minX, box.minY, box.maxZ).endVertex();

		//West inside
		buffer.pos(box.maxX, box.minY, box.maxZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
		buffer.pos(box.maxX, box.minY, box.minZ).endVertex();

		//Down inside
		buffer.pos(box.minX, box.minY, box.maxZ).endVertex();
		buffer.pos(box.maxX, box.minY, box.maxZ).endVertex();
		buffer.pos(box.maxX, box.minY, box.minZ).endVertex();
		buffer.pos(box.minX, box.minY, box.minZ).endVertex();

		//Up inside
		buffer.pos(box.minX, box.maxY, box.minZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
		buffer.pos(box.minX, box.maxY, box.maxZ).endVertex();

		//North outside
		buffer.pos(box.minX, box.minY, box.minZ).endVertex();
		buffer.pos(box.minX, box.maxY, box.minZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
		buffer.pos(box.maxX, box.minY, box.minZ).endVertex();

		//South outside
		buffer.pos(box.maxX, box.minY, box.maxZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
		buffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
		buffer.pos(box.minX, box.minY, box.maxZ).endVertex();

		//East outside
		buffer.pos(box.minX, box.minY, box.maxZ).endVertex();
		buffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
		buffer.pos(box.minX, box.maxY, box.minZ).endVertex();
		buffer.pos(box.minX, box.minY, box.minZ).endVertex();

		//West outside
		buffer.pos(box.maxX, box.minY, box.minZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
		buffer.pos(box.maxX, box.minY, box.maxZ).endVertex();

		//Down outside
		buffer.pos(box.minX, box.minY, box.minZ).endVertex();
		buffer.pos(box.maxX, box.minY, box.minZ).endVertex();
		buffer.pos(box.maxX, box.minY, box.maxZ).endVertex();
		buffer.pos(box.minX, box.minY, box.maxZ).endVertex();

		//Up outside
		buffer.pos(box.minX, box.maxY, box.maxZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.maxZ).endVertex();
		buffer.pos(box.maxX, box.maxY, box.minZ).endVertex();
		buffer.pos(box.minX, box.maxY, box.minZ).endVertex();

		tessellator.draw();
	}

	private static void renderBoxEdges(AxisAlignedBB box)
	{
		Vec3d minXminYminZ = new Vec3d(box.minX, box.minY, box.minZ);
		Vec3d minXminYmaxZ = new Vec3d(box.minX, box.minY, box.maxZ);
		Vec3d minXmaxYminZ = new Vec3d(box.minX, box.maxY, box.minZ);
		Vec3d maxXminYminZ = new Vec3d(box.maxX, box.minY, box.minZ);
		Vec3d minXmaxYmaxZ = new Vec3d(box.minX, box.maxY, box.maxZ);
		Vec3d maxXmaxYminZ = new Vec3d(box.maxX, box.maxY, box.minZ);
		Vec3d maxXminYmaxZ = new Vec3d(box.maxX, box.minY, box.maxZ);
		Vec3d maxXmaxYmaxZ = new Vec3d(box.maxX, box.maxY, box.maxZ);
		renderBoxEdgesForSide(EnumFacing.UP, minXmaxYmaxZ, maxXmaxYmaxZ, maxXmaxYminZ, minXmaxYminZ);
		renderBoxEdgesForSide(EnumFacing.DOWN, minXminYminZ, maxXminYminZ, maxXminYmaxZ, minXminYmaxZ);
		renderBoxEdgesForSide(EnumFacing.NORTH, maxXminYminZ, minXminYminZ, minXmaxYminZ, maxXmaxYminZ);
		renderBoxEdgesForSide(EnumFacing.SOUTH, minXminYmaxZ, maxXminYmaxZ, maxXmaxYmaxZ, minXmaxYmaxZ);
		renderBoxEdgesForSide(EnumFacing.EAST, maxXminYmaxZ, maxXminYminZ, maxXmaxYminZ, maxXmaxYmaxZ);
		renderBoxEdgesForSide(EnumFacing.WEST, minXminYminZ, minXminYmaxZ, minXmaxYmaxZ, minXmaxYminZ);
	}

	private static void renderBoxEdgesForSide(EnumFacing facing, Vec3d... corners)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		List<Triple<Vec3d, Vec3d, Vec3d>> offsetByVertex = OFFSETS.get(facing);
		//Outer
		buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
		for(int i = 0; i < 5; i++)
		{
			int actualI = i < 4 ? i : 0;
			Triple<Vec3d, Vec3d, Vec3d> triple = offsetByVertex.get(actualI);
			Vec3d v = corners[actualI].add(triple.getLeft().scale(LMConfig.client.areaBoxEdgeThickness));
			buffer.pos(v.x, v.y, v.z).endVertex();
			v = corners[actualI].add(triple.getMiddle().scale(LMConfig.client.areaBoxEdgeThickness));
			buffer.pos(v.x, v.y, v.z).endVertex();
		}
		tessellator.draw();
		//Inner
		buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION);
		for(int i = 0; i < 5; i++)
		{
			int actualI = i < 4 ? i : 0;
			Triple<Vec3d, Vec3d, Vec3d> triple = offsetByVertex.get(actualI);
			Vec3d v = corners[actualI].add(triple.getRight().scale(LMConfig.client.areaBoxEdgeThickness));
			buffer.pos(v.x, v.y, v.z).endVertex();
			v = corners[actualI].add(triple.getLeft().scale(LMConfig.client.areaBoxEdgeThickness));
			buffer.pos(v.x, v.y, v.z).endVertex();
		}
		tessellator.draw();
	}

	//Copied a lot of this from EntityRenderer#drawNameplate and changed for my needs
	private static void renderName(Area area, Vec3d center)
	{
		RenderManager rm = MC.getRenderManager();
		float viewerYaw = rm.playerViewY;
		float viewerPitch = rm.playerViewX;
		boolean isThirdPersonFrontal = rm.options.thirdPersonView == 2;

		GlStateManager.translate(center.x, center.y, center.z);
		GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-viewerYaw, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate((float) (isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
		float scale = 0.04f * LMConfig.client.areaNameScale;
		GlStateManager.scale(-scale, -scale, scale);
		GlStateManager.disableTexture2D();

		FontRenderer fr = MC.fontRenderer;
		String name = area.getName();
		int i = fr.getStringWidth(name) / 2;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos((double) (-i - 1), -1D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		bufferbuilder.pos((double) (-i - 1), 8D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		bufferbuilder.pos((double) (i + 1), 8D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		bufferbuilder.pos((double) (i + 1), -1D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		fr.drawString(name, -i, 0, -1);
		GlStateManager.disableBlend();
	}
}
