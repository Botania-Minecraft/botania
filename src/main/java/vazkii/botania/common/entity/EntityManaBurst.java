/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Packet;
import net.minecraft.tag.Tag;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.api.mana.*;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.BlockPistonRelay;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.network.PacketSpawnEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.*;

public class EntityManaBurst extends ThrownEntity implements IManaBurst {
	private static final String TAG_TICKS_EXISTED = "ticksExisted";
	private static final String TAG_COLOR = "color";
	private static final String TAG_MANA = "mana";
	private static final String TAG_STARTING_MANA = "startingMana";
	private static final String TAG_MIN_MANA_LOSS = "minManaLoss";
	private static final String TAG_TICK_MANA_LOSS = "manaLossTick";
	private static final String TAG_SPREADER_X = "spreaderX";
	private static final String TAG_SPREADER_Y = "spreaderY";
	private static final String TAG_SPREADER_Z = "spreaderZ";
	private static final String TAG_GRAVITY = "gravity";
	private static final String TAG_LENS_STACK = "lensStack";
	private static final String TAG_LAST_MOTION_X = "lastMotionX";
	private static final String TAG_LAST_MOTION_Y = "lastMotionY";
	private static final String TAG_LAST_MOTION_Z = "lastMotionZ";
	private static final String TAG_HAS_SHOOTER = "hasShooter";
	private static final String TAG_SHOOTER = "shooterUUID";
	private static final String TAG_LAST_COLLISION_X = "lastCollisionX";
	private static final String TAG_LAST_COLLISION_Y = "lastCollisionY";
	private static final String TAG_LAST_COLLISION_Z = "lastCollisionZ";
	private static final String TAG_WARPED = "warped";
	private static final String TAG_ORBIT_TIME = "orbitTime";
	private static final String TAG_TRIPPED = "tripped";
	private static final String TAG_MAGNETIZE_POS = "magnetizePos";

	private static final TrackedData<Integer> COLOR = DataTracker.registerData(EntityManaBurst.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> MANA = DataTracker.registerData(EntityManaBurst.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> START_MANA = DataTracker.registerData(EntityManaBurst.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Integer> MIN_MANA_LOSS = DataTracker.registerData(EntityManaBurst.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Float> MANA_LOSS_PER_TICK = DataTracker.registerData(EntityManaBurst.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<Float> GRAVITY = DataTracker.registerData(EntityManaBurst.class, TrackedDataHandlerRegistry.FLOAT);
	private static final TrackedData<BlockPos> SOURCE_COORDS = DataTracker.registerData(EntityManaBurst.class, TrackedDataHandlerRegistry.BLOCK_POS);
	private static final TrackedData<ItemStack> SOURCE_LENS = DataTracker.registerData(EntityManaBurst.class, TrackedDataHandlerRegistry.ITEM_STACK);

	private float accumulatedManaLoss = 0;
	private boolean fake = false;
	private final Set<BlockPos> alreadyCollidedAt = new HashSet<>();
	private boolean fullManaLastTick = true;
	private UUID shooterIdentity = null;
	private int _ticksExisted = 0;
	private boolean scanBeam = false;
	private BlockPos lastCollision;
	private boolean warped = false;
	private int orbitTime = 0;
	private boolean tripped = false;
	private BlockPos magnetizePos = null;

	public final List<PositionProperties> propsList = new ArrayList<>();

	public EntityManaBurst(EntityType<EntityManaBurst> type, World world) {
		super(type, world);
	}

	@Override
	protected void initDataTracker() {
		dataTracker.startTracking(COLOR, 0);
		dataTracker.startTracking(MANA, 0);
		dataTracker.startTracking(START_MANA, 0);
		dataTracker.startTracking(MIN_MANA_LOSS, 0);
		dataTracker.startTracking(MANA_LOSS_PER_TICK, 0F);
		dataTracker.startTracking(GRAVITY, 0F);
		dataTracker.startTracking(SOURCE_COORDS, BlockPos.ORIGIN);
		dataTracker.startTracking(SOURCE_LENS, ItemStack.EMPTY);
	}

	public EntityManaBurst(IManaSpreader spreader, boolean fake) {
		this(ModEntities.MANA_BURST, ((BlockEntity) spreader).getWorld());

		BlockEntity tile = spreader.tileEntity();

		this.fake = fake;

		setBurstSourceCoords(tile.getPos());
		refreshPositionAndAngles(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5, 0, 0);
		yaw = -(spreader.getRotationX() + 90F);
		pitch = spreader.getRotationY();

		float f = 0.4F;
		double mx = MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * f / 2D;
		double mz = -(MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * f) / 2D;
		double my = MathHelper.sin(pitch / 180.0F * (float) Math.PI) * f / 2D;
		setBurstMotion(mx, my, mz);
	}

	public EntityManaBurst(PlayerEntity player) {
		super(ModEntities.MANA_BURST, player, player.world);

		setBurstSourceCoords(new BlockPos(0, -1, 0));
		setRotation(player.yaw + 180, -player.pitch);

		float f = 0.4F;
		double mx = MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * f / 2D;
		double mz = -(MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI) * f) / 2D;
		double my = MathHelper.sin(pitch / 180.0F * (float) Math.PI) * f / 2D;
		setBurstMotion(mx, my, mz);
	}

	@Override
	public void tick() {
		setTicksExisted(getTicksExisted() + 1);
		super.tick();

		if (!fake && isAlive() && !scanBeam) {
			ping();
		}

		ILensEffect lens = getLensInstance();
		if (lens != null) {
			lens.updateBurst(this, getSourceLens());
		}

		int mana = getMana();
		if (getTicksExisted() >= getMinManaLoss()) {
			accumulatedManaLoss += getManaLossPerTick();
			int loss = (int) accumulatedManaLoss;
			setMana(mana - loss);
			accumulatedManaLoss -= loss;

			if (getMana() <= 0) {
				remove();
			}
		}

		particles();

		setBurstMotion(getVelocity().getX(), getVelocity().getY(), getVelocity().getZ());

		fullManaLastTick = getMana() == getStartingMana();

		if (scanBeam) {
			PositionProperties props = new PositionProperties(this);
			if (propsList.isEmpty()) {
				propsList.add(props);
			} else {
				PositionProperties lastProps = propsList.get(propsList.size() - 1);
				if (!props.coordsEqual(lastProps)) {
					propsList.add(props);
				}
			}
		}
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
		updatePosition(x, y, z);
		setRotation(yaw, pitch);
	}

	@Override
	public boolean updateMovementInFluid(Tag<Fluid> fluid, double mag) {
		return false;
	}

	@Override
	public boolean isInLava() {
		return false;
	}

	private BlockEntity collidedTile = null;
	private boolean noParticles = false;

	public BlockEntity getCollidedTile(boolean noParticles) {
		this.noParticles = noParticles;

		int iterations = 0;
		while (isAlive() && iterations < ConfigHandler.COMMON.spreaderTraceTime.getValue()) {
			tick();
			iterations++;
		}

		if (fake) {
			incrementFakeParticleTick();
		}

		return collidedTile;
	}

	@Override
	public void writeCustomDataToTag(CompoundTag tag) {
		super.writeCustomDataToTag(tag);
		tag.putInt(TAG_TICKS_EXISTED, getTicksExisted());
		tag.putInt(TAG_COLOR, getColor());
		tag.putInt(TAG_MANA, getMana());
		tag.putInt(TAG_STARTING_MANA, getStartingMana());
		tag.putInt(TAG_MIN_MANA_LOSS, getMinManaLoss());
		tag.putFloat(TAG_TICK_MANA_LOSS, getManaLossPerTick());
		tag.putFloat(TAG_GRAVITY, getGravity());

		ItemStack stack = getSourceLens();
		CompoundTag lensCmp = new CompoundTag();
		if (!stack.isEmpty()) {
			lensCmp = stack.toTag(lensCmp);
		}
		tag.put(TAG_LENS_STACK, lensCmp);

		BlockPos coords = getBurstSourceBlockPos();
		tag.putInt(TAG_SPREADER_X, coords.getX());
		tag.putInt(TAG_SPREADER_Y, coords.getY());
		tag.putInt(TAG_SPREADER_Z, coords.getZ());

		tag.putDouble(TAG_LAST_MOTION_X, getVelocity().getX());
		tag.putDouble(TAG_LAST_MOTION_Y, getVelocity().getY());
		tag.putDouble(TAG_LAST_MOTION_Z, getVelocity().getZ());

		if (lastCollision != null) {
			tag.putInt(TAG_LAST_COLLISION_X, coords.getX());
			tag.putInt(TAG_LAST_COLLISION_Y, coords.getY());
			tag.putInt(TAG_LAST_COLLISION_Z, coords.getZ());
		}

		UUID identity = getShooterUUID();
		boolean hasShooter = identity != null;
		tag.putBoolean(TAG_HAS_SHOOTER, hasShooter);
		if (hasShooter) {
			tag.putUuid(TAG_SHOOTER, identity);
		}
		tag.putBoolean(TAG_WARPED, warped);
		tag.putInt(TAG_ORBIT_TIME, orbitTime);
		tag.putBoolean(TAG_TRIPPED, tripped);
		if (magnetizePos != null) {
			tag.put(TAG_MAGNETIZE_POS, BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, magnetizePos).get().orThrow());
		}
	}

	@Override
	public void readCustomDataFromTag(CompoundTag cmp) {
		super.readCustomDataFromTag(cmp);
		setTicksExisted(cmp.getInt(TAG_TICKS_EXISTED));
		setColor(cmp.getInt(TAG_COLOR));
		setMana(cmp.getInt(TAG_MANA));
		setStartingMana(cmp.getInt(TAG_STARTING_MANA));
		setMinManaLoss(cmp.getInt(TAG_MIN_MANA_LOSS));
		setManaLossPerTick(cmp.getFloat(TAG_TICK_MANA_LOSS));
		setGravity(cmp.getFloat(TAG_GRAVITY));

		CompoundTag lensCmp = cmp.getCompound(TAG_LENS_STACK);
		ItemStack stack = ItemStack.fromTag(lensCmp);
		if (!stack.isEmpty()) {
			setSourceLens(stack);
		} else {
			setSourceLens(ItemStack.EMPTY);
		}

		int x = cmp.getInt(TAG_SPREADER_X);
		int y = cmp.getInt(TAG_SPREADER_Y);
		int z = cmp.getInt(TAG_SPREADER_Z);

		setBurstSourceCoords(new BlockPos(x, y, z));

		if (cmp.contains(TAG_LAST_COLLISION_X)) {
			x = cmp.getInt(TAG_SPREADER_X);
			y = cmp.getInt(TAG_SPREADER_Y);
			z = cmp.getInt(TAG_SPREADER_Z);
			lastCollision = new BlockPos(x, y, z);
		}

		double lastMotionX = cmp.getDouble(TAG_LAST_MOTION_X);
		double lastMotionY = cmp.getDouble(TAG_LAST_MOTION_Y);
		double lastMotionZ = cmp.getDouble(TAG_LAST_MOTION_Z);

		setBurstMotion(lastMotionX, lastMotionY, lastMotionZ);

		boolean hasShooter = cmp.getBoolean(TAG_HAS_SHOOTER);
		if (hasShooter) {
			UUID serializedUuid = cmp.getUuid(TAG_SHOOTER);
			UUID identity = getShooterUUID();
			if (!serializedUuid.equals(identity)) {
				setShooterUUID(serializedUuid);
			}
		}
		warped = cmp.getBoolean(TAG_WARPED);
		orbitTime = cmp.getInt(TAG_ORBIT_TIME);
		tripped = cmp.getBoolean(TAG_TRIPPED);
		if (cmp.contains(TAG_MAGNETIZE_POS)) {
			magnetizePos = BlockPos.CODEC.parse(NbtOps.INSTANCE, cmp.get(TAG_MAGNETIZE_POS)).get().orThrow();
		} else {
			magnetizePos = null;
		}
	}

	public void particles() {
		if (!isAlive() || !world.isClient) {
			return;
		}

		ILensEffect lens = getLensInstance();
		if (lens != null && !lens.doParticles(this, getSourceLens())) {
			return;
		}

		int color = getColor();
		float r = (color >> 16 & 0xFF) / 255F;
		float g = (color >> 8 & 0xFF) / 255F;
		float b = (color & 0xFF) / 255F;
		float osize = getParticleSize();
		float size = osize;

		if (fake) {
			if (getMana() == getStartingMana()) {
				size = 2F;
			} else if (fullManaLastTick) {
				size = 4F;
			}

			if (!noParticles && shouldDoFakeParticles()) {
				SparkleParticleData data = SparkleParticleData.fake(0.4F * size, r, g, b, 1);
				Botania.proxy.addParticleForce(world, data, getX(), getY(), getZ(), 0, 0, 0);
			}
		} else {
			boolean depth = !Botania.proxy.isClientPlayerWearingMonocle();

			if (ConfigHandler.CLIENT.subtlePowerSystem.getValue()) {
				WispParticleData data = WispParticleData.wisp(0.1F * size, r, g, b, depth);
				Botania.proxy.addParticleForceNear(world, data, getX(), getY(), getZ(), (float) (Math.random() - 0.5F) * 0.02F, (float) (Math.random() - 0.5F) * 0.02F, (float) (Math.random() - 0.5F) * 0.01F);
			} else {
				float or = r;
				float og = g;
				float ob = b;

				double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b; // Standard relative luminance calculation

				double iterX = getX();
				double iterY = getY();
				double iterZ = getZ();

				Vec3d currentPos = getPos();
				Vec3d oldPos = new Vec3d(prevX, prevY, prevZ);
				Vec3d diffVec = oldPos.subtract(currentPos);
				Vec3d diffVecNorm = diffVec.normalize();

				double distance = 0.095;

				do {
					if (luminance < 0.1) {
						r = or + (float) Math.random() * 0.125F;
						g = og + (float) Math.random() * 0.125F;
						b = ob + (float) Math.random() * 0.125F;
					}
					size = osize + ((float) Math.random() - 0.5F) * 0.065F + (float) Math.sin(new Random(uuid.getMostSignificantBits()).nextInt(9001)) * 0.4F;
					WispParticleData data = WispParticleData.wisp(0.2F * size, r, g, b, depth);
					Botania.proxy.addParticleForceNear(world, data, iterX, iterY, iterZ,
							(float) -getVelocity().getX() * 0.01F,
							(float) -getVelocity().getY() * 0.01F,
							(float) -getVelocity().getZ() * 0.01F);

					iterX += diffVecNorm.x * distance;
					iterY += diffVecNorm.y * distance;
					iterZ += diffVecNorm.z * distance;

					currentPos = new Vec3d(iterX, iterY, iterZ);
					diffVec = oldPos.subtract(currentPos);
					if (getOrbitTime() > 0) {
						break;
					}
				} while (Math.abs(diffVec.length()) > distance);

				WispParticleData data = WispParticleData.wisp(0.1F * size, or, og, ob, depth);
				world.addParticle(data, iterX, iterY, iterZ, (float) (Math.random() - 0.5F) * 0.06F, (float) (Math.random() - 0.5F) * 0.06F, (float) (Math.random() - 0.5F) * 0.06F);
			}
		}
	}

	public float getParticleSize() {
		return (float) getMana() / (float) getStartingMana();
	}

	@Override
	protected void onCollision(@Nonnull HitResult rtr) {
		BlockPos pos = null;
		boolean dead = false;

		if (rtr.getType() == HitResult.Type.BLOCK) {
			pos = ((BlockHitResult) rtr).getBlockPos();
			if (pos.equals(lastCollision)) {
				return;
			}
			lastCollision = pos.toImmutable();
			BlockEntity tile = world.getBlockEntity(pos);
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if (block instanceof IManaCollisionGhost
					&& ((IManaCollisionGhost) block).isGhost(state, world, pos)
					&& !(block instanceof IManaTrigger)
					|| block instanceof PlantBlock
					|| block instanceof LeavesBlock) {
				return;
			}

			BlockPos coords = getBurstSourceBlockPos();
			if (tile != null && !tile.getPos().equals(coords)) {
				collidedTile = tile;
			}

			if (tile == null || !tile.getPos().equals(coords)) {
				if (!fake && !noParticles && !world.isClient && tile instanceof IManaReceiver && ((IManaReceiver) tile).canReceiveManaFromBursts()) {
					onReceiverImpact((IManaReceiver) tile, tile.getPos());
				}

				if (block instanceof IManaTrigger) {
					((IManaTrigger) block).onBurstCollision(this, world, pos);
				}

				boolean ghost = block instanceof IManaCollisionGhost;
				dead = !ghost;
				if (ghost) {
					return;
				}
			}
		}

		ILensEffect lens = getLensInstance();
		if (lens != null) {
			dead = lens.collideBurst(this, rtr, collidedTile instanceof IManaReceiver
					&& ((IManaReceiver) collidedTile).canReceiveManaFromBursts(), dead, getSourceLens());
		}

		if (pos != null && !hasAlreadyCollidedAt(pos)) {
			alreadyCollidedAt.add(pos);
		}

		if (dead && isAlive()) {
			if (!fake && world.isClient) {
				int color = getColor();
				float r = (color >> 16 & 0xFF) / 255F;
				float g = (color >> 8 & 0xFF) / 255F;
				float b = (color & 0xFF) / 255F;

				int mana = getMana();
				int maxMana = getStartingMana();
				float size = (float) mana / (float) maxMana;

				if (!ConfigHandler.CLIENT.subtlePowerSystem.getValue()) {
					for (int i = 0; i < 4; i++) {
						WispParticleData data = WispParticleData.wisp(0.15F * size, r, g, b);
						world.addParticle(data, getX(), getY(), getZ(), (float) (Math.random() - 0.5F) * 0.04F, (float) (Math.random() - 0.5F) * 0.04F, (float) (Math.random() - 0.5F) * 0.04F);
					}
				}
				SparkleParticleData data = SparkleParticleData.sparkle((float) 4, r, g, b, 2);
				world.addParticle(data, getX(), getY(), getZ(), 0, 0, 0);
			}

			remove();
		}
	}

	private void onReceiverImpact(IManaReceiver tile, BlockPos pos) {
		if (hasWarped() && this.hasAlreadyCollidedAt(pos)) {
			return;
		}

		ILensEffect lens = getLensInstance();
		int mana = getMana();

		if (lens != null) {
			ItemStack stack = getSourceLens();
			mana = lens.getManaToTransfer(this, stack, tile);
		}

		if (tile instanceof IManaCollector) {
			mana *= ((IManaCollector) tile).getManaYieldMultiplier(this);
		}

		tile.receiveMana(mana);

		if (tile instanceof IThrottledPacket) {
			((IThrottledPacket) tile).markDispatchable();
		} else {
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(tile.tileEntity());
		}
	}

	@Override
	public void remove() {
		super.remove();

		if (!fake) {
			BlockEntity tile = getShooter();
			if (tile instanceof IManaSpreader) {
				((IManaSpreader) tile).setCanShoot(true);
			}
		} else {
			setDeathTicksForFakeParticle();
		}
	}

	private BlockEntity getShooter() {
		return world.getBlockEntity(getBurstSourceBlockPos());
	}

	@Override
	protected float getGravity() {
		return getBurstGravity();
	}

	@Override
	public boolean isFake() {
		return fake;
	}

	@Override
	public void setFake(boolean fake) {
		this.fake = fake;
	}

	public void setScanBeam() {
		scanBeam = true;
	}

	@Override
	public int getColor() {
		return dataTracker.get(COLOR);
	}

	@Override
	public void setColor(int color) {
		dataTracker.set(COLOR, color);
	}

	@Override
	public int getMana() {
		return dataTracker.get(MANA);
	}

	@Override
	public void setMana(int mana) {
		dataTracker.set(MANA, mana);
	}

	@Override
	public int getStartingMana() {
		return dataTracker.get(START_MANA);
	}

	@Override
	public void setStartingMana(int mana) {
		dataTracker.set(START_MANA, mana);
	}

	@Override
	public int getMinManaLoss() {
		return dataTracker.get(MIN_MANA_LOSS);
	}

	@Override
	public void setMinManaLoss(int minManaLoss) {
		dataTracker.set(MIN_MANA_LOSS, minManaLoss);
	}

	@Override
	public float getManaLossPerTick() {
		return dataTracker.get(MANA_LOSS_PER_TICK);
	}

	@Override
	public void setManaLossPerTick(float mana) {
		dataTracker.set(MANA_LOSS_PER_TICK, mana);
	}

	@Override
	public float getBurstGravity() {
		return dataTracker.get(GRAVITY);
	}

	@Override
	public void setGravity(float gravity) {
		dataTracker.set(GRAVITY, gravity);
	}

	@Override
	public BlockPos getBurstSourceBlockPos() {
		return dataTracker.get(SOURCE_COORDS);
	}

	@Override
	public void setBurstSourceCoords(BlockPos pos) {
		dataTracker.set(SOURCE_COORDS, pos);
	}

	@Override
	public ItemStack getSourceLens() {
		return dataTracker.get(SOURCE_LENS);
	}

	@Override
	public void setSourceLens(ItemStack lens) {
		dataTracker.set(SOURCE_LENS, lens);
	}

	@Override
	public int getTicksExisted() {
		return _ticksExisted;
	}

	public void setTicksExisted(int ticks) {
		_ticksExisted = ticks;
	}

	private ILensEffect getLensInstance() {
		ItemStack lens = getSourceLens();
		if (!lens.isEmpty() && lens.getItem() instanceof ILensEffect) {
			return (ILensEffect) lens.getItem();
		}

		return null;
	}

	@Override
	public void setBurstMotion(double x, double y, double z) {
		this.setVelocity(x, y, z);
	}

	@Override
	public boolean hasAlreadyCollidedAt(BlockPos pos) {
		return alreadyCollidedAt.contains(pos);
	}

	@Override
	public void setCollidedAt(BlockPos pos) {
		if (!hasAlreadyCollidedAt(pos)) {
			alreadyCollidedAt.add(pos.toImmutable());
		}
	}

	@Override
	public void setShooterUUID(UUID uuid) {
		shooterIdentity = uuid;
	}

	@Override
	public UUID getShooterUUID() {
		return shooterIdentity;
	}

	@Override
	public void ping() {
		BlockEntity tile = getShooter();
		if (tile instanceof IPingable) {
			((IPingable) tile).pingback(this, getShooterUUID());
		}
	}

	@Override
	public boolean hasWarped() {
		return warped;
	}

	@Override
	public void setWarped(boolean warped) {
		this.warped = warped;
	}

	@Override
	public int getOrbitTime() {
		return orbitTime;
	}

	@Override
	public void setOrbitTime(int time) {
		this.orbitTime = time;
	}

	@Override
	public boolean hasTripped() {
		return tripped;
	}

	@Override
	public void setTripped(boolean tripped) {
		this.tripped = tripped;
	}

	@Nullable
	@Override
	public BlockPos getMagnetizedPos() {
		return magnetizePos;
	}

	@Override
	public void setMagnetizePos(@Nullable BlockPos pos) {
		this.magnetizePos = pos;
	}

	@Nonnull
	@Override
	public Packet<?> createSpawnPacket() {
		return PacketSpawnEntity.make(this);
	}

	protected boolean shouldDoFakeParticles() {
		if (ConfigHandler.CLIENT.staticWandBeam.getValue()) {
			return true;
		}

		BlockEntity tile = getShooter();
		return tile instanceof IManaSpreader
				&& (getMana() != getStartingMana() && fullManaLastTick
						|| Math.abs(((IManaSpreader) tile).getBurstParticleTick() - getTicksExisted()) < 4);
	}

	private void incrementFakeParticleTick() {
		BlockEntity tile = getShooter();
		if (tile instanceof IManaSpreader) {
			IManaSpreader spreader = (IManaSpreader) tile;
			spreader.setBurstParticleTick(spreader.getBurstParticleTick() + 2);
			if (spreader.getLastBurstDeathTick() != -1 && spreader.getBurstParticleTick() > spreader.getLastBurstDeathTick()) {
				spreader.setBurstParticleTick(0);
			}
		}
	}

	private void setDeathTicksForFakeParticle() {
		BlockPos coords = getBurstSourceBlockPos();
		BlockEntity tile = world.getBlockEntity(coords);
		if (tile != null && tile instanceof IManaSpreader) {
			((IManaSpreader) tile).setLastBurstDeathTick(getTicksExisted());
		}
	}

	public static class PositionProperties {

		public final BlockPos coords;
		public final BlockState state;

		public boolean invalid = false;

		public PositionProperties(Entity entity) {
			int x = MathHelper.floor(entity.getX());
			int y = MathHelper.floor(entity.getY());
			int z = MathHelper.floor(entity.getZ());
			coords = new BlockPos(x, y, z);
			state = entity.world.getBlockState(coords);
		}

		public boolean coordsEqual(PositionProperties props) {
			return coords.equals(props.coords);
		}

		public boolean contentsEqual(World world) {
			if (!world.isChunkLoaded(coords)) {
				invalid = true;
				return false;
			}

			return world.getBlockState(coords) == state;
		}

		@Override
		public int hashCode() {
			return Objects.hash(coords, state);
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof PositionProperties
					&& ((PositionProperties) o).state == state
					&& ((PositionProperties) o).coords.equals(coords);
		}
	}

}
