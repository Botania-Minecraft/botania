/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jul 12, 2014, 7:59:00 PM (GMT)]
 */
package vazkii.botania.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.lib.LibMisc;

import javax.annotation.Nonnull;
import java.util.List;

public class EntityMagicLandmine extends Entity {
	@ObjectHolder(LibMisc.MOD_ID + ":magic_landmine")
	public static EntityType<EntityMagicLandmine> TYPE;

	public EntityDoppleganger summoner;

	public EntityMagicLandmine(EntityType<EntityMagicLandmine> type, World world) {
		super(type, world);
	}

	public EntityMagicLandmine(World world) {
		this(TYPE, world);
	}

	@Override
	public void tick() {
		setMotion(Vec3d.ZERO);
		super.tick();

		float range = 2.5F;

		float r = 0.2F;
		float g = 0F;
		float b = 0.2F;

		//Botania.proxy.wispFX(world, getX(), getY(), getZ(), r, g, b, 0.6F, -0.2F, 1);
		for(int i = 0; i < 6; i++) {
            WispParticleData data = WispParticleData.wisp(0.4F, r, g, b, (float) 1);
            world.addParticle(data, getX() - range + Math.random() * range * 2, getY(), getZ() - range + Math.random() * range * 2, 0, - -0.015F, 0);
        }

		if(ticksExisted >= 55) {
			world.playSound(null, getX(), getY(), getZ(), ModSounds.gaiaTrap, SoundCategory.NEUTRAL, 0.3F, 1F);

			float m = 0.35F;
			g = 0.4F;
			for(int i = 0; i < 25; i++) {
				WispParticleData data = WispParticleData.wisp(0.5F, r, g, b);
				world.addParticle(data, getX(), getY() + 1, getZ(), (float) (Math.random() - 0.5F) * m, (float) (Math.random() - 0.5F) * m, (float) (Math.random() - 0.5F) * m);
			}

			if(!world.isRemote) {
				List<PlayerEntity> players = world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(getX() - range, getY() - range, getZ() - range, getX() + range, getY() + range, getZ() + range));
				for(PlayerEntity player : players) {
					player.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, summoner), 10);
					player.addPotionEffect(new EffectInstance(Effects.BLINDNESS, 25, 0));
					EffectInstance wither = new EffectInstance(Effects.WITHER, 120, 2);
					wither.getCurativeItems().clear();
					player.addPotionEffect(wither);
				}
			}

			remove();
		}
	}

	@Override
	protected void registerData() {
	}

	@Override
	protected void readAdditional(@Nonnull CompoundNBT var1) {
	}

	@Override
	protected void writeAdditional(@Nonnull CompoundNBT var1) {
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
