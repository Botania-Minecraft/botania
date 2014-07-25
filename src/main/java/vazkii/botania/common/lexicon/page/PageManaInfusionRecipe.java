/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Feb 8, 2014, 1:11:42 PM (GMT)]
 */
package vazkii.botania.common.lexicon.page;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import vazkii.botania.api.internal.IGuiLexiconEntry;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.lexicon.LexiconRecipeMappings;
import vazkii.botania.api.recipe.RecipeManaInfusion;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.mana.TilePool;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PageManaInfusionRecipe extends PageRecipe {

	private static final ResourceLocation manaInfusionOverlay = new ResourceLocation(LibResources.GUI_MANA_INFUSION_OVERLAY);

	List<RecipeManaInfusion> recipes;
	int ticksElapsed = 0;
	int recipeAt = 0;

	public PageManaInfusionRecipe(String unlocalizedName, List<RecipeManaInfusion> recipes) {
		super(unlocalizedName);
		this.recipes = recipes;
	}

	public PageManaInfusionRecipe(String unlocalizedName, RecipeManaInfusion recipe) {
		this(unlocalizedName, Arrays.asList(recipe));
	}

	@Override
	public void onPageAdded(LexiconEntry entry, int index) {
		for(RecipeManaInfusion recipe : recipes)
			LexiconRecipeMappings.map(recipe.getOutput(), entry, index);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderRecipe(IGuiLexiconEntry gui, int mx, int my) {
		RecipeManaInfusion recipe = recipes.get(recipeAt);
		TextureManager render = Minecraft.getMinecraft().renderEngine;
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;

		Object input = recipe.getInput();
		if(input instanceof String)
			input = OreDictionary.getOres((String) input).get(0);

		renderItemAtGridPos(gui, 1, 1, (ItemStack) input, false);
		renderItemAtGridPos(gui, 2, 1, new ItemStack(ModBlocks.pool), false);
		renderItemAtGridPos(gui, 3, 1, recipe.getOutput(), false);
		if(recipe.isAlchemy())
			renderItemAtGridPos(gui, 1, 2, new ItemStack(ModBlocks.alchemyCatalyst), false);
		else if(recipe.isConjuration())
			renderItemAtGridPos(gui, 1, 2, new ItemStack(ModBlocks.conjurationCatalyst), false);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		String manaUsage = StatCollector.translateToLocal("botaniamisc.manaUsage");
		font.drawString(manaUsage, gui.getLeft() + gui.getWidth() / 2 - font.getStringWidth(manaUsage) / 2, gui.getTop() + 105, 0x66000000);

		int ratio = 10;
		int x = gui.getLeft() + gui.getWidth() / 2 - 50;
		int y = gui.getTop() + 115;

		if(mx > x + 1 && mx <= x + 101 && my > y - 14 && my <= y + 11)
			ratio = 1;

		HUDHandler.renderManaBar(x, y, 0x0000FF, 0.75F, recipe.getManaToConsume(), TilePool.MAX_MANA / ratio);

		String ratioString = String.format(StatCollector.translateToLocal("botaniamisc.ratio"), ratio);

		boolean unicode = font.getUnicodeFlag();
		font.setUnicodeFlag(true);
		font.drawString(ratioString, x + 100 - font.getStringWidth(ratioString), y + 5, 0x50000000);
		font.setUnicodeFlag(unicode);

		GL11.glDisable(GL11.GL_BLEND);

		render.bindTexture(manaInfusionOverlay);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(1F, 1F, 1F, 1F);
		((GuiScreen) gui).drawTexturedModalRect(gui.getLeft(), gui.getTop(), 0, 0, gui.getWidth(), gui.getHeight());
		GL11.glDisable(GL11.GL_BLEND);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateScreen() {
		if(ticksElapsed % 20 == 0) {
			recipeAt++;

			if(recipeAt == recipes.size())
				recipeAt = 0;
		}
		++ticksElapsed;
	}

}
