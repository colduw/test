package lain.mods.cos.api.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This is the actual inventory associated with the player. <br>
 * Changes made to server side CAStacks will be sync to the clients. <br>
 * Do not make changes to client side CAStacks, it is not expected, and can cause problems. <br>
 * <br>
 * CosmeticArmorReworked uses 4 slots. <br>
 * Slot 0-3 are {@link net.minecraft.world.entity.EquipmentSlot#FEET FEET}, {@link net.minecraft.world.entity.EquipmentSlot#LEGS LEGS}, {@link net.minecraft.world.entity.EquipmentSlot#CHEST CHEST}, {@link net.minecraft.world.entity.EquipmentSlot#HEAD HEAD}. <br>
 * <br>
 * For toggling visibilities of other mods, use these methods: <br>
 * {@link #setHidden(String, String, boolean) setHidden}, {@link #isHidden(String, String) isHidden}, {@link #forEachHidden(BiConsumer) forEachHidden}.
 */
public class CAStacksBase {
    protected final Map<String, Set<String>> hidden = new HashMap<>();
    protected boolean[] isSkinArmor;
    protected NonNullList<ItemStack> stacks;

    protected void validateSlotIndex(int slot) {
        if (slot < 0 || slot >= stacks.size())
            throw new RuntimeException("Slot " + slot + " not in valid range - [0," + stacks.size() + ")");
    }

    protected void onLoad() {}

    protected void onContentsChanged(int slot) {}

    public ItemStack getStackInSlot(int slot) {
        validateSlotIndex(slot);
        return this.stacks.get(slot);
    }

    public int getSlots() {
        return stacks.size();
    }

    public void setStackInSlot(int slot, ItemStack stack) {
        validateSlotIndex(slot);
        this.stacks.set(slot, stack);
        onContentsChanged(slot);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0)
            return ItemStack.EMPTY;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        if (existing.isEmpty())
            return ItemStack.EMPTY;

        int toExtract = Math.min(amount, existing.getMaxStackSize());

        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
                return existing;
            } else {
                return existing.copy();
            }
        } else {
            if (!simulate) {
                this.stacks.set(slot, existing.copyWithCount(existing.getCount() - toExtract));
                onContentsChanged(slot);
            }

            return existing.copyWithCount(toExtract);
        }
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    public int getSlotLimit(int slot) {
        return Item.ABSOLUTE_MAX_STACK_SIZE;
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        if (!isItemValid(slot, stack))
            return stack;

        validateSlotIndex(slot);

        ItemStack existing = this.stacks.get(slot);

        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing))
                return stack;

            limit -= existing.getCount();
        }

        if (limit <= 0)
            return stack;

        boolean reachedLimit = stack.getCount() > limit;

        if (!simulate) {
            if (existing.isEmpty()) {
                this.stacks.set(slot, reachedLimit ? stack.copyWithCount(limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
            onContentsChanged(slot);
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    public CAStacksBase() {
        this(4);
    }

    public CAStacksBase(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        isSkinArmor = new boolean[stacks.size()];
    }

    public void deserialize(ValueInput nbt) {
        setSize(nbt.getInt("Size").orElse(stacks.size()));

        nbt.childrenList("Items").ifPresent(tagList -> {
            for (ValueInput itemTags : tagList) {
                itemTags.getInt("Slot").ifPresent(slot -> {
                    if (slot >= 0 && slot < stacks.size()) {
                        itemTags.read("cosmeticArmorStack", ItemStack.CODEC).ifPresent(stack -> stacks.set(slot, stack));
                        
                        isSkinArmor[slot] = itemTags.getBooleanOr("isSkinArmor", false);
                    }
                });
            }
        });

        hidden.clear();

        nbt.getString("Hidden").ifPresent(h -> Arrays.stream(h.split("\0")).forEach(str -> {
            int i = str.indexOf(":");
            if (i != -1)
                hidden.computeIfAbsent(str.substring(0, i), key -> new HashSet<>()).add(str.substring(i + 1));
        }));

        onLoad();
    }

    /**
     * Iterates through all set hidden other mods' things.
     *
     * @param consumer the consumer that will be accepting pairs of modid and identifier
     */
    public void forEachHidden(BiConsumer<String, String> consumer) {
        for (String modid : hidden.keySet())
            for (String identifier : hidden.get(modid))
                consumer.accept(modid, identifier);
    }

    /**
     * Checks to see if something should be hidden when rendering.
     *
     * @param modid      the modid of the related mod (example: curios)
     * @param identifier the identifier of the related slot (format: slotId#slotIndex) (example: ring#0)
     * @return true if the item in the related slot should be hidden when rendering
     */
    public boolean isHidden(String modid, String identifier) {
        return hidden.getOrDefault(modid, Collections.emptySet()).contains(identifier);
    }

    public boolean isSkinArmor(int slot) {
        validateSlotIndex(slot);
        return isSkinArmor[slot];
    }

    public void serialize(ValueOutput provider) {
        ValueOutputList itemList = provider.childrenList("Items");
        
        for (int i = 0; i < stacks.size(); i++) {
            if (!stacks.get(i).isEmpty() || isSkinArmor[i]) {
                ValueOutput itemTag = itemList.addChild();
                itemTag.putInt("Slot", i);

                if (!stacks.get(i).isEmpty()) {
                    itemTag.store("cosmeticArmorStack", ItemStack.CODEC, stacks.get(i));
                }
                    
                if (isSkinArmor[i]) {
                    itemTag.putBoolean("isSkinArmor", true);
                }
            }
        }

        provider.putInt("Size", stacks.size());

        // writeUTF limit = a 16-bit unsigned integer = 65535 - Should be enough
        provider.putString("Hidden", hidden.entrySet().stream().map(entry -> entry.getValue().stream().map(value -> entry.getKey() + ":" + value).collect(Collectors.joining("\0"))).collect(Collectors.joining("\0")));
    }

    /**
     * Sets or removes something from hidden when rendering.
     *
     * @param modid      the modid of the related mod (example: curios)
     * @param identifier the identifer of the related slot (format: slotId#slotIndex) (example: ring#0)
     * @param set        true for set, false for remove
     * @return if something changed due to this invocation
     */
    public boolean setHidden(String modid, String identifier, boolean set) {
        if (set)
            return hidden.computeIfAbsent(modid, key -> new HashSet<>()).add(identifier);
        else
            return hidden.getOrDefault(modid, Collections.emptySet()).remove(identifier);
    }

    public void setSize(int size) {
        stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        isSkinArmor = new boolean[stacks.size()];
    }

    public void setSkinArmor(int slot, boolean enabled) {
        validateSlotIndex(slot);
        if (isSkinArmor[slot] == enabled)
            return;
        isSkinArmor[slot] = enabled;
        onContentsChanged(slot);
    }
}
