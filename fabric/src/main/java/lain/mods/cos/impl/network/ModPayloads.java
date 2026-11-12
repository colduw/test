package lain.mods.cos.impl.network;

import lain.mods.cos.impl.InventoryManager;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.network.payload.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ModPayloads {
    public static void setupClient() {
        ClientPlayNetworking.registerGlobalReceiver(PayloadSyncCosArmor.TYPE, (p, c) -> {
            ModObjects.invMan.getCosArmorInventoryClient(p.uuid()).setStackInSlot(p.slot(), p.itemCosArmor());
            ModObjects.invMan.getCosArmorInventoryClient(p.uuid()).setSkinArmor(p.slot(), p.isSkinArmor());
        });

        ClientPlayNetworking.registerGlobalReceiver(PayloadSyncHiddenFlags.TYPE, (p, c) -> {
            if (InventoryManager.checkIdentifier(p.modid(), p.identifier())) {
                ModObjects.invMan.getCosArmorInventoryClient(p.uuid()).setHidden(p.modid(), p.identifier(), p.hidden());
            }
        });
    }

    public static void setupServer() {
        PayloadTypeRegistry.clientboundPlay().register(PayloadSyncCosArmor.TYPE, PayloadSyncCosArmor.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PayloadSyncHiddenFlags.TYPE, PayloadSyncHiddenFlags.STREAM_CODEC);

        PayloadTypeRegistry.serverboundPlay().register(PayloadSetSkinArmor.TYPE, PayloadSetSkinArmor.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PayloadOpenCosArmorInventory.TYPE, PayloadOpenCosArmorInventory.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PayloadOpenNormalInventory.TYPE, PayloadOpenNormalInventory.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PayloadSetHiddenFlags.TYPE, PayloadSetHiddenFlags.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PayloadSetSkinArmor.TYPE, (p, c) -> ModObjects.invMan.getCosArmorInventory(c.player().getUUID()).setSkinArmor(p.slot(), p.isSkinArmor()));
        ServerPlayNetworking.registerGlobalReceiver(PayloadOpenCosArmorInventory.TYPE, (p, c) -> c.player().openMenu(ModObjects.invMan.getCosArmorInventory(c.player().getUUID())));
        ServerPlayNetworking.registerGlobalReceiver(PayloadOpenNormalInventory.TYPE, (p, c) -> c.player().doCloseContainer());
        ServerPlayNetworking.registerGlobalReceiver(PayloadSetHiddenFlags.TYPE, (p, c) -> {
            if (InventoryManager.checkIdentifier(p.modid(), p.identifier())) {
                ModObjects.invMan.getCosArmorInventory(c.player().getUUID()).setHidden(p.modid(), p.identifier(), p.hidden());
            }
        });
    }
}
