/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Mar 16, 2015, 6:42:40 PM (GMT)]
 */
package vazkii.botania.client.gui.bag;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import vazkii.botania.client.gui.SlotLocked;

public class ContainerFlowerBag extends Container {

	private final InventoryFlowerBag flowerBagInv;

	public ContainerFlowerBag(InventoryPlayer playerInv, InventoryFlowerBag flowerBagInv) {
		int i;
		int j;

		this.flowerBagInv = flowerBagInv;

		for(i = 0; i < 2; ++i)
			for(j = 0; j < 8; ++j) {
				int k = j + i * 8;
				addSlotToContainer(new SlotItemHandler(flowerBagInv, k, 17 + j * 18, 26 + i * 18));
			}

		for(i = 0; i < 3; ++i)
			for(j = 0; j < 9; ++j)
				addSlotToContainer(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));

		for(i = 0; i < 9; ++i) {
			if(playerInv.getStackInSlot(i) == flowerBagInv.bag)
				addSlotToContainer(new SlotLocked(playerInv, i, 8 + i * 18, 142));
			else addSlotToContainer(new Slot(playerInv, i, 8 + i * 18, 142));
		}

	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer player) {
		return player.getHeldItemMainhand() == flowerBagInv.bag
				|| player.getHeldItemOffhand() == flowerBagInv.bag;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
		ItemStack itemstack = null;
		Slot slot = inventorySlots.get(slotIndex);

		if(slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if(slotIndex < 16) {
				if(!mergeItemStack(itemstack1, 16, 52, true))
					return null;
			} else {
				int i = itemstack.getItemDamage();
				if(i < 16) {
					Slot slot1 = inventorySlots.get(i);
					if(slot1.isItemValid(itemstack) && !mergeItemStack(itemstack1, i, i + 1, true))
						return null;
				}
			}

			if(itemstack1.stackSize == 0)
				slot.putStack(null);
			else slot.onSlotChanged();

			if(itemstack1.stackSize == itemstack.stackSize)
				return null;

			slot.onPickupFromSlot(player, itemstack1);
		}

		return itemstack;
	}

}
