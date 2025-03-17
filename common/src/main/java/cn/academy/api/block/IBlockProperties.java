package cn.academy.api.block;

public interface IForgeBlockFactory {
    IBlockProperties createBlockProperties();
    IBlockContainer createBlockContainer(String material);
    String getBlockMaterial(String type);
}

public interface IBlockProperties {
    Object createBooleanProperty(String name);
    Object createIntegerProperty(String name, int min, int max);
    Object createEnumProperty(String name, Set<String> values);
}

public interface IBlockContainer {
    void setHardness(float hardness);
    void setHarvestLevel(String tool, int level);
    void addProperty(Object property);
    void setBlockStateHandler(BlockStateHandler handler);
    void setTileEntityProvider(String className);
    void setNodeProperties(Map<String, Object> properties);
}

public interface BlockStateHandler {
    void handleState(Object state, Object world, Object pos);
}