/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Jul 23, 2014, 5:28:55 PM (GMT)]
 */
package vazkii.botania.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.client.lib.LibRenderIDs;
import vazkii.botania.common.block.tile.TileSpawnerClaw;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibBlockNames;

public class BlockSpawnerClaw extends BlockModContainer implements ILexiconable {

	protected BlockSpawnerClaw() {
		super(Material.iron);
		setHardness(3.0F);
		setBlockName(LibBlockNames.SPAWNER_CLAW);

		float f = 1F / 8F;
		float f1 = 1F / 16F;
		setBlockBounds(f, 0F, f, 1F - f, f1, 1F - f);
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		//NO-OP
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return LibRenderIDs.idSpawnerClaw;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileSpawnerClaw();
	}

	@Override
	public LexiconEntry getEntry(World world, int x, int y, int z, EntityPlayer player, ItemStack lexicon) {
		return LexiconData.spawnerClaw;
	}

}
