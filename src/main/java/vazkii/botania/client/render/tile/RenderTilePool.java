/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 26, 2014, 12:25:11 AM (GMT)]
 */
package vazkii.botania.client.render.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.mana.IPoolOverlayProvider;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.helper.ShaderHelper;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.mana.BlockPool;
import vazkii.botania.common.block.tile.mana.TilePool;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Random;

public class RenderTilePool extends TileEntityRenderer<TilePool> {

	// Overrides for when we call this TESR from a cart
	public static int cartMana = -1;

	@Override
	public void render(@Nullable TilePool pool, double d0, double d1, double d2, float f, int digProgress) {
		if(pool != null && (!pool.getWorld().isBlockLoaded(pool.getPos())
				|| !(pool.getBlockState().getBlock() instanceof BlockPool)))
			return;

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.enableRescaleNormal();

		GlStateManager.color4f(1F, 1F, 1F, 1F);
		if (pool == null) { // A null pool means we are calling the TESR without a pool (on a minecart). Adjust accordingly
			GlStateManager.translatef(0, 0, -1);
		} else {
			GlStateManager.translated(d0, d1, d2);
		}

		boolean fab = pool != null && ((BlockPool) pool.getBlockState().getBlock()).variant == BlockPool.Variant.FABULOUS;

		Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

		if (fab) {
			float time = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
			time += new Random(pool.getPos().getX() ^ pool.getPos().getY() ^ pool.getPos().getZ()).nextInt(100000);
			time *= 0.005F;
			int color = MathHelper.multiplyColor(MathHelper.hsvToRGB(time - (int) time, 0.6F, 1F), pool.color.colorValue);

			int red = (color & 0xFF0000) >> 16;
			int green = (color & 0xFF00) >> 8;
			int blue = color & 0xFF;
			IBakedModel model = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getModel(pool.getWorld().getBlockState(pool.getPos()));
			Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(model, 1.0F, red / 255F, green / 255F, blue / 255F);
		}

		GlStateManager.translatef(0.5F, 1.5F, 0.5F);
		GlStateManager.color4f(1, 1, 1, 1);
		GlStateManager.enableRescaleNormal();

		int mana = pool == null ? cartMana : pool.getCurrentMana();
		int cap = pool == null ? -1 : pool.manaCap;
		if(cap == -1)
			cap = TilePool.MAX_MANA;

		float waterLevel = (float) mana / (float) cap * 0.4F;

		float s = 1F / 16F;
		float v = 1F / 8F;
		float w = -v * 3.5F;

		if(pool != null) {
			Block below = pool.getWorld().getBlockState(pool.getPos().down()).getBlock();
			if(below instanceof IPoolOverlayProvider) {
				TextureAtlasSprite overlay = ((IPoolOverlayProvider) below).getIcon(pool.getWorld(), pool.getPos());
				if(overlay != null) {
					GlStateManager.pushMatrix();
					GlStateManager.enableBlend();
					GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					GlStateManager.disableAlphaTest();
					GlStateManager.color4f(1F, 1F, 1F, 1 * (float) ((Math.sin((ClientTickHandler.ticksInGame + f) / 20.0) + 1) * 0.3 + 0.2));
					GlStateManager.translatef(-0.5F, -1F - 0.43F, -0.5F);
					GlStateManager.rotatef(90F, 1F, 0F, 0F);
					GlStateManager.scalef(s, s, s);

					renderIcon(0, 0, overlay, 16, 16, 240);

					GlStateManager.enableAlphaTest();
					GlStateManager.disableBlend();
					GlStateManager.popMatrix();
				}
			}
		}

		if(waterLevel > 0) {
			s = 1F / 256F * 14F;
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			GlStateManager.disableAlphaTest();
			GlStateManager.color4f(1F, 1F, 1F, 1F);
			GlStateManager.translatef(w, -1F - (0.43F - waterLevel), w);
			GlStateManager.rotatef(90F, 1F, 0F, 0F);
			GlStateManager.scalef(s, s, s);

			ShaderHelper.useShader(ShaderHelper.manaPool);
			renderIcon(0, 0, MiscellaneousIcons.INSTANCE.manaWater, 16, 16, 240);
			ShaderHelper.releaseShader();

			GlStateManager.enableAlphaTest();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		GlStateManager.popMatrix();

		cartMana = -1;
	}

	private void renderIcon(int par1, int par2, TextureAtlasSprite par3Icon, int par4, int par5, int brightness) {
		Tessellator tessellator = Tessellator.getInstance();
		tessellator.getBuffer().begin(GL11.GL_QUADS, ClientProxy.POSITION_TEX_LMAP);
		tessellator.getBuffer().pos(par1 + 0, par2 + par5, 0).tex(par3Icon.getMinU(), par3Icon.getMaxV()).lightmap(brightness, brightness).endVertex();
		tessellator.getBuffer().pos(par1 + par4, par2 + par5, 0).tex(par3Icon.getMaxU(), par3Icon.getMaxV()).lightmap(brightness, brightness).endVertex();
		tessellator.getBuffer().pos(par1 + par4, par2 + 0, 0).tex(par3Icon.getMaxU(), par3Icon.getMinV()).lightmap(brightness, brightness).endVertex();
		tessellator.getBuffer().pos(par1 + 0, par2 + 0, 0).tex(par3Icon.getMinU(), par3Icon.getMinV()).lightmap(brightness, brightness).endVertex();
		tessellator.draw();
	}

	private BlockState poolForVariant(BlockPool.Variant v) {
		switch (v) {
			default:
			case DEFAULT: return ModBlocks.manaPool.getDefaultState();
			case CREATIVE: return ModBlocks.creativePool.getDefaultState();
			case DILUTED: return ModBlocks.dilutedPool.getDefaultState();
			case FABULOUS: return ModBlocks.fabulousPool.getDefaultState();
		}
	}

}
