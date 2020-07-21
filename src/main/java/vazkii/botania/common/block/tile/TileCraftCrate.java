/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.tile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;

import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.state.BotaniaStateProps;
import vazkii.botania.api.state.enums.CratePattern;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Optional;

public class TileCraftCrate extends TileOpenCrate {
	private static final String TAG_CRAFTING_RESULT = "craft_result";
	private int signal = 0;
	private ItemStack craftResult = ItemStack.EMPTY;

	public TileCraftCrate() {
		super(ModTiles.CRAFT_CRATE);
	}

	@Override
	protected Inventory createItemHandler() {
		return new Inventory(9) {
			@Override
			public int getInventoryStackLimit() {
				return 1;
			}

			@Override
			public boolean isItemValidForSlot(int slot, ItemStack stack) {
				return !isLocked(slot);
			}
		};
	}

	public CratePattern getPattern() {
		BlockState state = getBlockState();
		if (state.getBlock() != ModBlocks.craftCrate) {
			return CratePattern.NONE;
		}
		return state.get(BotaniaStateProps.CRATE_PATTERN);
	}

	private boolean isLocked(int slot) {
		return !getPattern().openSlots.get(slot);
	}

	@Override
	public void readPacketNBT(CompoundNBT tag) {
		super.readPacketNBT(tag);
		craftResult = ItemStack.read(tag.getCompound(TAG_CRAFTING_RESULT));
	}

	@Override
	public void writePacketNBT(CompoundNBT tag) {
		super.writePacketNBT(tag);
		tag.put(TAG_CRAFTING_RESULT, craftResult.write(new CompoundNBT()));
	}

	@Override
	public void tick() {
		if (world.isRemote) {
			return;
		}

		if (canEject() && isFull() && craft(true)) {
			ejectAll();
		}

		int newSignal = 0;
		for (; newSignal < 9; newSignal++) // dis for loop be derpy
		{
			if (!isLocked(newSignal) && getItemHandler().getStackInSlot(newSignal).isEmpty()) {
				break;
			}
		}

		if (newSignal != signal) {
			signal = newSignal;
			world.updateComparatorOutputLevel(pos, getBlockState().getBlock());
		}
	}

	private boolean craft(boolean fullCheck) {
		if (fullCheck && !isFull()) {
			return false;
		}

		CraftingInventory craft = new CraftingInventory(new Container(ContainerType.CRAFTING, -1) {
			@Override
			public boolean canInteractWith(@Nonnull PlayerEntity player) {
				return false;
			}
		}, 3, 3);
		for (int i = 0; i < craft.getSizeInventory(); i++) {
			ItemStack stack = getItemHandler().getStackInSlot(i);

			if (stack.isEmpty() || isLocked(i) || stack.getItem() == ModItems.placeholder) {
				continue;
			}

			craft.setInventorySlotContents(i, stack);
		}

		Optional<ICraftingRecipe> matchingRecipe = world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, craft, world);
		matchingRecipe.ifPresent(recipe -> {
			craftResult = recipe.getCraftingResult(craft);

			List<ItemStack> remainders = recipe.getRemainingItems(craft);
			for (int i = 0; i < craft.getSizeInventory(); i++) {
				ItemStack s = remainders.get(i);
				if (!getItemHandler().getStackInSlot(i).isEmpty()
						&& getItemHandler().getStackInSlot(i).getItem() == ModItems.placeholder) {
					continue;
				}
				getItemHandler().setInventorySlotContents(i, s);
			}
		});

		return matchingRecipe.isPresent();
	}

	boolean isFull() {
		for (int i = 0; i < getItemHandler().getSizeInventory(); i++) {
			if (!isLocked(i) && getItemHandler().getStackInSlot(i).isEmpty()) {
				return false;
			}
		}

		return true;
	}

	private void ejectAll() {
		for (int i = 0; i < inventorySize(); ++i) {
			ItemStack stack = getItemHandler().getStackInSlot(i);
			if (!stack.isEmpty()) {
				eject(stack, false);
			}
			getItemHandler().setInventorySlotContents(i, ItemStack.EMPTY);
		}
		if (!craftResult.isEmpty()) {
			eject(craftResult, false);
			craftResult = ItemStack.EMPTY;
		}
	}

	@Override
	public boolean onWanded(World world, PlayerEntity player, ItemStack stack) {
		if (!world.isRemote && canEject()) {
			craft(false);
			ejectAll();
		}
		return true;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (world != null && !world.isRemote) {
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		}
	}

	@Override
	public int getSignal() {
		return signal;
	}

}
