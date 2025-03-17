package cn.academy.forge.v1_12_2.block;

import cn.academy.api.block.IBlockState;
import net.minecraft.block.properties.IProperty;

public class ForgeBlockStateWrapper implements IBlockState {
    private final net.minecraft.block.state.IBlockState state;

    public ForgeBlockStateWrapper(net.minecraft.block.state.IBlockState state) {
        this.state = state;
    }

    @Override
    public IBlockState withProperty(Object property, Object value) {
        return new ForgeBlockStateWrapper(
            state.withProperty((IProperty)property, value)
        );
    }

    @Override
    public Object getProperty(Object property) {
        return state.getValue((IProperty)property);
    }

    @Override
    public Object getDefaultState() {
        return state.getBlock().getDefaultState();
    }
}