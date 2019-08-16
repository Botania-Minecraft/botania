package vazkii.botania.common.world;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import vazkii.botania.api.item.IFlowerlessBiome;
import vazkii.botania.api.item.IFlowerlessWorld;
import vazkii.botania.common.block.BlockModFlower;
import vazkii.botania.common.block.ModBlocks;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.function.Function;

public class MysticalFlowerFeature extends Feature<MysticalFlowerConfig> {
    public MysticalFlowerFeature(Function<Dynamic<?>, ? extends MysticalFlowerConfig> configFactoryIn) {
        super(configFactoryIn);
    }

    @Override
    public boolean place(@Nonnull IWorld world, @Nonnull ChunkGenerator<? extends GenerationSettings> generator, @Nonnull Random rand, @Nonnull BlockPos pos, @Nonnull MysticalFlowerConfig config) {
        boolean flowers = true;
        if(world.getDimension() instanceof IFlowerlessWorld)
            flowers = ((IFlowerlessWorld) world.getDimension()).generateFlowers(world);
        else if(world.getBiome(pos) instanceof IFlowerlessBiome)
            flowers = ((IFlowerlessBiome) world.getBiome(pos)).canGenerateFlowers(world, pos.getX(), pos.getZ());

        if(!flowers)
            return false;

        int dist = Math.min(8, Math.max(1, config.getPatchSize()));
        for(int i = 0; i < config.getPatchCount(); i++) {
            if(rand.nextInt(config.getPatchChance()) == 0) {
                int x = pos.getX() + rand.nextInt(16);
                int z = pos.getZ() + rand.nextInt(16);
                int y = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);

                DyeColor color = DyeColor.byId(rand.nextInt(16));
                BlockState flower = ModBlocks.getFlower(color).getDefaultState();

                for(int j = 0; j < config.getPatchDensity() * config.getPatchChance(); j++) {
                    int x1 = x + rand.nextInt(dist * 2) - dist;
                    int y1 = y + rand.nextInt(4) - rand.nextInt(4);
                    int z1 = z + rand.nextInt(dist * 2) - dist;
                    BlockPos pos2 = new BlockPos(x1, y1, z1);
                    if(world.isAirBlock(pos2) && (!world.getDimension().isNether() || y1 < 127) && flower.isValidPosition(world, pos2)) {
                        world.setBlockState(pos2, flower, 2);
                        if(rand.nextDouble() < config.getTallChance()
                                && ((BlockModFlower) flower.getBlock()).canGrow(world, pos2, world.getBlockState(pos2), false)) {
                            Block block = ModBlocks.getDoubleFlower(color);
                            if(block instanceof DoublePlantBlock) {
                                ((DoublePlantBlock) block).placeAt(world, pos2, 3);
                            }
                        }
                    }
                }
            }
        }

        for(int i = 0; i < config.getMushroomPatchSize(); i++) {
            int x = pos.getX() + rand.nextInt(16) + 8;
            int z = pos.getZ() + rand.nextInt(16) + 8;
            int y = rand.nextInt(26) + 4;
            BlockPos pos3 = new BlockPos(x, y, z);
            DyeColor color = DyeColor.byId(rand.nextInt(16));
            BlockState mushroom = ModBlocks.getMushroom(color).getDefaultState();
            if(world.isAirBlock(pos3) && mushroom.isValidPosition(world, pos3))
                world.setBlockState(pos3, mushroom, 2);
        }
        return false;
    }
}
