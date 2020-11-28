/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.fx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.core.helper.Vector3;

import javax.annotation.Nonnull;

import java.util.*;

// Originally taken with permission from WRCBE - heavily modified
public class FXLightning extends Particle {

	private static final Identifier outsideResource = new Identifier(LibResources.MISC_WISP_LARGE);
	private static final Identifier insideResource = new Identifier(LibResources.MISC_WISP_SMALL);
	private static final int fadetime = 20;
	private final int expandTime;
	private final int colorOuter;
	private final int colorInner;

	private final List<FXLightningSegment> segments;
	private final int segmentCount;

	public FXLightning(ClientWorld world, Vector3 sourcevec, Vector3 targetvec, float speed, long seed, int colorOuter, int colorInner) {
		super(world, sourcevec.x, sourcevec.y, sourcevec.z);
		this.colorOuter = colorOuter;
		this.colorInner = colorInner;
		double length = targetvec.subtract(sourcevec).mag();
		maxAge = fadetime + world.random.nextInt(fadetime) - fadetime / 2;
		expandTime = (int) (length * speed);
		age = -(int) (length * speed);

		LightningSegmentGenerator gen = new LightningSegmentGenerator(seed);
		Pair<Integer, List<FXLightningSegment>> res = gen.compute(sourcevec, targetvec, length);
		segmentCount = res.getFirst();
		segments = res.getSecond();
	}

	@Override
	public void buildGeometry(VertexConsumer buffer, Camera info, float partialTicks) {
		// todo fix this >.>

		// old way (bad position and too thick)
		// LightningHandler.queuedLightningBolts.offer(this);

		// new way (right position but heavy artifacting)

		Vec3d cameraPos = info.getPos();
		MatrixStack ms = new MatrixStack();
		// 0.25f offset for a more pleasing viewing experience
		ms.translate(-cameraPos.getX(), -cameraPos.getY() + 0.25F, -cameraPos.getZ());
		renderBolt(ms, buffer, 0, false);
		renderBolt(ms, buffer, 1, true);
	}

	@Nonnull
	@Override
	public ParticleTextureSheet getType() {
		return RENDER;
	}

	public void renderBolt(MatrixStack ms, VertexConsumer wr, int pass, boolean inner) {
		Matrix4f mat = ms.peek().getModel();

		float boltAge = age < 0 ? 0 : (float) age / (float) maxAge;
		float mainAlpha;
		if (pass == 0) {
			MinecraftClient.getInstance().getTextureManager().bindTexture(outsideResource);
			mainAlpha = (1 - boltAge) * 0.4F;
		} else {
			MinecraftClient.getInstance().getTextureManager().bindTexture(insideResource);
			mainAlpha = 1 - boltAge * 0.5F;
		}

		int renderstart = (int) ((expandTime / 2 - maxAge + age) / (float) (expandTime / 2) * segmentCount);
		int renderend = (int) ((age + expandTime) / (float) expandTime * segmentCount);

		for (FXLightningSegment rendersegment : segments) {
			if (rendersegment.segmentNo < renderstart || rendersegment.segmentNo > renderend) {
				continue;
			}

			Vector3 playerVec = getRelativeViewVector(rendersegment.startPoint.point).multiply(-1);

			double width = 0.025F * (playerVec.mag() / 5 + 1) * (1 + rendersegment.light) * 0.5F;

			Vector3 diff1 = playerVec.crossProduct(rendersegment.prevDiff).normalize().multiply(width / rendersegment.sinPrev);
			Vector3 diff2 = playerVec.crossProduct(rendersegment.nextDiff).normalize().multiply(width / rendersegment.sinNext);

			Vector3 startvec = rendersegment.startPoint.point;
			Vector3 endvec = rendersegment.endPoint.point;

			int color = inner ? colorInner : colorOuter;
			int r = (color & 0xFF0000) >> 16;
			int g = (color & 0xFF00) >> 8;
			int b = color & 0xFF;
			int a = (int) (mainAlpha * rendersegment.light * 0xFF);
			int fullbright = 0xF000F0;

			endvec.subtract(diff2).vertex(mat, wr);
			wr.color(r, g, b, a).texture(0.5F, 0).light(fullbright).next();
			startvec.subtract(diff1).vertex(mat, wr);
			wr.color(r, g, b, a).texture(0.5F, 0).light(fullbright).next();
			startvec.add(diff1).vertex(mat, wr);
			wr.color(r, g, b, a).texture(0.5F, 1).light(fullbright).next();
			endvec.add(diff2).vertex(mat, wr);
			wr.color(r, g, b, a).texture(0.5F, 1).light(fullbright).next();

			if (rendersegment.next == null) {
				Vector3 roundend = rendersegment.endPoint.point.add(rendersegment.diff.normalize().multiply(width));

				roundend.subtract(diff2).vertex(mat, wr);
				wr.color(r, g, b, a).texture(0, 0).light(fullbright).next();
				endvec.subtract(diff2).vertex(mat, wr);
				wr.color(r, g, b, a).texture(0.5F, 0).light(fullbright).next();
				endvec.add(diff2).vertex(mat, wr);
				wr.color(r, g, b, a).texture(0.5F, 1).light(fullbright).next();
				roundend.add(diff2).vertex(mat, wr);
				wr.color(r, g, b, a).texture(0, 1).light(fullbright).next();
			}

			if (rendersegment.prev == null) {
				Vector3 roundend = rendersegment.startPoint.point.subtract(rendersegment.diff.normalize().multiply(width));

				startvec.subtract(diff1).vertex(mat, wr);
				wr.color(r, g, b, a).texture(0.5F, 0).light(fullbright).next();
				roundend.subtract(diff1).vertex(mat, wr);
				wr.color(r, g, b, a).texture(0, 0).light(fullbright).next();
				roundend.add(diff1).vertex(mat, wr);
				wr.color(r, g, b, a).texture(0, 1).light(fullbright).next();
				startvec.add(diff1).vertex(mat, wr);
				wr.color(r, g, b, a).texture(0.5F, 1).light(fullbright).next();
			}
		}
	}

	private static Vector3 getRelativeViewVector(Vector3 pos) {
		Entity renderEntity = MinecraftClient.getInstance().getCameraEntity();
		return new Vector3((float) renderEntity.getX() - pos.x, (float) renderEntity.getY() - pos.y, (float) renderEntity.getZ() - pos.z);
	}

	public static final ParticleTextureSheet RENDER = new ParticleTextureSheet() {
		@Override
		public void begin(BufferBuilder buffer, TextureManager textureManager) {
			RenderSystem.depthMask(false);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			buffer.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
		}

		@Override
		public void draw(Tessellator tess) {
			tess.draw();
			RenderSystem.disableBlend();
			RenderSystem.depthMask(true);
		}

		@Override
		public String toString() {
			return "botania:lightning";
		}
	};

}
