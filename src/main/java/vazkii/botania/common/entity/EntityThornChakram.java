/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 25, 2015, 6:47:35 PM (GMT)]
 */
package vazkii.botania.common.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BushBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lib.LibMisc;

import javax.annotation.Nonnull;

@OnlyIn(
	value = Dist.CLIENT,
	_interface = IRendersAsItem.class
)
public class EntityThornChakram extends ThrowableEntity implements IRendersAsItem {
	@ObjectHolder(LibMisc.MOD_ID + ":thorn_chakram")
	public static EntityType<EntityThornChakram> TYPE;

	private static final DataParameter<Integer> BOUNCES = EntityDataManager.createKey(EntityThornChakram.class, DataSerializers.VARINT);
	private static final DataParameter<Boolean> FLARE = EntityDataManager.createKey(EntityThornChakram.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> RETURN_TO = EntityDataManager.createKey(EntityThornChakram.class, DataSerializers.VARINT);
	private static final int MAX_BOUNCES = 16;
	private boolean bounced = false;
	private ItemStack stack = ItemStack.EMPTY;

	public EntityThornChakram(EntityType<EntityThornChakram> type, World world) {
		super(type, world);
	}

	public EntityThornChakram(LivingEntity e, World world, ItemStack stack) {
		super(TYPE, e, world);
		this.stack = stack.copy();
	}

	@Override
	protected void registerData() {
		dataManager.register(BOUNCES, 0);
		dataManager.register(FLARE, false);
		dataManager.register(RETURN_TO, -1);
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public boolean isImmuneToExplosions() {
		return true;
	}

	@Override
	public void tick() {
		// Standard motion
		Vec3d old = getMotion();

		super.tick();

		if(!bounced) {
			// Reset the drag applied by super
			setMotion(old);
		}

		bounced = false;

		// Returning motion
		if(isReturning()) {
			Entity thrower = getThrower();
			if(thrower != null) {
				Vector3 motion = Vector3.fromEntityCenter(thrower).subtract(Vector3.fromEntityCenter(this)).normalize();
				setMotion(motion.toVec3D());
			}
		}

		// Client FX
		if(world.isRemote && isFire()) {
			double r = 0.1;
			double m = 0.1;
			for(int i = 0; i < 3; i++)
				world.addParticle(ParticleTypes.FLAME, getX() + r * (Math.random() - 0.5), getY() + r * (Math.random() - 0.5), getZ() + r * (Math.random() - 0.5), m * (Math.random() - 0.5), m * (Math.random() - 0.5), m * (Math.random() - 0.5));
		}

		// Server state control
		if(!world.isRemote && (getTimesBounced() >= MAX_BOUNCES || ticksExisted > 60)) {
			LivingEntity thrower = getThrower();
			if(thrower == null) {
				dropAndKill();
			} else {
				setEntityToReturnTo(thrower.getEntityId());
				if(getDistanceSq(thrower) < 2)
					dropAndKill();
			}
		}
	}

	private void dropAndKill() {
		ItemStack stack = getItemStack();
		ItemEntity item = new ItemEntity(world, getX(), getY(), getZ(), stack);
		world.addEntity(item);
		remove();
	}

	private ItemStack getItemStack() {
		return !stack.isEmpty()
				? stack.copy()
				: isFire() ? new ItemStack(ModItems.flareChakram) : new ItemStack(ModItems.thornChakram);
	}

	@Override
	protected void onImpact(@Nonnull RayTraceResult pos) {
		if(isReturning())
			return;

		switch (pos.getType()) {
		case BLOCK: {
			BlockRayTraceResult rtr = (BlockRayTraceResult) pos;
			Block block = world.getBlockState(rtr.getPos()).getBlock();
			if(block instanceof BushBlock || block instanceof LeavesBlock)
				return;

			int bounces = getTimesBounced();
			if(bounces < MAX_BOUNCES) {
				Vector3 currentMovementVec = new Vector3(getMotion());
				Direction dir = rtr.getFace();
				Vector3 normalVector = new Vector3(dir.getXOffset(), dir.getYOffset(), dir.getZOffset()).normalize();
				Vector3 movementVec = normalVector.multiply(-2 * currentMovementVec.dotProduct(normalVector)).add(currentMovementVec);

				setMotion(movementVec.toVec3D());
				bounced = true;

				if(!world.isRemote)
					setTimesBounced(getTimesBounced() + 1);
			}

			break;
		}
		case ENTITY: {
			EntityRayTraceResult rtr = (EntityRayTraceResult) pos;
			if(!world.isRemote && rtr.getEntity() instanceof LivingEntity && rtr.getEntity() != getThrower()) {
				LivingEntity thrower = getThrower();
				rtr.getEntity().attackEntityFrom(thrower != null
						? thrower instanceof PlayerEntity
							? DamageSource.causeThrownDamage(this, thrower)
							: DamageSource.causeMobDamage(thrower)
						: DamageSource.GENERIC, 12);
				if(isFire())
					rtr.getEntity().setFire(5);
				else if(world.rand.nextInt(3) == 0)
					((LivingEntity) rtr.getEntity()).addPotionEffect(new EffectInstance(Effects.POISON, 60, 0));
			}

			break;
		}
		default: break;
		}
	}

	@Override
	protected float getGravityVelocity() {
		return 0F;
	}

	private int getTimesBounced() {
		return dataManager.get(BOUNCES);
	}

	private void setTimesBounced(int times) {
		dataManager.set(BOUNCES, times);
	}

	public boolean isFire() {
		return dataManager.get(FLARE);
	}

	public void setFire(boolean fire) {
		dataManager.set(FLARE, fire);
	}

	private boolean isReturning() {
		return getEntityToReturnTo() > -1;
	}

	private int getEntityToReturnTo() {
		return dataManager.get(RETURN_TO);
	}

	private void setEntityToReturnTo(int entityID) {
		dataManager.set(RETURN_TO, entityID);
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		if(!stack.isEmpty()) {
			compound.put("fly_stack", stack.write(new CompoundNBT()));
		}
		compound.putBoolean("flare", isFire());
	}

	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		if(compound.contains("fly_stack")) {
			stack = ItemStack.read(compound.getCompound("fly_stack"));
		}
		setFire(compound.getBoolean("flare"));
	}

	@Nonnull
	@Override
	public ItemStack getItem() {
		return getItemStack();
	}
}
