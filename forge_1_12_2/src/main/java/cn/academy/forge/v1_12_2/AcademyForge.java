package cn.academy.forge.v1_12_2;

import cn.academy.common.block.BlockFactoryInitializer;
import cn.academy.forge.v1_12_2.block.ForgeBlockFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "academy", name = "Academy Craft", version = "1.0")
public class AcademyForge {
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize the block factory for 1.12.2
        BlockFactoryInitializer.initializeFactory("1.12.2", new ForgeBlockFactory());
    }
}