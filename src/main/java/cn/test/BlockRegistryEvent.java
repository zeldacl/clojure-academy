package cn.test;


import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class BlockRegistryEvent extends RegistryEvent.Register<Block> {
    public BlockRegistryEvent(ResourceLocation name, IForgeRegistry<Block> registry)
    {
        super(name, registry);
    }
}

//public class test {
//}
