/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jan 31, 2014, 3:02:58 PM (GMT)]
 */
package vazkii.botania.common.item.lens;

import java.awt.Color;
import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.oredict.RecipeSorter.Category;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.ILens;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.crafting.recipe.CompositeLensRecipe;
import vazkii.botania.common.crafting.recipe.LensDyingRecipe;
import vazkii.botania.common.item.ItemMod;
import vazkii.botania.common.lib.LibItemNames;
import cpw.mods.fml.common.registry.GameRegistry;

public class ItemLens extends ItemMod implements ILens {

	public static final int SUBTYPES = 19;

	public static final int NORMAL = 0,
			SPEED = 1,
			POWER = 2,
			TIME = 3,
			EFFICIENCY = 4,
			BOUNCE = 5,
			GRAVITY = 6,
			MINE = 7,
			DAMAGE = 8,
			PHANTOM = 9,
			MAGNET = 10,
			EXPLOSIVE = 11,
			INFLUENCE = 12,
			WEIGHT = 13,
			PAINT = 14,
			FIRE = 15,
			PISTON = 16,
			LIGHT = 17,
			WARP = 18;

	private static final int PROP_NONE = 0,
			PROP_POWER = 1,
			PROP_ORIENTATION = 1 << 1,
			PROP_TOUCH = 1 << 2,
			PROP_INTERACTION = 1 << 3,
			PROP_DAMAGE = 1 << 4;

	private static final int[] props = new int[SUBTYPES];
	private static final Lens[] lenses = new Lens[SUBTYPES];
	private static Lens fallbackLens = new Lens();

	static {
		setProps(NORMAL, PROP_NONE);
		setProps(SPEED, PROP_NONE);
		setProps(POWER, PROP_POWER);
		setProps(TIME, PROP_NONE);
		setProps(EFFICIENCY, PROP_NONE);
		setProps(BOUNCE, PROP_TOUCH);
		setProps(GRAVITY, PROP_ORIENTATION);
		setProps(MINE, PROP_TOUCH | PROP_INTERACTION);
		setProps(DAMAGE, PROP_DAMAGE);
		setProps(PHANTOM, PROP_TOUCH);
		setProps(MAGNET, PROP_ORIENTATION);
		setProps(EXPLOSIVE, PROP_DAMAGE | PROP_TOUCH | PROP_INTERACTION);
		setProps(INFLUENCE, PROP_NONE);
		setProps(WEIGHT, PROP_TOUCH | PROP_INTERACTION);
		setProps(PAINT, PROP_TOUCH | PROP_INTERACTION);
		setProps(FIRE, PROP_DAMAGE | PROP_TOUCH | PROP_INTERACTION);
		setProps(PISTON, PROP_TOUCH | PROP_INTERACTION);
		setProps(LIGHT, PROP_TOUCH | PROP_INTERACTION);
		setProps(WARP, PROP_NONE);

		setLens(NORMAL, fallbackLens);
		setLens(SPEED, new LensSpeed());
		setLens(POWER, new LensPower());
		setLens(TIME, new LensTime());
		setLens(EFFICIENCY, new LensEfficiency());
		setLens(BOUNCE, new LensBounce());
		setLens(GRAVITY, new LensGravity());
		setLens(MINE, new LensMine());
		setLens(DAMAGE, new LensDamage());
		setLens(PHANTOM, new LensPhantom());
		setLens(MAGNET, new LensMagnet());
		setLens(EXPLOSIVE, new LensExplosive());
		setLens(INFLUENCE, new LensInfluence());
		setLens(WEIGHT, new LensWeight());
		setLens(PAINT, new LensPaint());
		setLens(FIRE, new LensFire());
		setLens(PISTON, new LensPiston());
		setLens(LIGHT, new LensLight());
		setLens(WARP, new LensWarp());
	}

	private static final String TAG_COLOR = "color";
	private static final String TAG_COMPOSITE_LENS = "compositeLens";

	public static IIcon iconGlass;

	IIcon[] ringIcons;

	public ItemLens() {
		super();
		setUnlocalizedName(LibItemNames.LENS);
		setMaxStackSize(1);
		setHasSubtypes(true);

		GameRegistry.addRecipe(new CompositeLensRecipe());
		GameRegistry.addRecipe(new LensDyingRecipe());
		RecipeSorter.register("botania:compositeLens", CompositeLensRecipe.class, Category.SHAPELESS, "");
		RecipeSorter.register("botania:lensDying", LensDyingRecipe.class, Category.SHAPELESS, "");
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister) {
		iconGlass = IconHelper.forName(par1IconRegister, "lensInside");

		ringIcons = new IIcon[SUBTYPES];
		for(int i = 0; i < ringIcons.length; i++)
			ringIcons[i] = IconHelper.forName(par1IconRegister, LibItemNames.LENS_NAMES[i]);
	}

	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		for(int i = 0; i < SUBTYPES; i++)
			par3List.add(new ItemStack(par1, 1, i));
	}

	@Override
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	public IIcon getIconFromDamageForRenderPass(int par1, int par2) {
		return par2 == 1 ? ringIcons[Math.min(SUBTYPES - 1, par1)] : iconGlass;
	}

	@Override
	public IIcon getIconFromDamage(int par1) {
		return getIconFromDamageForRenderPass(par1, 0);
	}

	@Override
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
		return par2 == 0 ? getLensColor(par1ItemStack) : 0xFFFFFF;
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		return "item." + LibItemNames.LENS_NAMES[Math.min(SUBTYPES - 1, par1ItemStack.getItemDamage())];
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		int storedColor = getStoredColor(par1ItemStack);
		if(storedColor != -1)
			par3List.add(String.format(StatCollector.translateToLocal("botaniamisc.color"), StatCollector.translateToLocal("botania.color" + storedColor)));
	}


	public String getItemShortTermName(ItemStack stack) {
		return StatCollector.translateToLocal(stack.getUnlocalizedName().replaceAll("item.", "item.botania:") + ".short");
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		ItemStack compositeLens = getCompositeLens(stack);
		if(compositeLens == null)
			return super.getItemStackDisplayName(stack);
		return String.format(StatCollector.translateToLocal("item.botania:compositeLens.name"), getItemShortTermName(stack), getItemShortTermName(compositeLens));
	}

	@Override
	public void apply(ItemStack stack, BurstProperties props) {
		int storedColor = getStoredColor(stack);
		if(storedColor != -1)
			props.color = getLensColor(stack);

		getLens(stack.getItemDamage()).apply(stack, props);

		ItemStack compositeLens = getCompositeLens(stack);
		if(compositeLens != null && compositeLens.getItem() instanceof ILens)
			((ILens) compositeLens.getItem()).apply(compositeLens, props);
	}

	@Override
	public boolean collideBurst(IManaBurst burst, MovingObjectPosition pos, boolean isManaBlock, boolean dead, ItemStack stack) {
		EntityThrowable entity = (EntityThrowable) burst;

		dead = getLens(stack.getItemDamage()).collideBurst(burst, entity, pos, isManaBlock, dead, stack);

		ItemStack compositeLens = getCompositeLens(stack);
		if(compositeLens != null && compositeLens.getItem() instanceof ILens)
			dead = ((ILens) compositeLens.getItem()).collideBurst(burst, pos, isManaBlock, dead, compositeLens);

		return dead;
	}

	@Override
	public void updateBurst(IManaBurst burst, ItemStack stack) {
		EntityThrowable entity = (EntityThrowable) burst;
		int storedColor = getStoredColor(stack);

		if(storedColor == 16 && entity.worldObj.isRemote)
			burst.setColor(getLensColor(stack));

		getLens(stack.getItemDamage()).updateBurst(burst, entity, stack);

		ItemStack compositeLens = getCompositeLens(stack);
		if(compositeLens != null && compositeLens.getItem() instanceof ILens)
			((ILens) compositeLens.getItem()).updateBurst(burst, compositeLens);
	}

	@Override
	public int getLensColor(ItemStack stack) {
		int storedColor = getStoredColor(stack);

		if(storedColor == -1)
			return 0xFFFFFF;

		if(storedColor == 16)
			return Color.HSBtoRGB(Botania.proxy.getWorldElapsedTicks() * 2 % 360 / 360F, 1F, 1F);

		float[] color = EntitySheep.fleeceColorTable[storedColor];
		return new Color(color[0], color[1], color[2]).getRGB();
	}

	public static int getStoredColor(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_COLOR, -1);
	}

	public static ItemStack setLensColor(ItemStack stack, int color) {
		ItemNBTHelper.setInt(stack, TAG_COLOR, color);
		return stack;
	}

	@Override
	public boolean doParticles(IManaBurst burst, ItemStack stack) {
		return true;
	}

	public static void setProps(int lens, int props_) {
		props[lens] = props_;
	}

	public static void setLens(int index, Lens lens) {
		lenses[index] = lens;
	}

	public static boolean isBlacklisted(int lens1, int lens2) {
		return (props[lens1] & props[lens2]) != 0;
	}

	public static Lens getLens(int index) {
		Lens lens = lenses[index];
		return lens == null ? fallbackLens : lens;
	}

	@Override
	public boolean canCombineLenses(ItemStack sourceLens, ItemStack compositeLens) {
		if(sourceLens.getItemDamage() == compositeLens.getItemDamage())
			return false;

		if(sourceLens.getItemDamage() == NORMAL || compositeLens.getItemDamage() == NORMAL)
			return false;

		if(isBlacklisted(sourceLens.getItemDamage(), compositeLens.getItemDamage()))
			return false;

		return true;
	}

	@Override
	public ItemStack getCompositeLens(ItemStack stack) {
		NBTTagCompound cmp = ItemNBTHelper.getCompound(stack, TAG_COMPOSITE_LENS, false);
		ItemStack lens = ItemStack.loadItemStackFromNBT(cmp);
		return lens;
	}

	@Override
	public ItemStack setCompositeLens(ItemStack sourceLens, ItemStack compositeLens) {
		NBTTagCompound cmp = new NBTTagCompound();
		compositeLens.writeToNBT(cmp);
		ItemNBTHelper.setCompound(sourceLens, TAG_COMPOSITE_LENS, cmp);

		return sourceLens;
	}
}
