/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Feb 7, 2014, 9:46:24 PM (GMT)]
 */
package vazkii.botania.common.item.material;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import vazkii.botania.api.recipe.ICustomApothecaryColor;
import net.minecraft.item.Item;

public class ItemRune extends Item implements ICustomApothecaryColor {

	public ItemRune(Item.Properties builder) {
		super(builder);
	}

	@Override
	public int getParticleColor(ItemStack stack) {
		return 0xA8A8A8;
	}

}
