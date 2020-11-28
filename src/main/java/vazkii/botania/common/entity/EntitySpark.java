/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.entity;

import net.fabricmc.fabric.api.entity.EntityPickInteractionAware;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.spark.ISparkAttachable;
import vazkii.botania.api.mana.spark.ISparkEntity;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.api.mana.spark.SparkUpgradeType;
import vazkii.botania.common.item.ItemSparkUpgrade;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.network.PacketBotaniaEffect;
import vazkii.botania.common.network.PacketSpawnEntity;

import javax.annotation.Nonnull;

import java.util.*;
import java.util.stream.Collectors;

public class EntitySpark extends EntitySparkBase implements ISparkEntity, EntityPickInteractionAware {
	private static final int TRANSFER_RATE = 1000;
	private static final String TAG_UPGRADE = "upgrade";
	private static final TrackedData<Integer> UPGRADE = DataTracker.registerData(EntitySpark.class, TrackedDataHandlerRegistry.INTEGER);

	private final Set<ISparkEntity> transfers = Collections.newSetFromMap(new WeakHashMap<>());

	private int removeTransferants = 2;

	public EntitySpark(EntityType<EntitySpark> type, World world) {
		super(type, world);
	}

	public EntitySpark(World world) {
		this(ModEntities.SPARK, world);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		dataTracker.startTracking(UPGRADE, 0);
	}

	@Nonnull
	@Override
	public ItemStack getPickedStack(PlayerEntity player, HitResult target) {
		return new ItemStack(ModItems.spark);
	}

	@Override
	public void tick() {
		super.tick();

		if (world.isClient) {
			return;
		}

		ISparkAttachable tile = getAttachedTile();
		if (tile == null) {
			dropAndKill();
			return;
		}

		SparkUpgradeType upgrade = getUpgrade();
		Collection<ISparkEntity> transfers = getTransfers();

		switch (upgrade) {
		case DISPERSIVE: {
			List<PlayerEntity> players = SparkHelper.getEntitiesAround(PlayerEntity.class, world, getX(), getY() + (getHeight() / 2.0), getZ());

			Map<PlayerEntity, Map<ItemStack, Integer>> receivingPlayers = new HashMap<>();

			ItemStack input = new ItemStack(ModItems.spark);
			for (PlayerEntity player : players) {
				List<ItemStack> stacks = new ArrayList<>();
				stacks.addAll(player.inventory.main);
				stacks.addAll(player.inventory.armor);

				Inventory inv = BotaniaAPI.instance().getAccessoriesInventory(player);
				for (int i = 0; i < inv.size(); i++) {
					stacks.add(inv.getStack(i));
				}

				for (ItemStack stack : stacks) {
					if (stack.isEmpty() || !(stack.getItem() instanceof IManaItem)) {
						continue;
					}

					IManaItem manaItem = (IManaItem) stack.getItem();
					if (manaItem.canReceiveManaFromItem(stack, input)) {
						Map<ItemStack, Integer> receivingStacks;
						boolean add = false;
						if (!receivingPlayers.containsKey(player)) {
							add = true;
							receivingStacks = new HashMap<>();
						} else {
							receivingStacks = receivingPlayers.get(player);
						}

						int recv = Math.min(getAttachedTile().getCurrentMana(), Math.min(TRANSFER_RATE, manaItem.getMaxMana(stack) - manaItem.getMana(stack)));
						if (recv > 0) {
							receivingStacks.put(stack, recv);
							if (add) {
								receivingPlayers.put(player, receivingStacks);
							}
						}
					}
				}
			}

			if (!receivingPlayers.isEmpty()) {
				List<PlayerEntity> keys = new ArrayList<>(receivingPlayers.keySet());
				Collections.shuffle(keys);
				PlayerEntity player = keys.iterator().next();

				Map<ItemStack, Integer> items = receivingPlayers.get(player);
				ItemStack stack = items.keySet().iterator().next();
				int cost = items.get(stack);
				int manaToPut = Math.min(getAttachedTile().getCurrentMana(), cost);
				((IManaItem) stack.getItem()).addMana(stack, manaToPut);
				getAttachedTile().receiveMana(-manaToPut);
				particlesTowards(player);
			}

			break;
		}
		case DOMINANT: {
			List<ISparkEntity> validSparks = SparkHelper.getSparksAround(world, getX(), getY() + (getHeight() / 2), getZ(), getNetwork())
					.filter(s -> {
						SparkUpgradeType otherUpgrade = s.getUpgrade();
						return s != this && otherUpgrade == SparkUpgradeType.NONE && s.getAttachedTile() instanceof IManaPool;
					})
					.collect(Collectors.toList());
			if (validSparks.size() > 0) {
				validSparks.get(world.random.nextInt(validSparks.size())).registerTransfer(this);
			}

			break;
		}
		case RECESSIVE: {
			SparkHelper.getSparksAround(world, getX(), getY() + (getHeight() / 2), getZ(), getNetwork())
					.filter(s -> {
						SparkUpgradeType otherUpgrade = s.getUpgrade();
						return s != this
								&& otherUpgrade != SparkUpgradeType.DOMINANT
								&& otherUpgrade != SparkUpgradeType.RECESSIVE
								&& otherUpgrade != SparkUpgradeType.ISOLATED;
					})
					.forEach(transfers::add);
			break;
		}
		case NONE:
		default:
			break;
		}

		if (!transfers.isEmpty()) {
			int manaTotal = Math.min(TRANSFER_RATE * transfers.size(), tile.getCurrentMana());
			int manaForEach = manaTotal / transfers.size();
			int manaSpent = 0;

			if (manaForEach > transfers.size()) {
				for (ISparkEntity spark : transfers) {
					if (spark.getAttachedTile() == null || spark.getAttachedTile().isFull() || spark.areIncomingTransfersDone()) {
						manaTotal -= manaForEach;
						continue;
					}

					ISparkAttachable attached = spark.getAttachedTile();
					int spend = Math.min(attached.getAvailableSpaceForMana(), manaForEach);
					attached.receiveMana(spend);
					manaSpent += spend;

					particlesTowards((Entity) spark);
				}
				tile.receiveMana(-manaSpent);
			}
		}

		if (removeTransferants > 0) {
			removeTransferants--;
		}
		filterTransfers();
	}

	private void particlesTowards(Entity e) {
		PacketBotaniaEffect.sendNearby(this, PacketBotaniaEffect.EffectType.SPARK_MANA_FLOW, getX(), getY(), getZ(),
				getEntityId(), e.getEntityId());
	}

	public static void particleBeam(PlayerEntity player, Entity e1, Entity e2) {
		if (e1 != null && e2 != null && !e1.world.isClient) {
			PacketBotaniaEffect.send(player, PacketBotaniaEffect.EffectType.SPARK_NET_INDICATOR, e1.getX(), e1.getY(), e1.getZ(),
					e1.getEntityId(), e2.getEntityId());
		}
	}

	private void dropAndKill() {
		SparkUpgradeType upgrade = getUpgrade();
		dropStack(new ItemStack(ModItems.spark), 0F);
		if (upgrade != SparkUpgradeType.NONE) {
			dropStack(ItemSparkUpgrade.getByType(upgrade), 0F);
		}
		remove();
	}

	@Override
	public ActionResult interact(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (isAlive() && !stack.isEmpty()) {
			SparkUpgradeType upgrade = getUpgrade();
			if (stack.getItem() == ModItems.twigWand) {
				if (!world.isClient) {
					if (player.isSneaking()) {
						if (upgrade != SparkUpgradeType.NONE) {
							dropStack(ItemSparkUpgrade.getByType(upgrade), 0F);
							setUpgrade(SparkUpgradeType.NONE);

							transfers.clear();
							removeTransferants = 2;
						} else {
							dropAndKill();
						}
					} else {
						SparkHelper.getSparksAround(world, getX(), getY() + (getHeight() / 2), getZ(), getNetwork())
								.forEach(s -> particleBeam(player, this, (Entity) s));
					}
				}

				return ActionResult.success(world.isClient);
			} else if (stack.getItem() instanceof ItemSparkUpgrade && upgrade == SparkUpgradeType.NONE) {
				if (!world.isClient) {
					setUpgrade(((ItemSparkUpgrade) stack.getItem()).type);
					stack.decrement(1);
				}
				return ActionResult.success(world.isClient);
			} else if (stack.getItem() == ModItems.phantomInk) {
				if (!world.isClient) {
					setInvisible(true);
				}
				return ActionResult.success(world.isClient);
			} else if (stack.getItem() instanceof DyeItem) {
				DyeColor color = ((DyeItem) stack.getItem()).getColor();
				if (color != getNetwork()) {
					if (!world.isClient) {
						setNetwork(color);
						stack.decrement(1);
					}
					return ActionResult.success(world.isClient);
				}
			}
		}

		return ActionResult.PASS;
	}

	@Nonnull
	@Override
	public Packet<?> createSpawnPacket() {
		return PacketSpawnEntity.make(this);
	}

	@Override
	protected void readCustomDataFromTag(@Nonnull CompoundTag cmp) {
		super.readCustomDataFromTag(cmp);
		setUpgrade(SparkUpgradeType.values()[cmp.getInt(TAG_UPGRADE)]);
	}

	@Override
	protected void writeCustomDataToTag(@Nonnull CompoundTag cmp) {
		super.writeCustomDataToTag(cmp);
		cmp.putInt(TAG_UPGRADE, getUpgrade().ordinal());
	}

	@Override
	public ISparkAttachable getAttachedTile() {
		int x = MathHelper.floor(getX());
		int y = MathHelper.floor(getY()) - 1;
		int z = MathHelper.floor(getZ());
		BlockEntity tile = world.getBlockEntity(new BlockPos(x, y, z));
		if (tile instanceof ISparkAttachable) {
			return (ISparkAttachable) tile;
		}

		return null;
	}

	private void filterTransfers() {
		Iterator<ISparkEntity> iter = transfers.iterator();
		while (iter.hasNext()) {
			ISparkEntity spark = iter.next();
			SparkUpgradeType upgr = getUpgrade();
			SparkUpgradeType supgr = spark.getUpgrade();
			ISparkAttachable atile = spark.getAttachedTile();

			if (spark == this
					|| spark.areIncomingTransfersDone()
					|| getNetwork() != spark.getNetwork()
					|| atile == null
					|| atile.isFull()
					|| !(upgr == SparkUpgradeType.NONE && supgr == SparkUpgradeType.DOMINANT
							|| upgr == SparkUpgradeType.RECESSIVE && (supgr == SparkUpgradeType.NONE || supgr == SparkUpgradeType.DISPERSIVE)
							|| !(atile instanceof IManaPool))) {
				iter.remove();
			}
		}
	}

	@Override
	public Collection<ISparkEntity> getTransfers() {
		filterTransfers();
		return transfers;
	}

	private boolean hasTransfer(ISparkEntity entity) {
		return transfers.contains(entity);
	}

	@Override
	public void registerTransfer(ISparkEntity entity) {
		if (hasTransfer(entity)) {
			return;
		}
		transfers.add(entity);
	}

	@Override
	public SparkUpgradeType getUpgrade() {
		return SparkUpgradeType.values()[dataTracker.get(UPGRADE)];
	}

	@Override
	public void setUpgrade(SparkUpgradeType upgrade) {
		dataTracker.set(UPGRADE, upgrade.ordinal());
	}

	@Override
	public boolean areIncomingTransfersDone() {
		ISparkAttachable tile = getAttachedTile();
		if (tile instanceof IManaPool) {
			return removeTransferants > 0;
		}
		return tile != null && tile.areIncomingTranfersDone();
	}

}
