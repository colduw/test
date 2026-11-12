package lain.mods.cos.impl.client;

import com.mojang.blaze3d.platform.InputConstants;
import lain.mods.cos.impl.client.gui.GuiCosArmorInventory;
import lain.mods.cos.impl.network.payload.PayloadOpenCosArmorInventory;
import lain.mods.cos.init.ModConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

public enum KeyHandler {
    INSTANCE;
    public final KeyMapping.Category keyOpenCosArmorCategory = new KeyMapping.Category(Identifier.fromNamespaceAndPath(ModConstants.MODID, "cos.key.category"));
    public final KeyMapping keyOpenCosArmorInventory = new KeyMapping("cos.key.opencosarmorinventory", InputConstants.UNKNOWN.getValue(), keyOpenCosArmorCategory);
    private void handleClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (!mc.isWindowActive())
            return;
        if (keyOpenCosArmorInventory.consumeClick() && !(mc.gui.screen() instanceof GuiCosArmorInventory))
            ClientPacketDistributor.sendToServer(new PayloadOpenCosArmorInventory());
    }

    public void registerEvents() {
        NeoForge.EVENT_BUS.addListener(this::handleClientTick);
    }

    public void registerKeyMappings(Consumer<KeyMapping> register) {
        register.accept(keyOpenCosArmorInventory);
    }
}
