/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 19, 2014, 3:42:32 PM (GMT)]
 */
package vazkii.botania.common.block.subtile.functional;

import net.minecraft.block.BlockFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import vazkii.botania.api.item.IExoflameHeatable;
import vazkii.botania.api.subtile.SubTileFunctional;

public class SubTileExoflame extends SubTileFunctional {

	@Override
	public void onUpdate() {
		super.onUpdate();

		int range = 5;
		int rangeY = 2;
		boolean did = false;

		fireFurnaces : {
			for(int i = -range + 1; i < range + 1; i++)
				for(int j = -rangeY; j < rangeY + 1; j++)
					for(int k = -range + 1; k < range + 1; k++) {
						int x = supertile.xCoord + i;
						int y = supertile.yCoord + j;
						int z = supertile.zCoord + k;

						TileEntity tile = supertile.getWorldObj().getTileEntity(x, y, z);
						if(tile != null) {
							if(tile instanceof TileEntityFurnace) {
								TileEntityFurnace furnace = (TileEntityFurnace) tile;
								boolean canSmelt = canFurnaceSmelt(furnace);
								if(canSmelt && mana > 2) {
									if(furnace.furnaceBurnTime < 2) {
										if(furnace.furnaceBurnTime == 0)
											BlockFurnace.updateFurnaceBlockState(true, supertile.getWorldObj(), x, y, z);
										furnace.furnaceBurnTime = 200;
									}
									if(ticksExisted % 2 == 0)
										furnace.furnaceCookTime = Math.min(199, furnace.furnaceCookTime + 1);

									mana -= 2;
									did = true;

									if(mana == 0)
										break fireFurnaces;
								}
							} else if(tile instanceof IExoflameHeatable) {
								IExoflameHeatable heatable = (IExoflameHeatable) tile;

								if(heatable.canSmelt() && mana > 2) {
									if(heatable.getBurnTime() == 0)
										heatable.boostBurnTime();
									if(ticksExisted % 2 == 0)
										heatable.boostCookTime();

									mana -= 2;

									if(mana == 0)
										break fireFurnaces;
								}
							}
						}
					}
		}

		if(did)
			sync();
	}

	public boolean canFurnaceSmelt(TileEntityFurnace furnace){
		if(furnace.getStackInSlot(0) == null)
			return false;
		else {
			ItemStack itemstack = FurnaceRecipes.smelting().getSmeltingResult(furnace.getStackInSlot(0));

			if(itemstack == null)
				return false;

			if(furnace.getStackInSlot(2) == null)
				return true;

			if(!furnace.getStackInSlot(2).isItemEqual(itemstack))
				return false;

			int result = furnace.getStackInSlot(2).stackSize + itemstack.stackSize;
			return result <= 64 && result <= itemstack.getMaxStackSize();
		}
	}


	@Override
	public int getMaxMana() {
		return 300;
	}

	@Override
	public int getColor() {
		return 0x661600;
	}

}
