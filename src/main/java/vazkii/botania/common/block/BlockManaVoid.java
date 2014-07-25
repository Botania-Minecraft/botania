/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Mar 5, 2014, 12:55:47 AM (GMT)]
 */
package vazkii.botania.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.mana.IPoolOverlayProvider;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.common.block.tile.mana.TileManaVoid;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibBlockNames;

public class BlockManaVoid extends BlockModContainer implements ILexiconable, IPoolOverlayProvider {

	IIcon overlay;

	protected BlockManaVoid() {
		super(Material.rock);
		setHardness(2.0F);
		setResistance(2000F);
		setStepSound(Block.soundTypeStone);
		setBlockName(LibBlockNames.MANA_VOID);
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		blockIcon = IconHelper.forBlock(par1IconRegister, this, 0);
		overlay = IconHelper.forBlock(par1IconRegister, this, 1);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int id) {
		return new TileManaVoid();
	}

	@Override
	public LexiconEntry getEntry(World world, int x, int y, int z, EntityPlayer player, ItemStack lexicon) {
		return LexiconData.manaVoid;
	}

	@Override
	public IIcon getIcon(World world, int x, int y, int z) {
		return overlay;
	}

}
