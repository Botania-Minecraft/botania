/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 24, 2015, 4:37:33 PM (GMT)]
 */
package vazkii.botania.common.item.lens;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import vazkii.botania.api.internal.IManaBurst;

import java.util.List;

public class LensDamage extends Lens {

	@Override
	public void updateBurst(IManaBurst burst, ThrowableEntity entity, ItemStack stack) {
		if (entity.world.isRemote)
			return;
		AxisAlignedBB axis = new AxisAlignedBB(entity.getX(), entity.getY(), entity.getZ(), entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).grow(1);
		List<LivingEntity> entities = entity.world.getEntitiesWithinAABB(LivingEntity.class, axis);
		for(LivingEntity living : entities) {
			if(living instanceof PlayerEntity)
				continue;

			if(living.hurtTime == 0) {
				int mana = burst.getMana();
				if(mana >= 16) {
					burst.setMana(mana - 16);
					if(!burst.isFake()) {
						DamageSource src = entity.getThrower() != null
								? DamageSource.causeIndirectMagicDamage(entity, entity.getThrower())
								: DamageSource.MAGIC;
						living.attackEntityFrom(src, 8);
					}
					break;
				}
			}
		}
	}

}
