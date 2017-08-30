/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [12/09/2016, 03:17:42 (GMT)]
 */
package vazkii.botania.client.gui.lexicon.button;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.gui.lexicon.GuiLexicon;

public class GuiButtonScaleChange extends GuiButtonLexicon {

	public GuiButtonScaleChange(int id, int x, int y) {
		super(id, x, y, 11, 11, "");
	}

	@Override
	public void drawButton(@Nonnull Minecraft par1Minecraft, int par2, int par3) {
		hovered = par2 >= xPosition && par3 >= yPosition && par2 < xPosition + width && par3 < yPosition + height;
		int k = getHoverState(hovered);

		par1Minecraft.renderEngine.bindTexture(GuiLexicon.texture);
		GlStateManager.color(1F, 1F, 1F, 1F);
		drawTexturedModalRect(xPosition, yPosition, k == 2 ? 152 : 141, 191, 11, 11);

		List<String> tooltip = new ArrayList<>();
		tooltip.add(TextFormatting.GREEN + I18n.format("botaniamisc.scaleChange"));

		int tooltipY = (tooltip.size() - 1) * 10;
		if(k == 2)
			RenderHelper.renderTooltip(par2, par3 + tooltipY, tooltip);
	}

}
