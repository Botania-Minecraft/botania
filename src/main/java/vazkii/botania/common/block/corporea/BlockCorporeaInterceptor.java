/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.corporea;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;

import vazkii.botania.common.block.tile.corporea.TileCorporeaBase;
import vazkii.botania.common.block.tile.corporea.TileCorporeaInterceptor;

import javax.annotation.Nonnull;

import java.util.Random;

public class BlockCorporeaInterceptor extends BlockCorporeaBase {

	public BlockCorporeaInterceptor(Block.Properties builder) {
		super(builder);
		setDefaultState(getDefaultState().with(BlockStateProperties.POWERED, false));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(BlockStateProperties.POWERED);
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
		world.setBlockState(pos, state.with(BlockStateProperties.POWERED, false));
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return true;
	}

	@Override
	public int getWeakPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
		return state.get(BlockStateProperties.POWERED) ? 15 : 0;
	}

	@Override
	public int tickRate(IWorldReader world) {
		return 2;
	}

	@Nonnull
	@Override
	public TileCorporeaBase createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
		return new TileCorporeaInterceptor();
	}

}
