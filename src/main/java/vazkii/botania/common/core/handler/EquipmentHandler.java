/**
 * This class was created by <Hubry>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [May 30 2019, 16:45:42 (GMT)]
 */

package vazkii.botania.common.core.handler;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;
import vazkii.botania.common.Botania;
import vazkii.botania.common.integration.curios.CurioIntegration;
import vazkii.botania.common.item.equipment.bauble.ItemBauble;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public abstract class EquipmentHandler {
	private static EquipmentHandler instance;

	public static void init() { //todo maybe support baubles when/if that gets released
		if(Botania.curiosLoaded) {
			instance = new CurioIntegration();
			FMLJavaModLoadingContext.get().getModEventBus().register(CurioIntegration.class);
			MinecraftForge.EVENT_BUS.register(CurioIntegration.class);
		} else {
			InventoryEquipmentHandler handler = new InventoryEquipmentHandler();
			instance = handler;
			MinecraftForge.EVENT_BUS.addListener(handler::onPlayerTick);
		}
	}

	public static LazyOptional<IItemHandlerModifiable> getAllWorn(EntityLivingBase living) {
		return instance.getAllWornItems(living);
	}

	public static ItemStack findOrEmpty(Item item, EntityLivingBase living) {
		return instance.findItem(item, living);
	}

	public static ItemStack findOrEmpty(Predicate<ItemStack> pred, EntityLivingBase living) {
		return instance.findItem(pred, living);
	}

	public static ICapabilityProvider initBaubleCap(ItemStack stack) {
		if(instance != null) // Happens to be called in ModItems class init, which is too early to know about which handler to use
			return instance.initCap(stack);
		return null;
	}

	protected abstract LazyOptional<IItemHandlerModifiable> getAllWornItems(EntityLivingBase living);

	protected abstract ItemStack findItem(Item item, EntityLivingBase living);

	protected abstract ItemStack findItem(Predicate<ItemStack> pred, EntityLivingBase living);

	protected abstract ICapabilityProvider initCap(ItemStack stack);

	// Fallback equipment handler for curios-less (or baubles-less) installs.
	static class InventoryEquipmentHandler extends EquipmentHandler {
		private final Map<EntityPlayer, ItemStack[]> map = new WeakHashMap<>();

		@SubscribeEvent
		public void onPlayerTick(TickEvent.PlayerTickEvent event) {
			if(event.phase != TickEvent.Phase.START || event.player.world.isRemote)
				return;
			EntityPlayer player = event.player;
			player.world.profiler.startSection("botania:tick_wearables");

			ItemStack[] oldStacks = map.computeIfAbsent(player, p -> {
				ItemStack[] array = new ItemStack[9];
				Arrays.fill(array, ItemStack.EMPTY);
				return array;
			});

			InventoryPlayer inv = player.inventory;
			for(int i = 0; i < 9; i++) {
				ItemStack old = oldStacks[i];
				ItemStack current = inv.getStackInSlot(i);

				if(!ItemStack.areItemStacksEqual(old, current)) {
					if(old.getItem() instanceof ItemBauble) {
						player.getAttributeMap().removeAttributeModifiers(((ItemBauble) old.getItem()).getEquippedAttributeModifiers(old));
						((ItemBauble) old.getItem()).onUnequipped(old, player);
					}
					if(canEquip(current, player)) {
						player.getAttributeMap().applyAttributeModifiers(((ItemBauble) current.getItem()).getEquippedAttributeModifiers(current));
						((ItemBauble) current.getItem()).onEquipped(current, player);
					}
					oldStacks[i] = current.copy(); // shift-clicking mutates the stack we stored,
					// making it empty and failing the equality check - let's avoid that
				}

				if(canEquip(current, player)) {
					((ItemBauble) current.getItem()).onWornTick(current, player);
				}
			}
			player.world.profiler.endSection();
		}


		@Override
		protected LazyOptional<IItemHandlerModifiable> getAllWornItems(EntityLivingBase living) {
			if(living instanceof EntityPlayer) {
				return LazyOptional.of(() -> new RangedWrapper(new InvWrapper(((EntityPlayer) living).inventory), 0, 9));
			}
			return LazyOptional.empty();
		}

		@Override
		protected ItemStack findItem(Item item, EntityLivingBase living) {
			if(living instanceof EntityPlayer) {
				InventoryPlayer inv = ((EntityPlayer) living).inventory;
				for(int i = 0; i < 9; i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if(stack.getItem() == item && canEquip(stack, living)) {
						return stack;
					}
				}
			}
			return ItemStack.EMPTY;
		}

		@Override
		protected ItemStack findItem(Predicate<ItemStack> pred, EntityLivingBase living) {
			if(living instanceof EntityPlayer) {
				InventoryPlayer inv = ((EntityPlayer) living).inventory;
				for(int i = 0; i < 9; i++) {
					ItemStack stack = inv.getStackInSlot(i);
					if(pred.test(stack) && canEquip(stack, living)) {
						return stack;
					}
				}
			}
			return ItemStack.EMPTY;
		}

		@Override
		protected ICapabilityProvider initCap(ItemStack stack) {
			return null;
		}

		private static boolean canEquip(ItemStack stack, EntityLivingBase player) {
			return stack.getItem() instanceof ItemBauble && ((ItemBauble) stack.getItem()).canEquip(stack, player);
		}
	}
}
