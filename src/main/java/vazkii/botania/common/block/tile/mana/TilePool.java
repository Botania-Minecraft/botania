/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Jan 26, 2014, 12:23:55 AM (GMT)]
 */
package vazkii.botania.common.block.tile.mana;

import java.awt.Color;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.IKeyLocked;
import vazkii.botania.api.mana.IManaItem;
import vazkii.botania.api.mana.IManaPool;
import vazkii.botania.api.mana.ManaNetworkEvent;
import vazkii.botania.api.recipe.RecipeManaInfusion;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.client.core.handler.LightningHandler;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.TileMod;
import vazkii.botania.common.core.handler.ConfigHandler;
import vazkii.botania.common.core.handler.ManaNetworkHandler;
import vazkii.botania.common.core.helper.Vector3;
import vazkii.botania.common.item.ModItems;

public class TilePool extends TileMod implements IManaPool, IKeyLocked {

	public static final int MAX_MANA = 1000000;

	private static final String TAG_MANA = "mana";
	private static final String TAG_KNOWN_MANA = "knownMana";
	private static final String TAG_OUTPUTTING = "outputting";
	private static final String TAG_COLOR = "color";
	private static final String TAG_MANA_CAP = "manaCap";
	private static final String TAG_CAN_ACCEPT = "canAccept";
	private static final String TAG_CAN_SPARE = "canSpare";
	private static final String TAG_FRAGILE = "fragile";
	private static final String TAG_INPUT_KEY = "inputKey";
	private static final String TAG_OUTPUT_KEY = "outputKey";

	boolean outputting = false;
	public boolean alchemy = false;
	public boolean conjuration = false;

	public int color = 0;
	int mana;
	int knownMana = -1;
	int craftCooldown = 20;

	public int manaCap = MAX_MANA;
	boolean canAccept = true;
	boolean canSpare = true;
	public boolean fragile = false;

	String inputKey = "";
	String outputKey = "";

	@Override
	public boolean isFull() {
		Block blockBelow = worldObj.getBlock(xCoord, yCoord - 1, zCoord);
		return blockBelow != ModBlocks.manaVoid && getCurrentMana() >= manaCap;
	}

	@Override
	public void recieveMana(int mana) {
		boolean full = getCurrentMana() >= manaCap;

		this.mana = Math.min(getCurrentMana() + mana, manaCap);
		if(!full)
			worldObj.func_147453_f(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord));
	}

	@Override
	public void invalidate() {
		super.invalidate();
		ManaNetworkEvent.removePool(this);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		ManaNetworkEvent.removePool(this);
	}

	public boolean collideEntityItem(EntityItem item) {
		if(craftCooldown > 0 || item.isDead)
			return false;

		boolean didChange = false;
		ItemStack stack = item.getEntityItem();
		if(stack == null)
			return false;

		if(stack.getItem() == ModItems.dye && !worldObj.isRemote) {
			int meta = stack.getItemDamage();
			if(meta != color) {
				color = meta;
				stack.stackSize--;
				if(stack.stackSize == 0)
					item.setDead();
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		}

		for(RecipeManaInfusion recipe : BotaniaAPI.manaInfusionRecipes) {
			if(recipe.matches(stack) && (!recipe.isAlchemy() || alchemy) && (!recipe.isConjuration() || conjuration)) {
				int mana = recipe.getManaToConsume();
				if(getCurrentMana() >= mana) {
					recieveMana(-mana);

					if(!worldObj.isRemote) {
						stack.stackSize--;
						if(stack.stackSize == 0)
							item.setDead();

						ItemStack output = recipe.getOutput().copy();
						EntityItem outputItem = new EntityItem(worldObj, xCoord + 0.5, yCoord + 1.5, zCoord + 0.5, output);
						outputItem.age = 55;
						worldObj.spawnEntityInWorld(outputItem);
					}

					craftCooldown = 20;
					craftingFanciness();
					didChange = true;
				}

				break;
			}
		}

		return didChange;
	}

	public void craftingFanciness() {
		worldObj.playSoundEffect(xCoord, yCoord, zCoord, "random.levelup", 0.5F, 4F);
		for(int i = 0; i < 25; i++) {
			float red = (float) Math.random();
			float green = (float) Math.random();
			float blue = (float) Math.random();
			Botania.proxy.sparkleFX(worldObj, xCoord + 0.5 + Math.random() * 0.4 - 0.2, yCoord + 1, zCoord + 0.5 + Math.random() * 0.4 - 0.2, red, green, blue, (float) Math.random(), 10);
		}
	}

	@Override
	public void updateEntity() {
		if(!ManaNetworkHandler.instance.isPoolIn(this))
			ManaNetworkEvent.addPool(this);

		if(worldObj.isRemote) {
			double particleChance = 1F - (double) getCurrentMana() / (double) MAX_MANA * 0.1;
			Color color = new Color(0x00C6FF);
			if(Math.random() > particleChance)
				Botania.proxy.wispFX(worldObj, xCoord + 0.3 + Math.random() * 0.5, yCoord + 0.6 + Math.random() * 0.25, zCoord + Math.random(), color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, (float) Math.random() / 3F, (float) -Math.random() / 25F, 2F);
		}

		alchemy = worldObj.getBlock(xCoord, yCoord - 1, zCoord) == ModBlocks.alchemyCatalyst;
		conjuration = worldObj.getBlock(xCoord, yCoord - 1, zCoord) == ModBlocks.conjurationCatalyst;

		if(craftCooldown > 0)
			craftCooldown--;

		List<EntityItem> items = worldObj.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1));
		for(EntityItem item : items) {
			if(item.isDead)
				continue;

			ItemStack stack = item.getEntityItem();
			if(stack != null && stack.getItem() instanceof IManaItem) {
				IManaItem mana = (IManaItem) stack.getItem();
				if(outputting && mana.canReceiveManaFromPool(stack, this) || !outputting && mana.canExportManaToPool(stack, this)) {
					boolean didSomething = false;

					if(outputting) {
						if(canSpare) {
							if(getCurrentMana() > 0)
								didSomething = true;

							if(!worldObj.isRemote) {
								int manaVal = Math.min(1000, Math.min(getCurrentMana(), mana.getMaxMana(stack) - mana.getMana(stack)));
								mana.addMana(stack, manaVal);
								recieveMana(-manaVal);
							}
						}
					} else {
						if(canAccept) {
							if(mana.getMana(stack) > 0)
								didSomething = true;

							if(!worldObj.isRemote) {
								int manaVal = Math.min(1000, Math.min(MAX_MANA - getCurrentMana(), mana.getMana(stack)));
								mana.addMana(stack, -manaVal);
								recieveMana(manaVal);
							}
						}
					}

					if(didSomething) {
						worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
						if(worldObj.isRemote && ConfigHandler.chargingAnimationEnabled && worldObj.rand.nextInt(5) == 0) {
							Vector3 itemVec = Vector3.fromTileEntity(this).add(0.5, 0.5 + Math.random() * 0.3, 0.5);
							Vector3 tileVec = Vector3.fromTileEntity(this).add(0.2 + Math.random() * 0.6, 0, 0.2 + Math.random() * 0.6);
							LightningHandler.spawnLightningBolt(worldObj, outputting ? tileVec : itemVec, outputting ? itemVec : tileVec, 80, worldObj.rand.nextLong(), 0x4400799c, 0x4400C6FF);
						}
					}
				}
			}
		}
	}

	@Override
	public void writeCustomNBT(NBTTagCompound cmp) {
		cmp.setInteger(TAG_MANA, mana);
		cmp.setBoolean(TAG_OUTPUTTING, outputting);
		cmp.setInteger(TAG_COLOR, color);

		cmp.setInteger(TAG_MANA_CAP, manaCap);
		cmp.setBoolean(TAG_CAN_ACCEPT, canAccept);
		cmp.setBoolean(TAG_CAN_SPARE, canSpare);
		cmp.setBoolean(TAG_FRAGILE, fragile);

		cmp.setString(TAG_INPUT_KEY, inputKey);
		cmp.setString(TAG_OUTPUT_KEY, outputKey);
	}

	@Override
	public void readCustomNBT(NBTTagCompound cmp) {
		mana = cmp.getInteger(TAG_MANA);
		outputting = cmp.getBoolean(TAG_OUTPUTTING);
		color = cmp.getInteger(TAG_COLOR);

		if(cmp.hasKey(TAG_MANA_CAP))
			manaCap = cmp.getInteger(TAG_MANA_CAP);
		if(cmp.hasKey(TAG_CAN_ACCEPT))
			canAccept = cmp.getBoolean(TAG_CAN_ACCEPT);
		if(cmp.hasKey(TAG_CAN_SPARE))
			canSpare = cmp.getBoolean(TAG_CAN_SPARE);
		fragile = cmp.getBoolean(TAG_FRAGILE);

		if(cmp.hasKey(TAG_INPUT_KEY))
			inputKey = cmp.getString(TAG_INPUT_KEY);
		if(cmp.hasKey(TAG_OUTPUT_KEY))
			inputKey = cmp.getString(TAG_OUTPUT_KEY);

		if(cmp.hasKey(TAG_KNOWN_MANA))
			knownMana = cmp.getInteger(TAG_KNOWN_MANA);
	}

	public void onWanded(EntityPlayer player, ItemStack wand) {
		if(player == null)
			return;

		if(player.isSneaking()) {
			outputting = !outputting;
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}

		if(!worldObj.isRemote) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			writeCustomNBT(nbttagcompound);
			nbttagcompound.setInteger(TAG_KNOWN_MANA, getCurrentMana());
			if(player instanceof EntityPlayerMP)
				((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, -999, nbttagcompound));
		}

		worldObj.playSoundAtEntity(player, "random.orb", 0.11F, 1F);
	}

	public void renderHUD(Minecraft mc, ScaledResolution res) {
		String name = StatCollector.translateToLocal(new ItemStack(ModBlocks.pool, 1, getBlockMetadata()).getUnlocalizedName().replaceAll("tile.", "tile." + LibResources.PREFIX_MOD) + ".name");
		int color = 0x660000FF;
		HUDHandler.drawSimpleManaHUD(color, knownMana, MAX_MANA, name, res);

		String power = StatCollector.translateToLocal("botaniamisc." + (outputting ? "outputtingPower" : "inputtingPower"));
		int x = res.getScaledWidth() / 2 - mc.fontRenderer.getStringWidth(power) / 2;
		int y = res.getScaledHeight() / 2 + 30;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		mc.fontRenderer.drawStringWithShadow(power, x, y, color);
		GL11.glDisable(GL11.GL_BLEND);
	}

	@Override
	public boolean canRecieveManaFromBursts() {
		return true;
	}

	@Override
	public boolean isOutputtingPower() {
		return outputting;
	}

	@Override
	public int getCurrentMana() {
		return worldObj != null && getBlockMetadata() == 1 ? MAX_MANA : mana;
	}

	@Override
	public String getInputKey() {
		return inputKey;
	}

	@Override
	public String getOutputKey() {
		return outputKey;
	}
}
