/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [May 12, 2015, 5:56:45 PM (GMT)]
 */
package vazkii.botania.common.item;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.client.core.handler.ModelHandler;
import vazkii.botania.common.lib.LibItemNames;

public class ItemTemperanceStone extends ItemMod {

	public ItemTemperanceStone() {
		super(LibItemNames.TEMPERANCE_STONE);
		setMaxStackSize(1);
		setHasSubtypes(true);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
		int dmg = stack.getItemDamage();
		stack.setItemDamage(~dmg & 1);
		world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.NEUTRAL, 0.3F, 0.1F);
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer player, List<String> stacks, boolean par4) {
		if(par1ItemStack.getItemDamage() == 1)
			addStringToTooltip(I18n.format("botaniamisc.active"), stacks);
		else addStringToTooltip(I18n.format("botaniamisc.inactive"), stacks);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelHandler.registerItemAppendMeta(this, 2, LibItemNames.TEMPERANCE_STONE);
	}

	private void addStringToTooltip(String s, List<String> tooltip) {
		tooltip.add(s.replaceAll("&", "\u00a7"));
	}

	public static boolean hasTemperanceActive(EntityPlayer player) {
		IInventory inv = player.inventory;
		for(int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if(stack != null && stack.getItem() == ModItems.temperanceStone && stack.getItemDamage() == 1)
				return true;
		}

		return false;
	}

}
