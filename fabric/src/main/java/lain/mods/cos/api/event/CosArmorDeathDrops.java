package lain.mods.cos.api.event;

import lain.mods.cos.api.inventory.CAStacksBase;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.player.Player;

/**
 * This event is fired whenever a player dies and the associated CAStacks is about to be dropped. <br>
 * <br>
 * This event is fired on the {@link #EVENT_BUS}.<br>
 */
public interface CosArmorDeathDrops {
    Event<CosArmorDeathDrops> EVENT_BUS = EventFactory.createArrayBacked(CosArmorDeathDrops.class, (listeners) -> ((player, stacks) -> {
        for (CosArmorDeathDrops callback : listeners) {
            if (callback.post(player, stacks)) {
                return true;
            }
        }

        return false;
    }));

    boolean post(Player player, CAStacksBase stacks);
}