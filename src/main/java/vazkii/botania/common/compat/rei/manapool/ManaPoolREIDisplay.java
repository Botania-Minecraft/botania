/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.compat.rei.manapool;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.compat.rei.BotaniaRecipeDisplay;
import vazkii.botania.common.crafting.RecipeManaInfusion;

import javax.annotation.Nullable;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class ManaPoolREIDisplay extends BotaniaRecipeDisplay<RecipeManaInfusion> {
	@Nullable
	private BlockState catalyst;

	public ManaPoolREIDisplay(RecipeManaInfusion recipe) {
		super(recipe);
		this.catalyst = recipe.getCatalyst();
	}

	public Optional<BlockState> getCatalyst() {
		return Optional.ofNullable(this.catalyst);
	}

	@Override
	public int getManaCost() {
		return recipe.getManaToConsume();
	}

	@Override
	public @NotNull Identifier getRecipeCategory() {
		return recipe.TYPE_ID;
	}
}
