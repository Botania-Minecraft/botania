/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Apr 13, 2014, 6:38:21 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.armor.manasteel;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.core.BotaniaCreativeTab;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemManasteelArmor extends ItemArmor implements ISpecialArmor, IManaUsingItem {

	private static final int MANA_PER_DAMAGE = 70;

	public ItemManasteelArmor(int type, String name) {
		this(type, name, BotaniaAPI.manasteelArmorMaterial);
	}

	public ItemManasteelArmor(int type, String name, ArmorMaterial mat) {
		super(mat, 0, type);
		setCreativeTab(BotaniaCreativeTab.INSTANCE);
		setUnlocalizedName(name);
	}

	@Override
	public Item setUnlocalizedName(String par1Str) {
		GameRegistry.registerItem(this, par1Str);
		return super.setUnlocalizedName(par1Str);
	}

	@Override
	public String getUnlocalizedNameInefficiently(ItemStack par1ItemStack) {
		return super.getUnlocalizedNameInefficiently(par1ItemStack).replaceAll("item.", "item." + LibResources.PREFIX_MOD);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		itemIcon = IconHelper.forItem(par1IconRegister, this);
	}

	@Override
	public ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
		return new ArmorProperties(0, damageReduceAmount / 25D, armor.getMaxDamage() + 1 - armor.getItemDamage());
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
		return damageReduceAmount;
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity player, int par4, boolean par5) {
		if(player instanceof EntityPlayer)
			onArmorTick(world, (EntityPlayer) player, stack);
	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		if(!world.isRemote && stack.getItemDamage() > 0 && ManaItemHandler.requestManaExact(stack, player, MANA_PER_DAMAGE * 2, true))
			stack.setItemDamage(stack.getItemDamage() - 1);
	}

	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
		ToolCommons.damageItem(stack, damage, entity, MANA_PER_DAMAGE);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
		return slot == 2 ? LibResources.MODEL_MANASTEEL_1 : LibResources.MODEL_MANASTEEL_0;
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
		return par2ItemStack.getItem() == ModItems.manaResource && par2ItemStack.getItemDamage() == 0 ? true : super.getIsRepairable(par1ItemStack, par2ItemStack);
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv) {
		if(GuiScreen.isShiftKeyDown()) {
			addStringToTooltip(getArmorSetTitle(player), list);
			addArmorSetDescription(stack, list);
			ItemStack[] stacks = getArmorSetStacks();
			for(int i = 0; i < stacks.length; i++)
				addStringToTooltip((hasArmorSetItem(player, i) ? EnumChatFormatting.GREEN : "") + " - " + stacks[i].getDisplayName(), list);
		} else addStringToTooltip(StatCollector.translateToLocal("botaniamisc.shiftinfo"), list);
	}

	public void addStringToTooltip(String s, List<String> tooltip) {
		tooltip.add(s.replaceAll("&", "\u00a7"));
	}

	static ItemStack[] armorset;

	public ItemStack[] getArmorSetStacks() {
		if(armorset == null)
			armorset = new ItemStack[] {
				new ItemStack(ModItems.manasteelHelm),
				new ItemStack(ModItems.manasteelChest),
				new ItemStack(ModItems.manasteelLegs),
				new ItemStack(ModItems.manasteelBoots)
		};

		return armorset;
	}

	public boolean hasArmorSet(EntityPlayer player) {
		return hasArmorSetItem(player, 0) && hasArmorSetItem(player, 1) && hasArmorSetItem(player, 2) && hasArmorSetItem(player, 3);
	}

	public boolean hasArmorSetItem(EntityPlayer player, int i) {
		ItemStack stack = player.inventory.armorInventory[3 - i];
		if(stack == null)
			return false;

		switch(i) {
		case 0: return stack.getItem() == ModItems.manasteelHelm || stack.getItem() == ModItems.manasteelHelmRevealing;
		case 1: return stack.getItem() == ModItems.manasteelChest;
		case 2: return stack.getItem() == ModItems.manasteelLegs;
		case 3: return stack.getItem() == ModItems.manasteelBoots;
		}

		return false;
	}

	public int getSetPiecesEquipped(EntityPlayer player) {
		int pieces = 0;
		for(int i = 0; i < 4; i++)
			if(hasArmorSetItem(player, i))
				pieces++;

		return pieces;
	}

	public String getArmorSetName() {
		return StatCollector.translateToLocal("botania.armorset.manasteel.name");
	}

	public String getArmorSetTitle(EntityPlayer player) {
		return StatCollector.translateToLocal("botaniamisc.armorset") + " " + getArmorSetName() + " (" + getSetPiecesEquipped(player) + "/" + getArmorSetStacks().length + ")";
	}

	public void addArmorSetDescription(ItemStack stack, List<String> list) {
		addStringToTooltip(StatCollector.translateToLocal("botania.armorset.manasteel.desc"), list);
	}

}
