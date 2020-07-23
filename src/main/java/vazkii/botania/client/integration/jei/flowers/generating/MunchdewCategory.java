/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei.flowers.generating;

import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.runtime.IIngredientManager;
import vazkii.botania.common.block.ModSubtiles;
import vazkii.botania.common.block.subtile.generating.SubTileMunchdew;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class MunchdewCategory extends SimpleGenerationCategory {

	public MunchdewCategory(IGuiHelper guiHelper) {
		super(guiHelper, ModSubtiles.munchdew, ModSubtiles.munchdewFloating);
	}

	@Override
	protected Collection<SimpleManaGenRecipe> makeRecipes(IIngredientManager ingredientManager, IJeiHelpers helpers) {
		return Collections.singletonList(new SimpleManaGenRecipe(BlockTags.LEAVES.getAllElements()
				.stream()
				.map(ItemStack::new)
				.collect(Collectors.toList()),
				SubTileMunchdew.MANA_PER_LEAF));
	}

}
