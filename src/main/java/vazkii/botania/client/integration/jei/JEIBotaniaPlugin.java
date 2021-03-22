/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.recipe.IElvenTradeRecipe;
import vazkii.botania.client.core.handler.CorporeaInputHandler;
import vazkii.botania.client.gui.crafting.ContainerCraftingHalo;
import vazkii.botania.client.integration.jei.crafting.AncientWillRecipeWrapper;
import vazkii.botania.client.integration.jei.crafting.CompositeLensRecipeWrapper;
import vazkii.botania.client.integration.jei.crafting.TerraPickTippingRecipeWrapper;
import vazkii.botania.client.integration.jei.orechid.OrechidIgnemRecipeCategory;
import vazkii.botania.client.integration.jei.orechid.OrechidRecipeCategory;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.ModSubtiles;
import vazkii.botania.common.block.tile.TileAlfPortal;
import vazkii.botania.common.block.tile.mana.TilePool;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.crafting.LexiconElvenTradeRecipe;
import vazkii.botania.common.crafting.ModRecipeTypes;
import vazkii.botania.common.crafting.recipe.AncientWillRecipe;
import vazkii.botania.common.crafting.recipe.CompositeLensRecipe;
import vazkii.botania.common.crafting.recipe.TerraPickTippingRecipe;
import vazkii.botania.common.item.ItemLaputaShard;
import vazkii.botania.common.item.ItemLexicon;
import vazkii.botania.common.item.ItemManaTablet;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.brew.ItemBrewBase;
import vazkii.botania.common.item.equipment.bauble.ItemFlightTiara;
import vazkii.botania.common.item.equipment.tool.terrasteel.ItemTerraPick;

import javax.annotation.Nonnull;

import java.util.Arrays;
import java.util.List;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

@JeiPlugin
public class JEIBotaniaPlugin implements IModPlugin {
	private static final ResourceLocation ID = prefix("main");

	@Override
	public void registerItemSubtypes(@Nonnull ISubtypeRegistration registry) {
		registry.registerSubtypeInterpreter(ModItems.brewVial, ItemBrewBase::getSubtype);
		registry.registerSubtypeInterpreter(ModItems.brewFlask, ItemBrewBase::getSubtype);
		registry.registerSubtypeInterpreter(ModItems.incenseStick, ItemBrewBase::getSubtype);
		registry.registerSubtypeInterpreter(ModItems.bloodPendant, ItemBrewBase::getSubtype);

		registry.registerSubtypeInterpreter(ModItems.flightTiara, stack -> String.valueOf(ItemFlightTiara.getVariant(stack)));
		registry.registerSubtypeInterpreter(ModItems.lexicon, stack -> String.valueOf(ItemNBTHelper.getBoolean(stack, ItemLexicon.TAG_ELVEN_UNLOCK, false)));
		registry.registerSubtypeInterpreter(ModItems.laputaShard, stack -> String.valueOf(ItemLaputaShard.getShardLevel(stack)));

		registry.registerSubtypeInterpreter(ModItems.terraPick, stack -> String.valueOf(ItemTerraPick.getLevel(stack)) + ItemTerraPick.isTipped(stack));
		registry.registerSubtypeInterpreter(ModItems.manaTablet, stack -> String.valueOf(((IManaItem) ModItems.manaTablet).getMana(stack)) + ItemManaTablet.isStackCreative(stack));

		for (Item item : new Item[] { ModItems.manaRing, ModItems.manaRingGreater }) {
			registry.registerSubtypeInterpreter(item, stack -> String.valueOf(((IManaItem) item).getMana(stack)));
		}
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		registry.addRecipeCategories(
				new PureDaisyRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
				new ManaPoolRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
				new PetalApothecaryRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
				new RunicAltarRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
				new ElvenTradeRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
				new BreweryRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
				new OrechidRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
				new OrechidIgnemRecipeCategory(registry.getJeiHelpers().getGuiHelper()),
				new TerraPlateRecipeCategory(registry.getJeiHelpers().getGuiHelper())
		);
	}

	public static boolean doesOreExist(ResourceLocation tagId) {
		ITag<Block> tag = BlockTags.getCollection().get(tagId);
		return tag != null && !tag.getAllElements().isEmpty();
	}

	@Override
	public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
		registration.getCraftingCategory().addCategoryExtension(AncientWillRecipe.class, AncientWillRecipeWrapper::new);
		registration.getCraftingCategory().addCategoryExtension(TerraPickTippingRecipe.class, TerraPickTippingRecipeWrapper::new);
		registration.getCraftingCategory().addCategoryExtension(CompositeLensRecipe.class, CompositeLensRecipeWrapper::new);
	}

	@Override
	public void registerRecipes(@Nonnull IRecipeRegistration registry) {
		World world = Minecraft.getInstance().world;
		registry.addRecipes(ModRecipeTypes.getRecipes(world, ModRecipeTypes.BREW_TYPE).values(), BreweryRecipeCategory.UID);
		registry.addRecipes(ModRecipeTypes.getRecipes(world, ModRecipeTypes.PURE_DAISY_TYPE).values(), PureDaisyRecipeCategory.UID);
		registry.addRecipes(ModRecipeTypes.getRecipes(world, ModRecipeTypes.PETAL_TYPE).values(), PetalApothecaryRecipeCategory.UID);
		registry.addRecipes(ModRecipeTypes.getRecipes(world, ModRecipeTypes.ELVEN_TRADE_TYPE).values(), ElvenTradeRecipeCategory.UID);
		registry.addRecipes(ModRecipeTypes.getRecipes(world, ModRecipeTypes.RUNE_TYPE).values(), RunicAltarRecipeCategory.UID);
		registry.addRecipes(TilePool.manaInfusionRecipes(Minecraft.getInstance().world), ManaPoolRecipeCategory.UID);
		registry.addRecipes(ModRecipeTypes.getRecipes(world, ModRecipeTypes.TERRA_PLATE_TYPE).values(), TerraPlateRecipeCategory.UID);

		registry.addRecipes(BotaniaAPI.instance().getOrechidWeights(), OrechidRecipeCategory.UID);
		registry.addRecipes(BotaniaAPI.instance().getNetherOrechidWeights(), OrechidIgnemRecipeCategory.UID);
	}

	@Override
	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registry) {
		registry.addRecipeTransferHandler(ContainerCraftingHalo.class, VanillaRecipeCategoryUid.CRAFTING, 1, 9, 10, 36);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.brewery), BreweryRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.alfPortal), ElvenTradeRecipeCategory.UID);

		registry.addRecipeCatalyst(new ItemStack(ModBlocks.manaPool), ManaPoolRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.creativePool), ManaPoolRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.dilutedPool), ManaPoolRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.fabulousPool), ManaPoolRecipeCategory.UID);

		registry.addRecipeCatalyst(new ItemStack(ModBlocks.defaultAltar), PetalApothecaryRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.forestAltar), PetalApothecaryRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.plainsAltar), PetalApothecaryRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.mountainAltar), PetalApothecaryRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.fungalAltar), PetalApothecaryRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.swampAltar), PetalApothecaryRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.desertAltar), PetalApothecaryRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.taigaAltar), PetalApothecaryRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.mesaAltar), PetalApothecaryRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.mossyAltar), PetalApothecaryRecipeCategory.UID);

		registry.addRecipeCatalyst(new ItemStack(ModSubtiles.orechid), OrechidRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModSubtiles.orechidFloating), OrechidRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModSubtiles.orechidIgnem), OrechidIgnemRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModSubtiles.orechidIgnemFloating), OrechidIgnemRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModSubtiles.pureDaisy), PureDaisyRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModSubtiles.pureDaisyFloating), PureDaisyRecipeCategory.UID);

		registry.addRecipeCatalyst(new ItemStack(ModBlocks.runeAltar), RunicAltarRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModBlocks.terraPlate), TerraPlateRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(ModItems.autocraftingHalo), VanillaRecipeCategoryUid.CRAFTING);
		registry.addRecipeCatalyst(new ItemStack(ModItems.craftingHalo), VanillaRecipeCategoryUid.CRAFTING);
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		IRecipeManager recipeRegistry = jeiRuntime.getRecipeManager();
		// Hide the return recipes (iron ingot/diamond/ender pearl returns, not lexicon)
		for (IElvenTradeRecipe recipe : TileAlfPortal.elvenTradeRecipes(Minecraft.getInstance().world)) {
			if (recipe instanceof LexiconElvenTradeRecipe) {
				continue;
			}
			List<Ingredient> inputs = recipe.getIngredients();
			List<ItemStack> outputs = recipe.getOutputs();
			if (inputs.size() == 1 && outputs.size() == 1 && recipe.containsItem(outputs.get(0))) {
				recipeRegistry.hideRecipe(recipe, ElvenTradeRecipeCategory.UID);
			}
		}

		RecipeManager recipeManager = Minecraft.getInstance().world.getRecipeManager();
		recipeManager.getRecipe(prefix("petal_apothecary/daybloom_motif"))
				.ifPresent(r -> recipeRegistry.hideRecipe(r, PetalApothecaryRecipeCategory.UID));
		recipeManager.getRecipe(prefix("petal_apothecary/nightshade_motif"))
				.ifPresent(r -> recipeRegistry.hideRecipe(r, PetalApothecaryRecipeCategory.UID));

		jeiRuntime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, //TODO unhide labelia when ready
				Arrays.asList(new ItemStack(ModSubtiles.labelia), new ItemStack(ModSubtiles.labeliaFloating)));

		CorporeaInputHandler.jeiPanelSupplier = () -> {
			Object o = jeiRuntime.getIngredientListOverlay().getIngredientUnderMouse();

			if (o == null && Minecraft.getInstance().currentScreen == jeiRuntime.getRecipesGui()) {
				o = jeiRuntime.getRecipesGui().getIngredientUnderMouse();
			}

			if (o == null) {
				o = jeiRuntime.getBookmarkOverlay().getIngredientUnderMouse();
			}

			if (o instanceof ItemStack) {
				return (ItemStack) o;
			}
			return ItemStack.EMPTY;
		};

		CorporeaInputHandler.supportedGuiFilter = gui -> gui instanceof ContainerScreen || gui instanceof IRecipesGui;
	}

	@Nonnull
	@Override
	public ResourceLocation getPluginUid() {
		return ID;
	}
}
