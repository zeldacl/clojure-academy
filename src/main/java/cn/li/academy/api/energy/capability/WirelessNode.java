package cn.li.academy.api.energy.capability;

public class WirelessNode implements IWirelessNode {
    protected double energy = 0;
    protected String placerName = "";
    protected String password = "";
    protected String nodeName = "";
    @Override
    public double getMaxEnergy() {
        return 0;
    }

    @Override
    public String getPlacerName() {
        return placerName;
    }

    @Override
    public void setPlacerName(String name) {
        placerName = name;
    }

    @Override
    public double getEnergy() {
        return energy;
    }

    @Override
    public void setEnergy(double value) {
        energy = value;
    }

    @Override
    public double getBandwidth() {
        return 0;
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    @Override
    public double getRange() {
        return 0;
    }

    @Override
    public String getNodeName() {
        return nodeName;
    }

    @Override
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }
}