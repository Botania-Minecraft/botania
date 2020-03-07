/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 23, 2015, 9:22:10 PM (GMT)]
 */
package vazkii.botania.client.core.handler;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.regex.Pattern;

public final class ItemsRemainingRenderHandler {

	private static final int maxTicks = 30;
	private static final int leaveTicks = 20;

	private static ItemStack stack = ItemStack.EMPTY;
	private static String customString;
	private static int ticks, count;

	@OnlyIn(Dist.CLIENT)
	public static void render(float partTicks) {
		if(ticks > 0 && !stack.isEmpty()) {
			int pos = maxTicks - ticks;
			Minecraft mc = Minecraft.getInstance();
			int x = mc.getWindow().getScaledWidth() / 2 + 10 + Math.max(0, pos - leaveTicks);
			int y = mc.getWindow().getScaledHeight() / 2;

			int start = maxTicks - leaveTicks;
			float alpha = ticks + partTicks > start ? 1F : (ticks + partTicks) / start;

			RenderSystem.disableAlphaTest();
			RenderSystem.enableBlend();
			RenderSystem.enableRescaleNormal();
			RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			RenderSystem.color4f(1F, 1F, 1F, alpha);
			int xp = x + (int) (16F * (1F - alpha));
			RenderSystem.translatef(xp, y, 0F);
			RenderSystem.scalef(alpha, 1F, 1F);
			mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
			RenderSystem.scalef(1F / alpha,1F, 1F);
			RenderSystem.translatef(-xp, -y, 0F);
			RenderSystem.color4f(1F, 1F, 1F, 1F);
			RenderSystem.enableBlend();

			String text = "";

			if(customString == null) {
				if(!stack.isEmpty()) {
					text = TextFormatting.GREEN + stack.getDisplayName().getString();
					if(count >= 0) {
						int max = stack.getMaxStackSize();
						int stacks = count / max;
						int rem = count % max;

						if(stacks == 0)
							text = "" + count;
						else text = count + " (" + TextFormatting.AQUA + stacks + TextFormatting.RESET + "*" + TextFormatting.GRAY + max + TextFormatting.RESET + "+" + TextFormatting.YELLOW + rem + TextFormatting.RESET + ")";
					} else if(count == -1)
						text = "\u221E";
				}
			} else text = customString;

			int color = 0x00FFFFFF | (int) (alpha * 0xFF) << 24;
			mc.fontRenderer.drawStringWithShadow(text, x + 20, y + 6, color);

			RenderSystem.disableBlend();
			RenderSystem.enableAlphaTest();
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static void tick() {
		if(ticks > 0)
			--ticks;
	}

	public static void set(ItemStack stack, String str) {
		set(stack, 0, str);
	}

	public static void set(ItemStack stack, int count) {
		set(stack, count, null);
	}

	public static void set(ItemStack stack, int count, String str) {
		ItemsRemainingRenderHandler.stack = stack;
		ItemsRemainingRenderHandler.count = count;
		ItemsRemainingRenderHandler.customString = str;
		ticks = stack.isEmpty() ? 0 : maxTicks;
	}

	public static void set(PlayerEntity player, ItemStack displayStack, Pattern pattern) {
		int count = 0;
		for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if(!stack.isEmpty() && pattern.matcher(stack.getTranslationKey()).find())
				count += stack.getCount();
		}

		set(displayStack, count);
	}

}
