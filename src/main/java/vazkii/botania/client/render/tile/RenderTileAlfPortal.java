/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.tile;

import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.AlfPortalState;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.common.block.tile.TileAlfPortal;

import javax.annotation.Nonnull;

public class RenderTileAlfPortal extends BlockEntityRenderer<TileAlfPortal> {

	public RenderTileAlfPortal(BlockEntityRenderDispatcher manager) {
		super(manager);
	}

	@Override
	public void render(@Nonnull TileAlfPortal portal, float f, MatrixStack ms, VertexConsumerProvider buffers, int light, int overlay) {
		AlfPortalState state = portal.getCachedState().get(BotaniaStateProps.ALFPORTAL_STATE);
		if (state == AlfPortalState.OFF) {
			return;
		}

		float alpha = (float) Math.min(1F, (Math.sin((ClientTickHandler.ticksInGame + f) / 8D) + 1D) / 7D + 0.6D) * (Math.min(60, portal.ticksOpen) / 60F) * 0.5F;

		ms.push();
		if (state == AlfPortalState.ON_X) {
			ms.translate(0.75, 1, 2);
			ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));
		} else {
			ms.translate(-1, 1, 0.75);
		}
		renderIcon(ms, buffers, MiscellaneousIcons.INSTANCE.alfPortalTex.getSprite(), 0, 0, 3, 3, alpha, overlay);
		ms.pop();

		ms.push();
		if (state == AlfPortalState.ON_X) {
			ms.translate(0.25, 1, -1);
			ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90));
		} else {
			ms.translate(2, 1, 0.25);
		}
		ms.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180));
		renderIcon(ms, buffers, MiscellaneousIcons.INSTANCE.alfPortalTex.getSprite(), 0, 0, 3, 3, alpha, overlay);
		ms.pop();
	}

	public void renderIcon(MatrixStack ms, VertexConsumerProvider buffers, Sprite icon, int x, int y, int width, int height, float alpha, int overlay) {
		VertexConsumer buffer = buffers.getBuffer(TexturedRenderLayers.getItemEntityTranslucentCull());
		Matrix4f model = ms.peek().getModel();
		Matrix3f normal = ms.peek().getNormal();
		buffer.vertex(model, x, y + height, 0).color(1, 1, 1, alpha).texture(icon.getMinU(), icon.getMaxV()).overlay(overlay).light(0xF000F0).normal(normal, 1, 0, 0).next();
		buffer.vertex(model, x + width, y + height, 0).color(1, 1, 1, alpha).texture(icon.getMaxU(), icon.getMaxV()).overlay(overlay).light(0xF000F0).normal(normal, 1, 0, 0).next();
		buffer.vertex(model, x + width, y, 0).color(1, 1, 1, alpha).texture(icon.getMaxU(), icon.getMinV()).overlay(overlay).light(0xF000F0).normal(normal, 1, 0, 0).next();
		buffer.vertex(model, x, y, 0).color(1, 1, 1, alpha).texture(icon.getMinU(), icon.getMinV()).overlay(overlay).light(0xF000F0).normal(normal, 1, 0, 0).next();
	}

}
