package com.misterd.agritech.gui.custom;

import com.misterd.agritech.blockentity.custom.RaisedBedBlockEntity;
import com.misterd.agritech.config.PlantablesConfig;
import com.misterd.agritech.gui.ATMenuTypes;
import com.misterd.agritech.util.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RaisedBedMenu extends AbstractContainerMenu {

    public final RaisedBedBlockEntity blockEntity;
    private final Level level;

    public RaisedBedMenu(int id, Inventory inv, RaisedBedBlockEntity be) {
        super(ATMenuTypes.RAISED_BED_MENU, id);
        this.blockEntity = be;
        this.level = inv.player.level();

        addSlot(new RaisedBedSlot(be, RaisedBedBlockEntity.SLOT_PLANT, 62, 19));
        addSlot(new RaisedBedSlot(be, RaisedBedBlockEntity.SLOT_SOIL, 98, 19));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    public RaisedBedMenu(int id, Inventory inv, BlockPos pos) {
        this(id, inv, (RaisedBedBlockEntity) inv.player.level().getBlockEntity(pos));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (source == null || !source.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = source.getItem();
        ItemStack copy = stack.copy();
        String itemId = RegistryHelper.getItemId(stack);

        if (index >= 2) {
            if (PlantablesConfig.isValidSeed(itemId) || PlantablesConfig.isValidSapling(itemId)) {
                if (blockEntity.getItem(RaisedBedBlockEntity.SLOT_PLANT).isEmpty()) {
                    ItemStack existingSoil = blockEntity.getItem(RaisedBedBlockEntity.SLOT_SOIL);
                    if (!existingSoil.isEmpty()) {
                        String soilId = RegistryHelper.getItemId(existingSoil);
                        boolean valid = PlantablesConfig.isValidSeed(itemId)
                                ? PlantablesConfig.isSoilValidForSeed(soilId, itemId)
                                : PlantablesConfig.isSoilValidForSapling(soilId, itemId);
                        if (!valid) return ItemStack.EMPTY;
                    }
                    blockEntity.setItem(RaisedBedBlockEntity.SLOT_PLANT, stack.copyWithCount(1));
                    stack.shrink(1);
                    return copy;
                }
            } else if (PlantablesConfig.isValidSoil(itemId)) {
                if (blockEntity.getItem(RaisedBedBlockEntity.SLOT_SOIL).isEmpty()) {
                    ItemStack existingPlant = blockEntity.getItem(RaisedBedBlockEntity.SLOT_PLANT);
                    if (!existingPlant.isEmpty()) {
                        String plantId = RegistryHelper.getItemId(existingPlant);
                        boolean valid = PlantablesConfig.isValidSeed(plantId)
                                ? PlantablesConfig.isSoilValidForSeed(itemId, plantId)
                                : PlantablesConfig.isSoilValidForSapling(itemId, plantId);
                        if (!valid) return ItemStack.EMPTY;
                    }
                    blockEntity.setItem(RaisedBedBlockEntity.SLOT_SOIL, stack.copyWithCount(1));
                    stack.shrink(1);
                    return copy;
                }
            }
        } else {
            if (!moveItemStackTo(stack, 2, slots.size(), true)) return ItemStack.EMPTY;
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
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 52 + row * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(inv, col, 8 + col * 18, 111));
    }

    private static class RaisedBedSlot extends Slot {
        private final RaisedBedBlockEntity be;
        private final int index;

        public RaisedBedSlot(RaisedBedBlockEntity be, int index, int x, int y) {
            super(be, index, x, y);
            this.be = be;
            this.index = index;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return be.canPlaceItem(index, stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}