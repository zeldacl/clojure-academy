package cn.test;


import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
public class BlockRegistryEvent {
    private static final Logger LOGGER = LogManager.getLogger();
    @SubscribeEvent
    public static void onBlocksRegistry(RegistryEvent.Register<Block> blockRegistryEvent) {
        // register a new block here
        LOGGER.info("HELLO from Register Blockvvvvvvvvvv*************");
    }

    public static class Rrr implements java.util.function.Consumer<FMLCommonSetupEvent> {

        @Override
        public void accept(FMLCommonSetupEvent fmlCommonSetupEvent) {

        }
    }
//    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
//    public static class RegistryEvents {
//
//        private static final Logger LOGGER = LogManager.getLogger();
//
//        @SubscribeEvent
//        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
//            // register a new block here
//            LOGGER.info("HELLO from Register Blockoooooooooooooooo");
//        }
//    }
}

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
class RegistryEvents {

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onBlocksRegistry(RegistryEvent.Register<Block> blockRegistryEvent) {
        // register a new block here
        LOGGER.info("HELLO from Register Blockvvvvvvvvvvvvvvvvvv");
    }
}





//public class test {
//}
