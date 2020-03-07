/**
 * This class was created by <Hubry>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [2020-01-14, 01:26 (UTC+2)]
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.client.util.Splashes;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.Unit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.lib.LibMisc;

import javax.annotation.Nonnull;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = LibMisc.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SplashHandler {
	// This is probably the best spot to add a splash listener. It's an event fired after splash reload listener 
	// is registered, but before resource reload happens. Doing the initial load ourselves is not effective,
	// as we would do it before the splashes are added, and the list is cleared when adding.
	@SubscribeEvent
	public static void registerFactories(ParticleFactoryRegisterEvent event) {
		((SimpleReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(new SplashResourceListener());
	}

	private static class SplashResourceListener extends ReloadListener<Unit> {
		@Override
		@Nonnull
		protected Unit prepare(@Nonnull IResourceManager resourceManager, @Nonnull IProfiler profiler) {
			return Unit.INSTANCE;
		}

		@Override
		protected void apply(@Nonnull Unit unused, @Nonnull IResourceManager resourceManager, @Nonnull IProfiler profiler) {
			if (!ConfigHandler.CLIENT.splashesEnabled.get())
				return;

			Splashes splashes = Minecraft.getInstance().getSplashes();
			List<String> splashList = ObfuscationReflectionHelper.getPrivateValue(Splashes.class, splashes, "field_215280_c"); //possibleSplashes
			splashList.add("Do not feed bread to elves!");
		}
	}
}
