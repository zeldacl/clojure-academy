package cn.li.academy.api.energy.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.energy.EnergyStorage;

public class CapabilityWirelessNode {
    @CapabilityInject(IWirelessNode.class)
    public static Capability<IWirelessNode> NODE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IWirelessNode.class, new Capability.IStorage<IWirelessNode>()
                {
                    @Override
                    public INBT writeNBT(Capability<IWirelessNode> capability, IWirelessNode instance, Direction side)
                    {
                        if (!(instance instanceof WirelessNode)) {
                            throw new IllegalArgumentException("Can not serialize to an instance that isn't the default implementation");
                        }
                        return ((WirelessNode)instance).serializeNBT();
                    }

                    @Override
                    public void readNBT(Capability<IWirelessNode> capability, IWirelessNode instance, Direction side, INBT nbt)
                    {
                        if (!(instance instanceof WirelessNode)) {
                            throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                        }
                        CompoundNBT compound = (CompoundNBT) nbt;
                        ((WirelessNode)instance).deserializeNBT(compound);
                    }
                },
                WirelessNode::new);
    }
}
