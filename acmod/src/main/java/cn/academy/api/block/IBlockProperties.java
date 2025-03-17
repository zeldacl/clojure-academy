package cn.academy.api.block;

public interface IBlockProperties {
    Object createBooleanProperty(String name);
    Object createIntegerProperty(String name, int min, int max);
    Object getBlockMaterial(String name);
}