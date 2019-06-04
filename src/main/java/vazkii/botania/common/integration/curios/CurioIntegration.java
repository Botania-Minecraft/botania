package vazkii.botania.common.integration.curios;

import com.google.common.collect.Multimap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.capability.CuriosCapability;
import top.theillusivec4.curios.api.capability.ICurio;
import top.theillusivec4.curios.api.imc.CurioIMCMessage;
import vazkii.botania.common.core.handler.EquipmentHandler;
import vazkii.botania.common.core.handler.ModSounds;
import vazkii.botania.common.item.equipment.bauble.ItemBauble;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

// Classloading-safe way to attach curio behaviour to our items
public class CurioIntegration extends EquipmentHandler {

	private static class Provider implements ICapabilityProvider {
		private final ICurio curio;
		private final LazyOptional<ICurio> curioCap;

		Provider(ICurio curio) {
			this.curio = curio;
			curioCap = LazyOptional.of(() -> this.curio);
		}

		@Nonnull
		@Override
		public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
			return CuriosCapability.ITEM.orEmpty(cap, curioCap);
		}
	}

	@SubscribeEvent
	public static void sendImc(InterModEnqueueEvent evt) {
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("charm"));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("ring").setSize(2));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("belt"));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("body"));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("head"));
		InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE, () -> new CurioIMCMessage("trinket"));
	}

	@Override
	protected LazyOptional<IItemHandlerModifiable> getAllWornItems(EntityLivingBase living) {
		return CuriosAPI.getCuriosHandler(living).map(h -> {
			IItemHandlerModifiable[] invs = h.getCurioMap().values().toArray(new IItemHandlerModifiable[0]);
			return new CombinedInvWrapper(invs);
		});
	}

	@Override
	protected ItemStack findItem(Item item, EntityLivingBase living) {
		CuriosAPI.FinderData result = CuriosAPI.getCurioEquipped(item, living);
		return result == null ? ItemStack.EMPTY : result.getStack();
	}

	@Override
	protected ItemStack findItem(Predicate<ItemStack> pred, EntityLivingBase living) {
		CuriosAPI.FinderData result = CuriosAPI.getCurioEquipped(pred, living);
		return result == null ? ItemStack.EMPTY : result.getStack();
	}

	@Override
	protected ICapabilityProvider initCap(ItemStack stack) {
		return new Provider(new Wrapper(stack));
	}

	private static class Wrapper implements ICurio {
		private final ItemStack stack;

		Wrapper(ItemStack stack) {
			this.stack = stack;
		}

		private ItemBauble getItem() {
			return (ItemBauble) stack.getItem();
		}

		@Override
		public void onCurioTick(String identifier, EntityLivingBase entity) {
			getItem().onWornTick(stack, entity);
		}

		@Override
		public void onEquipped(String identifier, EntityLivingBase entity) {
			getItem().onEquipped(stack, entity);
		}

		@Override
		public void onUnequipped(String identifier, EntityLivingBase entity) {
			getItem().onUnequipped(stack, entity);
		}

		@Override
		public boolean canEquip(String identifier, EntityLivingBase entity) {
			return getItem().canEquip(stack, entity);
		}

		@Override
		public Multimap<String, AttributeModifier> getAttributeModifiers(String identifier) {
			return getItem().getEquippedAttributeModifiers(stack);
		}

		@Override
		public boolean shouldSyncToTracking(String identifier, EntityLivingBase entity) {
			return getItem().shouldSyncToTracking(stack, entity);
		}

		@Override
		public void playEquipSound(EntityLivingBase entity) {
			entity.world.playSound(null, entity.posX, entity.posY, entity.posZ, ModSounds.equipBauble, entity.getSoundCategory(), 0.1F, 1.3F);
		}

		@Override
		public boolean canRightClickEquip() {
			return true;
		}

		@Override
		public boolean hasRender(String identifier, EntityLivingBase entity) {
			return getItem().hasRender(stack, entity);
		}

		@Override
		public void doRender(String identifier, EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
			getItem().doRender(stack, entitylivingbaseIn, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		}
	}
}
