package cn.li.academy.api.energy.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityEnergy {
    @CapabilityInject(IWirelessNode.class)
    public static Capability<IWirelessNode> ENERGY = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IWirelessNode.class, new Capability.IStorage<IWirelessNode>()
                {
                    @Override
                    public INBT writeNBT(Capability<IWirelessNode> capability, IWirelessNode instance, Direction side)
                    {
                        CompoundNBT compound = new CompoundNBT();
                        compound.putDouble("energy", instance.getEnergy());
                        compound.putString("nodeName", instance.getNodeName());
                        compound.putString("password", instance.getPassword());
                        compound.putString("placer", instance.getPlacerName());
                        return compound;
//                        return new IntNBT(instance.getEnergyStored());
                    }

                    @Override
                    public void readNBT(Capability<IWirelessNode> capability, IWirelessNode instance, Direction side, INBT nbt)
                    {
                        if (!(instance instanceof WirelessNode))
                            throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                        CompoundNBT compound = (CompoundNBT) nbt;
                        instance.setEnergy(compound.getDouble("energy"));
                        instance.setNodeName(compound.getString("nodeName"));
                        instance.setPassword(compound.getString("password"));
                        instance.setPlacerName(compound.getString("placer"));
                    }
                },
                () -> new WirelessNode());
    }
}
