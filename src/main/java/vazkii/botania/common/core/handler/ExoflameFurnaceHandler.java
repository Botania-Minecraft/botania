/**
 * This class was created by <Hubry>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Dec 23, 2019, 15:42]
 */
package vazkii.botania.common.core.handler;

import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.IExoflameHeatable;
import vazkii.botania.common.Botania;
import vazkii.botania.common.capability.SimpleCapProvider;
import vazkii.botania.common.lib.LibMisc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = LibMisc.MOD_ID)
public class ExoflameFurnaceHandler {

	public static final ResourceLocation ID = new ResourceLocation(LibMisc.MOD_ID, "exoflame_heatable");

	private static final MethodHandle CAN_SMELT;
	private static final MethodHandle RECIPE_TYPE;

	static {
		try {
			Field recipeType = ObfuscationReflectionHelper.findField(AbstractFurnaceTileEntity.class, "field_214014_c");
			RECIPE_TYPE = MethodHandles.lookup().unreflectGetter(recipeType);
			Method canSmelt = ObfuscationReflectionHelper.findMethod(AbstractFurnaceTileEntity.class, "func_214008_b", IRecipe.class);
			CAN_SMELT = MethodHandles.lookup().unreflect(canSmelt);
		} catch (Exception e) {
			throw new RuntimeException("Failed to reflect furnace");
		}
	}

	@SubscribeEvent
	public static void attachFurnaceCapability(AttachCapabilitiesEvent<TileEntity> event) {
		TileEntity te = event.getObject();
		if(te instanceof AbstractFurnaceTileEntity) {
			AbstractFurnaceTileEntity furnace = (AbstractFurnaceTileEntity) te;
			SimpleCapProvider.attach(event, ID, BotaniaAPI.EXOFLAME_HEATABLE_CAP, new FurnaceExoflameHeatable(furnace));
		}
	}

	public static boolean canSmelt(AbstractFurnaceTileEntity furnace, IRecipe<?> recipe) throws Throwable {
		return (boolean) CAN_SMELT.invokeExact((AbstractFurnaceTileEntity) furnace, recipe);
	}

	@SuppressWarnings("unchecked")
	public static IRecipeType<? extends AbstractCookingRecipe> getRecipeType(AbstractFurnaceTileEntity furnace) throws Throwable {
		return (IRecipeType<? extends AbstractCookingRecipe>) RECIPE_TYPE.invokeExact(furnace);
	}

	private static class FurnaceExoflameHeatable implements IExoflameHeatable {
		private final AbstractFurnaceTileEntity furnace;

		private IRecipeType<? extends AbstractCookingRecipe> recipeType;
		private AbstractCookingRecipe currentRecipe;

		FurnaceExoflameHeatable(AbstractFurnaceTileEntity furnace) {
			this.furnace = furnace;
		}

		@Override
		public boolean canSmelt() {
			if(furnace.getStackInSlot(0).isEmpty()) {
				return false;
			}
			try {
				if (recipeType == null) {
					this.recipeType = ExoflameFurnaceHandler.getRecipeType(furnace);
				}
				if (currentRecipe != null) { // This is already more caching than Mojang does
					if(currentRecipe.matches(furnace, furnace.getWorld())
							&& ExoflameFurnaceHandler.canSmelt(furnace, currentRecipe)) {
						return true;
					}
				}
				currentRecipe = furnace.getWorld().getRecipeManager().getRecipe(recipeType, furnace, furnace.getWorld()).orElse(null);
				return ExoflameFurnaceHandler.canSmelt(furnace, currentRecipe);
			} catch (Throwable t) {
				Botania.LOGGER.error("Failed to determine if furnace TE can smelt", t);
				return false;
			}
		}

		@Override
		public int getBurnTime() {
			return furnace.burnTime;
		}

		@Override
		public void boostBurnTime() {
			if(getBurnTime() == 0) {
				World world = furnace.getWorld();
				BlockPos pos = furnace.getPos();
				world.setBlockState(pos, world.getBlockState(pos).with(BlockStateProperties.LIT, true));
			}
			furnace.burnTime += 200;
		}

		@Override
		public void boostCookTime() {
			furnace.cookTime = Math.min(currentRecipe.getCookTime() - 1, furnace.cookTime + 1);
		}
	}
}
