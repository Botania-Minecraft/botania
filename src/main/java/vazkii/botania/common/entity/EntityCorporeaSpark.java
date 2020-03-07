/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Feb 13, 2015, 10:52:40 PM (GMT)]
 */
package vazkii.botania.common.entity;

import com.google.common.base.Predicates;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ObjectHolder;
import vazkii.botania.api.corporea.ICorporeaSpark;
import vazkii.botania.api.corporea.InvWithLocation;
import vazkii.botania.common.core.helper.InventoryHelper;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.material.ItemDye;
import vazkii.botania.common.lib.LibMisc;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class EntityCorporeaSpark extends EntitySparkBase implements ICorporeaSpark {
	@ObjectHolder(LibMisc.MOD_ID + ":corporea_spark")
	public static EntityType<EntityCorporeaSpark> TYPE;

	private static final int SCAN_RANGE = 8;

	private static final String TAG_MASTER = "master";

	private static final DataParameter<Boolean> MASTER = EntityDataManager.createKey(EntityCorporeaSpark.class, DataSerializers.BOOLEAN);
	private static final DataParameter<Integer> ITEM_DISPLAY_TICKS = EntityDataManager.createKey(EntityCorporeaSpark.class, DataSerializers.VARINT);
	private static final DataParameter<ItemStack> DISPLAY_STACK = EntityDataManager.createKey(EntityCorporeaSpark.class, DataSerializers.ITEMSTACK);

	private ICorporeaSpark master;
	private List<ICorporeaSpark> connections = new ArrayList<>();
	private List<ICorporeaSpark> relatives = new ArrayList<>();
	private boolean firstTick = true;

	public EntityCorporeaSpark(EntityType<EntityCorporeaSpark> type, World world) {
		super(type, world);
	}

	public EntityCorporeaSpark(World world) {
		this(TYPE, world);
	}

	@Override
	protected void registerData() {
		super.registerData();
		dataManager.register(MASTER, false);
		dataManager.register(ITEM_DISPLAY_TICKS, 0);
		dataManager.register(DISPLAY_STACK, ItemStack.EMPTY);
	}

	@Nonnull
	@Override
	public ItemStack getPickedResult(RayTraceResult target) {
		return isMaster() ? new ItemStack(ModItems.corporeaSparkMaster) : new ItemStack(ModItems.corporeaSpark);
	}

	@Override
	public void tick() {
		super.tick();

		if(world.isRemote)
			return;

		InvWithLocation inv = getSparkInventory();
		if(inv == null) {
			dropAndKill();
			return;
		}

		if(isMaster())
			master = this;

		if(firstTick) {
			if(isMaster())
				restartNetwork();
			else findNetwork();

			firstTick = false;
		}

		if(master != null && (((Entity) master).removed || master.getNetwork() != getNetwork()))
			master = null;

		int displayTicks = getItemDisplayTicks();
		if(displayTicks > 0)
			setItemDisplayTicks(displayTicks - 1);
		else if(displayTicks < 0)
			setItemDisplayTicks(displayTicks + 1);
	}

	private void dropAndKill() {
		entityDropItem(new ItemStack(isMaster() ? ModItems.corporeaSparkMaster : ModItems.corporeaSpark), 0F);
		remove();
	}

	@Override
	public void remove() {
		super.remove();
		connections.remove(this);
		restartNetwork();
	}

	@Override
	public void registerConnections(ICorporeaSpark master, ICorporeaSpark referrer, List<ICorporeaSpark> connections) {
		relatives.clear();
		for(ICorporeaSpark spark : getNearbySparks()) {
			if(spark == null || connections.contains(spark) || spark.getNetwork() != getNetwork() || spark.isMaster() || ((Entity) spark).removed)
				continue;

			connections.add(spark);
			relatives.add(spark);
			spark.registerConnections(master, this, connections);
		}

		this.master = master;
		this.connections = connections;
	}

	@SuppressWarnings("unchecked")
	private List<ICorporeaSpark> getNearbySparks() {
		return (List) world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(getX() - SCAN_RANGE, getY() - SCAN_RANGE, getZ() - SCAN_RANGE, getX() + SCAN_RANGE, getY() + SCAN_RANGE, getZ() + SCAN_RANGE), Predicates.instanceOf(ICorporeaSpark.class));
	}

	private void restartNetwork() {
		connections = new ArrayList<>();
		relatives = new ArrayList<>();

		if(master != null) {
			ICorporeaSpark oldMaster = master;
			master = null;

			oldMaster.registerConnections(oldMaster, this, new ArrayList<>());
		}
	}

	private void findNetwork() {
		for(ICorporeaSpark spark : getNearbySparks())
			if(spark.getNetwork() == getNetwork() && !((Entity) spark).removed) {
				ICorporeaSpark master = spark.getMaster();
				if(master != null) {
					this.master = master;
					restartNetwork();

					break;
				}
			}
	}

	private static void displayRelatives(PlayerEntity player, List<ICorporeaSpark> checked, ICorporeaSpark spark) {
		if(spark == null)
			return;

		List<ICorporeaSpark> sparks = spark.getRelatives();
		if(sparks.isEmpty())
			EntitySpark.particleBeam(player, (Entity) spark, (Entity) spark.getMaster());
		else for(ICorporeaSpark endSpark : sparks) {
			if(!checked.contains(endSpark)) {
				EntitySpark.particleBeam(player, (Entity) spark, (Entity) endSpark);
				checked.add(endSpark);
				displayRelatives(player, checked, endSpark);
			}
		}
	}

	@Override
	public InvWithLocation getSparkInventory() {
		int x = MathHelper.floor(getX());
		int y = MathHelper.floor(getY() - 1);
		int z = MathHelper.floor(getZ());
		return InventoryHelper.getInventoryWithLocation(world, new BlockPos(x, y, z), Direction.UP);
	}

	@Override
	public List<ICorporeaSpark> getConnections() {
		return connections;
	}

	@Override
	public List<ICorporeaSpark> getRelatives() {
		return relatives;
	}

	@Override
	public void onItemExtracted(ItemStack stack) {
		setItemDisplayTicks(10);
		setDisplayedItem(stack);
	}

	@Override
	public void onItemsRequested(List<ItemStack> stacks) {
		if(!stacks.isEmpty()) {
			setItemDisplayTicks(-10);
			setDisplayedItem(stacks.get(0));
		}
	}

	@Override
	public ICorporeaSpark getMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		dataManager.set(MASTER, master);
	}

	@Override
	public boolean isMaster() {
		return dataManager.get(MASTER);
	}

	public int getItemDisplayTicks() {
		return dataManager.get(ITEM_DISPLAY_TICKS);
	}

	public void setItemDisplayTicks(int ticks) {
		dataManager.set(ITEM_DISPLAY_TICKS, ticks);
	}

	public ItemStack getDisplayedItem() {
		return dataManager.get(DISPLAY_STACK);
	}

	public void setDisplayedItem(ItemStack stack) {
		dataManager.set(DISPLAY_STACK, stack);
	}

	@Override
	public boolean processInitialInteract(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if(!removed && !stack.isEmpty()) {
			if(player.world.isRemote) {
				boolean valid = stack.getItem() == ModItems.twigWand || stack.getItem() instanceof ItemDye || stack.getItem() == ModItems.phantomInk;
				if(valid)
					player.swingArm(hand);
				return valid;
			}

			if(stack.getItem() == ModItems.twigWand) {
				if(player.isSneaking()) {
					dropAndKill();
					if(isMaster())
						restartNetwork();
				} else {
					displayRelatives(player, new ArrayList<>(), master);
				}
				return true;
			} else if(stack.getItem() instanceof ItemDye) {
				DyeColor color = ((ItemDye) stack.getItem()).color;
				if(color != getNetwork()) {
					setNetwork(color);

					if(isMaster())
						restartNetwork();
					else findNetwork();

					stack.shrink(1);
					return true;
				}
			} else if(stack.getItem() == ModItems.phantomInk) {
				setInvisible(true);
				return true;
			}
		}

		return false;
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void readAdditional(@Nonnull CompoundNBT cmp) {
		super.readAdditional(cmp);
		setMaster(cmp.getBoolean(TAG_MASTER));
	}

	@Override
	protected void writeAdditional(@Nonnull CompoundNBT cmp) {
		super.writeAdditional(cmp);
		cmp.putBoolean(TAG_MASTER, isMaster());
	}

}
