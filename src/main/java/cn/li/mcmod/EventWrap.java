package cn.li.mcmod;

//public class FMLCommonEvent {
//}

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;

public class EventWrap {
    public static class FMLCommonSetupEventWrap implements java.util.function.Consumer<FMLCommonSetupEvent> {

        @Override
        public void accept(FMLCommonSetupEvent fmlCommonSetupEvent) {

        }
    }

    public class FMLClientSetupEventWrap implements java.util.function.Consumer<FMLClientSetupEvent> {

        @Override
        public void accept(FMLClientSetupEvent fmlCommonSetupEvent) {

        }
    }

    public class InterModEnqueueEventWrap implements java.util.function.Consumer<InterModEnqueueEvent> {

        @Override
        public void accept(InterModEnqueueEvent fmlCommonSetupEvent) {

        }
    }

    public class InterModProcessEventWrap implements java.util.function.Consumer<InterModProcessEvent> {

        @Override
        public void accept(InterModProcessEvent fmlCommonSetupEvent) {

        }
    }
}


