/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 13, 2014, 7:46:05 PM (GMT)]
 */
package vazkii.botania.client.core.proxy;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.TallFlowerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import vazkii.botania.api.boss.IBotaniaBoss;
import vazkii.botania.client.core.handler.BaubleRenderHandler;
import vazkii.botania.client.core.handler.BossBarHandler;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.ColorHandler;
import vazkii.botania.client.core.handler.ContributorFancinessHandler;
import vazkii.botania.client.core.handler.LayerTerraHelmet;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.handler.PersistentVariableHelper;
import vazkii.botania.client.core.helper.ShaderHelper;
import vazkii.botania.client.fx.FXLightning;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.BlockModDoubleFlower;
import vazkii.botania.common.block.BlockModFlower;
import vazkii.botania.common.block.BlockSpecialFlower;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.ModFluffBlocks;
import vazkii.botania.common.block.decor.BlockFloatingFlower;
import vazkii.botania.common.block.decor.BlockModMushroom;
import vazkii.botania.common.block.decor.BlockShinyFlower;
import vazkii.botania.common.block.decor.panes.BlockModPane;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.core.proxy.IProxy;
import vazkii.botania.common.item.ItemSextant;
import vazkii.botania.common.item.equipment.bauble.ItemMonocle;
import vazkii.botania.common.lib.LibMisc;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.client.handler.MultiblockVisualizationHandler;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;

public class ClientProxy implements IProxy {

	public static final VertexFormat POSITION_TEX_LMAP_NORMAL =
			new VertexFormat(ImmutableList.<VertexFormatElement>builder()
					.add(DefaultVertexFormats.POSITION_3F)
					.add(DefaultVertexFormats.TEX_2F)
					.add(DefaultVertexFormats.LIGHT_ELEMENT)
					.add(DefaultVertexFormats.NORMAL_3B)
					.build());
	public static final VertexFormat POSITION_TEX_LMAP =
			new VertexFormat(ImmutableList.<VertexFormatElement>builder()
					.add(DefaultVertexFormats.POSITION_3F)
					.add(DefaultVertexFormats.TEX_2F)
					.add(DefaultVertexFormats.LIGHT_ELEMENT)
					.build());
	public static boolean jingleTheBells = false;
	public static boolean dootDoot = false;

	public static KeyBinding CORPOREA_REQUEST;

	@Override
	public void registerHandlers() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
		FMLJavaModLoadingContext.get().getModEventBus().register(MiscellaneousIcons.INSTANCE);
		// This is the only place it works, but mods are constructed in parallel (brilliant idea) so this
		// *could* end up blowing up if it races with someone else. Let's pray that doesn't happen.
		ShaderHelper.initShaders();
	}

	private void clientSetup(FMLClientSetupEvent event) {
		PersistentVariableHelper.setCacheFile(new File(Minecraft.getInstance().gameDir, "BotaniaVars.dat"));
		try {
			PersistentVariableHelper.load();
			PersistentVariableHelper.save();
		} catch (IOException e) {
			Botania.LOGGER.fatal("Persistent Variables couldn't load!!");
		}

		if(ConfigHandler.CLIENT.enableSeasonalFeatures.get()) {
			LocalDateTime now = LocalDateTime.now();
			if (now.getMonth() == Month.DECEMBER && now.getDayOfMonth() >= 16 || now.getMonth() == Month.JANUARY && now.getDayOfMonth() <= 2)
				jingleTheBells = true;
			if(now.getMonth() == Month.OCTOBER)
				dootDoot = true;
		}

		DeferredWorkQueue.runLater(() -> {
			CORPOREA_REQUEST = new KeyBinding("key.botania_corporea_request", KeyConflictContext.GUI, InputMappings.getInputByCode(GLFW.GLFW_KEY_C, 0), LibMisc.MOD_NAME);
			ClientRegistry.registerKeyBinding(ClientProxy.CORPOREA_REQUEST);
		});

		registerRenderTypes();
	}

	private static void registerRenderTypes() {
		RenderTypeLookup.setRenderLayer(ModBlocks.defaultAltar, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.forestAltar, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.plainsAltar, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.mountainAltar, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.fungalAltar, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.swampAltar, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.desertAltar, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.taigaAltar, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.mesaAltar, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.mossyAltar, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.ghostRail, RenderType.getCutout());
		RenderTypeLookup.setRenderLayer(ModBlocks.solidVines, RenderType.getCutout());

		RenderTypeLookup.setRenderLayer(ModBlocks.manaGlass, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(ModFluffBlocks.managlassPane, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(ModBlocks.elfGlass, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(ModFluffBlocks.alfglassPane, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(ModBlocks.bifrost, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(ModFluffBlocks.bifrostPane, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(ModBlocks.bifrostPerm, RenderType.getTranslucent());
		RenderTypeLookup.setRenderLayer(ModBlocks.prism, RenderType.getTranslucent());

		RenderTypeLookup.setRenderLayer(ModBlocks.starfield, RenderType.getCutoutMipped());
		RenderTypeLookup.setRenderLayer(ModBlocks.abstrusePlatform, t -> true);
		RenderTypeLookup.setRenderLayer(ModBlocks.infrangiblePlatform, t -> true);
		RenderTypeLookup.setRenderLayer(ModBlocks.spectralPlatform, t -> true);

		Registry.BLOCK.stream().filter(b -> b.getRegistryName().getNamespace().equals(LibMisc.MOD_ID))
				.forEach(b -> {
					if (b instanceof BlockFloatingFlower || b instanceof FlowerBlock
							|| b instanceof TallFlowerBlock || b instanceof BlockModMushroom)
						RenderTypeLookup.setRenderLayer(b, RenderType.getCutout());
				});
	}

	private void loadComplete(FMLLoadCompleteEvent event) {
	    DeferredWorkQueue.runLater(() -> {
			initAuxiliaryRender();
			ColorHandler.init();
		});
	}

	private void initAuxiliaryRender() {
		Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();
		PlayerRenderer render;
		render = skinMap.get("default");
		render.addLayer(new ContributorFancinessHandler(render));
		if(Botania.curiosLoaded)
			render.addLayer(new BaubleRenderHandler(render));
		render.addLayer(new LayerTerraHelmet(render));

		render = skinMap.get("slim");
		render.addLayer(new ContributorFancinessHandler(render));
		if(Botania.curiosLoaded)
			render.addLayer(new BaubleRenderHandler(render));
		render.addLayer(new LayerTerraHelmet(render));
	}

	@Override
	public boolean isTheClientPlayer(LivingEntity entity) {
		return entity == Minecraft.getInstance().player;
	}

	@Override
	public PlayerEntity getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	@Override
	public boolean isClientPlayerWearingMonocle() {
		return ItemMonocle.hasMonocle(Minecraft.getInstance().player);
	}

	@Override
	public long getWorldElapsedTicks() {
		return ClientTickHandler.ticksInGame;
	}

	@Override
	public void lightningFX(Vector3 vectorStart, Vector3 vectorEnd, float ticksPerMeter, long seed, int colorOuter, int colorInner) {
		Minecraft.getInstance().particles.addEffect(new FXLightning(Minecraft.getInstance().world, vectorStart, vectorEnd, ticksPerMeter, seed, colorOuter, colorInner));
	}

	@Override
	public void addBoss(IBotaniaBoss boss) {
		BossBarHandler.bosses.add(boss);
	}

	@Override
	public void removeBoss(IBotaniaBoss boss) {
		BossBarHandler.bosses.remove(boss);
	}

	@Override
	public int getClientRenderDistance() {
		return Minecraft.getInstance().gameSettings.renderDistanceChunks;
	}

	@Override
	public void addParticleForce(World world, IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
		world.addParticle(particleData, true, x, y, z, xSpeed, ySpeed, zSpeed);
	}

	@Override
	public void showMultiblock(IMultiblock mb, String name, BlockPos anchor, Rotation rot) {
		MultiblockVisualizationHandler.setMultiblock(mb, name, null, false);
		MultiblockVisualizationHandler.anchorTo(anchor, Rotation.NONE);
	}

	@Override
	public void clearSextantMultiblock() {
		if (MultiblockVisualizationHandler.hasMultiblock
				&& MultiblockVisualizationHandler.getMultiblock() != null
				&& MultiblockVisualizationHandler.getMultiblock().getID().equals(ItemSextant.MULTIBLOCK_ID))
			MultiblockVisualizationHandler.setMultiblock(null, null, null, true);
	}
}

