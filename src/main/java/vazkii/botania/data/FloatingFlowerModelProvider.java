package vazkii.botania.data;

import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelProvider;
import vazkii.botania.common.block.decor.BlockFloatingFlower;
import vazkii.botania.common.lib.LibMisc;

import javax.annotation.Nonnull;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class FloatingFlowerModelProvider extends ModelProvider<FloatingFlowerModelBuilder> {
	public FloatingFlowerModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, LibMisc.MOD_ID, BLOCK_FOLDER, FloatingFlowerModelBuilder::new, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		for (Block b : Registry.BLOCK) {
			if (LibMisc.MOD_ID.equals(b.getRegistryName().getNamespace()) && b instanceof BlockFloatingFlower) {
				String name = b.getRegistryName().getPath();
				String nonFloat;
				if (name.endsWith("_floating_flower")) {
					nonFloat = name.replace("_floating_flower", "_mystical_flower");
				} else {
					nonFloat = name.replace("floating_", "");
				}

				getBuilder(name)
						.parent(getExistingFile(new ResourceLocation("block/block")))
						.withFlowerModel(getExistingFile(prefix(nonFloat)));
			}
		}
	}

	@Nonnull
	@Override
	public String getName() {
		return "Botania floating flower models";
	}
}
