package cn.academy.forge.v1_12_2.block;

import cn.academy.api.block.*;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
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
        return new ItemStack((Block)block, count, meta);
    }

    private static class ForgeBlockProperties implements IBlockProperties {
        @Override
        public Object createBooleanProperty(String name) {
            return PropertyBool.create(name);
        }

        @Override
        public Object createIntegerProperty(String name, int min, int max) {
            return PropertyInteger.create(name, min, max);
        }

        @Override
        public Object getBlockMaterial(String name) {
            if ("rock".equals(name)) {
                return Material.ROCK;
            }
            throw new IllegalArgumentException("Unknown material: " + name);
        }
    }
}