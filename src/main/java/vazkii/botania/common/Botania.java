/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common;

import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.mana.ManaNetworkCallback;
import vazkii.botania.client.fx.ModParticles;
import vazkii.botania.common.advancements.*;
import vazkii.botania.common.block.ModBanners;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.ModFluffBlocks;
import vazkii.botania.common.block.ModSubtiles;
import vazkii.botania.common.block.string.BlockRedStringInterceptor;
import vazkii.botania.common.block.tile.ModTiles;
import vazkii.botania.common.block.tile.TileAlfPortal;
import vazkii.botania.common.block.tile.TileEnchanter;
import vazkii.botania.common.block.tile.TileTerraPlate;
import vazkii.botania.common.block.tile.corporea.TileCorporeaIndex;
import vazkii.botania.common.brew.ModBrews;
import vazkii.botania.common.brew.ModPotions;
import vazkii.botania.common.core.ModStats;
import vazkii.botania.common.core.command.SkyblockCommand;
import vazkii.botania.common.core.handler.*;
import vazkii.botania.common.core.loot.LootHandler;
import vazkii.botania.common.core.loot.ModLootModifiers;
import vazkii.botania.common.core.proxy.IProxy;
import vazkii.botania.common.crafting.ModRecipeTypes;
import vazkii.botania.common.entity.ModEntities;
import vazkii.botania.common.impl.BotaniaAPIImpl;
import vazkii.botania.common.impl.corporea.CorporeaItemStackMatcher;
import vazkii.botania.common.impl.corporea.CorporeaStringMatcher;
import vazkii.botania.common.item.ItemGrassSeeds;
import vazkii.botania.common.item.ItemKeepIvy;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.bauble.ItemFlightTiara;
import vazkii.botania.common.item.material.ItemEnderAir;
import vazkii.botania.common.item.relic.ItemLokiRing;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.common.network.PacketHandler;
import vazkii.botania.common.world.ModFeatures;
import vazkii.botania.common.world.SkyblockChunkGenerator;
import vazkii.botania.common.world.SkyblockWorldEvents;
import vazkii.botania.data.DataGenerators;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.IStateMatcher;
import vazkii.patchouli.api.PatchouliAPI;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class Botania implements ModInitializer {

	public static boolean gardenOfGlassLoaded = false;

	public static boolean trinketsLoaded = false;

	public static IProxy proxy = new IProxy() {};
	public static volatile boolean configLoaded = false;

	public static final Logger LOGGER = LogManager.getLogger(LibMisc.MOD_ID);

	@Override
	public void onInitialize() {
		gardenOfGlassLoaded = FabricLoader.getInstance().isModLoaded(LibMisc.GOG_MOD_ID);
		trinketsLoaded = FabricLoader.getInstance().isModLoaded("trinkets");
		ConfigHandler.setup();

		EquipmentHandler.init();
		ModFeatures.registerFeatures();
		ModBanners.init();
		ModItems.registerItems();
		ModItems.registerRecipeSerializers();
		ModEntities.registerEntities();
		ModRecipeTypes.registerRecipeTypes();
		ModSounds.init();
		ModBrews.registerBrews();
		ModPotions.registerPotions();
		ModBlocks.registerBlocks();
		ModBlocks.registerItemBlocks();
		ModTiles.registerTiles();
		ModFluffBlocks.registerBlocks();
		ModFluffBlocks.registerItemBlocks();
		ModParticles.registerParticles();
		ModSubtiles.registerBlocks();
		ModSubtiles.registerItemBlocks();
		ModSubtiles.registerTEs();
		PixieHandler.registerAttribute();

		commonSetup();
		ServerLifecycleEvents.SERVER_STARTED.register(this::serverAboutToStart);
		CommandRegistrationCallback.EVENT.register(this::registerCommands);
		ServerLifecycleEvents.SERVER_STOPPING.register(this::serverStopping);
		UseBlockCallback.EVENT.register(ItemLokiRing::onPlayerInteract);
		UseItemCallback.EVENT.register(ItemEnderAir::onPlayerInteract);
		ServerTickEvents.END_WORLD_TICK.register(ItemGrassSeeds::onTickEnd);
		ServerPlayerEvents.AFTER_RESPAWN.register(ItemKeepIvy::onPlayerRespawn);
		ServerTickEvents.END_WORLD_TICK.register(CommonTickHandler::onTick);
		UseBlockCallback.EVENT.register(BlockRedStringInterceptor::onInteract);
		ManaNetworkCallback.EVENT.register(ManaNetworkHandler.instance::onNetworkEvent);
		LootTableLoadingCallback.EVENT.register(LootHandler::lootLoad);
		ServerPlayConnectionEvents.DISCONNECT.register(ItemFlightTiara::playerLoggedOut);
		OrechidResourceListener.registerListener();

		ModLootModifiers.init();
		ModCriteriaTriggers.init();
	}

	private void commonSetup() {
		PacketHandler.init();
		PaintableData.init();

		CorporeaHelper.instance().registerRequestMatcher(prefix("string"), CorporeaStringMatcher.class, CorporeaStringMatcher::createFromNBT);
		CorporeaHelper.instance().registerRequestMatcher(prefix("item_stack"), CorporeaItemStackMatcher.class, CorporeaItemStackMatcher::createFromNBT);

		if (Botania.gardenOfGlassLoaded) {
			UseBlockCallback.EVENT.register(SkyblockWorldEvents::onPlayerInteract);
		}

		SkyblockChunkGenerator.init();

		ModEntities.registerAttributes();

		PatchouliAPI.get().registerMultiblock(Registry.BLOCK.getKey(ModBlocks.alfPortal), TileAlfPortal.MULTIBLOCK.get());
		PatchouliAPI.get().registerMultiblock(Registry.BLOCK.getKey(ModBlocks.terraPlate), TileTerraPlate.MULTIBLOCK.get());
		PatchouliAPI.get().registerMultiblock(Registry.BLOCK.getKey(ModBlocks.enchanter), TileEnchanter.MULTIBLOCK.get());

		String[][] pat = new String[][] {
				{
						"P_______P",
						"_________",
						"_________",
						"_________",
						"_________",
						"_________",
						"_________",
						"_________",
						"P_______P",
				},
				{
						"_________",
						"_________",
						"_________",
						"_________",
						"____B____",
						"_________",
						"_________",
						"_________",
						"_________",
				},
				{
						"_________",
						"_________",
						"_________",
						"___III___",
						"___I0I___",
						"___III___",
						"_________",
						"_________",
						"_________",
				}
		};
		IStateMatcher sm = PatchouliAPI.get().predicateMatcher(Blocks.IRON_BLOCK,
				state -> state.is(BlockTags.BEACON_BASE_BLOCKS));
		IMultiblock mb = PatchouliAPI.get().makeMultiblock(
				pat,
				'P', ModBlocks.gaiaPylon,
				'B', Blocks.BEACON,
				'I', sm,
				'0', sm
		);
		PatchouliAPI.get().registerMultiblock(prefix("gaia_ritual"), mb);

		ModBlocks.addDispenserBehaviours();

		ModStats.init();
	}

	private void serverAboutToStart(MinecraftServer server) {
		if (BotaniaAPI.instance().getClass() != BotaniaAPIImpl.class) {
			String clname = BotaniaAPI.instance().getClass().getName();
			throw new IllegalAccessError("The Botania API has been overriden. "
					+ "This will cause crashes and compatibility issues, and that's why it's marked as"
					+ " \"Do not Override\". Whoever had the brilliant idea of overriding it needs to go"
					+ " back to elementary school and learn to read. (Actual classname: " + clname + ")");
		}

		if (server.isDedicatedServer()) {
			ContributorList.firstStart();
		}
	}

	private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
		if (Botania.gardenOfGlassLoaded) {
			SkyblockCommand.register(dispatcher);
		}
		DataGenerators.registerCommands(dispatcher);
	}

	private void serverStopping(MinecraftServer server) {
		ManaNetworkHandler.instance.clear();
		TileCorporeaIndex.clearIndexCache();
	}

}
