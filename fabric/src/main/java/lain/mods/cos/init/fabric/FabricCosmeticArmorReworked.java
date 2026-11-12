package lain.mods.cos.init.fabric;

import lain.mods.cos.impl.ModConfigs;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.inventory.ContainerCosArmor;
import lain.mods.cos.impl.network.ModPayloads;
import lain.mods.cos.init.ModConstants;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class FabricCosmeticArmorReworked implements ModInitializer {
    public static final MenuType<ContainerCosArmor> typeContainerCosArmor = Registry.register(BuiltInRegistries.MENU, ResourceLocation.fromNamespaceAndPath(ModConstants.MODID, "inventorycosarmor"), new MenuType<>(ModObjects.invMan::createContainerClient, FeatureFlags.VANILLA_SET));

    @Override
    public void onInitialize() {
        ModObjects.invMan.registerEvents();
        ModPayloads.setupServer();
        ModConfigs.registerConfigs();
    }
}
