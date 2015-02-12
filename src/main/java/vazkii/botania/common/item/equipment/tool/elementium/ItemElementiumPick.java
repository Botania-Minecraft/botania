package vazkii.botania.common.item.equipment.tool.elementium;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.oredict.OreDictionary;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.equipment.tool.ItemTerraPick;
import vazkii.botania.common.item.equipment.tool.manasteel.ItemManasteelPick;
import vazkii.botania.common.lib.LibItemNames;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ItemElementiumPick extends ItemManasteelPick {

	static final List<String> validBlocks = Arrays.asList(new String[] {
			"dirt", "sand", "gravel", "cobblestone", "netherrack"
	});

	public ItemElementiumPick() {
		super(BotaniaAPI.elementiumToolMaterial, LibItemNames.ELEMENTIUM_PICK);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onHarvestDrops(HarvestDropsEvent event) {
		if(event.harvester != null) {
			ItemStack stack = event.harvester.getCurrentEquippedItem();
			if(stack != null && (stack.getItem() == this || stack.getItem() == ModItems.terraPick && ItemTerraPick.isTipped(stack)))
				for(int i = 0; i < event.drops.size(); i++) {
					ItemStack drop = event.drops.get(i);
					if(drop != null) {
						Block block = Block.getBlockFromItem(drop.getItem());
						if(block != null && isDisposable(block))
							event.drops.remove(i);
					}
				}
		}
	}

	public static boolean isDisposable(Block block) {
		for(int id : OreDictionary.getOreIDs(new ItemStack(block)))
			if(validBlocks.contains(OreDictionary.getOreName(id)))
				return true;

		return false;
	}

}
