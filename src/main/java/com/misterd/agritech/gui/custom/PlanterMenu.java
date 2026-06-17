package com.misterd.agritech.gui.custom;

import com.misterd.agritech.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritech.datamap.ATDataMaps;
import com.misterd.agritech.gui.ATMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PlanterMenu extends AbstractContainerMenu {

    public final PlanterBlockEntity blockEntity;
    private final Level level;

    public PlanterMenu(int id, Inventory inv, BlockPos pos) {
        this(id, inv, (PlanterBlockEntity) inv.player.level().getBlockEntity(pos));
    }

    public PlanterMenu(int id, Inventory inv, PlanterBlockEntity be) {
        super(ATMenuTypes.PLANTER_MENU, id);
        this.blockEntity = be;
        this.level = inv.player.level();

        addSlot(new PlanterSlot(be, PlanterBlockEntity.SLOT_PLANT, 8, 18));
        addSlot(new PlanterSlot(be, PlanterBlockEntity.SLOT_SOIL, 8, 54));
        addSlot(new FertilizerSlot(be, PlanterBlockEntity.SLOT_FERTILIZER, 152, 18));

        int slot = PlanterBlockEntity.SLOT_OUTPUT_START;
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 4; col++)
                addSlot(new OutputSlot(be, slot++, 62 + col * 18, 18 + row * 18));

        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (source == null || !source.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = source.getItem();
        ItemStack copy = stack.copy();

        if (index < 15) {
            if (!moveItemStackTo(stack, 15, 51, true)) return ItemStack.EMPTY;
        } else {
            if (!moveToSpecialSlots(stack)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();

        source.onTake(player, stack);
        return copy;
    }

    private boolean moveToSpecialSlots(ItemStack stack) {
        if (blockEntity.isValidPlant(stack) && blockEntity.getItem(PlanterBlockEntity.SLOT_PLANT).isEmpty()) {
            ItemStack existingSoil = blockEntity.getItem(PlanterBlockEntity.SLOT_SOIL);
            if (!existingSoil.isEmpty() && !blockEntity.isValidPlantSoilCombination(stack, existingSoil)) return false;
            blockEntity.setItem(PlanterBlockEntity.SLOT_PLANT, stack.copyWithCount(1));
            stack.shrink(1);
            return true;
        }

        if (blockEntity.isValidSoilForAnyRecipe(stack) && blockEntity.getItem(PlanterBlockEntity.SLOT_SOIL).isEmpty()) {
            ItemStack existingPlant = blockEntity.getItem(PlanterBlockEntity.SLOT_PLANT);
            if (!existingPlant.isEmpty() && !blockEntity.isValidPlantSoilCombination(existingPlant, stack)) return false;
            blockEntity.setItem(PlanterBlockEntity.SLOT_SOIL, stack.copyWithCount(1));
            stack.shrink(1);
            return true;
        }

        if (ATDataMaps.getFertilizer(stack.getItem()) != null) {
            return moveItemStackTo(stack, PlanterBlockEntity.SLOT_FERTILIZER, PlanterBlockEntity.SLOT_FERTILIZER + 1, false);
        }

        return false;
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
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 88 + row * 18));
    }

    private void addPlayerHotbar(Inventory inv) {
        for (int i = 0; i < 9; i++)
            addSlot(new Slot(inv, i, 8 + i * 18, 146));
    }

    private static class PlanterSlot extends Slot {
        private final PlanterBlockEntity be;
        private final int index;

        public PlanterSlot(PlanterBlockEntity be, int index, int x, int y) {
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

    private static class FertilizerSlot extends Slot {
        public FertilizerSlot(PlanterBlockEntity be, int index, int x, int y) {
            super(be, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ATDataMaps.getFertilizer(stack.getItem()) != null;
        }
    }

    private static class OutputSlot extends Slot {
        public OutputSlot(PlanterBlockEntity be, int index, int x, int y) {
            super(be, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}