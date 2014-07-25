/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Jan 26, 2014, 12:25:11 AM (GMT)]
 */
package vazkii.botania.client.render.tile;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import vazkii.botania.api.mana.IPoolOverlayProvider;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.ShaderHelper;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.client.model.ModelPool;
import vazkii.botania.common.block.tile.mana.TilePool;

public class RenderTilePool extends TileEntitySpecialRenderer {

	private static final ResourceLocation texture = new ResourceLocation(LibResources.MODEL_POOL);
	private static final ResourceLocation textureInf = new ResourceLocation(LibResources.MODEL_INFINITE_POOL);

	private static final ModelPool model = new ModelPool();
	RenderItem renderItem = new RenderItem();

	public static boolean forceAllMana = false;

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f) {
		TilePool pool = (TilePool) tileentity;

		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		GL11.glTranslated(d0, d1, d2);
		boolean inf = tileentity.getWorldObj() == null ? forceAllMana : tileentity.getBlockMetadata() == 1;

		Minecraft.getMinecraft().renderEngine.bindTexture(inf ? textureInf : texture);

		GL11.glTranslatef(0.5F, 1.5F, 0.5F);
		GL11.glScalef(1F, -1F, -1F);
		int color = pool.color;
		float[] acolor = EntitySheep.fleeceColorTable[color];
		GL11.glColor3f(acolor[0], acolor[1], acolor[2]);
		model.render();
		GL11.glColor3f(1F, 1F, 1F);
		GL11.glScalef(1F, -1F, -1F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);

		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

		float waterLevel = (float) pool.getCurrentMana() / (float) TilePool.MAX_MANA * 0.4F;
		if(forceAllMana) {
			waterLevel = 0.4F;
			forceAllMana = false;
		}

		float s = 1F / 16F;
		float v = 1F / 8F;
		float w = -v * 3.5F;

		if(pool.getWorldObj() != null) {
			Block below = pool.getWorldObj().getBlock(pool.xCoord, pool.yCoord - 1, pool.zCoord);
			if(below instanceof IPoolOverlayProvider) {
				IIcon overlay = ((IPoolOverlayProvider) below).getIcon(pool.getWorldObj(), pool.xCoord, pool.yCoord - 1, pool.zCoord);
				if(overlay != null) {
					GL11.glPushMatrix();
					GL11.glEnable(GL11.GL_BLEND);
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					GL11.glDisable(GL11.GL_ALPHA_TEST);
					GL11.glColor4f(1F, 1F, 1F, (float) ((Math.sin(ClientTickHandler.ticksInGame / 20.0) + 1) * 0.3 + 0.2));
					GL11.glTranslatef(-0.5F, -1F - 0.43F, -0.5F);
					GL11.glRotatef(90F, 1F, 0F, 0F);
					GL11.glScalef(s, s, s);

					renderIcon(0, 0, overlay, 16, 16, 240);

					GL11.glEnable(GL11.GL_ALPHA_TEST);
					GL11.glDisable(GL11.GL_BLEND);
					GL11.glPopMatrix();
				}
			}
		}

		if(waterLevel > 0) {
			s = 1F / 256F * 14F;
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			GL11.glColor4f(1F, 1F, 1F, 1F);
			GL11.glTranslatef(w, -1F - (0.43F - waterLevel), w);
			GL11.glRotatef(90F, 1F, 0F, 0F);
			GL11.glScalef(s, s, s);

			IIcon waterIcon = Blocks.water.getIcon(0, 0);

			ShaderHelper.useShader(ShaderHelper.manaPool);
			renderIcon(0, 0, waterIcon, 16, 16, 240);
			ShaderHelper.releaseShader();

			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();
	}

	public void renderIcon(int par1, int par2, IIcon par3Icon, int par4, int par5, int brightness) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setBrightness(brightness);
		tessellator.addVertexWithUV(par1 + 0, par2 + par5, 0, par3Icon.getMinU(), par3Icon.getMaxV());
		tessellator.addVertexWithUV(par1 + par4, par2 + par5, 0, par3Icon.getMaxU(), par3Icon.getMaxV());
		tessellator.addVertexWithUV(par1 + par4, par2 + 0, 0, par3Icon.getMaxU(), par3Icon.getMinV());
		tessellator.addVertexWithUV(par1 + 0, par2 + 0, 0, par3Icon.getMinU(), par3Icon.getMinV());
		tessellator.draw();
	}

}
