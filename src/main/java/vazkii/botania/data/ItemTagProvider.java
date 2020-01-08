package vazkii.botania.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.lib.ModTags;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Comparator;

public class ItemTagProvider extends ItemTagsProvider {
    public ItemTagProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerTags() {
        this.copy(BlockTags.RAILS, ItemTags.RAILS);
        this.copy(BlockTags.SLABS, ItemTags.SLABS);
        this.copy(BlockTags.STAIRS, ItemTags.STAIRS);
        this.copy(BlockTags.WALLS, ItemTags.WALLS);
        this.copy(BlockTags.FENCES, ItemTags.FENCES);

        this.copy(ModTags.Blocks.MUNDANE_FLOATING_FLOWERS, ModTags.Items.MUNDANE_FLOATING_FLOWERS);
        this.copy(ModTags.Blocks.SPECIAL_FLOATING_FLOWERS, ModTags.Items.SPECIAL_FLOATING_FLOWERS);
        this.copy(ModTags.Blocks.FLOATING_FLOWERS, ModTags.Items.FLOATING_FLOWERS);
        this.copy(ModTags.Blocks.DOUBLE_MYSTICAL_FLOWERS, ModTags.Items.DOUBLE_MYSTICAL_FLOWERS);
        this.copy(ModTags.Blocks.MYSTICAL_FLOWERS, ModTags.Items.MYSTICAL_FLOWERS);

        Tag.Builder<Item> builder = getBuilder(ModTags.Items.DYES);
        Arrays.stream(DyeColor.values()).map(ModItems::getDye).sorted(Comparator.comparing(Item::getRegistryName)).forEach(builder::add);
    }

    @Nonnull
    @Override
    public String getName() {
        return "Botania item tags";
    }
}
