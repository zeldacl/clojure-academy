package cn.academy.api.block;

public interface IBlockContainer {
    void setHardness(float hardness);
    void setHarvestLevel(String toolClass, int level);
    Object createTileEntity(Object world, int meta);
    void onBlockPlaced(Object world, Object pos, Object state, Object player, Object stack);
    IBlockState getActualState(Object world, Object pos);
}