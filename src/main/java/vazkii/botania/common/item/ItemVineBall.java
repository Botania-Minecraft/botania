/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jun 26, 2014, 7:50:37 PM (GMT)]
 */
package vazkii.botania.common.item;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import vazkii.botania.common.entity.EntityVineBall;
import vazkii.botania.common.lib.LibItemNames;

public class ItemVineBall extends ItemMod {

	public ItemVineBall() {
		super(LibItemNames.VINE_BALL);
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(@Nonnull ItemStack par1ItemStack, World world, EntityPlayer player, EnumHand hand) {
		if(!player.capabilities.isCreativeMode)
			--par1ItemStack.stackSize;

		world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

		if(!world.isRemote) {
			EntityVineBall ball = new EntityVineBall(player, true);
			ball.setHeadingFromThrower(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
			world.spawnEntityInWorld(ball);
		}

		return ActionResult.newResult(EnumActionResult.SUCCESS, par1ItemStack);
	}

}
