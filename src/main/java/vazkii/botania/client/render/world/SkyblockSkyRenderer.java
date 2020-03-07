/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [18/12/2015, 02:06:56 (GMT)]
 */
package vazkii.botania.client.render.world;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.lib.LibResources;

import java.util.Random;

public class SkyblockSkyRenderer implements IRenderHandler {

	private static final ResourceLocation textureSkybox = new ResourceLocation(LibResources.MISC_SKYBOX);
	private static final ResourceLocation textureRainbow = new ResourceLocation(LibResources.MISC_RAINBOW);
	private static final ResourceLocation MOON_PHASES_TEXTURES = new ResourceLocation("textures/environment/moon_phases.png");
	private static final ResourceLocation SUN_TEXTURES = new ResourceLocation("textures/environment/sun.png");
	private static final ResourceLocation[] planetTextures = new ResourceLocation[] {
			new ResourceLocation(LibResources.MISC_PLANET + "0.png"),
			new ResourceLocation(LibResources.MISC_PLANET + "1.png"),
			new ResourceLocation(LibResources.MISC_PLANET + "2.png"),
			new ResourceLocation(LibResources.MISC_PLANET + "3.png"),
			new ResourceLocation(LibResources.MISC_PLANET + "4.png"),
			new ResourceLocation(LibResources.MISC_PLANET + "5.png")
	};

	// [VanillaCopy] WorldRenderer.renderSky, overworld section, edits noted
	@Override
	public void render(int ticks, float partialTicks, ClientWorld world, Minecraft mc) {
		// todo 1.15 need the matrix to do anything useful
	}

	/*
	private void renderExtra(ClientWorld world, float partialTicks, float insideVoid) {
		// Botania - Begin extra stuff
		Tessellator tessellator = Tessellator.getInstance();
		float rain = 1.0F - world.getRainStrength(partialTicks);
		float celAng = world.getCelestialAngle(partialTicks);
		float effCelAng = celAng;
		if(celAng > 0.5)
			effCelAng = 0.5F - (celAng - 0.5F);

		// === Planets
		float scale = 20F;
		float lowA = Math.max(0F, effCelAng - 0.3F) * rain;
		float a = Math.max(0.1F, lowA);

		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		GlStateManager.pushMatrix();
		GlStateManager.color4f(1F, 1F, 1F, a * 4 * (1F - insideVoid));
		GlStateManager.rotatef(90F, 0.5F, 0.5F, 0.0F);
		for(int p = 0; p < planetTextures.length; p++) {
			Minecraft.getInstance().textureManager.bindTexture(planetTextures[p]);
			tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			tessellator.getBuffer().pos(-scale, 100.0D, -scale).tex(0.0D, 0.0D).endVertex();
			tessellator.getBuffer().pos(scale, 100.0D, -scale).tex(1.0D, 0.0D).endVertex();
			tessellator.getBuffer().pos(scale, 100.0D, scale).tex(1.0D, 1.0D).endVertex();
			tessellator.getBuffer().pos(-scale, 100.0D, scale).tex(0.0D, 1.0D).endVertex();
			tessellator.draw();

			switch(p) {
				case 0:
					GlStateManager.rotatef(70F, 1F, 0F, 0F);
					scale = 12F;
					break;
				case 1:
					GlStateManager.rotatef(120F, 0F, 0F, 1F);
					scale = 15F;
					break;
				case 2:
					GlStateManager.rotatef(80F, 1F, 0F, 1F);
					scale = 25F;
					break;
				case 3:
					GlStateManager.rotatef(100F, 0F, 0F, 1F);
					scale = 10F;
					break;
				case 4:
					GlStateManager.rotatef(-60F, 1F, 0F, 0.5F);
					scale = 40F;
			}
		}
		GlStateManager.color4f(1F, 1F, 1F, 1F);
		GlStateManager.popMatrix();

		// === Rays
		Minecraft.getInstance().textureManager.bindTexture(textureSkybox);

		scale = 20F;
		a = lowA;
		GlStateManager.pushMatrix();
		GlStateManager.blendFuncSeparate(770, 1, 1, 0);
		GlStateManager.translatef(0F, -1F, 0F);
		GlStateManager.rotatef(220F, 1F, 0F, 0F);
		GlStateManager.color4f(1F, 1F, 1F, a);
		int angles = 90;
		float y = 2F;
		float y0 = 0F;
		float uPer = 1F / 360F;
		float anglePer = 360F / angles;
		double fuzzPer = Math.PI * 10 / angles;
		float rotSpeed = 1F;
		float rotSpeedMod = 0.4F;

		for(int p = 0; p < 3; p++) {
			float baseAngle = rotSpeed * rotSpeedMod * (ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks);
			GlStateManager.rotatef((ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks) * 0.25F * rotSpeed * rotSpeedMod, 0F, 1F, 0F);

			tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			for(int i = 0; i < angles; i++) {
				int j = i;
				if(i % 2 == 0)
					j--;

				float ang = j * anglePer + baseAngle;
				double xp = Math.cos(ang * Math.PI / 180F) * scale;
				double zp = Math.sin(ang * Math.PI / 180F) * scale;
				double yo = Math.sin(fuzzPer * j) * 1;

				float ut = ang * uPer;
				if(i % 2 == 0) {
					tessellator.getBuffer().pos(xp, yo + y0 + y, zp).tex(ut, 1F).endVertex();
					tessellator.getBuffer().pos(xp, yo + y0, zp).tex(ut, 0).endVertex();
				} else {
					tessellator.getBuffer().pos(xp, yo + y0, zp).tex(ut, 0).endVertex();
					tessellator.getBuffer().pos(xp, yo + y0 + y, zp).tex(ut, 1F).endVertex();
				}

			}
			tessellator.draw();

			switch(p) {
				case 0:
					GlStateManager.rotatef(20F, 1F, 0F, 0F);
					GlStateManager.color4f(1F, 0.4F, 0.4F, a);
					fuzzPer = Math.PI * 14 / angles;
					rotSpeed = 0.2F;
					break;
				case 1:
					GlStateManager.rotatef(50F, 1F, 0F, 0F);
					GlStateManager.color4f(0.4F, 1F, 0.7F, a);
					fuzzPer = Math.PI * 6 / angles;
					rotSpeed = 2F;
					break;
			}
		}
		GlStateManager.popMatrix();

		// === Rainbow
		GlStateManager.pushMatrix();
		GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		Minecraft.getInstance().textureManager.bindTexture(textureRainbow);
		scale = 10F;
		float effCelAng1 = celAng;
		if(effCelAng1 > 0.25F)
			effCelAng1 = 1F - effCelAng1;
		effCelAng1 = 0.25F - Math.min(0.25F, effCelAng1);

		long time = world.getDayTime() + 1000;
		int day = (int) (time / 24000L);
		Random rand = new Random(day * 0xFF);
		float angle1 = rand.nextFloat() * 360F;
		float angle2 = rand.nextFloat() * 360F;
		GlStateManager.color4f(1F, 1F, 1F, effCelAng1 * (1F - insideVoid));
		GlStateManager.rotatef(angle1, 0F, 1F, 0F);
		GlStateManager.rotatef(angle2, 0F, 0F, 1F);

		tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		for(int i = 0; i < angles; i++) {
			int j = i;
			if(i % 2 == 0)
				j--;

			float ang = j * anglePer;
			double xp = Math.cos(ang * Math.PI / 180F) * scale;
			double zp = Math.sin(ang * Math.PI / 180F) * scale;
			double yo = 0;

			float ut = ang * uPer;
			if(i % 2 == 0) {
				tessellator.getBuffer().pos(xp, yo + y0 + y, zp).tex(ut, 1F).endVertex();
				tessellator.getBuffer().pos(xp, yo + y0, zp).tex(ut, 0).endVertex();
			} else {
				tessellator.getBuffer().pos(xp, yo + y0, zp).tex(ut, 0).endVertex();
				tessellator.getBuffer().pos(xp, yo + y0 + y, zp).tex(ut, 1F).endVertex();
			}

		}
		tessellator.draw();
		GlStateManager.popMatrix();
		GlStateManager.color4f(1F, 1F, 1F, 1F - insideVoid);
		GlStateManager.blendFuncSeparate(770, 1, 1, 0);
	}

	private void renderStars(Minecraft mc, float alpha, float partialTicks) {
		int starGLCallList = mc.worldRenderer.starGLCallList;
		net.minecraft.client.renderer.vertex.VertexBuffer starVBO = mc.worldRenderer.starVBO;

		float t = (ClientTickHandler.ticksInGame + partialTicks + 2000) * 0.005F;
		GlStateManager.pushMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotatef(t * 3, 0F, 1F, 0F);
		GlStateManager.color4f(1F, 1F, 1F, alpha);
		drawVboOrList(starVBO, starGLCallList);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotatef(t, 0F, 1F, 0F);
		GlStateManager.color4f(0.5F, 1F, 1F, alpha);
		drawVboOrList(starVBO, starGLCallList);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotatef(t * 2, 0F, 1F, 0F);
		GlStateManager.color4f(1F, 0.75F, 0.75F, alpha);
		drawVboOrList(starVBO, starGLCallList);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotatef(t * 3, 0F, 0F, 1F);
		GlStateManager.color4f(1F, 1F, 1F, 0.25F * alpha);
		drawVboOrList(starVBO, starGLCallList);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotatef(t, 0F, 0F, 1F);
		GlStateManager.color4f(0.5F, 1F, 1F, 0.25F * alpha);
		drawVboOrList(starVBO, starGLCallList);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.rotatef(t * 2, 0F, 0F, 1F);
		GlStateManager.color4f(1F, 0.75F, 0.75F, 0.25F * alpha);
		drawVboOrList(starVBO, starGLCallList);
		GlStateManager.popMatrix();

		GlStateManager.popMatrix();
	}

	// Excised from many occurences in WorldRenderer
	private void drawVboOrList(net.minecraft.client.renderer.vertex.VertexBuffer vbo, int displayList) {
		if (GLX.useVbo()) {
			vbo.bindBuffer();
			GlStateManager.enableClientState(GL11.GL_VERTEX_ARRAY);
			GlStateManager.vertexPointer(3, GL11.GL_FLOAT, 12, 0);
			vbo.drawArrays(GL11.GL_QUADS);
			vbo.unbindBuffer();
			GlStateManager.disableClientState(GL11.GL_VERTEX_ARRAY);
		} else {
			GlStateManager.callList(displayList);
		}
	}
	*/

}
