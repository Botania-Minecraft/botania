/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jul 20, 2015, 9:53:57 PM (GMT)]
 */
package vazkii.botania.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.NoteBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.TileCacophonium;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.lib.LibObfuscation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ItemCacophonium extends ItemMod {

	private static final String TAG_SOUND = "sound";
	private static final String TAG_SOUND_NAME = "soundName";

	public ItemCacophonium(Properties props) {
		super(props);
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
		if(entity instanceof MobEntity) {
			MobEntity living = (MobEntity) entity;
			SoundEvent sound = null;

			if(living instanceof CreeperEntity)
				sound = SoundEvents.ENTITY_CREEPER_PRIMED;
			else if(living instanceof SlimeEntity)
				sound = ((SlimeEntity) living).isSmallSlime() ? SoundEvents.ENTITY_SLIME_SQUISH_SMALL : SoundEvents.ENTITY_SLIME_SQUISH;
			else {
				try {
					sound = (SoundEvent) ObfuscationReflectionHelper.findMethod(MobEntity.class, LibObfuscation.GET_LIVING_SOUND).invoke(living);
				} catch (InvocationTargetException | IllegalAccessException ignored) {
					Botania.LOGGER.debug("Couldn't get living sound");
				}
			}

			if(sound != null) {
				ItemNBTHelper.setString(stack, TAG_SOUND, sound.getRegistryName().toString());
				ItemNBTHelper.setString(stack, TAG_SOUND_NAME, entity.getType().getTranslationKey());
				player.setHeldItem(hand, stack);

				if(player.world.isRemote)
					player.swingArm(hand);

				return true;
			}
		}

		return false;
	}

	@Nonnull
	@Override
	public ActionResultType onItemUse(ItemUseContext ctx) {
		ItemStack stack = ctx.getItem();
		if(getSound(stack) != null) {
			World world = ctx.getWorld();
			BlockPos pos = ctx.getPos();

			Block block = world.getBlockState(pos).getBlock();
			if(block instanceof NoteBlock) {
				world.setBlockState(pos, ModBlocks.cacophonium.getDefaultState());
				((TileCacophonium) world.getTileEntity(pos)).stack = stack.copy();
				stack.shrink(1);
				return ActionResultType.SUCCESS;
			}
		}

		return ActionResultType.PASS;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World world, List<ITextComponent> list, ITooltipFlag flags) {
		if(isDOIT(stack))
			list.add(new TranslationTextComponent("botaniamisc.justDoIt").applyTextStyle(TextFormatting.GRAY));
		else if(getSound(stack) != null)
			list.add(new TranslationTextComponent(ItemNBTHelper.getString(stack, TAG_SOUND_NAME, "")).applyTextStyle(TextFormatting.GRAY));
	}

	@Nonnull
	@Override
	public UseAction getUseAction(ItemStack par1ItemStack) {
		return UseAction.BLOCK;
	}

	@Override
	public int getUseDuration(ItemStack par1ItemStack) {
		return 72000;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if(getSound(stack) != null)
			player.setActiveHand(hand);
		return ActionResult.newResult(ActionResultType.SUCCESS, stack);
	}

	@Override
	public void onUsingTick(ItemStack stack, LivingEntity player, int count) {
		if(count % (isDOIT(stack) ? 20 : 6) == 0)
			playSound(player.world, stack, player.posX, player.posY, player.posZ, SoundCategory.PLAYERS, 0.9F);
	}

	public static void playSound(World world, ItemStack stack, double x, double y, double z, SoundCategory category, float volume) {
		if(stack.isEmpty())
			return;

		SoundEvent sound = getSound(stack);

		if(sound != null)
			world.playSound(null, x, y, z, sound, category, volume, sound == ModSounds.doit ? 1F : (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F);
	}

	@Nullable
	private static SoundEvent getSound(ItemStack stack) {
		if(isDOIT(stack))
			return ModSounds.doit;
		else {
			try {
				return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(ItemNBTHelper.getString(stack, TAG_SOUND, "")));
			} catch (ResourceLocationException ex) {
				return null;
			}
		}
	}

	private static boolean isDOIT(ItemStack stack) {
		return !stack.isEmpty() && stack.getDisplayName().getString().equalsIgnoreCase("shia labeouf");
	}
}
