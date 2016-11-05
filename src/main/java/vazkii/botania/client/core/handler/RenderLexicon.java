package vazkii.botania.client.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import vazkii.botania.client.gui.lexicon.GuiLexicon;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.item.ItemLexicon;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lexicon.page.PageText;

// Hacky way to render 3D lexicon, will be reevaluated in the future.
public class RenderLexicon {

	private final ModelBook model = new ModelBook();
	private final ResourceLocation texture = new ResourceLocation(LibResources.MODEL_LEXICA);

	private final String[] QUOTES = new String[] {
			"\"Neat!\" - Direwolf20",
			"\"It's pretty ledge.\" - Haighyorkie",
			"\"I don't really like it.\" - CrustyMustard",
			"\"It's a very thinky mod.\" - AdamG3691",
			"\"You must craft the tiny potato.\" - TheFractangle",
			"\"Vazkii did a thing.\" - cpw"
	};

	int quote = -1;

	@SubscribeEvent
	public void renderItem(RenderSpecificHandEvent evt) {
		Minecraft mc = Minecraft.getMinecraft();
		if(!ConfigHandler.lexicon3dModel
				|| mc.gameSettings.thirdPersonView != 0
				|| mc.thePlayer.getHeldItem(evt.getHand()) == null
				|| mc.thePlayer.getHeldItem(evt.getHand()).getItem() != ModItems.lexicon)
			return;
		evt.setCanceled(true);
		try {
			renderItemInFirstPerson(mc.thePlayer, evt.getPartialTicks(), evt.getInterpolatedPitch(), evt.getHand(), evt.getSwingProgress(), evt.getItemStack(), evt.getEquipProgress());
		} catch (Throwable throwable) {
			Botania.LOGGER.warn("Failed to render lexicon");
		}
	}

	private void renderItemInFirstPerson(AbstractClientPlayer player, float partialTicks, float interpPitch, EnumHand hand, float swingProgress, ItemStack stack, float equipProgress) throws Throwable {
		// Cherry picked from ItemRenderer.renderItemInFirstPerson
		boolean flag = hand == EnumHand.MAIN_HAND;
		EnumHandSide enumhandside = flag ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
		GlStateManager.pushMatrix();
		boolean flag1 = enumhandside == EnumHandSide.RIGHT;
		float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float)Math.PI);
		float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * ((float)Math.PI * 2F));
		float f2 = -0.2F * MathHelper.sin(swingProgress * (float)Math.PI);
		int i = flag1 ? 1 : -1;
		GlStateManager.translate(i * f, f1, f2);
		transformSideFirstPerson(enumhandside, equipProgress);
		transformFirstPerson(enumhandside, swingProgress);
		doRender(enumhandside, partialTicks, stack);
		GlStateManager.popMatrix();
	}

	private void doRender(EnumHandSide side, float partialTicks, ItemStack stack) {
		Minecraft mc = Minecraft.getMinecraft();

		GlStateManager.pushMatrix();
		mc.renderEngine.bindTexture(texture);
		float opening;
		float pageFlip;

		float ticks = ClientTickHandler.ticksWithLexicaOpen;
		if(ticks > 0 && ticks < 10) {
			if(Minecraft.getMinecraft().currentScreen instanceof GuiLexicon)
				ticks += partialTicks;
			else ticks -= partialTicks;
		}

		GlStateManager.translate(0.3F + 0.02F * ticks, 0.475F + 0.01F * ticks, -0.2F - 0.01F * ticks);
		GlStateManager.rotate(87.5F + ticks * (side == EnumHandSide.RIGHT ? 5 : 10), 0F, 1F, 0F);
		GlStateManager.rotate(ticks * 2.85F, 0F, 0F, 1F);
		opening = ticks / 12F;

		float pageFlipTicks = ClientTickHandler.pageFlipTicks;
		if(pageFlipTicks > 0)
			pageFlipTicks -= ClientTickHandler.partialTicks;

		pageFlip = pageFlipTicks / 5F;

		model.render(null, 0F, 0F, pageFlip, opening, 0F, 1F / 16F);
		if(ticks < 3) {
			FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
			GlStateManager.rotate(180F, 0F, 0F, 1F);
			GlStateManager.translate(-0.3F, -0.21F, -0.07F);
			GlStateManager.scale(0.0035F, 0.0035F, -0.0035F);


			String title = ItemLexicon.getTitle(stack);

			font.drawString(font.trimStringToWidth(title, 80), 0, 0, 0xD69700);

			GlStateManager.translate(0F, 10F, 0F);
			GlStateManager.scale(0.6F, 0.6F, 0.6F);
			font.drawString(TextFormatting.ITALIC + "" + TextFormatting.BOLD + I18n.format("botaniamisc.edition", ItemLexicon.getEdition()), 0, 0, 0xA07100);

			if(quote == -1)
				quote = mc.theWorld.rand.nextInt(QUOTES.length);

			String quoteStr = QUOTES[quote];

			GlStateManager.translate(-5F, 3F, 0F);
			PageText.renderText(0, 0, 140, 100, 0, false, 0x79ff92, quoteStr);
			GlStateManager.color(1F, 1F, 1F);

			GlStateManager.translate(2F, 98F, 0F);
			font.drawString(I18n.format("botaniamisc.lexiconcover0"), 0, 0, 0x79ff92);

			GlStateManager.translate(0F, 10F, 0F);
			font.drawString(TextFormatting.UNDERLINE + "" + TextFormatting.ITALIC + I18n.format("botaniamisc.lexiconcover1"), 0, 0, 0x79ff92);

			GlStateManager.translate(0F, -30F, 0F);

			String authorTitle = I18n.format("botaniamisc.lexiconcover2");
			int len = font.getStringWidth(authorTitle);
			font.drawString(authorTitle, 62 - len / 2, 0, 0xD69700);
		}

		GlStateManager.popMatrix();
	}

	// Copy - ItemRenderer.transformSideFirstPerson
	// Arg - Side, EquipProgress
	private void transformSideFirstPerson(EnumHandSide p_187459_1_, float p_187459_2_)
	{
		int i = p_187459_1_ == EnumHandSide.RIGHT ? 1 : -1;
		GlStateManager.translate(i * 0.56F, -0.52F + p_187459_2_ * -0.6F, -0.72F);
	}

	// Copy with modification - ItemRenderer.transformFirstPerson
	// Arg - Side, SwingProgress
	private void transformFirstPerson(EnumHandSide p_187453_1_, float p_187453_2_)
	{
		int i = p_187453_1_ == EnumHandSide.RIGHT ? 1 : -1;
		// Botania - added
		GlStateManager.translate(p_187453_1_ == EnumHandSide.RIGHT ? 0.5F : 0.3F, -0.25F, 0.2F);
		GlStateManager.rotate(90F, 0F, 1F, 0F);
		GlStateManager.rotate(12F, 0F, 0F, -1F);
		// End add
		float f = MathHelper.sin(p_187453_2_ * p_187453_2_ * (float)Math.PI);
		GlStateManager.rotate(i * (45.0F + f * -20.0F), 0.0F, 1.0F, 0.0F);
		float f1 = MathHelper.sin(MathHelper.sqrt_float(p_187453_2_) * (float)Math.PI);
		GlStateManager.rotate(i * f1 * -20.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(i * -45.0F, 0.0F, 1.0F, 0.0F);
	}

}
