/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.mana;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.wand.IWandHUD;
import vazkii.botania.api.wand.IWandable;
import vazkii.botania.common.block.BlockMod;
import vazkii.botania.common.block.tile.TileEnchanter;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nonnull;

public class BlockEnchanter extends BlockMod implements IWandable, IWandHUD {

	public BlockEnchanter(Properties builder) {
		super(builder);
		setDefaultState(getDefaultState().with(BotaniaStateProps.ENCHANTER_DIRECTION, Direction.Axis.X));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(BotaniaStateProps.ENCHANTER_DIRECTION);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Nonnull
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
		return new TileEnchanter();
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		TileEnchanter enchanter = (TileEnchanter) world.getTileEntity(pos);
		ItemStack stack = player.getHeldItem(hand);
		if (!stack.isEmpty() && stack.getItem() == ModItems.twigWand) {
			return ActionResultType.PASS;
		}

		boolean stackEnchantable = !stack.isEmpty()
				&& stack.getItem() != Items.BOOK
				&& stack.isEnchantable()
				&& stack.getCount() == 1;

		if (enchanter.itemToEnchant.isEmpty()) {
			if (stackEnchantable) {
				enchanter.itemToEnchant = stack.copy();
				player.setHeldItem(hand, ItemStack.EMPTY);
				enchanter.sync();
			} else {
				return ActionResultType.PASS;
			}
		} else if (enchanter.stage == TileEnchanter.State.IDLE) {
			ItemHandlerHelper.giveItemToPlayer(player, enchanter.itemToEnchant.copy());
			enchanter.itemToEnchant = ItemStack.EMPTY;
			enchanter.sync();
		}

		return ActionResultType.SUCCESS;
	}

	@Override
	public void onReplaced(@Nonnull BlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEnchanter enchanter = (TileEnchanter) world.getTileEntity(pos);

			if (!enchanter.itemToEnchant.isEmpty()) {
				world.addEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), enchanter.itemToEnchant));
			}

			world.updateComparatorOutputLevel(pos, state.getBlock());
			super.onReplaced(state, world, pos, newState, isMoving);
		}
	}

	@Override
	public boolean onUsedByWand(PlayerEntity player, ItemStack stack, World world, BlockPos pos, Direction side) {
		((TileEnchanter) world.getTileEntity(pos)).onWanded(player, stack);
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void renderHUD(Minecraft mc, World world, BlockPos pos) {
		((TileEnchanter) world.getTileEntity(pos)).renderHUD();
	}
}
