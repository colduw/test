package lain.mods.cos.init.forge;

import lain.mods.cos.impl.ModConfigs;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.client.GuiHandler;
import lain.mods.cos.impl.client.KeyHandler;
import lain.mods.cos.impl.client.PlayerRenderHandler;
import lain.mods.cos.impl.inventory.ContainerCosArmor;
import lain.mods.cos.impl.network.packet.*;
import lain.mods.cos.init.ModConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.net.URI;

@Mod(ModConstants.MODID)
public class ForgeCosmeticArmorReworked {
    private static final DeferredRegister<MenuType<?>> MENU = DeferredRegister.create(ForgeRegistries.Keys.MENU_TYPES, ModConstants.MODID);
    public static final RegistryObject<MenuType<ContainerCosArmor>> typeContainerCosArmor = MENU.register("inventorycosarmor", () -> IForgeMenuType.create(ModObjects.invMan::createContainerClient));

    protected void checkClientUpdate(ClientPlayerNetworkEvent.LoggingIn event) {
        var tempContainer = ModList.getModContainerById(ModConstants.MODID);
        if (tempContainer.isEmpty()) {
            return;
        }

        ModContainer container = tempContainer.get();
        VersionChecker.CheckResult result = VersionChecker.getResult(container.getModInfo());

        switch (result.status()) {
            case AHEAD -> event.getPlayer().sendSystemMessage(
                    Component.translatable("cos.init.update.ahead",
                            Component.literal(container.getModInfo().getVersion().toString()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                    )
            );
            case UP_TO_DATE -> event.getPlayer().sendSystemMessage(
                    Component.translatable("cos.init.update.latest",
                            Component.literal(container.getModInfo().getVersion().toString()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                    )
            );
            case BETA_OUTDATED, OUTDATED -> event.getPlayer().sendSystemMessage(
                    Component.translatable("cos.init.update.behind",
                                    Component.literal(container.getModInfo().getVersion().toString()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD),
                                    Component.literal(result.target().toString()).withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD),
                                    Component.literal("Click here to open the CurseForge page").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                                            .withStyle(c->c.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://www.curseforge.com/minecraft/mc-mods/cosmetic-armor-next"))))
                            )
                            .withStyle(ChatFormatting.GOLD)
            );
            case FAILED -> event.getPlayer().sendSystemMessage(
                    Component.translatable("cos.init.update.error",
                            Component.literal("check latest.log"), // does NOT store the ex message, only in logs.
                            Component.literal("Click here to open the CurseForge page").withStyle(ChatFormatting.LIGHT_PURPLE).withStyle(ChatFormatting.BOLD)
                                    .withStyle(c->c.withClickEvent(new ClickEvent.OpenUrl(URI.create("https://www.curseforge.com/minecraft/mc-mods/cosmetic-armor-next"))))
                    ).withStyle(ChatFormatting.RED)
            );
        }
    }

    public ForgeCosmeticArmorReworked(FMLJavaModLoadingContext context) {
        BusGroup busGroup = context.getModBusGroup();
        MENU.register(busGroup);
        FMLCommonSetupEvent.getBus(busGroup).addListener(this::setup);
        FMLClientSetupEvent.getBus(busGroup).addListener(this::setupClient);
        if (FMLEnvironment.dist.isClient()) {
            ClientPlayerNetworkEvent.LoggingIn.BUS.addListener(this::checkClientUpdate);
            RegisterKeyMappingsEvent.BUS.addListener(this::setupKeyMappings);
        }
        
        ModConfigs.registerConfigs(context);
    }

    private void setup(FMLCommonSetupEvent event) {
        ModObjects.invMan.registerEvents();
        setupNetworkPackets();
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

    private void setupNetworkPackets() {
        ModObjects.network.registerPacket(PacketSyncCosArmor.class, PacketSyncCosArmor::new);
        ModObjects.network.registerPacket(PacketSetSkinArmor.class, PacketSetSkinArmor::new);
        ModObjects.network.registerPacket(PacketOpenCosArmorInventory.class, PacketOpenCosArmorInventory::new);
        ModObjects.network.registerPacket(PacketOpenNormalInventory.class, PacketOpenNormalInventory::new);
        ModObjects.network.registerPacket(PacketSyncHiddenFlags.class, PacketSyncHiddenFlags::new);
        ModObjects.network.registerPacket(PacketSetHiddenFlags.class, PacketSetHiddenFlags::new);
        ModObjects.network.finishSetup();
    }

}
