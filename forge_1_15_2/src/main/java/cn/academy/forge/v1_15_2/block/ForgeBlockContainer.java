package cn.academy.forge.v1_15_2.block;

import cn.academy.api.block.IBlockContainer;
import cn.academy.api.block.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ForgeBlockContainer extends Block implements IBlockContainer {
    
    public ForgeBlockContainer(Material material) {
        super(Block.Properties.create(material));
    }

    @Override
    public void setHardness(float hardness) {
        // In 1.15+ we need to override the block properties
        this.properties = this.properties.hardnessAndResistance(hardness);
    }

    @Override
    public void setHarvestLevel(String toolClass, int level) {
        this.properties = this.properties.harvestLevel(level);
    }

    @Override
    public Object createTileEntity(Object world, int meta) {
        return createTileEntity(getDefaultState(), (World)world);
    }

    @Override
    public void onBlockPlaced(Object world, Object pos, Object state, Object player, Object stack) {
        // Note: Method signature changed in 1.15+
        onBlockPlacedBy((World)world, (BlockPos)pos, (BlockState)state, 
                       (PlayerEntity)player, (ItemStack)stack);
    }

    @Override
    public IBlockState getActualState(Object world, Object pos) {
        BlockState state = getDefaultState();
        // In 1.15+ getActualState is handled differently
        return new ForgeBlockStateWrapper(state);
    }
}package cn.academy.forge.v1_15_2.block;

public class ForgeBlockContainer {
    
}
