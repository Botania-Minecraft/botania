/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 30, 2015, 1:37:26 PM (GMT)]
 */
package vazkii.botania.common.block.subtile.generating;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ObjectHolder;
import org.lwjgl.opengl.GL11;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.TileEntityGeneratingFlower;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.lib.LibMisc;

import java.awt.*;
import java.util.List;
import java.util.function.Predicate;

public class SubTileSpectrolus extends TileEntityGeneratingFlower {
	@ObjectHolder(LibMisc.MOD_ID + ":spectrolus")
	public static TileEntityType<SubTileSpectrolus> TYPE;

	public static final String TAG_NEXT_COLOR = "nextColor";
	private static final int WOOL_GEN = 1200;
	private static final int SHEEP_GEN = 6400;
	private static final int BABY_SHEEP_GEN = 1; // you are a monster

	private static final int RANGE = 1;

	private DyeColor nextColor = DyeColor.WHITE;

	public SubTileSpectrolus() {
		super(TYPE);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();

		if (getWorld().isRemote)
			return;

		// sheep need to enter the actual block space
		List<Entity> targets = getWorld().getEntitiesWithinAABB(SheepEntity.class, new AxisAlignedBB(getPos()), Entity::isAlive);

		AxisAlignedBB itemAABB = new AxisAlignedBB(getPos().add(-RANGE, -RANGE, -RANGE), getPos().add(RANGE + 1, RANGE + 1, RANGE + 1));
		int slowdown = getSlowdownFactor();
		Predicate<Entity> selector = e -> (e instanceof ItemEntity && e.isAlive() && ((ItemEntity) e).age >= slowdown);
		targets.addAll(getWorld().getEntitiesWithinAABB(Entity.class, itemAABB, selector));

		for(Entity target : targets) {
			if (target instanceof SheepEntity) {
				SheepEntity sheep = (SheepEntity) target;
				if (!sheep.getSheared() && sheep.getFleeceColor() == nextColor) {
					addManaAndCycle(sheep.isChild() ? BABY_SHEEP_GEN : SHEEP_GEN);
					float pitch = sheep.isChild() ? (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.5F : (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F + 1.0F;
					sheep.playSound(SoundEvents.ENTITY_SHEEP_DEATH, 0.9F, pitch);
					sheep.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1, 1);

					ItemStack morbid = new ItemStack(sheep.isBurning() ? Items.COOKED_MUTTON : Items.MUTTON);
					((ServerWorld) getWorld()).spawnParticle(new ItemParticleData(ParticleTypes.ITEM, morbid), target.posX, target.posY + target.getEyeHeight(), target.posZ, 20, 0.1D, 0.1D, 0.1D, 0.05D);

					ItemStack wool = new ItemStack(ModBlocks.getWool(sheep.getFleeceColor()));
					((ServerWorld) getWorld()).spawnParticle(new ItemParticleData(ParticleTypes.ITEM, wool), target.posX, target.posY + target.getEyeHeight(), target.posZ, 20, 0.1D, 0.1D, 0.1D, 0.05D);
				}
				sheep.setHealth(0);
			} else if (target instanceof ItemEntity) {
				ItemStack stack = ((ItemEntity) target).getItem();

				if(!stack.isEmpty()) {
					Block expected = ModBlocks.getWool(nextColor);

					if(expected.asItem() == stack.getItem()) {
						addManaAndCycle(WOOL_GEN);
						((ServerWorld) getWorld()).spawnParticle(new ItemParticleData(ParticleTypes.ITEM, stack), target.posX, target.posY, target.posZ, 20, 0.1D, 0.1D, 0.1D, 0.05D);
						target.remove();
					}
				}
			}
		}
	}

	private void addManaAndCycle(int toAdd) {
		addMana(toAdd);
		nextColor = nextColor == DyeColor.BLACK ? DyeColor.WHITE : DyeColor.values()[nextColor.ordinal() + 1];
		sync();
	}

	@Override
	public RadiusDescriptor getRadius() {
        return new RadiusDescriptor.Square(getPos(), RANGE);
	}

	@Override
	public int getMaxMana() {
		return 16000;
	}

	@Override
	public int getColor() {
		return Color.HSBtoRGB(ticksExisted / 100F, 1F, 1F);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void renderHUD(Minecraft mc) {
		super.renderHUD(mc);

		ItemStack stack = new ItemStack(ModBlocks.getWool(nextColor));
		int color = getColor();

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		if(!stack.isEmpty()) {
			ITextComponent stackName = stack.getDisplayName();
			int width = 16 + mc.fontRenderer.getStringWidth(stackName.getString()) / 2;
			int x = mc.mainWindow.getScaledWidth() / 2 - width;
			int y = mc.mainWindow.getScaledHeight() / 2 + 30;

			mc.fontRenderer.drawStringWithShadow(stackName.getFormattedText(), x + 20, y + 5, color);
			RenderHelper.enableGUIStandardItemLighting();
			mc.getItemRenderer().renderItemAndEffectIntoGUI(stack, x, y);
			RenderHelper.disableStandardItemLighting();
		}

		GlStateManager.disableLighting();
		GlStateManager.disableBlend();
	}

	@Override
	public void writeToPacketNBT(CompoundNBT cmp) {
		super.writeToPacketNBT(cmp);
		cmp.putInt(TAG_NEXT_COLOR, nextColor.ordinal());
	}

	@Override
	public void readFromPacketNBT(CompoundNBT cmp) {
		super.readFromPacketNBT(cmp);
		nextColor = DyeColor.byId(cmp.getInt(TAG_NEXT_COLOR));
	}
}
