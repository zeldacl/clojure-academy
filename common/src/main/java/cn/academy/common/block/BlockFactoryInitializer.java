package cn.academy.common.block;

import cn.academy.api.block.IForgeBlockFactory;
import cn.academy.block.block.BlockNode;

public class BlockFactoryInitializer {
    
    public static void initializeFactory(String version, IForgeBlockFactory factory) {
        // Set the factory in the BlockNode factory class
        BlockNode.Factory.setForgeFactory(factory);
    }
}