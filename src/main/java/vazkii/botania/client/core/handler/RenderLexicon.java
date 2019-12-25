package vazkii.botania.client.core.handler;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.model.BookModel;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.item.ItemLexicon;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lib.LibMisc;
import vazkii.patchouli.client.book.gui.GuiBook;

import java.util.ArrayList;
import java.util.List;

// Hacky way to render 3D lexicon, will be reevaluated in the future.
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = LibMisc.MOD_ID)
public class RenderLexicon {
	private static final BookModel model = new BookModel();
	// todo 1.12 improve
	private static final ResourceLocation texture = new ResourceLocation(LibResources.MODEL_LEXICA_DEFAULT);
	private static final ResourceLocation elvenTexture = new ResourceLocation(LibResources.MODEL_LEXICA_ELVEN);

	private static final String[] QUOTES = new String[] {
			"\"Neat!\" - Direwolf20",
			"\"It's pretty ledge.\" - Haighyorkie",
			"\"I don't really like it.\" - CrustyMustard",
			"\"It's a very thinky mod.\" - AdamG3691",
			"\"You must craft the tiny potato.\" - TheFractangle",
			"\"Vazkii did a thing.\" - cpw"
	};

	private static int quote = -1;

	@SubscribeEvent
	public static void renderItem(RenderSpecificHandEvent evt) {
		Minecraft mc = Minecraft.getInstance();
		if(!ConfigHandler.CLIENT.lexicon3dModel.get()
				|| mc.gameSettings.thirdPersonView != 0
				|| mc.player.getHeldItem(evt.getHand()).isEmpty()
				|| mc.player.getHeldItem(evt.getHand()).getItem() != ModItems.lexicon)
			return;
		evt.setCanceled(true);
		try {
			renderItemInFirstPerson(mc.player, evt.getPartialTicks(), evt.getInterpolatedPitch(), evt.getHand(), evt.getSwingProgress(), evt.getItemStack(), evt.getEquipProgress());
		} catch (Throwable throwable) {
			Botania.LOGGER.warn("Failed to render lexicon");
		}
	}

	private static void renderItemInFirstPerson(AbstractClientPlayerEntity player, float partialTicks, float interpPitch, Hand hand, float swingProgress, ItemStack stack, float equipProgress) throws Throwable {
		// Cherry picked from ItemRenderer.renderItemInFirstPerson
		boolean flag = hand == Hand.MAIN_HAND;
		HandSide enumhandside = flag ? player.getPrimaryHand() : player.getPrimaryHand().opposite();
		GlStateManager.pushMatrix();
		boolean flag1 = enumhandside == HandSide.RIGHT;
		float f = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float)Math.PI);
		float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * ((float)Math.PI * 2F));
		float f2 = -0.2F * MathHelper.sin(swingProgress * (float)Math.PI);
		int i = flag1 ? 1 : -1;
		GlStateManager.translated(i * f, f1, f2);
		transformSideFirstPerson(enumhandside, equipProgress);
		transformFirstPerson(enumhandside, swingProgress);
		doRender(enumhandside, partialTicks, stack);
		GlStateManager.popMatrix();
	}

	private static void doRender(HandSide side, float partialTicks, ItemStack stack) {
		Minecraft mc = Minecraft.getInstance();

		GlStateManager.pushMatrix();
		GlStateManager.color3f(1F, 1F, 1F);
		mc.textureManager.bindTexture(((ItemLexicon) ModItems.lexicon).isElvenItem(stack) ? elvenTexture : texture);
		float opening;
		float pageFlip;

		float ticks = ClientTickHandler.ticksWithLexicaOpen;
		if(ticks > 0 && ticks < 10) {
			if(Minecraft.getInstance().currentScreen instanceof GuiBook
				&& ((GuiBook) Minecraft.getInstance().currentScreen).book.getBookItem().getItem() == ModItems.lexicon)
				ticks += partialTicks;
			else ticks -= partialTicks;
		}

		GlStateManager.translated(0.3F + 0.02F * ticks, 0.475F + 0.01F * ticks, -0.2F - (side == HandSide.RIGHT ? 0.035F : 0.01F) * ticks);
		GlStateManager.rotatef(87.5F + ticks * (side == HandSide.RIGHT ? 8 : 12), 0F, 1F, 0F);
		GlStateManager.rotatef(ticks * 2.85F, 0F, 0F, 1F);
		opening = ticks / 12F;

		float pageFlipTicks = ClientTickHandler.pageFlipTicks;
		if(pageFlipTicks > 0)
			pageFlipTicks -= ClientTickHandler.partialTicks;

		pageFlip = pageFlipTicks / 5F;

		model.render(0F, 0F, pageFlip, opening, 0F, 1F / 16F);
		if(ticks < 3) {
			FontRenderer font = Minecraft.getInstance().fontRenderer;
			GlStateManager.rotatef(180F, 0F, 0F, 1F);
			GlStateManager.translatef(-0.30F, -0.24F, -0.07F);
			GlStateManager.scalef(0.0030F, 0.0030F, -0.0030F);


			String title = ItemLexicon.getTitle(stack).getFormattedText();

			font.drawString(font.trimStringToWidth(title, 80), 0, 0, 0xD69700);

			GlStateManager.translatef(0F, 10F, 0F);
			GlStateManager.scalef(0.6F, 0.6F, 0.6F);
			font.drawString(TextFormatting.ITALIC + "" + TextFormatting.BOLD + ItemLexicon.getEdition(), 0, 0, 0xA07100);

			if(quote == -1)
				quote = mc.world.rand.nextInt(QUOTES.length);

			String quoteStr = QUOTES[quote];

			GlStateManager.translatef(-5F, 15F, 0F);
			renderText(0, 0, 140, 100, 0, false, 0x79ff92, quoteStr);
			GlStateManager.color3f(1F, 1F, 1F);

			GlStateManager.translatef(8F, 110F, 0F);
			font.drawString(I18n.format("botaniamisc.lexiconcover0"), 0, 0, 0x79ff92);

			GlStateManager.translatef(0F, 10F, 0F);
			font.drawString(TextFormatting.UNDERLINE + "" + TextFormatting.ITALIC + I18n.format("botaniamisc.lexiconcover1"), 0, 0, 0x79ff92);

			GlStateManager.translatef(0F, -30F, 0F);

			String authorTitle = I18n.format("botaniamisc.lexiconcover2");
			int len = font.getStringWidth(authorTitle);
			font.drawString(authorTitle, 58 - len / 2, -8, 0xD69700);
		}

		GlStateManager.popMatrix();
	}

	// Copy - ItemRenderer.transformSideFirstPerson
	// Arg - Side, EquipProgress
	private static void transformSideFirstPerson(HandSide p_187459_1_, float p_187459_2_)
	{
		int i = p_187459_1_ == HandSide.RIGHT ? 1 : -1;
		GlStateManager.translatef(i * 0.56F, -0.44F + p_187459_2_ * -0.8F, -0.72F);
	}

	// Copy with modification - ItemRenderer.transformFirstPerson
	// Arg - Side, SwingProgress
	private static void transformFirstPerson(HandSide p_187453_1_, float p_187453_2_)
	{
		int i = p_187453_1_ == HandSide.RIGHT ? 1 : -1;
		// Botania - added
		GlStateManager.translatef(p_187453_1_ == HandSide.RIGHT ? 0.2F : 0.52F, -0.125F, p_187453_1_ == HandSide.RIGHT ? 0.6F : 0.25F);
		GlStateManager.rotatef(p_187453_1_ == HandSide.RIGHT ? 60F : 120F, 0F, 1F, 0F);
		GlStateManager.rotatef(30F, 0F, 0F, -1F);
		// End add
		float f = MathHelper.sin(p_187453_2_ * p_187453_2_ * (float)Math.PI);
		GlStateManager.rotatef(i * (45.0F + f * -20.0F), 0.0F, 1.0F, 0.0F);
		float f1 = MathHelper.sin(MathHelper.sqrt(p_187453_2_) * (float)Math.PI);
		GlStateManager.rotatef(i * f1 * -20.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotatef(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotatef(i * -45.0F, 0.0F, 1.0F, 0.0F);
	}

	private static void renderText(int x, int y, int width, int height, int paragraphSize, boolean useUnicode, int color, String unlocalizedText) {
		x += 2;
		y += 10;
		width -= 4;

		FontRenderer font = Minecraft.getInstance().fontRenderer;
		String text = I18n.format(unlocalizedText).replaceAll("&", "\u00a7");
		String[] textEntries = text.split("<br>");

		List<List<String>> lines = new ArrayList<>();

		String controlCodes;
		for(String s : textEntries) {
			List<String> words = new ArrayList<>();
			String lineStr = "";
			String[] tokens = s.split(" ");
			for(String token : tokens) {
				String prev = lineStr;
				String spaced = token + " ";
				lineStr += spaced;

				controlCodes = toControlCodes(getControlCodes(prev));
				if(font.getStringWidth(lineStr) > width) {
					lines.add(words);
					lineStr = controlCodes + spaced;
					words = new ArrayList<>();
				}

				words.add(controlCodes + token);
			}

			if(!lineStr.isEmpty())
				lines.add(words);
			lines.add(new ArrayList<>());
		}

		int i = 0;
		for(List<String> words : lines) {
			words.size();
			int xi = x;
			int spacing = 4;
			int wcount = words.size();
			int compensationSpaces = 0;
			boolean justify = ConfigHandler.CLIENT.lexiconJustifiedText.get() && wcount > 0 && lines.size() > i && !lines.get(i + 1).isEmpty();

			if(justify) {
				String s = Joiner.on("").join(words);
				int swidth = font.getStringWidth(s);
				int space = width - swidth;

				spacing = wcount == 1 ? 0 : space / (wcount - 1);
				compensationSpaces = wcount == 1 ? 0 : space % (wcount - 1);
			}

			for(String s : words) {
				int extra = 0;
				if(compensationSpaces > 0) {
					compensationSpaces--;
					extra++;
				}
				font.drawString(s, xi, y, color);
				xi += font.getStringWidth(s) + spacing + extra;
			}

			y += words.isEmpty() ? paragraphSize : 10;
			i++;
		}
	}

	private static String getControlCodes(String s) {
		String controls = s.replaceAll("(?<!\u00a7)(.)", "");
		return controls.replaceAll(".*r", "r");
	}

	private static String toControlCodes(String s) {
		return s.replaceAll(".", "\u00a7$0");
	}
}
