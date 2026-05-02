package com.misterd.agritech.blockentity.custom;

import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.config.Config;
import com.misterd.agritech.gui.custom.CrateMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class CrateBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int SIZE = 54;
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    private int tickCounter = 0;
    private boolean collecting = true;

    public CrateBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATBlockEntities.CRATE_BE, pos, blockState);
    }

    public boolean isCollecting() { return collecting; }

    public void setCollecting(boolean collecting) {
        this.collecting = collecting;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrateBlockEntity be) {
        if (level.isClientSide()) return;
        if (!be.collecting) return;

        be.tickCounter++;
        if (be.tickCounter < Config.basketPickupIntervalTicks) return;
        be.tickCounter = 0;

        int radius = 2;
        AABB area = new AABB(
                pos.getX() - radius, pos.getY() - radius, pos.getZ() - radius,
                pos.getX() + 1 + radius, pos.getY() + 1 + radius, pos.getZ() + 1 + radius
        );

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);
        for (ItemEntity itemEntity : items) {
            if (itemEntity.isRemoved()) continue;
            ItemStack stack = itemEntity.getItem();
            if (stack.isEmpty()) continue;

            int inserted = 0;

            for (int slot = 0; slot < SIZE && !stack.isEmpty(); slot++) {
                ItemStack existing = be.inventory.get(slot);
                if (existing.isEmpty()) {
                    int toInsert = stack.getCount();
                    be.inventory.set(slot, stack.copyWithCount(toInsert));
                    inserted += toInsert;
                    stack.setCount(0);
                } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    if (space <= 0) continue;
                    int toInsert = Math.min(space, stack.getCount());
                    existing.grow(toInsert);
                    inserted += toInsert;
                    stack.shrink(toInsert);
                }
            }

            if (inserted > 0) {
                if (stack.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(stack);
                }
                be.setChanged();
            }
        }
    }

    public void drops() {
        Containers.dropContents(level, worldPosition, this);
    }

    // --- Container ---

    @Override
    public int getContainerSize() { return SIZE; }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) { return inventory.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(inventory, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() { inventory.clear(); }

    // --- Serialization ---

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
        output.putBoolean("collecting", collecting);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
        collecting = input.getBooleanOr("collecting", true);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.agritech.crate_menu");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new CrateMenu(id, playerInv, this);
    }
}