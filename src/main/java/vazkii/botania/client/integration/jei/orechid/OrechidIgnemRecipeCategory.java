/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei.orechid;

import mezz.jei.api.helpers.IGuiHelper;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.block.ModSubtiles;

import javax.annotation.Nonnull;

import java.util.Map;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class OrechidIgnemRecipeCategory extends OrechidRecipeCategoryBase<OrechidIgnemRecipeWrapper> {

	public static final ResourceLocation UID = prefix("orechid_ignem");

	public OrechidIgnemRecipeCategory(IGuiHelper guiHelper) {
		super(guiHelper, new ItemStack(ModSubtiles.orechidIgnem), new ItemStack(Blocks.NETHERRACK, 64),
				I18n.format("botania.nei.orechidIgnem"));
	}

	@Nonnull
	@Override
	public ResourceLocation getUid() {
		return UID;
	}

	@Nonnull
	@Override
	public Class<? extends OrechidIgnemRecipeWrapper> getRecipeClass() {
		return OrechidIgnemRecipeWrapper.class;
	}

	@Override
	protected Map<ResourceLocation, Integer> getOreWeights() {
		return BotaniaAPI.instance().getNetherOreWeights();
	}
}
