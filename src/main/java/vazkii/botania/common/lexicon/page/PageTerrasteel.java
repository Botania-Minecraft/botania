/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Apr 14, 2014, 5:57:26 PM (GMT)]
 */
package vazkii.botania.common.lexicon.page;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import vazkii.botania.api.internal.IGuiLexiconEntry;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.lexicon.LexiconRecipeMappings;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.item.ModItems;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PageTerrasteel extends PageRecipe {

	private static final ResourceLocation terrasteelOverlay = new ResourceLocation(LibResources.GUI_TERRASTEEL_OVERLAY);

	int ticksElapsed = 0;
	int index = 0;

	static final Block[] blocks = new Block[] {
		Blocks.iron_block, Blocks.gold_block, Blocks.emerald_block, Blocks.diamond_block, ModBlocks.storage
	};

	public PageTerrasteel(String unlocalizedName) {
		super(unlocalizedName);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderRecipe(IGuiLexiconEntry gui, int mx, int my) {
		Block block = blocks[index];

		GL11.glTranslatef(0F, 0F, -10F);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8, gui.getTop() + 103, new ItemStack(block), false);

		GL11.glTranslatef(0F, 0F, 5F);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8 + 7, gui.getTop() + 106, new ItemStack(block), false);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8 - 6, gui.getTop() + 106, new ItemStack(block), false);

		GL11.glTranslatef(0F, 0F, 5F);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8, gui.getTop() + 110, new ItemStack(block), false);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8 + 14, gui.getTop() + 110, new ItemStack(block), false);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8 - 13, gui.getTop() + 110, new ItemStack(block), false);

		GL11.glTranslatef(0F, 0F, 5F);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8 - 6, gui.getTop() + 114, new ItemStack(block), false);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8 + 7, gui.getTop() + 114, new ItemStack(block), false);

		GL11.glTranslatef(0F, 0F, 5F);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8 + 1, gui.getTop() + 117, new ItemStack(block), false);

		GL11.glTranslatef(0F, 0F, 5F);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8, gui.getTop() + 102, new ItemStack(Blocks.beacon), false);
		GL11.glTranslatef(0F, 0F, -10F);

		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8, gui.getTop() + 30, new ItemStack(ModItems.manaResource, 1, 4), false);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8, gui.getTop() + 80, new ItemStack(ModItems.manaResource, 1, 0), false);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8 - 20, gui.getTop() + 86, new ItemStack(ModItems.manaResource, 1, 1), false);
		renderItem(gui, gui.getLeft() + gui.getWidth() / 2 - 8 + 19, gui.getTop() + 86, new ItemStack(ModItems.manaResource, 1, 2), false);

		Minecraft.getMinecraft().renderEngine.bindTexture(terrasteelOverlay);

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
			index++;

			if(index == blocks.length)
				index = 0;
		}
		++ticksElapsed;
	}

	@Override
	public void onPageAdded(LexiconEntry entry, int index) {
		LexiconRecipeMappings.map(new ItemStack(ModItems.manaResource, 1, 4), entry, index);
	}

}
