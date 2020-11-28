/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.SpawnHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.block.subtile.generating.SubTileNarslimmus;
import vazkii.botania.common.brew.potion.PotionEmptiness;

@Mixin(SpawnHelper.class)
public class MixinSpawnHelper {
	/**
	 * Adds the naturally-spawned tag to slimes
	 */
	@ModifyArg(
		at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V"),
		method = "spawnEntitiesInChunk(Lnet/minecraft/entity/SpawnGroup;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/SpawnHelper$Checker;Lnet/minecraft/world/SpawnHelper$Runner;)V"
	)
	private static Entity onSpawned(Entity entity) {
		SubTileNarslimmus.onSpawn(entity);
		return entity;
	}

	/**
	 * Prevents spawning when near emptiness users
	 */
	@Inject(at = @At("HEAD"), method = "isValidSpawn", cancellable = true)
	private static void emptiness(ServerWorld world, MobEntity entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
		if (PotionEmptiness.shouldCancel(entity)) {
			cir.setReturnValue(false);
		}
	}
}
