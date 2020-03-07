/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [28/09/2016, 17:55:36 (GMT)]
 */
package vazkii.botania.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.common.block.tile.TileAnimatedTorch;

import java.util.Random;

public class RenderTileAnimatedTorch extends TileEntityRenderer<TileAnimatedTorch> {

	public RenderTileAnimatedTorch(TileEntityRendererDispatcher manager) {
		super(manager);
	}

	@Override
	public void render(TileAnimatedTorch te, float partialTicks, MatrixStack ms, IRenderTypeBuffer buffers, int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		ms.push();

		boolean hasWorld = te != null && te.getWorld() != null;
		int wtime = !hasWorld ? 0 : ClientTickHandler.ticksInGame;
		if(wtime != 0)
			wtime += new Random(te.getPos().hashCode()).nextInt(360);

		float time = wtime == 0 ? 0 : wtime + partialTicks;
		float xt = 0.5F + (float) Math.cos(time * 0.05F) * 0.025F;
		float yt = 0.1F + (float) (Math.sin(time * 0.04F) + 1F) * 0.05F;
		float zt = 0.5F + (float) Math.sin(time * 0.05F) * 0.025F;
		ms.translate(xt, yt, zt);

		ms.scale(2, 2, 2);
		ms.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(90));
		float rotation = (float) te.rotation;
		if(te.rotating)
			rotation += te.anglePerTick * partialTicks;

		ms.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(rotation));
		mc.getItemRenderer().renderItem(new ItemStack(Blocks.REDSTONE_TORCH), ItemCameraTransforms.TransformType.GROUND, light, overlay, ms, buffers);
		ms.pop();
	}

}
