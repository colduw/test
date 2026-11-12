package lain.mods.cos.impl.client.gui;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;

public class InventoryScreenAccess {
    public static float getXMouse(InventoryScreen screen) {
        return screen.xMouse;
    }

    public static float getYMouse(InventoryScreen screen) {
        return screen.yMouse;
    }

    public static void setXMouse(InventoryScreen screen, float xMouse) {
        screen.xMouse = xMouse;
    }

    public static void setYMouse(InventoryScreen screen, float yMouse) {
        screen.yMouse = yMouse;
    }
}
