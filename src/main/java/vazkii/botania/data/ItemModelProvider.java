package vazkii.botania.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.item.BlockItem;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import vazkii.botania.common.block.decor.BlockFloatingFlower;
import vazkii.botania.common.lib.LibMisc;

import javax.annotation.Nonnull;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ItemModelProvider extends net.minecraftforge.client.model.generators.ItemModelProvider {
	public ItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, LibMisc.MOD_ID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		Registry.ITEM.stream().filter(i -> LibMisc.MOD_ID.equals(i.getRegistryName().getNamespace()))
				.forEach(i -> {
					// todo 1.15 expand to all item models that simply reference their parent
					if (i instanceof BlockItem && ((BlockItem) i).getBlock() instanceof BlockFloatingFlower) {
						String name = i.getRegistryName().getPath();
						withExistingParent(name, prefix("block/" + name));
					}
				});
	}

	@Nonnull
	@Override
	public String getName() {
		return "Botania item models";
	}
}
