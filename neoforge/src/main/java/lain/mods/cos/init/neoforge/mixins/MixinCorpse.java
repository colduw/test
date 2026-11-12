package lain.mods.cos.init.neoforge.mixins;

import de.maxhenkel.corpse.gui.CorpseAdditionalContainer;
import de.maxhenkel.corpse.gui.CorpseInventoryContainer;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.inventory.InventoryCosArmor;
import lain.mods.cos.init.neoforge.NeoForgeCosmeticArmorReworked;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {CorpseInventoryContainer.class, CorpseAdditionalContainer.class})
public class MixinCorpse {
    @Inject(method = "transferItems", at = @At("HEAD"))
    private void CosArmor_transferItems(CallbackInfo ci) {
        if ((Object) this instanceof CorpseInventoryContainer container) {
            if (!container.isEditable()) {
                return;
            }

            InventoryCosArmor cosArmor = ModObjects.invMan.getCosArmorInventory(( (Inventory) container.getPlayerInventory() ).player.getUUID());
            NonNullList<ItemStack> additionalItems = container.getCorpse().getDeath().getAdditionalItems();

            for (ItemStack stack : additionalItems) {
                Integer slot = stack.get(NeoForgeCosmeticArmorReworked.SLOT_PLACEBACK.get());
                if (slot == null) {
                    continue;
                }

                stack.remove(NeoForgeCosmeticArmorReworked.SLOT_PLACEBACK.get());
                if (cosArmor.getItem(slot).isEmpty()) {
                    cosArmor.setItem(slot, stack.copyAndClear());
                }
            }

            additionalItems.removeIf(ItemStack::isEmpty);
        }

        if ((Object) this instanceof CorpseAdditionalContainer container) {
            if (!container.isEditable()) {
                return;
            }

            InventoryCosArmor cosArmor = ModObjects.invMan.getCosArmorInventory(( (Inventory) container.getPlayerInventory() ).player.getUUID());
            NonNullList<ItemStack> additionalItems = container.getCorpse().getDeath().getAdditionalItems();

            for (ItemStack stack : additionalItems) {
                Integer slot = stack.get(NeoForgeCosmeticArmorReworked.SLOT_PLACEBACK.get());
                if (slot == null) {
                    continue;
                }

                stack.remove(NeoForgeCosmeticArmorReworked.SLOT_PLACEBACK.get());
                if (cosArmor.getItem(slot).isEmpty()) {
                    cosArmor.setItem(slot, stack.copyAndClear());
                }
            }
        }
    }
}
