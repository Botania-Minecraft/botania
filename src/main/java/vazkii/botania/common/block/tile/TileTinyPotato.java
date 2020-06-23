/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.tile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ObjectHolder;

import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.core.helper.PlayerHelper;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.common.lib.LibMisc;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileTinyPotato extends TileSimpleInventory implements ITickableTileEntity, INameable {
	private static final String TAG_NAME = "name";
	private static final int JUMP_EVENT = 0;

	public int jumpTicks = 0;
	public ITextComponent name = new StringTextComponent("");
	private int nextDoIt = 0;

	public TileTinyPotato() {
		super(ModTiles.TINY_POTATO);
	}

	public void interact(PlayerEntity player, Hand hand, ItemStack stack, Direction side) {
		int index = side.getIndex();
		if (index >= 0) {
			ItemStack stackAt = getItemHandler().getStackInSlot(index);
			if (!stackAt.isEmpty() && stack.isEmpty()) {
				player.setHeldItem(hand, stackAt);
				getItemHandler().setStackInSlot(index, ItemStack.EMPTY);
			} else if (!stack.isEmpty()) {
				ItemStack copy = stack.split(1);

				if (stack.isEmpty()) {
					player.setHeldItem(hand, stackAt);
				} else if (!stackAt.isEmpty()) {
					ItemHandlerHelper.giveItemToPlayer(player, stackAt);
				}

				getItemHandler().setStackInSlot(index, copy);
			}
		}

		if (!world.isRemote) {
			jump();

			if (name.getString().toLowerCase().trim().endsWith("shia labeouf") && nextDoIt == 0) {
				nextDoIt = 40;
				world.playSound(null, pos, ModSounds.doit, SoundCategory.BLOCKS, 1F, 1F);
			}

			for (int i = 0; i < getSizeInventory(); i++) {
				ItemStack stackAt = getItemHandler().getStackInSlot(i);
				if (!stackAt.isEmpty() && stackAt.getItem() == ModBlocks.tinyPotato.asItem()) {
					player.sendMessage(new StringTextComponent("Don't talk to me or my son ever again."));
					return;
				}
			}

			PlayerHelper.grantCriterion((ServerPlayerEntity) player, new ResourceLocation(LibMisc.MOD_ID, "main/tiny_potato_pet"), "code_triggered");
		}
	}

	private void jump() {
		if (jumpTicks == 0) {
			world.addBlockEvent(getPos(), getBlockState().getBlock(), JUMP_EVENT, 20);
		}
	}

	@Override
	public boolean receiveClientEvent(int id, int param) {
		if (id == JUMP_EVENT) {
			jumpTicks = param;
			return true;
		} else {
			return super.receiveClientEvent(id, param);
		}
	}

	@Override
	public void tick() {
		if (jumpTicks > 0) {
			jumpTicks--;
		}

		if (!world.isRemote) {
			if (world.rand.nextInt(100) == 0) {
				jump();
			}
			if (nextDoIt > 0) {
				nextDoIt--;
			}
		}
	}

	@Override
	public void markDirty() {
		super.markDirty();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

	@Override
	public void writePacketNBT(CompoundNBT cmp) {
		super.writePacketNBT(cmp);
		cmp.putString(TAG_NAME, ITextComponent.Serializer.toJson(name));
	}

	@Override
	public void readPacketNBT(CompoundNBT cmp) {
		super.readPacketNBT(cmp);
		name = ITextComponent.Serializer.fromJson(cmp.getString(TAG_NAME));
	}

	@Override
	public int getSizeInventory() {
		return 6;
	}

	@Nonnull
	@Override
	public ITextComponent getName() {
		return new TranslationTextComponent(ModBlocks.tinyPotato.getTranslationKey());
	}

	@Nullable
	@Override
	public ITextComponent getCustomName() {
		return name.getString().isEmpty() ? null : name;
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName() {
		return hasCustomName() ? getCustomName() : getName();
	}
}
