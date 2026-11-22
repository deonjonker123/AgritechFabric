package com.misterd.agritech.blockentity.custom;

import com.misterd.agritech.block.custom.PlanterBlock;
import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.config.ATConfig;
import com.misterd.agritech.datamap.ATDataMaps;
import com.misterd.agritech.gui.custom.PlanterMenu;
import com.misterd.agritech.item.ATItems;
import com.misterd.agritech.recipe.DropEntry;
import com.misterd.agritech.recipe.PlantIndex;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PlanterBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int SLOT_PLANT = 0;
    public static final int SLOT_SOIL = 1;
    public static final int SLOT_FERTILIZER = 2;
    public static final int SLOT_OUTPUT_START = 3;
    public static final int SLOT_OUTPUT_END = 14;
    public static final int SIZE = 15;

    private static final int[] OUTPUT_SLOTS = buildRange(SLOT_OUTPUT_START, SLOT_OUTPUT_END);
    private static final int[] FERTILIZER_SLOTS = { SLOT_FERTILIZER };
    private static final int[] NO_SLOTS = new int[0];
    private static final int PUSH_INTERVAL = 8;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    private int growthProgress = 0;
    private int growthTicks = 0;
    private boolean readyToHarvest = false;
    private int lastSyncedStep = -1;
    private @Nullable List<ItemStack> pendingDrops = null;
    private int pushCooldown = 0;

    public PlanterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATBlockEntities.PLANTER_BE, pos, blockState);
    }

    private PlantIndex plants() {
        return PlantIndex.get(level);
    }

    public boolean isValidPlant(ItemStack stack) {
        return plants().isPlant(stack);
    }

    public boolean isValidSoilForAnyRecipe(ItemStack stack) {
        return plants().isSoil(stack);
    }

    public boolean isValidPlantSoilCombination(ItemStack plant, ItemStack soil) {
        return plants().isValidCombination(plant, soil);
    }

    public boolean isTree() {
        PlantIndex.Plant plant = plants().find(inventory.get(SLOT_PLANT));
        return plant != null && plant.tree();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PlanterBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        be.tryPushOutputBelow(serverLevel, pos);

        ItemStack plantStack = be.inventory.get(SLOT_PLANT);
        ItemStack soilStack = be.inventory.get(SLOT_SOIL);
        PlantIndex.Plant plant = be.plants().find(plantStack);

        if (plant == null || !plant.acceptsSoil(soilStack)) {
            be.resetGrowth();
            return;
        }

        if (!be.readyToHarvest) {
            float totalMod = be.getSoilGrowthModifier(soilStack) * be.getFertilizerGrowthModifier() * be.getClocheGrowthModifier();
            int adjustedTime = Math.max(1, Math.round(ATConfig.get().planterBaseProcessingTime / totalMod));

            be.growthTicks++;
            if (be.growthTicks >= adjustedTime) {
                be.readyToHarvest = true;
                be.growthProgress = 100;
            } else {
                be.growthProgress = (int) ((float) be.growthTicks / adjustedTime * 100.0F);
            }
            be.syncIfStepChanged();
        }

        if (be.readyToHarvest) {
            be.tryHarvest(serverLevel, plant);
        }
    }

    private int visualStep() {
        if (readyToHarvest) return Integer.MAX_VALUE;
        return isTree() ? growthProgress / 10 : getGrowthStage();
    }

    private void syncIfStepChanged() {
        int step = visualStep();
        if (step == lastSyncedStep) return;
        lastSyncedStep = step;
        sync();
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public float getSoilGrowthModifier(ItemStack soilStack) {
        if (soilStack.isEmpty()) return 1.0F;
        var data = ATDataMaps.getSoilModifier(soilStack.getItem());
        return data != null ? data.growthModifier() : 1.0F;
    }

    private float getFertilizerGrowthModifier() {
        ItemStack stack = inventory.get(SLOT_FERTILIZER);
        if (stack.isEmpty()) return 1.0F;
        var data = ATDataMaps.getFertilizer(stack.getItem());
        return data != null ? data.speedMultiplier() : 1.0F;
    }

    private float getFertilizerYieldModifier() {
        ItemStack stack = inventory.get(SLOT_FERTILIZER);
        if (stack.isEmpty()) return 1.0F;
        var data = ATDataMaps.getFertilizer(stack.getItem());
        return data != null ? data.yieldMultiplier() : 1.0F;
    }

    public boolean isCloched() {
        BlockState state = getBlockState();
        return state.hasProperty(PlanterBlock.CLOCHED) && state.getValue(PlanterBlock.CLOCHED);
    }

    private float getClocheGrowthModifier() {
        return isCloched() ? (float) ATConfig.get().clocheSpeedMultiplier : 1.0F;
    }

    private float getClocheYieldModifier() {
        return isCloched() ? (float) ATConfig.get().clocheYieldMultiplier : 1.0F;
    }

    private void resetGrowth() {
        pendingDrops = null;
        if (growthProgress == 0 && growthTicks == 0 && !readyToHarvest) return;
        growthProgress = 0;
        growthTicks = 0;
        readyToHarvest = false;
        lastSyncedStep = -1;
        sync();
    }

    private void tryHarvest(ServerLevel level, PlantIndex.Plant plant) {
        if (pendingDrops == null) {
            float yield = getFertilizerYieldModifier() * getClocheYieldModifier();
            pendingDrops = DropEntry.roll(plant.drops(), level.getRandom(), yield);
        }

        if (!fitsInOutput(pendingDrops)) return;

        List<ItemStack> outputs = inventory.subList(SLOT_OUTPUT_START, SLOT_OUTPUT_END + 1);
        for (ItemStack drop : pendingDrops) insert(outputs, drop);

        consumeFertilizer();
        resetGrowth();
    }

    private boolean fitsInOutput(List<ItemStack> drops) {
        List<ItemStack> simulated = new ArrayList<>(SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1);
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) simulated.add(inventory.get(slot).copy());
        for (ItemStack drop : drops) {
            if (insert(simulated, drop) > 0) return false;
        }
        return true;
    }

    private static int insert(List<ItemStack> slots, ItemStack stack) {
        int remaining = stack.getCount();
        for (int i = 0; i < slots.size() && remaining > 0; i++) {
            ItemStack existing = slots.get(i);
            if (existing.isEmpty() || !ItemStack.isSameItemSameComponents(existing, stack)) continue;
            int toAdd = Math.min(existing.getMaxStackSize() - existing.getCount(), remaining);
            if (toAdd <= 0) continue;
            existing.grow(toAdd);
            remaining -= toAdd;
        }
        for (int i = 0; i < slots.size() && remaining > 0; i++) {
            if (!slots.get(i).isEmpty()) continue;
            int toPlace = Math.min(remaining, stack.getMaxStackSize());
            slots.set(i, stack.copyWithCount(toPlace));
            remaining -= toPlace;
        }
        return remaining;
    }

    public void applyManualFertilizer(float speedMultiplier) {
        if (readyToHarvest) return;
        ItemStack plantStack = inventory.get(SLOT_PLANT);
        ItemStack soilStack = inventory.get(SLOT_SOIL);
        if (plantStack.isEmpty() || soilStack.isEmpty()) return;

        float mod = getSoilGrowthModifier(soilStack) * getClocheGrowthModifier();
        int adjustedTime = Math.max(1, Math.round(ATConfig.get().planterBaseProcessingTime / mod));

        int boost = Math.max(1, Math.round(adjustedTime * 0.25F * speedMultiplier));
        growthTicks = Math.min(adjustedTime, growthTicks + boost);
        growthProgress = (int) ((float) growthTicks / adjustedTime * 100.0F);

        if (growthTicks >= adjustedTime) {
            readyToHarvest = true;
            growthProgress = 100;
        }

        lastSyncedStep = visualStep();
        setChanged();
    }

    private void consumeFertilizer() {
        ItemStack stack = inventory.get(SLOT_FERTILIZER);
        if (stack.isEmpty()) return;
        stack.shrink(1);
        setChanged();
    }

    private void tryPushOutputBelow(ServerLevel level, BlockPos pos) {
        if (--pushCooldown > 0) return;
        pushCooldown = PUSH_INTERVAL;
        if (!hasOutput()) return;

        Storage<ItemVariant> target = ItemStorage.SIDED.find(level, pos.below(), Direction.UP);
        if (target == null || !target.supportsInsertion()) return;

        boolean changed = false;
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) {
            ItemStack stack = inventory.get(slot);
            if (stack.isEmpty()) continue;

            try (Transaction tx = Transaction.openOuter()) {
                long inserted = target.insert(ItemVariant.of(stack), stack.getCount(), tx);
                if (inserted > 0) {
                    tx.commit();
                    stack.shrink((int) inserted);
                    changed = true;
                }
            }
        }
        if (changed) setChanged();
    }

    private boolean hasOutput() {
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) {
            if (!inventory.get(slot).isEmpty()) return true;
        }
        return false;
    }

    public float getGrowthProgress() {
        return growthProgress / 100.0F;
    }

    public int getGrowthProgressPercent() {
        return growthProgress;
    }

    public int getGrowthStage() {
        if (isTree()) return growthProgress > 50 ? 1 : 0;
        return Math.min(8, (int) (growthProgress / 12.5F));
    }

    public boolean isReadyToHarvest() {
        return readyToHarvest;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (!(level instanceof ServerLevel)) return;
        Containers.dropContents(level, pos, this);
        if (state.hasProperty(PlanterBlock.CLOCHED) && state.getValue(PlanterBlock.CLOCHED)) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(ATItems.CLOCHE));
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) return OUTPUT_SLOTS;
        if (side.getAxis().isHorizontal()) return FERTILIZER_SLOTS;
        return NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (side == null || !side.getAxis().isHorizontal()) return false;
        return slot == SLOT_FERTILIZER && ATDataMaps.getFertilizer(stack.getItem()) != null;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && slot >= SLOT_OUTPUT_START && slot <= SLOT_OUTPUT_END;
    }

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

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_PLANT -> isValidPlant(stack);
            case SLOT_SOIL -> isValidSoilForAnyRecipe(stack);
            case SLOT_FERTILIZER -> ATDataMaps.getFertilizer(stack.getItem()) != null;
            default -> false;
        };
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
        output.putInt("growthProgress", growthProgress);
        output.putInt("growthTicks", growthTicks);
        output.putBoolean("readyToHarvest", readyToHarvest);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
        growthProgress = input.getIntOr("growthProgress", 0);
        growthTicks = input.getIntOr("growthTicks", 0);
        readyToHarvest = input.getBooleanOr("readyToHarvest", false);
        lastSyncedStep = -1;
        pendingDrops = null;
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
        return Component.translatable("gui.agritech.planter_menu");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new PlanterMenu(id, playerInv, this);
    }

    private static int[] buildRange(int from, int to) {
        int[] arr = new int[to - from + 1];
        for (int i = 0; i < arr.length; i++) arr[i] = from + i;
        return arr;
    }
}