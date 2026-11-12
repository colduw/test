package lain.mods.cos.init.fabric;

import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.client.GuiHandler;
import lain.mods.cos.impl.client.KeyHandler;
import lain.mods.cos.impl.client.PlayerRenderHandler;
import lain.mods.cos.impl.network.ModPayloads;
import lain.mods.cos.impl.update.UpdateCheck;
import lain.mods.cos.impl.update.UpdateResult;
import lain.mods.cos.init.ModConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import java.net.URI;

public class FabricCosmeticArmorReworkedClient implements ClientModInitializer {
    protected static UpdateResult updateResult;

    protected void checkClientUpdate(ClientPacketListener handler, PacketSender sender, Minecraft client) {
        if (updateResult != null && client.player != null) {
            ModObjects.logger.info("Cosmetic Armor Next version {}. (from version JSON): latest: {}, recommended: {}", updateResult.currentVersion(), updateResult.latestVersion(), updateResult.recommendedVersion());
            switch (updateResult.verStates()) {
                case AHEAD -> client.player.displayClientMessage(
                        Component.translatable("cos.init.update.ahead",
                                Component.literal(updateResult.currentVersion()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                        ), false
                );
                case LATEST -> client.player.displayClientMessage(
                        Component.translatable("cos.init.update.latest",
                                Component.literal(updateResult.latestVersion()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                        ), false
                );
                case RECOMMENDED -> client.player.displayClientMessage(
                        Component.translatable("cos.init.update.recommended",
                                Component.literal(updateResult.latestVersion()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                        ), false
                );
                case BEHIND_LATEST_AHEAD_RECOMMENDED -> client.player.displayClientMessage(
                        Component.translatable("cos.init.update.behindlatestaheadrecommended",
                                Component.literal(updateResult.currentVersion()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD),
                                Component.literal(updateResult.recommendedVersion()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD),
                                Component.literal("Click here to open the CurseForge page").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                                        .withStyle(c->c.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://www.curseforge.com/minecraft/mc-mods/cosmetic-armor-next"))))
                        ).withStyle(ChatFormatting.GOLD), false
                );
                case BEHIND -> client.player.displayClientMessage(
                        Component.translatable("cos.init.update.behind",
                                Component.literal(updateResult.currentVersion()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD),
                                Component.literal(updateResult.latestVersion()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD),
                                Component.literal("Click here to open the CurseForge page").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                                        .withStyle(c->c.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://www.curseforge.com/minecraft/mc-mods/cosmetic-armor-next"))))
                        )
                        .withStyle(ChatFormatting.GOLD), false
                );
                case ERROR -> client.player.displayClientMessage(
                        Component.translatable("cos.init.update.error",
                                Component.literal(updateResult.currentVersion()), // stores the ex message
                                Component.literal("Click here to open the CurseForge page").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                                        .withStyle(c->c.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://www.curseforge.com/minecraft/mc-mods/cosmetic-armor-next"))))
                        ).withStyle(ChatFormatting.RED), false
                );
            }
        }
    }

    @Override
    public void onInitializeClient() {
        UpdateCheck.updateManager().asyncUpdate(ModConstants.MODID, update -> updateResult = update);
        ClientPlayConnectionEvents.JOIN.register(this::checkClientUpdate);

        ModObjects.invMan.registerEventsClient();
        GuiHandler.INSTANCE.registerEvents();
        KeyHandler.INSTANCE.registerEvents();
        PlayerRenderHandler.INSTANCE.registerEvents();
        KeyHandler.INSTANCE.registerKeyMappings(KeyBindingHelper::registerKeyBinding);
        GuiHandler.INSTANCE.registerMenuScreens();
        ModPayloads.setupClient();
    }
}
