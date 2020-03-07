/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [22/10/2016, 20:18:44 (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.botania.api.mana.IManaUsingItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.helper.IconHelper;

import java.util.List;

public class ItemThirdEye extends ItemBauble implements IManaUsingItem {

	private static final int COST = 2;

	public ItemThirdEye(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity living) {
		if(!(living instanceof PlayerEntity))
			return;
		PlayerEntity eplayer = (PlayerEntity) living;

		double range = 24;
		AxisAlignedBB aabb = new AxisAlignedBB(living.getX(), living.getY(), living.getZ(), living.getX(), living.getY(), living.getZ()).grow(range);
		List<LivingEntity> mobs = living.world.getEntitiesWithinAABB(LivingEntity.class, aabb, (Entity e) -> e instanceof IMob);

		for(LivingEntity e : mobs) {
			EffectInstance potion = e.getActivePotionEffect(Effects.GLOWING);
			if((potion == null || potion.getDuration() <= 2) && ManaItemHandler.requestManaExact(stack, eplayer, COST, true))
				e.addPotionEffect(new EffectInstance(Effects.GLOWING, 12, 0));
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void doRender(ItemStack stack, LivingEntity living, MatrixStack ms, IRenderTypeBuffer buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		boolean armor = !living.getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty();
		ms.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(180));
		ms.translate(-0.3, -0.6, armor ? -0.18 : -0.12);
		ms.scale(0.6F, 0.6F, 0.6F);

		for(int i = 0; i < 3; i++) {
		    ms.push();
			switch (i) {
			case 0: break;
			case 1:
				float scale1 = 0.75F;

				ms.translate(0.15, 0.15, -0.05);
				double time = ClientTickHandler.total * 0.12;
				double dist = 0.05;
				ms.translate(Math.sin(time) * dist, Math.cos(time * 0.5) * dist, 0);

				ms.scale(scale1, scale1, scale1);
				break;
			case 2:
				ms.translate(0, 0, -0.05);
				break;
			}

			IBakedModel model = MiscellaneousIcons.INSTANCE.thirdEyeLayers[i];
			IVertexBuilder buffer = buffers.getBuffer(Atlases.getEntitySolid());
			Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer()
					.render(ms.peek(), buffer, null, model, 1, 1, 1, light, OverlayTexture.DEFAULT_UV);
			ms.pop();
		}
	}

	@Override
	public boolean usesMana(ItemStack stack) {
		return true;
	}

}
