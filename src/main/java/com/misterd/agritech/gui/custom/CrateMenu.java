package com.misterd.agritech.gui.custom;

import com.misterd.agritech.blockentity.custom.CrateBlockEntity;
import com.misterd.agritech.gui.ATMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CrateMenu extends AbstractContainerMenu {

    public final CrateBlockEntity blockEntity;
    private final Level level;
    private final ContainerData data;

    private static final int DATA_COLLECTING = 0;
    private static final int DATA_COUNT = 1;

    public CrateMenu(int id, Inventory inv, BlockPos pos) {
        this(id, inv, (CrateBlockEntity) inv.player.level().getBlockEntity(pos));
    }

    public CrateMenu(int id, Inventory inv, CrateBlockEntity be) {
        super(ATMenuTypes.CRATE_MENU, id);
        this.blockEntity = be;
        this.level = inv.player.level();

        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return index == DATA_COLLECTING ? (blockEntity.isCollecting() ? 1 : 0) : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == DATA_COLLECTING) blockEntity.setCollecting(value == 1);
            }

            @Override
            public int getCount() { return DATA_COUNT; }
        };

        for (int row = 0; row < 6; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(be, row * 9 + col, 8 + col * 18, 19 + row * 18));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
        addDataSlots(this.data);
    }

    public boolean isCollecting() { return data.get(DATA_COLLECTING) == 1; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (source == null || !source.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = source.getItem();
        ItemStack copy = stack.copy();

        if (index < 54) {
            if (!moveItemStackTo(stack, 54, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 54, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();

        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(
                ContainerLevelAccess.create(level, blockEntity.getBlockPos()),
                player,
                blockEntity.getBlockState().getBlock()
        );
    }

    private void addPlayerInventory(Inventory inv) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 124 + row * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(inv, col, 8 + col * 18, 183));
    }
}