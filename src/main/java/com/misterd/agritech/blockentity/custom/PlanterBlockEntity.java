package com.misterd.agritech.blockentity.custom;

import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.gui.custom.PlanterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jspecify.annotations.Nullable;

public class PlanterBlockEntity extends BlockEntity implements ContainerSingleItem.BlockContainerSingleItem, MenuProvider {
    public final NonNullList<ItemStack> inventory = NonNullList.withSize(15, ItemStack.EMPTY);

    public PlanterBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ATBlockEntities.PLANTER_BE, worldPosition, blockState);
    }

    @Override
    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    @Override
    public ItemStack getTheItem() {
        return inventory.getFirst();
    }

    @Override
    public void setTheItem(ItemStack itemStack) {
        setChanged();
        inventory.set(0, itemStack.copyWithCount(1));
    }

    @Override
    public void clearContent() {
        inventory.set(0, ItemStack.EMPTY);
    }

    public void drops() {
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.agritech.planter_menu");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PlanterMenu(containerId, inventory, this.worldPosition);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
    }
}
