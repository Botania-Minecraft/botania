/**
 * This class was created by <Azanor>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [? (GMT)]
 */
package vazkii.botania.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.core.helper.ObfuscationHelper;
import cpw.mods.fml.client.FMLClientHandler;

public class FXWisp extends EntityFX {

	public static final ResourceLocation particles = new ResourceLocation(
			LibResources.MISC_WISP_LARGE);

	public FXWisp(World world, double d, double d1, double d2, float size,
			float red, float green, float blue, boolean distanceLimit,
			float maxAgeMul) {
		super(world, d, d1, d2, 0.0D, 0.0D, 0.0D);
		particleRed = red;
		particleGreen = green;
		particleBlue = blue;
		particleGravity = 0;
		motionX = motionY = motionZ = 0;
		particleScale *= size;
		moteParticleScale = particleScale;
		particleMaxAge = (int) (28D / (Math.random() * 0.3D + 0.7D) * maxAgeMul);

		moteHalfLife = particleMaxAge / 2;
		noClip = true;
		setSize(0.01F, 0.01F);
		EntityLivingBase renderentity = FMLClientHandler.instance().getClient().renderViewEntity;

		if (distanceLimit) {
			int visibleDistance = 50;
			if (!FMLClientHandler.instance().getClient().gameSettings.fancyGraphics)
				visibleDistance = 25;

			if (renderentity == null
					|| renderentity.getDistance(posX, posY, posZ) > visibleDistance)
				particleMaxAge = 0;
		}

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;
	}

	@Override
	public void renderParticle(Tessellator tessellator, float f, float f1,
			float f2, float f3, float f4, float f5) {
		if (Minecraft.getMinecraft().gameSettings.particleSetting == 2)
			return;
		float agescale = 0;
		agescale = (float) particleAge / (float) moteHalfLife;
		if (agescale > 1F)
			agescale = 2 - agescale;

		particleScale = moteParticleScale * agescale;

		tessellator.draw();
		GL11.glPushMatrix();

		GL11.glDepthMask(false);
		GL11.glEnable(3042);
		GL11.glBlendFunc(770, blendmode);

		Minecraft.getMinecraft().renderEngine.bindTexture(particles);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.75F);

		float f10 = 0.5F * particleScale;
		float f11 = (float) (prevPosX + (posX - prevPosX) * f - interpPosX);
		float f12 = (float) (prevPosY + (posY - prevPosY) * f - interpPosY);
		float f13 = (float) (prevPosZ + (posZ - prevPosZ) * f - interpPosZ);

		tessellator.startDrawingQuads();
		tessellator.setBrightness(240);
		tessellator.setColorRGBA_F(particleRed, particleGreen, particleBlue,
				0.5F);
		tessellator.addVertexWithUV(f11 - f1 * f10 - f4 * f10, f12 - f2 * f10,
				f13 - f3 * f10 - f5 * f10, 0, 1);
		tessellator.addVertexWithUV(f11 - f1 * f10 + f4 * f10, f12 + f2 * f10,
				f13 - f3 * f10 + f5 * f10, 1, 1);
		tessellator.addVertexWithUV(f11 + f1 * f10 + f4 * f10, f12 + f2 * f10,
				f13 + f3 * f10 + f5 * f10, 1, 0);
		tessellator.addVertexWithUV(f11 + f1 * f10 - f4 * f10, f12 - f2 * f10,
				f13 + f3 * f10 - f5 * f10, 0, 0);

		tessellator.draw();

		GL11.glDisable(3042);
		GL11.glDepthMask(true);

		GL11.glPopMatrix();
		Minecraft.getMinecraft().renderEngine.bindTexture(ObfuscationHelper
				.getParticleTexture());
		tessellator.startDrawingQuads();
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (particleAge++ >= particleMaxAge)
			setDead();

		motionY -= 0.04D * particleGravity;
		posX += motionX;
		posY += motionY;
		posZ += motionZ;
		motionX *= 0.98000001907348633D;
		motionY *= 0.98000001907348633D;
		motionZ *= 0.98000001907348633D;
	}

	public void setGravity(float value) {
		particleGravity = value;
	}

	public boolean distanceLimit = true;
	float moteParticleScale;
	int moteHalfLife;
	public boolean tinkle = false;
	public int blendmode = 1;
}
