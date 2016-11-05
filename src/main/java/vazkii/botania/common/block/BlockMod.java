/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 14, 2014, 5:31:15 PM (GMT)]
 */
package vazkii.botania.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.recipe.IElvenItem;
import vazkii.botania.client.core.handler.ModelHandler;
import vazkii.botania.client.render.IModelRegister;
import vazkii.botania.common.core.BotaniaCreativeTab;
import vazkii.botania.common.item.block.ItemBlockElven;
import vazkii.botania.common.item.block.ItemBlockMod;
import vazkii.botania.common.lib.LibMisc;

public abstract class BlockMod extends Block implements IModelRegister {

	public BlockMod(Material par2Material, String name) {
		super(par2Material);
		setUnlocalizedName(name);
		setDefaultState(pickDefaultState()); // This MUST happen before registering the block
		setRegistryName(new ResourceLocation(LibMisc.MOD_ID, name));
		GameRegistry.register(this);
		registerItemForm();
		if(registerInCreative())
			setCreativeTab(BotaniaCreativeTab.INSTANCE);
	}

	protected IBlockState pickDefaultState() {
		return blockState.getBaseState();
	}

	public void registerItemForm() {
		GameRegistry.register(this instanceof IElvenItem ? new ItemBlockElven(this) : new ItemBlockMod(this), getRegistryName());
	}

	protected boolean registerInCreative() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		if(Item.getItemFromBlock(this) != null)
			ModelHandler.registerBlockToState(this, 0, getDefaultState());
	}
}
