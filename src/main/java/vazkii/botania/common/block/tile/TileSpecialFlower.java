/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Jan 22, 2014, 7:21:51 PM (GMT)]
 */
package vazkii.botania.common.block.tile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.subtile.SubTileEntity;
import vazkii.botania.api.wand.ITileBound;

public class TileSpecialFlower extends TileMod implements ITileBound {

	private static final String TAG_SUBTILE_NAME = "subTileName";
	private static final String TAG_SUBTILE_CMP = "subTileCmp";

	public String subTileName = "";
	SubTileEntity subTile;

	public SubTileEntity getSubTile() {
		return subTile;
	}

	public void setSubTile(String name) {
		subTileName = name;
		provideSubTile(subTileName);
	}

	public void setSubTile(SubTileEntity tile) {
		subTile = tile;
		subTile.setSupertile(this);
	}

	private void provideSubTile(String name) {
		subTileName = name;

		Class<? extends SubTileEntity> tileClass = BotaniaAPI.getSubTileMapping(name);
		try {
			SubTileEntity tile = tileClass.newInstance();
			setSubTile(tile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateEntity() {
		if(subTile != null)
			subTile.onUpdate();
	}

	@Override
	public boolean canUpdate() {
		return subTile == null || subTile.canUpdate();
	}

	@Override
	public void writeCustomNBT(NBTTagCompound cmp) {
		super.writeCustomNBT(cmp);

		cmp.setString(TAG_SUBTILE_NAME, subTileName);
		NBTTagCompound subCmp = new NBTTagCompound();
		cmp.setTag(TAG_SUBTILE_CMP, subCmp);

		if(subTile != null)
			subTile.writeToPacketNBT(subCmp);
	}

	@Override
	public void readCustomNBT(NBTTagCompound cmp) {
		super.readCustomNBT(cmp);

		subTileName = cmp.getString(TAG_SUBTILE_NAME);
		NBTTagCompound subCmp = cmp.getCompoundTag(TAG_SUBTILE_CMP);

		if(subTile == null || !BotaniaAPI.getSubTileStringMapping(subTile.getClass()).equals(subTileName))
			provideSubTile(subTileName);

		if(subTile != null)
			subTile.readFromPacketNBT(subCmp);
	}

	public IIcon getIcon() {
		return subTile == null ? Blocks.red_flower.getIcon(0, 0) : subTile.getIcon();
	}

	public LexiconEntry getEntry() {
		return subTile == null ? null : subTile.getEntry();
	}

	public boolean onWanded(ItemStack wand, EntityPlayer player) {
		return subTile == null ? false : subTile.onWanded(player, wand);
	}

	public void renderHUD(Minecraft mc, ScaledResolution res) {
		if(subTile != null)
			subTile.renderHUD(mc, res);
	}

	@Override
	public ChunkCoordinates getBinding() {
		if(subTile == null)
			return null;
		return subTile.getBinding();
	}
}
