package lain.mods.cos.init.neoforge;

import com.mojang.serialization.Codec;
import lain.mods.cos.impl.ModConfigs;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.client.GuiHandler;
import lain.mods.cos.impl.client.KeyHandler;
import lain.mods.cos.impl.client.PlayerRenderHandler;
import lain.mods.cos.impl.inventory.ContainerCosArmor;
import lain.mods.cos.impl.network.ModPayloads;
import lain.mods.cos.init.ModConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.net.URI;

@Mod(ModConstants.MODID)
public class NeoForgeCosmeticArmorReworked {
    private static final DeferredRegister<MenuType<?>> MENU = DeferredRegister.create(BuiltInRegistries.MENU, ModConstants.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<ContainerCosArmor>> typeContainerCosArmor = MENU.register("inventorycosarmor", () -> new MenuType<>(ModObjects.invMan::createContainerClient, FeatureFlags.VANILLA_SET));

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ModConstants.MODID);
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> SLOT_PLACEBACK = COMPONENTS.register("cosarmor_slot_index", () ->
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    protected void checkClientUpdate(ClientPlayerNetworkEvent.LoggingIn event) {
        var tempContainer = ModList.get().getModContainerById(ModConstants.MODID);
        if (tempContainer.isEmpty()) {
            return;
        }

        ModContainer container = tempContainer.get();
        VersionChecker.CheckResult result = VersionChecker.getResult(container.getModInfo());
        if (result == null) {
            return;
        }

        switch (result.status()) {
            case AHEAD -> event.getPlayer().displayClientMessage(
                    Component.translatable("cos.init.update.ahead",
                            Component.literal(container.getModInfo().getVersion().toString()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                    ), false
            );
            case UP_TO_DATE -> event.getPlayer().displayClientMessage(
                    Component.translatable("cos.init.update.latest",
                            Component.literal(container.getModInfo().getVersion().toString()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                    ), false
            );
            case BETA_OUTDATED, OUTDATED -> event.getPlayer().displayClientMessage(
                    Component.translatable("cos.init.update.behind",
                                    Component.literal(container.getModInfo().getVersion().toString()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD),
                                    Component.literal(result.target().toString()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD),
                                    Component.literal("Click here to open the CurseForge page").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                                            .withStyle(c->c.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://www.curseforge.com/minecraft/mc-mods/cosmetic-armor-next"))))
                            )
                            .withStyle(ChatFormatting.GOLD), false
            );
            case FAILED -> event.getPlayer().displayClientMessage(
                    Component.translatable("cos.init.update.error",
                            Component.literal("check latest.log"), // does NOT store the ex message, only in logs.
                            Component.literal("Click here to open the CurseForge page").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                                    .withStyle(c->c.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://www.curseforge.com/minecraft/mc-mods/cosmetic-armor-next"))))
                    ).withStyle(ChatFormatting.RED), false
            );
        }
    }

    public NeoForgeCosmeticArmorReworked(IEventBus bus) {
        MENU.register(bus);
        COMPONENTS.register(bus);
        bus.addListener(this::setup);
        bus.addListener(this::setupClient);
        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(this::checkClientUpdate);
            bus.addListener(this::setupKeyMappings);
            bus.addListener(this::setupMenuScreens);
        }
        bus.addListener(this::setupPayloadHandlers);
        ModConfigs.registerConfigs();
    }

    private void setup(FMLCommonSetupEvent event) {
        ModObjects.invMan.registerEvents();
    }

    private void setupClient(FMLClientSetupEvent event) {
        ModObjects.invMan.registerEventsClient();
        GuiHandler.INSTANCE.registerEvents();
        KeyHandler.INSTANCE.registerEvents();
        PlayerRenderHandler.INSTANCE.registerEvents();
    }

    private void setupKeyMappings(RegisterKeyMappingsEvent event) {
        KeyHandler.INSTANCE.registerKeyMappings(event::register);
    }

    private void setupMenuScreens(RegisterMenuScreensEvent event) {
        GuiHandler.INSTANCE.registerMenuScreens(event);
    }

    private void setupPayloadHandlers(RegisterPayloadHandlersEvent event) {
        ModPayloads.setupPayloads(event.registrar("5"));
    }

}
