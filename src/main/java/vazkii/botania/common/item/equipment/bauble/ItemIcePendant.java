/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Apr 26, 2014, 2:06:17 PM (GMT)]
 */
package vazkii.botania.common.item.equipment.bauble;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Atlases;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.enchantment.FrostWalkerEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.botania.client.core.handler.MiscellaneousIcons;
import vazkii.botania.client.core.helper.IconHelper;

public class ItemIcePendant extends ItemBauble {

	public ItemIcePendant(Properties props) {
		super(props);
	}

	@Override
	public void onWornTick(ItemStack stack, LivingEntity entity) {
		if (!entity.world.isRemote && !entity.isSneaking()) {
			boolean lastOnGround = entity.onGround;
			entity.onGround = true;
			FrostWalkerEnchantment.freezeNearby(entity, entity.world, new BlockPos(entity), 8);
			entity.onGround = lastOnGround;

			int x = MathHelper.floor(entity.getX());
			int y = MathHelper.floor(entity.getY());
			int z = MathHelper.floor(entity.getZ());
			BlockState blockstate = Blocks.SNOW.getDefaultState();

			for(int l = 0; l < 4; ++l) {
				x = MathHelper.floor(entity.getX() + (double)((float)(l % 2 * 2 - 1) * 0.25F));
				z = MathHelper.floor(entity.getZ() + (double)((float)(l / 2 % 2 * 2 - 1) * 0.25F));
				BlockPos blockpos = new BlockPos(x, y, z);
				if (entity.world.isAirBlock(blockpos) && entity.world.getBiome(blockpos).getTemperatureCached(blockpos) < 0.9F && blockstate.isValidPosition(entity.world, blockpos)) {
					entity.world.setBlockState(blockpos, blockstate);
				}
			}
		}
		else if (entity.world.isRemote && !entity.isSneaking()) {
			if(entity.world.rand.nextFloat() >= 0.25F) {
				entity.world.addParticle(new BlockParticleData(ParticleTypes.FALLING_DUST, Blocks.SNOW_BLOCK.getDefaultState()), entity.getX() + entity.world.rand.nextFloat() * 0.6 - 0.3, entity.getY() + 1.1, entity.getZ()  + entity.world.rand.nextFloat() * 0.6 - 0.3, 0, -0.15, 0);
			}
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void doRender(ItemStack stack, LivingEntity player, MatrixStack ms, IRenderTypeBuffer buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		boolean armor = !player.getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty();
		ms.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(180F));
		ms.translate(-0.36F, -0.3F, armor ? 0.2F : 0.15F);
		ms.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(-45F));
		ms.scale(0.5F, 0.5F, 0.5F);

		IBakedModel model = MiscellaneousIcons.INSTANCE.snowflakePendantGem;
		IVertexBuilder buffer = buffers.getBuffer(Atlases.getEntitySolid());
		Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer()
				.render(ms.peek(), buffer, null, model, 1, 1, 1, light, OverlayTexture.DEFAULT_UV);
	}

}
