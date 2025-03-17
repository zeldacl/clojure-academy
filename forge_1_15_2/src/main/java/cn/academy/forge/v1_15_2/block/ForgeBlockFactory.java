package cn.academy.forge.v1_15_2.block;

import cn.academy.api.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;

public class ForgeBlockFactory implements IForgeBlockFactory {
    @Override
    public IBlockProperties createBlockProperties() {
        return new ForgeBlockProperties();
    }

    @Override
    public IBlockContainer createBlockContainer(Object material) {
        return new ForgeBlockContainer((Material)material);
    }

    @Override
    public Object createBlockPos(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    @Override
    public Object createItemStack(Object block, int count, int meta) {
        return new ItemStack((Block)block, count);  // Note: meta is ignored in 1.15+
    }

    private static class ForgeBlockProperties implements IBlockProperties {
        @Override
        public Object createBooleanProperty(String name) {
            return BooleanProperty.create(name);
        }

        @Override
        public Object createIntegerProperty(String name, int min, int max) {
            return IntegerProperty.create(name, min, max);
        }

        @Override
        public Object getBlockMaterial(String name) {
            if ("rock".equals(name)) {
                return Material.ROCK;
            }
            throw new IllegalArgumentException("Unknown material: " + name);
        }
    }
}package cn.academy.forge.v1_15_2.block;

public class ForgeBlockFactory {
    
}
