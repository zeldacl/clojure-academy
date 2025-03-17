package cn.academy.api.block;

public interface IBlockState {
    IBlockState withProperty(Object property, Object value);
    Object getProperty(Object property);
    Object getDefaultState();
}