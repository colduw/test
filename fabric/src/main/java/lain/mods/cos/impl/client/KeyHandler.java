package lain.mods.cos.impl.client;

import com.mojang.blaze3d.platform.InputConstants;
import lain.mods.cos.impl.client.gui.GuiCosArmorInventory;
import lain.mods.cos.impl.network.payload.PayloadOpenCosArmorInventory;
import lain.mods.cos.init.ModConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public enum KeyHandler {
    INSTANCE;
    public final KeyMapping.Category keyOpenCosArmorCategory = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ModConstants.MODID, "cos.key.category"));
    public final KeyMapping keyOpenCosArmorInventory = new KeyMapping("cos.key.opencosarmorinventory", InputConstants.UNKNOWN.getValue(), keyOpenCosArmorCategory);
    private void handleClientTick(Minecraft mc) {
        if (!mc.isWindowActive())
            return;
        if (keyOpenCosArmorInventory.consumeClick() && !(mc.gui.screen() instanceof GuiCosArmorInventory))
            ClientPlayNetworking.send(new PayloadOpenCosArmorInventory());
    }

    public void registerEvents() {
        ClientTickEvents.START_CLIENT_TICK.register(this::handleClientTick);
    }

    public void registerKeyMappings(Consumer<KeyMapping> register) {
        register.accept(keyOpenCosArmorInventory);
    }
}
