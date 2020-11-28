/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data;

/*

public class FloatingFlowerModelProvider extends ModelProvider<FloatingFlowerModelBuilder> {
	public FloatingFlowerModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, LibMisc.MOD_ID, BLOCK_FOLDER, FloatingFlowerModelBuilder::new, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		for (Block b : Registry.BLOCK) {
			Identifier id = Registry.BLOCK.getId(b);
			if (LibMisc.MOD_ID.equals(id.getNamespace()) && b instanceof BlockFloatingFlower) {
				String name = id.getPath();
				String nonFloat;
				if (name.endsWith("_floating_flower")) {
					nonFloat = name.replace("_floating_flower", "_mystical_flower");
				} else {
					nonFloat = name.replace("floating_", "");
				}

				getBuilder(name)
						.parent(getExistingFile(new Identifier("block/block")))
						.withFlowerModel(new ModelFile.UncheckedModelFile(prefix("block/" + nonFloat)));
			}
		}
	}

	@Nonnull
	@Override
	public String getName() {
		return "Botania floating flower models";
	}
}
*/
