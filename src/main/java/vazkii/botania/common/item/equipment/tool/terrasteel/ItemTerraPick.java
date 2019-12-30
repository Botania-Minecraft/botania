/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [May 20, 2014, 10:56:14 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.tool.terrasteel;

import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.ISequentialBreaker;
import vazkii.botania.api.mana.IManaGivingItem;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.core.helper.PlayerHelper;
import vazkii.botania.common.item.ItemTemperanceStone;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelPick;
import vazkii.botania.common.item.relic.ItemLokiRing;
import vazkii.botania.common.item.relic.ItemThorRing;
import vazkii.botania.common.lib.LibMisc;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public class ItemTerraPick extends ItemManasteelPick implements IManaItem, ISequentialBreaker {

	private static final String TAG_ENABLED = "enabled";
	private static final String TAG_MANA = "mana";
	private static final String TAG_TIPPED = "tipped";

	private static final int MAX_MANA = Integer.MAX_VALUE;
	private static final int MANA_PER_DAMAGE = 100;

	private static final List<Material> MATERIALS = Arrays.asList(Material.ROCK, Material.IRON, Material.ICE,
			Material.GLASS, Material.PISTON, Material.ANVIL, Material.ORGANIC, Material.EARTH, Material.SAND,
			Material.SNOW, Material.SNOW_BLOCK, Material.CLAY);

	public static final int[] LEVELS = new int[] {
			0, 10000, 1000000, 10000000, 100000000, 1000000000
	};

	private static final int[] CREATIVE_MANA = new int[] {
			10000 - 1, 1000000 - 1, 10000000 - 1, 100000000 - 1, 1000000000 - 1, MAX_MANA - 1
	};

	public ItemTerraPick(Properties props) {
		super(BotaniaAPI.TERRASTEEL_ITEM_TIER, props, -2.8F);
		addPropertyOverride(new ResourceLocation("botania", "tipped"), (itemStack, world, entityLivingBase) -> isTipped(itemStack) ? 1 : 0);
		addPropertyOverride(new ResourceLocation("botania", "enabled"), (itemStack, world, entityLivingBase) -> isEnabled(itemStack) ? 1 : 0);
	}

	@Override
	public void fillItemGroup(@Nonnull ItemGroup tab, @Nonnull NonNullList<ItemStack> list) {
		if(isInGroup(tab)) {
			for(int mana : CREATIVE_MANA) {
				ItemStack stack = new ItemStack(this);
				setMana(stack, mana);
				list.add(stack);
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack par1ItemStack, World world, List<ITextComponent> stacks, ITooltipFlag flags) {
		ITextComponent rank = new TranslationTextComponent("botania.rank" + getLevel(par1ItemStack));
		ITextComponent rankFormat = new TranslationTextComponent("botaniamisc.toolRank", rank);
		stacks.add(rankFormat);
		if(getMana(par1ItemStack) == Integer.MAX_VALUE)
			stacks.add(new TranslationTextComponent("botaniamisc.getALife").applyTextStyle(TextFormatting.RED));
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, @Nonnull Hand hand) {
		ItemStack stack = player.getHeldItem(hand);

		getMana(stack);
		int level = getLevel(stack);

		if(level != 0) {
			setEnabled(stack, !isEnabled(stack));
			if(!world.isRemote)
				world.playSound(null, player.posX, player.posY, player.posZ, ModSounds.terraPickMode, SoundCategory.PLAYERS, 0.5F, 0.4F);
		}

		return ActionResult.newResult(ActionResultType.SUCCESS, stack);
	}

	@Nonnull
	@Override
	public ActionResultType onItemUse(ItemUseContext ctx) {
		return ctx.getPlayer() == null || ctx.getPlayer().isSneaking() ? super.onItemUse(ctx)
				: ActionResultType.PASS;
	}

	@Override
	public void inventoryTick(ItemStack par1ItemStack, World world, Entity par3Entity, int par4, boolean par5) {
		super.inventoryTick(par1ItemStack, world, par3Entity, par4, par5);
		if(isEnabled(par1ItemStack)) {
			int level = getLevel(par1ItemStack);

			if(level == 0)
				setEnabled(par1ItemStack, false);
			else if(par3Entity instanceof PlayerEntity && !((PlayerEntity) par3Entity).isSwingInProgress)
				addMana(par1ItemStack, -level);
		}
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, PlayerEntity player) {
		RayTraceResult raycast = ToolCommons.raytraceFromEntity(player.world, player, RayTraceContext.FluidMode.NONE, 10);
		if(!player.world.isRemote && raycast.getType() == RayTraceResult.Type.BLOCK) {
			Direction face = ((BlockRayTraceResult) raycast).getFace();
			breakOtherBlock(player, stack, pos, pos, face);
			ItemLokiRing.breakOnAllCursors(player, this, stack, pos, face);
			// ^ Doable with API access through the IInternalMethodHandler.
		}

		return false;
	}

	@Override
	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}

	@Override
	public void breakOtherBlock(PlayerEntity player, ItemStack stack, BlockPos pos, BlockPos originPos, Direction side) {
		if(!isEnabled(stack))
			return;

		World world = player.world;
		Material mat = world.getBlockState(pos).getMaterial();
		if(!MATERIALS.contains(mat))
			return;

		if(world.isAirBlock(pos))
			return;

		int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
		boolean silk = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, stack) > 0;
		boolean thor = !ItemThorRing.getThorRing(player).isEmpty();
		boolean doX = thor || side.getXOffset() == 0;
		boolean doY = thor || side.getYOffset() == 0;
		boolean doZ = thor || side.getZOffset() == 0;

		int origLevel = getLevel(stack);
		int level = origLevel + (thor ? 1 : 0);
		if(ItemTemperanceStone.hasTemperanceActive(player) && level > 2)
			level = 2;

		int range = level - 1;
		int rangeY = Math.max(1, range);

		if(range == 0 && level != 1)
			return;

		Vec3i beginDiff = new Vec3i(doX ? -range : 0, doY ? -1 : 0, doZ ? -range : 0);
		Vec3i endDiff = new Vec3i(doX ? range : 0, doY ? rangeY * 2 - 1 : 0, doZ ? range : 0);

		ToolCommons.removeBlocksInIteration(player, stack, world, pos, beginDiff, endDiff, state -> MATERIALS.contains(state.getMaterial()), isTipped(stack));

		if(origLevel == 5) {
			PlayerHelper.grantCriterion((ServerPlayerEntity) player, new ResourceLocation(LibMisc.MOD_ID, "challenge/rank_ss_pick"), "code_triggered");
		}
	}

	@Override
	public int getEntityLifespan(ItemStack itemStack, World world) {
		return Integer.MAX_VALUE;
	}

	public static boolean isTipped(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_TIPPED, false);
	}

	public static void setTipped(ItemStack stack) {
		ItemNBTHelper.setBoolean(stack, TAG_TIPPED, true);
	}

	public static boolean isEnabled(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_ENABLED, false);
	}

	void setEnabled(ItemStack stack, boolean enabled) {
		ItemNBTHelper.setBoolean(stack, TAG_ENABLED, enabled);
	}

	public static void setMana(ItemStack stack, int mana) {
		ItemNBTHelper.setInt(stack, TAG_MANA, mana);
	}

	@Override
	public int getMana(ItemStack stack) {
		return getMana_(stack);
	}

	public static int getMana_(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_MANA, 0);
	}

	public static int getLevel(ItemStack stack) {
		int mana = getMana_(stack);
		for(int i = LEVELS.length - 1; i > 0; i--)
			if(mana >= LEVELS[i])
				return i;

		return 0;
	}

	@Override
	public int getMaxMana(ItemStack stack) {
		return MAX_MANA;
	}

	@Override
	public void addMana(ItemStack stack, int mana) {
		setMana(stack, Math.min(getMana(stack) + mana, MAX_MANA));
	}

	@Override
	public boolean canReceiveManaFromPool(ItemStack stack, TileEntity pool) {
		return true;
	}

	@Override
	public boolean canReceiveManaFromItem(ItemStack stack, ItemStack otherStack) {
		return !(otherStack.getItem() instanceof IManaGivingItem);
	}

	@Override
	public boolean canExportManaToPool(ItemStack stack, TileEntity pool) {
		return false;
	}

	@Override
	public boolean canExportManaToItem(ItemStack stack, ItemStack otherStack) {
		return false;
	}

	@Override
	public boolean isNoExport(ItemStack stack) {
		return true;
	}

	@Override
	public boolean disposeOfTrashBlocks(ItemStack stack) {
		return isTipped(stack);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack before, @Nonnull ItemStack after, boolean slotChanged) {
		return after.getItem() != this || isEnabled(before) != isEnabled(after);
	}

}
