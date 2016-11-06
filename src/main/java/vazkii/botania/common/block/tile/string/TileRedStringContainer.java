/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Nov 14, 2014, 5:26:39 PM (GMT)]
 */
package vazkii.botania.common.block.tile.string;

import java.util.Arrays;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileRedStringContainer extends TileRedString {

	@Override
	public boolean acceptBlock(BlockPos pos) {
		TileEntity tile = worldObj.getTileEntity(pos);
		return tile != null
				&& Arrays.stream(EnumFacing.VALUES)
				.anyMatch(e -> tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, e));
	}

	@Override
	public boolean hasCapability(@Nonnull Capability<?> cap, @Nonnull EnumFacing side) {
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				&& getTileAtBinding() != null
				&& getTileAtBinding().hasCapability(cap, side))
			return true;
		return super.hasCapability(cap, side);
	}

	@Nonnull
	@Override
	public <T> T getCapability(@Nonnull Capability<T> cap, @Nonnull EnumFacing side) {
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				&& getTileAtBinding() != null
				&& getTileAtBinding().hasCapability(cap, side))
			return getTileAtBinding().getCapability(cap, side);
		return super.getCapability(cap, side);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		TileEntity tile = getTileAtBinding();
		if(tile != null)
			tile.markDirty();
	}

}
