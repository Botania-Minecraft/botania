/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 *
 * File Created @ [Apr 20, 2014, 3:30:06 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import vazkii.botania.common.item.ItemMod;
import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;

public abstract class ItemBauble extends ItemMod implements IBauble {

	public ItemBauble(String name) {
		super();
		setMaxStackSize(1);
		setUnlocalizedName(name);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(par3EntityPlayer);
		for(int i = 0; i < baubles.getSizeInventory(); i++)
			if(baubles.getStackInSlot(i) == null && baubles.isItemValidForSlot(i, par1ItemStack)) {
				if(!par2World.isRemote) {
					baubles.setInventorySlotContents(i, par1ItemStack.copy());
					if(!par3EntityPlayer.capabilities.isCreativeMode)
						par3EntityPlayer.inventory.setInventorySlotContents(par3EntityPlayer.inventory.currentItem, null);
				}

				onEquipped(par1ItemStack, par3EntityPlayer);
				break;
			}

		return par1ItemStack;
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		BaubleType type = getBaubleType(par1ItemStack);
		if(GuiScreen.isShiftKeyDown()) {
			addStringToTooltip(StatCollector.translateToLocal("botania.baubletype." + type.name().toLowerCase()), par3List);

			KeyBinding key = null;
			KeyBinding[] keys = Minecraft.getMinecraft().gameSettings.keyBindings;
			for(KeyBinding otherKey : keys)
				if(otherKey.getKeyDescription().equals("Baubles Inventory")) {
					key = otherKey;
					break;
				}

			if(key != null)
				addStringToTooltip(StatCollector.translateToLocal("botania.baubletooltip").replaceAll("%key%", Keyboard.getKeyName(key.getKeyCode())), par3List);
		} else addStringToTooltip(StatCollector.translateToLocal("botaniamisc.shiftinfo"), par3List);
	}

	private void addStringToTooltip(String s, List<String> tooltip) {
		tooltip.add(s.replaceAll("&", "\u00a7"));
	}

	@Override
	public boolean canEquip(ItemStack stack, EntityLivingBase player) {
		return true;
	}

	@Override
	public boolean canUnequip(ItemStack stack, EntityLivingBase player) {
		return true;
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		if(player.ticksExisted == 1)
			onEquippedOrLoadedIntoWorld(stack, player);
	}

	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase player) {
		if(!player.worldObj.isRemote)
			player.worldObj.playSoundAtEntity(player, "random.orb", 0.1F, 1.3F);

		onEquippedOrLoadedIntoWorld(stack, player);
	}

	public void onEquippedOrLoadedIntoWorld(ItemStack stack, EntityLivingBase player) {
		// NO-OP
	}

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase player) {
		// NO-OP
	}

}
