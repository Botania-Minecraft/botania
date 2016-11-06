/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 24, 2015, 4:46:03 PM (GMT)]
 */
package vazkii.botania.common.item.lens;

import net.minecraft.block.Block;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.TileIncensePlate;

public class LensFire extends Lens {

	@Override
	public boolean collideBurst(IManaBurst burst, EntityThrowable entity, RayTraceResult rtr, boolean isManaBlock, boolean dead, ItemStack stack) {
		BlockPos coords = burst.getBurstSourceBlockPos();
		BlockPos pos = rtr.getBlockPos();
		if(!entity.worldObj.isRemote && pos != null && !coords.equals(pos) && !burst.isFake() && !isManaBlock) {
			EnumFacing dir = rtr.sideHit;

			BlockPos offPos = pos.offset(dir);

			Block blockAt = entity.worldObj.getBlockState(pos).getBlock();
			Block blockAtOffset = entity.worldObj.getBlockState(offPos).getBlock();

			if(blockAt == Blocks.PORTAL)
				entity.worldObj.setBlockState(pos, Blocks.AIR.getDefaultState());
			if(blockAtOffset == Blocks.PORTAL)
				entity.worldObj.setBlockState(offPos, Blocks.AIR.getDefaultState());
			else if(blockAt == ModBlocks.incensePlate) {
				TileIncensePlate plate = (TileIncensePlate) entity.worldObj.getTileEntity(pos);
				plate.ignite();
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(plate);
			} else if(blockAtOffset.isAir(entity.worldObj.getBlockState(offPos), entity.worldObj, offPos))
				entity.worldObj.setBlockState(offPos, Blocks.FIRE.getDefaultState());
		}

		return dead;
	}

}
