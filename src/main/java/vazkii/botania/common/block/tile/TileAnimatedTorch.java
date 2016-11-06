/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [28/09/2016, 17:21:24 (GMT)]
 */
package vazkii.botania.common.block.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.internal.VanillaPacketDispatcher;

public class TileAnimatedTorch extends TileMod {

	public static final String TAG_SIDE = "side";
	public static final String TAG_ROTATING = "rotating";
	public static final String TAG_ROTATION_TICKS = "rotationTicks";
	public static final String TAG_ANGLE_PER_TICK = "anglePerTick";
	public static final String TAG_TORCH_MODE = "torchMode";
	public static final String TAG_NEXT_RANDOM_ROTATION = "nextRandomRotation";

	public static final EnumFacing[] SIDES = new EnumFacing[] {
			EnumFacing.NORTH,
			EnumFacing.EAST,
			EnumFacing.SOUTH,
			EnumFacing.WEST
	};

	public int side;
	public double rotation;
	public boolean rotating;
	public double lastTickRotation;
	public int nextRandomRotation;
	public int currentRandomRotation;

	private int rotationTicks;
	public double anglePerTick;

	private TorchMode torchMode = TorchMode.TOGGLE;

	@Override
	public void onLoad() {
		if(!worldObj.isRemote)
			nextRandomRotation = worldObj.rand.nextInt(4);
	}

	public void handRotate() {
		rotateTo((side + 1) % 4);
	}

	public void toggle() {
		rotateTo(torchMode.modeSwitcher.rotate(this, side));

		if(!worldObj.isRemote) {
			nextRandomRotation = worldObj.rand.nextInt(4);
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		}
	}

	public void onWanded() {
		int modeOrdinal = torchMode.ordinal();
		TorchMode[] modes = TorchMode.class.getEnumConstants();

		modeOrdinal++;
		if(modeOrdinal >= modes.length)
			modeOrdinal = 0;

		torchMode = modes[modeOrdinal];
	}

	public void rotateTo(int side) {
		if(rotating)
			return;

		currentRandomRotation = nextRandomRotation;
		int finalRotation = side * 90;

		double diff = (finalRotation - rotation % 360) % 360;
		if(diff < 0)
			diff = 360 + diff;

		rotationTicks = 4;
		anglePerTick = diff / rotationTicks;
		this.side = side;
		rotating = true;

		worldObj.notifyNeighborsOfStateChange(getPos(), getBlockType());
	}

	@SideOnly(Side.CLIENT)
	public void renderHUD(Minecraft mc, ScaledResolution res) {
		int x = res.getScaledWidth() / 2 + 10;
		int y = res.getScaledHeight() / 2 - 8;

		mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Blocks.REDSTONE_TORCH), x, y);
		mc.fontRendererObj.drawStringWithShadow(I18n.translateToLocal("botania.animatedTorch." + torchMode.name().toLowerCase()), x + 18, y + 6, 0xFF4444);
	}

	@Override
	public void update() {
		if(rotating) {
			lastTickRotation = rotation;
			rotation = (rotation + anglePerTick) % 360;
			rotationTicks--;

			if(rotationTicks <= 0) {
				rotating = false;
				worldObj.notifyNeighborsOfStateChange(getPos(), getBlockType());
			}

		} else rotation = side * 90;

		int amt = rotating ? 3 : Math.random() < 0.1 ? 1 : 0;
		double x = getPos().getX() + 0.5 + Math.cos((rotation + 90) / 180.0 * Math.PI) * 0.35;
		double y = getPos().getY() + 0.2;
		double z = getPos().getZ() + 0.5 + Math.sin((rotation + 90) / 180.0 * Math.PI) * 0.35;

		for(int i = 0; i < amt; i++)
			worldObj.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 0.0D, 0.0D, 0.0D, new int[0]);
	}

	@Override
	public void writePacketNBT(NBTTagCompound cmp) {
		cmp.setInteger(TAG_SIDE, side);
		cmp.setBoolean(TAG_ROTATING, rotating);
		cmp.setInteger(TAG_ROTATION_TICKS, rotationTicks);
		cmp.setDouble(TAG_ANGLE_PER_TICK, anglePerTick);
		cmp.setInteger(TAG_TORCH_MODE, torchMode.ordinal());
		cmp.setInteger(TAG_NEXT_RANDOM_ROTATION, nextRandomRotation);
	}

	@Override
	public void readPacketNBT(NBTTagCompound cmp) {
		side = cmp.getInteger(TAG_SIDE);
		rotating = cmp.getBoolean(TAG_ROTATING);
		if(worldObj != null && !worldObj.isRemote)
			rotationTicks = cmp.getInteger(TAG_ROTATION_TICKS);
		anglePerTick = cmp.getDouble(TAG_ANGLE_PER_TICK);
		nextRandomRotation = cmp.getInteger(TAG_NEXT_RANDOM_ROTATION);

		int modeOrdinal = cmp.getInteger(TAG_TORCH_MODE);
		TorchMode[] modes = TorchMode.class.getEnumConstants();
		torchMode = modes[Math.max(0, Math.min(modes.length - 1, modeOrdinal))];
	}

	public static enum TorchMode {
		TOGGLE((t, i) -> (i + 2) % 4),
		ROTATE((t, i) -> (i + 1) % 4),
		RANDOM((t, i) -> t.currentRandomRotation);

		private TorchMode(RotationHandler modeSwitcher) {
			this.modeSwitcher = modeSwitcher;
		}

		public final RotationHandler modeSwitcher;

		private static interface RotationHandler {
			int rotate(TileAnimatedTorch tile, int curr);
		}
	}
}
