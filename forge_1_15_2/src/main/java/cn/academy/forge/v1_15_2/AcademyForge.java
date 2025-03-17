package cn.academy.forge.v1_15_2;

import cn.academy.common.block.BlockFactoryInitializer;
import cn.academy.forge.v1_15_2.block.ForgeBlockFactory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("academy")
public class AcademyForge {
    
    public AcademyForge() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        // Initialize the block factory for 1.15.2
        BlockFactoryInitializer.initializeFactory("1.15.2", new ForgeBlockFactory());
    }
}