/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.tile;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.common.lib.LibMisc;

public class TileManaFlame extends TileMod implements ITickableTileEntity {
	private static final String TAG_COLOR = "color";

	private int color = 0x20FF20;
	private int lightColor = -1;

	public TileManaFlame() {
		super(ModTiles.MANA_FLAME);
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	@Override
	public void tick() {
		float c = 0.3F;

		if (world.isRemote && Math.random() < c) {
			float v = 0.1F;

			float r = (float) (color >> 16 & 0xFF) / 0xFF;
			float g = (float) (color >> 8 & 0xFF) / 0xFF;
			float b = (float) (color & 0xFF) / 0xFF;

			double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b; // Standard relative luminance calculation

			if (luminance < v) {
				r += (float) Math.random() * 0.125F;
				g += (float) Math.random() * 0.125F;
				b += (float) Math.random() * 0.125F;
			}

			float w = 0.15F;
			float h = 0.05F;
			double x = pos.getX() + 0.5 + (Math.random() - 0.5) * w;
			double y = pos.getY() + 0.25 + (Math.random() - 0.5) * h;
			double z = pos.getZ() + 0.5 + (Math.random() - 0.5) * w;

			float s = 0.2F + (float) Math.random() * 0.1F;
			float m = 0.03F + (float) Math.random() * 0.015F;

			WispParticleData data = WispParticleData.wisp(s, r, g, b, 1);
			world.addParticle(data, x, y, z, 0, m, 0);
		}
	}

	public int getLightColor() {
		if (lightColor == -1) {
			lightColor = 0xFFFFFF;
		}

		return lightColor;
	}

	@Override
	public void writePacketNBT(CompoundNBT cmp) {
		cmp.putInt(TAG_COLOR, color);
	}

	@Override
	public void readPacketNBT(CompoundNBT cmp) {
		color = cmp.getInt(TAG_COLOR);
	}

}
