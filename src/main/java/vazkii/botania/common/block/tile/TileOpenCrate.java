/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.tile;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;

import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.common.lib.LibMisc;

public class TileOpenCrate extends TileSimpleInventory implements ITickableTileEntity {
	public TileOpenCrate() {
		this(ModTiles.OPEN_CRATE);
	}

	public TileOpenCrate(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public void tick() {
		if (world.isRemote) {
			return;
		}

		boolean redstone = false;
		for (Direction dir : Direction.values()) {
			int redstoneSide = world.getRedstonePower(pos.offset(dir), dir);
			if (redstoneSide > 0) {
				redstone = true;
				break;
			}
		}

		if (canEject()) {
			ItemStack stack = itemHandler.getStackInSlot(0);
			if (!stack.isEmpty()) {
				eject(stack, redstone);
			}
		}
	}

	public boolean canEject() {
		double ejectX = pos.getX() + 0.5;
		double ejectY = pos.getY() - 0.5;
		double ejectZ = pos.getZ() + 0.5;
		float width = EntityType.ITEM.getWidth();
		float height = EntityType.ITEM.getHeight();
		AxisAlignedBB itemBB = new AxisAlignedBB(ejectX - width / 2, ejectY, ejectZ - width / 2, ejectX + width / 2, ejectY + height, ejectZ + width / 2);
		return world.hasNoCollisions(itemBB);
	}

	public void eject(ItemStack stack, boolean redstone) {
		ItemEntity item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() - 0.5, pos.getZ() + 0.5, stack);
		item.setMotion(Vec3d.ZERO);
		if (redstone) {
			item.age = -200;
		}

		itemHandler.setStackInSlot(0, ItemStack.EMPTY);
		world.addEntity(item);
	}

	public boolean onWanded(World world, PlayerEntity player, ItemStack stack) {
		return false;
	}

	public int getSignal() {
		return 0;
	}
}
