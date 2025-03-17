package cn.academy.api.block;

public interface IForgeBlockFactory {
    IBlockProperties createBlockProperties();
    IBlockContainer createBlockContainer(Object material);
    Object createBlockPos(int x, int y, int z);
    Object createItemStack(Object block, int count, int meta);
}