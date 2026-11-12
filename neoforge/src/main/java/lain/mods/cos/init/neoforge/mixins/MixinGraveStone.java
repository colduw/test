package lain.mods.cos.init.neoforge.mixins;

import de.maxhenkel.gravestone.blocks.GraveStoneBlock;
import de.maxhenkel.gravestone.corelib.death.Death;
import lain.mods.cos.impl.ModObjects;
import lain.mods.cos.impl.inventory.InventoryCosArmor;
import lain.mods.cos.init.neoforge.NeoForgeCosmeticArmorReworked;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GraveStoneBlock.class)
public class MixinGraveStone {
    @Inject(method = "fillPlayerInventory", at = @At("HEAD"))
    private void CosArmor_onFillPlayerInventory(Player player, Death death, CallbackInfoReturnable<NonNullList<ItemStack>> ci) {
        InventoryCosArmor cosArmor = ModObjects.invMan.getCosArmorInventory(player.getUUID());

        for (ItemStack itemStack : death.getAdditionalItems()) {
            Integer slot = itemStack.get(NeoForgeCosmeticArmorReworked.SLOT_PLACEBACK.get());
            if (slot == null) {
                continue;
            }

            itemStack.remove(NeoForgeCosmeticArmorReworked.SLOT_PLACEBACK.get());
            if (cosArmor.getStackInSlot(slot).isEmpty()) {
                cosArmor.setStackInSlot(slot, itemStack.copyAndClear());
            }
        }
    }
}
