package cn.academy.api.gui;

public interface IGuiHandler {
    Object getClientContainer(Object player, Object world, int x, int y, int z);
    Object getServerContainer(Object player, Object world, int x, int y, int z);
}

public interface IGuiAPI {
    Object getServerContainer(Object player, Object world, int x, int y, int z);
    Object getTileEntity(Object world, int x, int y, int z);
    boolean isNode(Object tileEntity);
    Object createNodeGui(Object container);
    Object createNodeContainer(Object tileEntity, Object player);
}