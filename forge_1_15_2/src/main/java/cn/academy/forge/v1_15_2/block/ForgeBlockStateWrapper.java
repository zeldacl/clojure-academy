package cn.academy.forge.v1_15_2.block;

import cn.academy.api.block.IBlockState;
import net.minecraft.state.Property;
import net.minecraft.block.BlockState;

public class ForgeBlockStateWrapper implements IBlockState {
    private final BlockState state;

    public ForgeBlockStateWrapper(BlockState state) {
        this.state = state;
    }

    @Override
    public IBlockState withProperty(Object property, Object value) {
        return new ForgeBlockStateWrapper(
            state.with((Property)property, value)
        );
    }

    @Override
    public Object getProperty(Object property) {
        return state.get((Property)property);
    }

    @Override
    public Object getDefaultState() {
        return state.getBlock().getDefaultState();
    }
}