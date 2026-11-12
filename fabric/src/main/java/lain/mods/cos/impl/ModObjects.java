package lain.mods.cos.impl;

import lain.mods.cos.impl.client.InventoryManagerClient;
import lain.mods.cos.impl.inventory.ContainerCosArmor;
import lain.mods.cos.init.fabric.FabricCosmeticArmorReworked;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.inventory.MenuType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModObjects {
    public static final Logger logger = LogManager.getLogger(FabricCosmeticArmorReworked.class);
    public static final InventoryManager invMan = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? new InventoryManagerClient() : new InventoryManager();

    public static MenuType<ContainerCosArmor> getTypeContainerCosArmor() {
        return FabricCosmeticArmorReworked.typeContainerCosArmor;
    }
}
