package cn.academy.forge.v1_12_2.block;

import cn.academy.api.block.IBlockContainer;
import cn.academy.api.block.IBlockState;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ForgeBlockContainer extends BlockContainer implements IBlockContainer {
    
    public ForgeBlockContainer(Material material) {
        super(material);
    }

    @Override
    public void setHardness(float hardness) {
        this.blockHardness = hardness;
    }

    @Override
    public void setHarvestLevel(String toolClass, int level) {
        super.setHarvestLevel(toolClass, level);
    }

    @Override
    public Object createTileEntity(Object world, int meta) {
        return createNewTileEntity((World)world, meta);
    }

    @Override
    public void onBlockPlaced(Object world, Object pos, Object state, Object player, Object stack) {
        onBlockPlacedBy((World)world, (BlockPos)pos, (net.minecraft.block.state.IBlockState)state, 
                       (EntityPlayer)player, (ItemStack)stack);
    }

    @Override
    public IBlockState getActualState(Object world, Object pos) {
        net.minecraft.block.state.IBlockState state = getActualState(
            getDefaultState(), 
            (World)world,
            (BlockPos)pos
        );
        return new ForgeBlockStateWrapper(state);
    }

    @Override
    public net.minecraft.tileentity.TileEntity createNewTileEntity(World worldIn, int meta) {
        return null; // Override in specific implementations
    }
}