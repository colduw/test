package lain.mods.cos.api.event;

import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.HasResult;
import net.minecraftforge.eventbus.api.bus.CancellableEventBus;
import net.minecraftforge.eventbus.api.event.RecordEvent;
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;

/**
 * This event is fired whenever a player dies and the associated CAStacks is about to be dropped. <br>
 * <br>
 * <p>
 * {@link #player} contains the instance of EntityPlayer for the event.<br>
 * {@link #stacks} contains the instance of CAStacks for the player.<br>
 * <br>
 * This event is {@link Cancellable}. <br>
 * If the event is canceled, the CAStacks for the player will not be altered and nothing will be added to the drops.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 */
public class CosArmorDeathDrops implements RecordEvent, Cancellable {
    private final Player player;
    private final CAStacksBase stacks;

    public static final CancellableEventBus<CosArmorDeathDrops> BUS = CancellableEventBus.create(CosArmorDeathDrops.class);

    public CosArmorDeathDrops(Player player, CAStacksBase stacks) {
        this.player = player;
        this.stacks = stacks;
    }

    public CAStacksBase getCAStacks() {
        return stacks;
    }

    public Player getEntityPlayer() {
        return player;
    }
}
