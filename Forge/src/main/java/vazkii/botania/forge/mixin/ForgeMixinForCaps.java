package vazkii.botania.forge.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.ItemWaterBowl;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.bauble.ItemBauble;
import vazkii.botania.forge.CapabilityUtil;
import vazkii.botania.forge.integration.curios.CurioIntegration;

// [SelfMixin] IForgeItem#initCapabilities, which can't exist in common code
@Mixin(
	{
			ItemBauble.class,
			ItemWaterBowl.class,
	}
)
public class ForgeMixinForCaps extends Item {
	private ForgeMixinForCaps(Properties properties) {
		super(properties);
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		if (stack.getItem() instanceof ItemBauble && EquipmentHandler.instance instanceof CurioIntegration ci) {
			return ci.initCapability(stack);
		} else if (stack.is(ModItems.waterBowl)) {
			return new CapabilityUtil.WaterBowlFluidHandler(stack);
		}
		return null;
	}
}
