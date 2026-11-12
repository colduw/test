package lain.mods.cos.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.brigadier.CommandDispatcher;
import lain.mods.cos.api.event.CosArmorDeathDrops;
import lain.mods.cos.impl.inventory.ContainerCosArmor;
import lain.mods.cos.impl.inventory.InventoryCosArmor;
import lain.mods.cos.impl.network.payload.PayloadSyncCosArmor;
import lain.mods.cos.impl.network.payload.PayloadSyncHiddenFlags;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;

public class InventoryManager {
    protected static final InventoryCosArmor Dummy = new InventoryCosArmor() {
        @Override
        @NotNull
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        @NotNull
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        @NotNull
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public boolean isHidden(String modid, String identifier) {
            return false;
        }

        @Override
        public boolean isSkinArmor(int slot) {
            return false;
        }

        @Override
        protected void onContentsChanged(int slot) {
        }

        @Override
        protected void onLoad() {
        }

        @Override
        public boolean setHidden(String modid, String identifier, boolean set) {
            return false;
        }

        @Override
        public void setSkinArmor(int slot, boolean enabled) {
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        }

        @Override
        public void setUpdateListener(ContentsChangeListener listener) {
        }

        @Override
        public void setUpdateListener(HiddenFlagsChangeListener listener) {
        }
    };

    protected static final Random RANDOM = new Random();
    protected MinecraftServer server;

    protected final LoadingCache<UUID, InventoryCosArmor> CommonCache = CacheBuilder.newBuilder().build(new CacheLoader<>() {

        @Override
        public InventoryCosArmor load(UUID key) {
            InventoryCosArmor inventory = new InventoryCosArmor();
            inventory.setUpdateListener((inv, slot) -> onInventoryChanged(key, inv, slot));
            inventory.setUpdateListener((inv, modid, identifier) -> onHiddenFlagsChanged(key, inv, modid, identifier));
            loadInventory(key, inventory);
            return inventory;
        }

    });

    public static boolean checkIdentifier(String modid, String identifier) {
        if (modid == null || modid.isEmpty() || identifier == null || identifier.isEmpty() || !FabricLoader.getInstance().isModLoaded(modid))
            return false;

        return false;
    }

    public ContainerCosArmor createContainerClient(int windowId, Inventory invPlayer) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public InventoryCosArmor getCosArmorInventory(UUID uuid) {
        if (uuid == null)
            return Dummy;
        return CommonCache.getUnchecked(uuid);
    }

    @NotNull
    public InventoryCosArmor getCosArmorInventoryClient(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    protected File getDataFile(UUID uuid) {
        return new File(server.getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile(), uuid + ".cosarmor");
    }

    private void handlePlayerDrops(LivingEntity event, DamageSource source) {
        if (event instanceof Player) {
            if (event.isEffectiveAi() && !((ServerLevel) event.level()).getGameRules().get(GameRules.KEEP_INVENTORY) && !ModConfigs.CosArmorKeepThroughDeath.get()) {
                InventoryCosArmor inv = getCosArmorInventory(event.getUUID());
                if (CosArmorDeathDrops.EVENT_BUS.invoker().post((Player) event, inv)) {
                    return;
                }
                for (int i = 0; i < inv.getSlots(); i++) {
                    ItemStack stack = inv.getStackInSlot(i).copy();
                    if (stack.isEmpty())
                        continue;

                    float fX = RANDOM.nextFloat() * 0.75F + 0.125F;
                    float fY = RANDOM.nextFloat() * 0.75F;
                    float fZ = RANDOM.nextFloat() * 0.75F + 0.125F;
                    while (!stack.isEmpty()) {
                        ItemEntity entity = new ItemEntity(event.level(), event.getX() + (double) fX, event.getY() + (double) fY, event.getZ() + (double) fZ, stack.split(RANDOM.nextInt(21) + 10));
                        entity.setDeltaMovement(RANDOM.nextGaussian() * (double) 0.05F, RANDOM.nextGaussian() * (double) 0.05F + (double) 0.2F, RANDOM.nextGaussian() * (double) 0.05F);
                        event.level().addFreshEntity(entity);
                    }

                    inv.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
    }

    private void handlePlayerLoggedIn(ServerPlayer event) {
        CommonCache.invalidate(event.getUUID());
        getCosArmorInventory(event.getUUID());

        if (event instanceof ServerPlayer player) {
            for (ServerPlayer other : server.getPlayerList().getPlayers()) {
                if (other == player)
                    continue;
                UUID uuid = other.getUUID();
                InventoryCosArmor inv = getCosArmorInventory(uuid);
                for (int i = 0; i < inv.getSlots(); i++)
                    ServerPlayNetworking.send(player, new PayloadSyncCosArmor(uuid, inv, i));
                inv.forEachHidden((modid, identifier) -> ServerPlayNetworking.send(player, new PayloadSyncHiddenFlags(uuid, inv, modid, identifier)));
            }
        }
    }

    private void handlePlayerLoggedOut(ServerPlayer event) {
        UUID uuid = event.getUUID();
        InventoryCosArmor inv = CommonCache.getIfPresent(uuid);
        if (inv != null) {
            saveInventory(uuid, inv);
            CommonCache.invalidate(uuid);
        }
    }

    private void handleRegisterCommands(CommandDispatcher<CommandSourceStack> event, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        event.register(Commands.literal("clearcosarmor").requires(s -> s.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)).executes(s -> {
            int count = 0;
            ServerPlayer player = s.getSource().getPlayerOrException();
            InventoryCosArmor inv = getCosArmorInventory(player.getUUID());
            for (int i = 0; i < inv.getSlots(); i++)
                count += inv.extractItem(i, Integer.MAX_VALUE, false).getCount();

            final int result = count;
            s.getSource().sendSuccess(() -> Component.translatable("cos.command.clearcosarmor.success.single", result, player.getDisplayName()), true);
            return result;
        }).then(Commands.argument("targets", EntityArgument.players()).executes(s -> {
            int count = 0;
            Collection<ServerPlayer> players = EntityArgument.getPlayers(s, "targets");
            for (ServerPlayer player : players) {
                InventoryCosArmor inv = getCosArmorInventory(player.getUUID());
                for (int i = 0; i < inv.getSlots(); i++)
                    count += inv.extractItem(i, Integer.MAX_VALUE, false).getCount();
            }

            final int result = count;
            if (players.size() == 1)
                s.getSource().sendSuccess(() -> Component.translatable("cos.command.clearcosarmor.success.single", result, players.iterator().next().getDisplayName()), true);
            else
                s.getSource().sendSuccess(() -> Component.translatable("cos.command.clearcosarmor.success.multiple", result, players.size()), true);
            return result;
        })));

        if (!ModConfigs.CosArmorDisableCosHatCommand.get()) {
            event.register(Commands.literal("coshat").requires(s -> s.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.ALL))).executes(s -> {
                ServerPlayer player = s.getSource().getPlayerOrException();
                InventoryCosArmor inv = getCosArmorInventory(player.getUUID());
                ItemStack stack1 = player.getItemBySlot(EquipmentSlot.MAINHAND);
                ItemStack stack2 = inv.getStackInSlot(3);
                player.setItemSlot(EquipmentSlot.MAINHAND, stack2);
                inv.setStackInSlot(3, stack1);
                return 0;
            }));
        }
    }

    private void handleSaveToFile(MinecraftServer server, boolean flush, boolean force) {
        CommonCache.asMap().forEach(this::saveInventory);
    }

    private void handleServerStopping(MinecraftServer event) {
        CommonCache.asMap().forEach(this::saveInventory);
        CommonCache.invalidateAll();
    }

    protected void loadInventory(UUID uuid, InventoryCosArmor inventory) {
        if (inventory == Dummy) {
            return;
        }

        try {
            File file;
            if ((file = getDataFile(uuid)).exists()) {
                CompoundTag fileData = NbtIo.read(file.toPath());

                ValueInput cosData = TagValueInput.create(ProblemReporter.DISCARDING, server.registryAccess(), fileData);

                inventory.deserialize(cosData);
            }
        } catch (Throwable t) {
            ModObjects.logger.fatal("Failed to load CosmeticArmor data", t);
        }
    }

    protected void onHiddenFlagsChanged(UUID uuid, InventoryCosArmor inventory, String modid, String identifier) {
        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, new PayloadSyncHiddenFlags(uuid, inventory, modid, identifier)));
    }

    protected void onInventoryChanged(UUID uuid, InventoryCosArmor inventory, int slot) {
        server.getPlayerList().getPlayers().forEach(player -> ServerPlayNetworking.send(player, new PayloadSyncCosArmor(uuid, inventory, slot)));
    }

    protected void serverStarting(MinecraftServer server) {
        this.server = server;
    }

    protected void serverStopped(MinecraftServer server) {
        this.server = null;
    }

    public void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::serverStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::serverStopped);
        ServerLivingEntityEvents.AFTER_DEATH.register(this::handlePlayerDrops);
        ServerPlayerEvents.JOIN.register(this::handlePlayerLoggedIn);
        ServerPlayerEvents.LEAVE.register(this::handlePlayerLoggedOut);
        ServerLifecycleEvents.AFTER_SAVE.register(this::handleSaveToFile);
        CommandRegistrationCallback.EVENT.register(this::handleRegisterCommands);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::handleServerStopping);
    }

    public void registerEventsClient() {
        throw new UnsupportedOperationException();
    }

    protected void saveInventory(UUID uuid, InventoryCosArmor inventory) {
        if (inventory == Dummy) {
            return;
        }
        try {
            TagValueOutput cosData = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, server.registryAccess());

            inventory.serialize(cosData);

            NbtIo.write(cosData.buildResult(), getDataFile(uuid).toPath());
        } catch (Throwable t) {
            ModObjects.logger.fatal("Failed to save CosmeticArmor data", t);
        }
    }
}
