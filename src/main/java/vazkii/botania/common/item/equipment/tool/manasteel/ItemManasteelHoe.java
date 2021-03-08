/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool.manasteel;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import javax.annotation.Nonnull;

import java.util.function.Consumer;

public class ItemManasteelHoe extends HoeItem implements IManaUsingItem {
	private static final int MANA_PER_DAMAGE = 60;
	private static long tiltUntil = -1;

	public ItemManasteelHoe(Properties props) {
		this(BotaniaAPI.instance().getManasteelItemTier(), props, -1f);
	}

	public ItemManasteelHoe(IItemTier mat, Properties properties, float attackSpeed) {
		super(mat, (int) -mat.getAttackDamage(), attackSpeed, properties);
	}

	@Nonnull
	@Override
	public ActionResultType onItemUse(@Nonnull ItemUseContext context) {
		ActionResultType result = super.onItemUse(context);
		if (context.getWorld().isRemote && ClientProxy.hoeTilts && result.isSuccessOrConsume()) {
			tiltUntil = ClientTickHandler.ticksInGame + 10;
		}
		return result;
	}

	public static boolean shouldTilt() {
		return ClientTickHandler.ticksInGame <= tiltUntil;
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
		return ToolCommons.damageItemIfPossible(stack, amount, entity, getManaPerDamage());
	}

	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity player, int slot, boolean selected) {
		if (!world.isRemote && player instanceof PlayerEntity && stack.getDamage() > 0 && ManaItemHandler.instance().requestManaExactForTool(stack, (PlayerEntity) player, getManaPerDamage() * 2, true)) {
			stack.setDamage(stack.getDamage() - 1);
		}
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}
}
