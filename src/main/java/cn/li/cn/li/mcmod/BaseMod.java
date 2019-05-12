package cn.li.cn.li.mcmod;

import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaseMod {
    private static final Logger LOGGER = LogManager.getLogger();
    public BaseMod () {
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("HELLO FROM BaseMod 33333333333333333333333333333333333333333333");
    }
}
