/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Apr 24, 2014, 4:43:47 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import vazkii.botania.api.mana.IManaGivingItem;
import vazkii.botania.api.mana.ManaItemHandler;

public class ItemAuraRing extends ItemBauble implements IManaGivingItem {

	public ItemAuraRing(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase player) {
		if (player instanceof EntityPlayer && player.ticksExisted % 10 == 0)
			ManaItemHandler.dispatchManaExact(stack, (EntityPlayer) player, 1, true);
	}
}
