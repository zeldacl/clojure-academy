package cn.li.academy.api.energy.capability;

import cn.li.academy.api.energy.IWirelessTile;

/**
 * Information providing interface of a wireless node.
 * @author WeathFolD
 */
public interface IWirelessNode extends IWirelessTile {
    
    double getMaxEnergy();
    String getPlacerName();
    void setPlacerName(String name);
    double getEnergy();
    void setEnergy(double value);
    
    /**
     * @return How many energy that this node can transfer each tick.
     */
    double getBandwidth();
    
    int getCapacity();
    
    /**
     * @return How far this node's signal can reach.
     */
    double getRange();
    
    /**
     * @return the user custom name of the node
     */
    String getNodeName();

    void setNodeName(String nodeName);

    /**
     * @return the password of the node
     */
    String getPassword();
    void setPassword(String password);
    
}