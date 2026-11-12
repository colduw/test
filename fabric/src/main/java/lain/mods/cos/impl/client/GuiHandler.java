package lain.mods.cos.impl.client;

import lain.mods.cos.impl.ModConfigs;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.client.gui.*;
import lain.mods.cos.impl.network.payload.PayloadOpenCosArmorInventory;
import lain.mods.cos.impl.network.payload.PayloadOpenNormalInventory;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

public enum GuiHandler {
    INSTANCE;

    private int lastLeft;
    private boolean lastInventoryOpen;

    private void handleGuiDrawPreInit(Minecraft mc, Screen event, int scaledWidth, int scaledHeight) {
        ScreenEvents.beforeRender(event).register(this::handleGuiDrawPre);
    }

    private void handleGuiDrawPre(Screen event, GuiGraphics drawContext, int mouseX, int mouseY, float tickDelta) {
        if (event instanceof AbstractContainerScreen<?> screen) {
            if (lastLeft != screen.leftPos) {
                int diffLeft = screen.leftPos - lastLeft;
                lastLeft = screen.leftPos;
                screen.children().stream().filter(IShiftingWidget.class::isInstance).map(IShiftingWidget.class::cast).forEach(b -> b.shiftLeft(diffLeft));
            }
            if (event instanceof CreativeModeInventoryScreen) {
                boolean isInventoryOpen = ((CreativeModeInventoryScreen) event).isInventoryOpen();
                if (lastInventoryOpen != isInventoryOpen) {
                    lastInventoryOpen = isInventoryOpen;
                    screen.children().stream().filter(ICreativeInvWidget.class::isInstance).map(ICreativeInvWidget.class::cast).forEach(b -> b.onSelectedTabChanged(isInventoryOpen));
                }
            }
        }
    }

    private void handleGuiInitPost(Minecraft mc, Screen event, int scaledWidth, int scaledHeight) {
        if (event instanceof AbstractContainerScreen<?> screen) {
            lastLeft = screen instanceof CreativeModeInventoryScreen ? 0 : screen.leftPos;
            lastInventoryOpen = true;
        }

        if (event instanceof InventoryScreen || event instanceof GuiCosArmorInventory) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) event;

            if (!ModConfigs.CosArmorGuiButton_Hidden.get()) {
                Screens.getButtons(screen).add(new GuiCosArmorButton(
                        screen.leftPos + ModConfigs.CosArmorGuiButton_Left.get()/* 65 */,
                        screen.topPos + ModConfigs.CosArmorGuiButton_Top.get()/* 67 */,
                        10, 10,
                        event instanceof GuiCosArmorInventory ?
                                Component.translatable("cos.gui.buttonnormal") :
                                Component.translatable("cos.gui.buttoncos"),
                        button -> {
                            if (screen instanceof GuiCosArmorInventory) {
                                InventoryScreen newGui = new InventoryScreen(Minecraft.getInstance().player);
                                InventoryScreenAccess.setXMouse(newGui, ((GuiCosArmorInventory) screen).oldMouseX);
                                InventoryScreenAccess.setYMouse(newGui, ((GuiCosArmorInventory) screen).oldMouseY);
                                Minecraft.getInstance().setScreen(newGui);
                                ClientPlayNetworking.send(new PayloadOpenNormalInventory());
                            } else {
                                ClientPlayNetworking.send(new PayloadOpenCosArmorInventory());
                            }
                        },
                        null));
            }
            if (!ModConfigs.CosArmorToggleButton_Hidden.get()) {
                Screens.getButtons(screen).add(new GuiCosArmorToggleButton(
                        screen.leftPos + ModConfigs.CosArmorToggleButton_Left.get()/* 59 */,
                        screen.topPos + ModConfigs.CosArmorToggleButton_Top.get()/* 72 */,
                        5, 5,
                        Component.empty(),
                        PlayerRenderHandler.Disabled ? 1 : 0,
                        button -> {
                            PlayerRenderHandler.Disabled = !PlayerRenderHandler.Disabled;
                            ((GuiCosArmorToggleButton) button).state = PlayerRenderHandler.Disabled ? 1 : 0;
                        }));
            }
        } else if (event instanceof CreativeModeInventoryScreen screen) {
            if (!ModConfigs.CosArmorCreativeGuiButton_Hidden.get()) {
                Screens.getButtons(screen).add(new GuiCosArmorButton(
                        /*screen.leftPos + */ModConfigs.CosArmorCreativeGuiButton_Left.get()/* 95 */,
                        screen.topPos + ModConfigs.CosArmorCreativeGuiButton_Top.get()/* 38 */,
                        10, 10,
                        Component.translatable("cos.gui.buttoncos"),
                        button -> ClientPlayNetworking.send(new PayloadOpenCosArmorInventory()),
                        (button, isInventoryOpen) -> button.visible = isInventoryOpen));
            }
        }
    }

    public void registerEvents() {
        ScreenEvents.BEFORE_INIT.register(this::handleGuiDrawPreInit);
        ScreenEvents.AFTER_INIT.register(this::handleGuiInitPost);
    }

    public void registerMenuScreens() {
        MenuScreens.register(ModObjects.getTypeContainerCosArmor(), GuiCosArmorInventory::new);
    }
}
