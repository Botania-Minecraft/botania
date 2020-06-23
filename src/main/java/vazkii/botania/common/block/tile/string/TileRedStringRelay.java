/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.tile.string;

import net.minecraft.block.Block;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.MushroomBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.registries.ObjectHolder;

import vazkii.botania.api.subtile.TileEntitySpecialFlower;
import vazkii.botania.common.block.tile.ModTiles;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.common.lib.LibMisc;

public class TileRedStringRelay extends TileRedString {
	public TileRedStringRelay() {
		super(ModTiles.RED_STRING_RELAY);
	}

	@Override
	public boolean acceptBlock(BlockPos pos) {
		if (pos.equals(getPos().up())) {
			return false;
		}

		Block block = world.getBlockState(pos).getBlock();
		TileEntity tile = world.getTileEntity(pos);
		return (block instanceof FlowerBlock || block instanceof MushroomBlock || block instanceof DoublePlantBlock) && (tile == null || !(tile instanceof TileEntitySpecialFlower));
	}

}
