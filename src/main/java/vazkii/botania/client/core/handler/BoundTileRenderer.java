/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Mar 24, 2014, 7:02:37 PM (GMT)]
 */
package vazkii.botania.client.core.handler;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.IWireframeCoordinateListProvider;
import vazkii.botania.api.wand.ICoordBoundItem;
import vazkii.botania.api.wand.IWireframeAABBProvider;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.lib.LibMisc;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = LibMisc.MOD_ID, value = Dist.CLIENT)
public final class BoundTileRenderer {
	private static final IRenderTypeBuffer.Impl LINE_BUFFERS = IRenderTypeBuffer.immediate(Util.make(() -> {
		Map<RenderType, BufferBuilder> ret = new IdentityHashMap<>();
		ret.put(RenderHelper.LINE_1_NO_DEPTH, new BufferBuilder(RenderHelper.LINE_1_NO_DEPTH.getExpectedBufferSize()));
		ret.put(RenderHelper.LINE_4_NO_DEPTH, new BufferBuilder(RenderHelper.LINE_4_NO_DEPTH.getExpectedBufferSize()));
		ret.put(RenderHelper.LINE_5_NO_DEPTH, new BufferBuilder(RenderHelper.LINE_5_NO_DEPTH.getExpectedBufferSize()));
		ret.put(RenderHelper.LINE_8_NO_DEPTH, new BufferBuilder(RenderHelper.LINE_8_NO_DEPTH.getExpectedBufferSize()));
		return ret;
	}), Tessellator.getInstance().getBuffer());
	private BoundTileRenderer() {}

	@SubscribeEvent
	public static void onWorldRenderLast(RenderWorldLastEvent event) {
		if (!ConfigHandler.CLIENT.boundBlockWireframe.get())
			return;

		MatrixStack ms = event.getMatrixStack();
		ms.push();

		PlayerEntity player = Minecraft.getInstance().player;
		int color = 0xFF000000 | MathHelper.hsvToRGB(ClientTickHandler.ticksInGame % 200 / 200F, 0.6F, 1F);

		if(!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ICoordBoundItem) {
			BlockPos coords = ((ICoordBoundItem) player.getHeldItemMainhand().getItem()).getBinding(player.getHeldItemMainhand());
			if(coords != null)
				renderBlockOutlineAt(ms, LINE_BUFFERS, coords, color);
		}

		if(!player.getHeldItemOffhand().isEmpty() && player.getHeldItemOffhand().getItem() instanceof ICoordBoundItem) {
			BlockPos coords = ((ICoordBoundItem) player.getHeldItemOffhand().getItem()).getBinding(player.getHeldItemOffhand());
			if(coords != null)
				renderBlockOutlineAt(ms, LINE_BUFFERS, coords, color);
		}

		LazyOptional<IItemHandler> mainInvCap = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
		mainInvCap.ifPresent(mainInv -> {
			IItemHandlerModifiable acc = BotaniaAPI.internalHandler.getAccessoriesInventory(player);
			IItemHandler joined = acc != null ? new CombinedInvWrapper((IItemHandlerModifiable) mainInv, acc) : mainInv;

			for (int i = 0; i < joined.getSlots(); i++) {
				ItemStack stackInSlot = joined.getStackInSlot(i);

				if (!stackInSlot.isEmpty() && stackInSlot.getItem() instanceof IWireframeCoordinateListProvider) {
					IWireframeCoordinateListProvider provider = (IWireframeCoordinateListProvider) stackInSlot.getItem();
					List<BlockPos> coordsList = provider.getWireframesToDraw(player, stackInSlot);
					for (BlockPos coords : coordsList)
						renderBlockOutlineAt(ms, LINE_BUFFERS, coords, color);

					BlockPos coords = provider.getSourceWireframe(player, stackInSlot);
					if (coords != null && coords.getY() > -1)
						renderBlockOutlineAt(ms, LINE_BUFFERS, coords, color, true);
				}
			}
		});

		ms.pop();
		RenderSystem.disableDepthTest();
		LINE_BUFFERS.draw();
	}

	private static void renderBlockOutlineAt(MatrixStack ms, IRenderTypeBuffer buffers, BlockPos pos, int color) {
		renderBlockOutlineAt(ms, buffers, pos, color, false);
	}

	private static void renderBlockOutlineAt(MatrixStack ms, IRenderTypeBuffer buffers, BlockPos pos, int color, boolean thick) {
		double renderPosX = Minecraft.getInstance().getRenderManager().info.getProjectedView().getX();
		double renderPosY = Minecraft.getInstance().getRenderManager().info.getProjectedView().getY();
		double renderPosZ = Minecraft.getInstance().getRenderManager().info.getProjectedView().getZ();

		ms.push();
		ms.translate(pos.getX() - renderPosX, pos.getY() - renderPosY, pos.getZ() - renderPosZ + 1);

		World world = Minecraft.getInstance().world;
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		List<AxisAlignedBB> list;

		if(block instanceof IWireframeAABBProvider)
			list = ((IWireframeAABBProvider) block).getWireframeAABB(world, pos);
		else{
			VoxelShape shape = state.getShape(world, pos);
			list = shape.toBoundingBoxList().stream().map(b -> b.offset(pos)).collect(Collectors.toList());
		}

		if (!list.isEmpty()) {
			ms.scale(1F, 1F, 1F);

			IVertexBuilder buffer = buffers.getBuffer(thick ? RenderHelper.LINE_5_NO_DEPTH : RenderHelper.LINE_1_NO_DEPTH);
			for (AxisAlignedBB axis : list) {
				axis = axis.offset(-pos.getX(), -pos.getY(), -(pos.getZ() + 1));
				renderBlockOutline(ms.peek().getModel(), buffer, axis, color);
			}

			buffer = buffers.getBuffer(thick ? RenderHelper.LINE_8_NO_DEPTH : RenderHelper.LINE_4_NO_DEPTH);
			int alpha = 64;
			color = (color & ~0xff000000) | (alpha << 24);
			for (AxisAlignedBB axis : list) {
				axis = axis.offset(-pos.getX(), -pos.getY(), -(pos.getZ() + 1));
				renderBlockOutline(ms.peek().getModel(), buffer, axis, color);
			}
		}

		ms.pop();
	}

	private static void renderBlockOutline(Matrix4f mat, IVertexBuilder buffer, AxisAlignedBB aabb, int color) {
		float ix = (float) aabb.minX;
		float iy = (float) aabb.minY;
		float iz = (float) aabb.minZ;
		float ax = (float) aabb.maxX;
		float ay = (float) aabb.maxY;
		float az = (float) aabb.maxZ;
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;

		buffer.vertex(mat, ix, iy, iz).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ix, ay, iz).color(r, g, b, a).endVertex();

		buffer.vertex(mat, ix, ay, iz).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ax, ay, iz).color(r, g, b, a).endVertex();

		buffer.vertex(mat, ax, ay, iz).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ax, iy, iz).color(r, g, b, a).endVertex();

		buffer.vertex(mat, ax, iy, iz).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ix, iy, iz).color(r, g, b, a).endVertex();

		buffer.vertex(mat, ix, iy, az).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ix, ay, az).color(r, g, b, a).endVertex();

		buffer.vertex(mat, ix, iy, az).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ax, iy, az).color(r, g, b, a).endVertex();

		buffer.vertex(mat, ax, iy, az).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ax, ay, az).color(r, g, b, a).endVertex();

		buffer.vertex(mat, ix, ay, az).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ax, ay, az).color(r, g, b, a).endVertex();

		buffer.vertex(mat, ix, iy, iz).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ix, iy, az).color(r, g, b, a).endVertex();

		buffer.vertex(mat, ix, ay, iz).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ix, ay, az).color(r, g, b, a).endVertex();

		buffer.vertex(mat, ax, iy, iz).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ax, iy, az).color(r, g, b, a).endVertex();

		buffer.vertex(mat, ax, ay, iz).color(r, g, b, a).endVertex();
		buffer.vertex(mat, ax, ay, az).color(r, g, b, a).endVertex();
	}
}
