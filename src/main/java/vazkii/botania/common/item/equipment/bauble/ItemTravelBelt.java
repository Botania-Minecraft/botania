/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Apr 24, 2014, 11:14:57 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import vazkii.botania.api.item.AccessoryRenderHelper;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.core.handler.EquipmentHandler;

import java.util.ArrayList;
import java.util.List;

public class ItemTravelBelt extends ItemBauble implements IManaUsingItem {

	private static final ResourceLocation texture = new ResourceLocation(LibResources.MODEL_TRAVEL_BELT);
	@OnlyIn(Dist.CLIENT)
	private static ModelBiped model;

	private static final int COST = 1;
	private static final int COST_INTERVAL = 10;

	public static final List<String> playersWithStepup = new ArrayList<>();

	public final float speed;
	public final float jump;
	public final float fallBuffer;

	public ItemTravelBelt(Properties props) {
		this(props, 0.035F, 0.2F, 2F);
		MinecraftForge.EVENT_BUS.register(this);
	}

	public ItemTravelBelt(Properties props, float speed, float jump, float fallBuffer) {
		super(props);
		this.speed = speed;
		this.jump = jump;
		this.fallBuffer = fallBuffer;
	}

	@SubscribeEvent
	public void updatePlayerStepStatus(LivingUpdateEvent event) {
		if(event.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			ItemStack belt = EquipmentHandler.findOrEmpty(s -> s.getItem() instanceof ItemTravelBelt, player);
			String s = playerStr(player);

			if(playersWithStepup.contains(s)) {
				if(shouldPlayerHaveStepup(player)) {
					ItemTravelBelt beltItem = (ItemTravelBelt) belt.getItem();

					if(player.world.isRemote) {
						if((player.onGround || player.abilities.isFlying) && player.moveForward > 0F && !player.isInWaterOrBubbleColumn()) {
							float speed = beltItem.getSpeed(belt);
							player.moveRelative(0F, 0F, 1F, player.abilities.isFlying ? speed : speed);
							beltItem.onMovedTick(belt, player);

							if(player.ticksExisted % COST_INTERVAL == 0)
								ManaItemHandler.requestManaExact(belt, player, COST, true);
						} else beltItem.onNotMovingTick(belt, player);
					}

					if(player.isSneaking())
						player.stepHeight = 0.60001F; // Not 0.6F because that is the default
					else player.stepHeight = 1.25F;

				} else {
					player.stepHeight = 0.6F;
					playersWithStepup.remove(s);
				}
			} else if(shouldPlayerHaveStepup(player)) {
				playersWithStepup.add(s);
				player.stepHeight = 1.25F;
			}
		}
	}

	public float getSpeed(ItemStack stack) {
		return speed;
	}

	public void onMovedTick(ItemStack stack, EntityPlayer player) {}

	public void onNotMovingTick(ItemStack stack, EntityPlayer player) {}

	@SubscribeEvent
	public void onPlayerJump(LivingJumpEvent event) {
		if(event.getEntityLiving() instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.getEntityLiving();
			ItemStack belt = EquipmentHandler.findOrEmpty(s -> s.getItem() instanceof ItemTravelBelt, player);

			if(!belt.isEmpty() && ManaItemHandler.requestManaExact(belt, player, COST, false)) {
				player.motionY += ((ItemTravelBelt) belt.getItem()).jump;
				player.fallDistance = -((ItemTravelBelt) belt.getItem()).fallBuffer;
			}
		}
	}

	private boolean shouldPlayerHaveStepup(EntityPlayer player) {
		ItemStack result = EquipmentHandler.findOrEmpty(s -> s.getItem() instanceof ItemTravelBelt, player);
		return !result.isEmpty() && ManaItemHandler.requestManaExact(result, player, COST, false);
	}

	@SubscribeEvent
	public void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
		String username = event.getPlayer().getGameProfile().getName();
		playersWithStepup.remove(username + ":false");
		playersWithStepup.remove(username + ":true");
	}

	public static String playerStr(EntityPlayer player) {
		return player.getGameProfile().getName() + ":" + player.world.isRemote;
	}

	@OnlyIn(Dist.CLIENT)
	ResourceLocation getRenderTexture() {
		return texture;
	}


	@Override
	@OnlyIn(Dist.CLIENT)
	public void doRender(ItemStack stack, EntityLivingBase player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		Minecraft.getInstance().textureManager.bindTexture(((ItemTravelBelt) stack.getItem()).getRenderTexture());
		AccessoryRenderHelper.rotateIfSneaking(player);

		GlStateManager.translatef(0F, 0.2F, 0F);

		float s = 1.05F / 16F;
		GlStateManager.scalef(s, s, s);
		if(model == null)
			model = new ModelBiped();

		model.bipedBody.render(1F);
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

}
