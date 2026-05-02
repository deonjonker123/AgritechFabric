package com.misterd.agritech.blockentity.custom;

import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.config.Config;
import com.misterd.agritech.config.PlantablesConfig;
import com.misterd.agritech.gui.custom.PlanterMenu;
import com.misterd.agritech.util.RegistryHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class PlanterBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider {

    public static final int SLOT_PLANT = 0;
    public static final int SLOT_SOIL = 1;
    public static final int SLOT_FERTILIZER = 2;
    public static final int SLOT_OUTPUT_START = 3;
    public static final int SLOT_OUTPUT_END = 14;
    public static final int SIZE = 15;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    private int growthProgress = 0;
    private int growthTicks = 0;
    private boolean readyToHarvest = false;
    private int lastGrowthStage = -1;

    public PlanterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATBlockEntities.PLANTER_BE, pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PlanterBlockEntity be) {
        if (level.isClientSide()) return;

        ItemStack plantStack = be.inventory.get(SLOT_PLANT);
        ItemStack soilStack = be.inventory.get(SLOT_SOIL);

        if (plantStack.isEmpty() || soilStack.isEmpty()) {
            be.resetGrowth();
            return;
        }

        String plantId = RegistryHelper.getItemId(plantStack);
        String soilId = RegistryHelper.getItemId(soilStack);

        if (!be.isValidPlantSoilCombination(plantId, soilId)) {
            be.resetGrowth();
            return;
        }

        if (!be.readyToHarvest) {
            float soilMod = PlantablesConfig.getSoilGrowthModifier(soilId);
            float fertMod = be.getFertilizerSpeedModifier();
            float totalMod = soilMod * fertMod;
            int baseTime = Config.planterBaseProcessingTime;
            int adjustedTime = Math.max(1, Math.round(baseTime / totalMod));

            be.growthTicks++;

            if (be.growthTicks >= adjustedTime) {
                be.readyToHarvest = true;
                be.growthProgress = 100;
                be.lastGrowthStage = be.getGrowthStage();
                level.sendBlockUpdated(pos, state, state, 3);
                be.setChanged();
            } else {
                be.growthProgress = (int) ((float) be.growthTicks / adjustedTime * 100.0F);
                int stage = be.getGrowthStage();
                if (stage != be.lastGrowthStage) {
                    be.lastGrowthStage = stage;
                }
                if (be.growthTicks % 20 == 0) {
                    level.sendBlockUpdated(pos, state, state, 3);
                    be.setChanged();
                }
            }
        }

        if (be.readyToHarvest && be.hasOutputSpace()) {
            be.harvestPlant();
        }

        tryPushOutputBelow(level, pos, be);
    }

    private float getFertilizerSpeedModifier() {
        ItemStack stack = inventory.get(SLOT_FERTILIZER);
        if (stack.isEmpty()) return 1.0F;
        PlantablesConfig.FertilizerInfo info = PlantablesConfig.getFertilizerInfo(RegistryHelper.getItemId(stack));
        return info != null ? info.speedMultiplier : 1.0F;
    }

    private float getFertilizerYieldModifier() {
        ItemStack stack = inventory.get(SLOT_FERTILIZER);
        if (stack.isEmpty()) return 1.0F;
        PlantablesConfig.FertilizerInfo info = PlantablesConfig.getFertilizerInfo(RegistryHelper.getItemId(stack));
        return info != null ? info.yieldMultiplier : 1.0F;
    }

    private boolean isValidPlantSoilCombination(String plantId, String soilId) {
        if (PlantablesConfig.isValidSeed(plantId)) return PlantablesConfig.isSoilValidForSeed(soilId, plantId);
        if (PlantablesConfig.isValidSapling(plantId)) return PlantablesConfig.isSoilValidForSapling(soilId, plantId);
        return false;
    }

    private boolean isTree() {
        ItemStack s = inventory.get(SLOT_PLANT);
        return !s.isEmpty() && PlantablesConfig.isValidSapling(RegistryHelper.getItemId(s));
    }

    private void resetGrowth() {
        if (growthProgress == 0 && growthTicks == 0 && !readyToHarvest) return;
        growthProgress = 0;
        growthTicks = 0;
        readyToHarvest = false;
        lastGrowthStage = -1;
        setChanged();
    }

    public void harvestPlant() {
        if (!readyToHarvest) return;

        float yieldMod = getFertilizerYieldModifier();
        List<ItemStack> drops = applyYieldModifier(getHarvestDrops(inventory.get(SLOT_PLANT)), yieldMod);

        for (ItemStack drop : drops) {
            int remaining = drop.getCount();

            for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && remaining > 0; slot++) {
                ItemStack existing = inventory.get(slot);
                if (!existing.isEmpty() && existing.is(drop.getItem())) {
                    int space = existing.getMaxStackSize() - existing.getCount();
                    if (space <= 0) continue;
                    int toAdd = Math.min(space, remaining);
                    existing.grow(toAdd);
                    remaining -= toAdd;
                }
            }

            for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && remaining > 0; slot++) {
                if (inventory.get(slot).isEmpty()) {
                    int toPlace = Math.min(remaining, drop.getMaxStackSize());
                    inventory.set(slot, new ItemStack(drop.getItem(), toPlace));
                    remaining -= toPlace;
                }
            }
        }

        consumeFertilizer();
        resetGrowth();
        setChanged();
    }

    public void applyManualFertilizer(float speedMultiplier) {
        if (readyToHarvest) return;
        ItemStack plantStack = inventory.get(SLOT_PLANT);
        ItemStack soilStack = inventory.get(SLOT_SOIL);
        if (plantStack.isEmpty() || soilStack.isEmpty()) return;

        float soilMod = PlantablesConfig.getSoilGrowthModifier(RegistryHelper.getItemId(soilStack));
        int adjustedTime = Math.max(1, Math.round(Config.planterBaseProcessingTime / soilMod));

        int boost = Math.max(1, Math.round(adjustedTime * 0.25F * speedMultiplier));
        growthTicks = Math.min(adjustedTime, growthTicks + boost);
        growthProgress = (int) ((float) growthTicks / adjustedTime * 100.0F);

        if (growthTicks >= adjustedTime) {
            readyToHarvest = true;
            growthProgress = 100;
        }

        lastGrowthStage = getGrowthStage();
        setChanged();
    }

    private void consumeFertilizer() {
        ItemStack stack = inventory.get(SLOT_FERTILIZER);
        if (stack.isEmpty()) return;
        stack.shrink(1);
        setChanged();
    }

    private List<ItemStack> applyYieldModifier(List<ItemStack> drops, float mod) {
        if (mod == 1.0F) return drops;
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack drop : drops) {
            out.add(new ItemStack(drop.getItem(), Math.max(1, Math.round(drop.getCount() * mod))));
        }
        return out;
    }

    private List<ItemStack> getHarvestDrops(ItemStack plantStack) {
        List<ItemStack> drops = new ArrayList<>();
        if (plantStack.isEmpty()) return drops;

        String plantId = RegistryHelper.getItemId(plantStack);
        List<PlantablesConfig.DropInfo> configDrops;

        if (PlantablesConfig.isValidSeed(plantId)) configDrops = PlantablesConfig.getCropDrops(plantId);
        else if (PlantablesConfig.isValidSapling(plantId)) configDrops = PlantablesConfig.getTreeDrops(plantId);
        else return drops;

        Random rng = new Random();
        for (PlantablesConfig.DropInfo info : configDrops) {
            if (rng.nextFloat() <= info.chance) {
                int count = info.maxCount > info.minCount
                        ? info.minCount + rng.nextInt(info.maxCount - info.minCount + 1)
                        : info.minCount;
                Item item = RegistryHelper.getItem(info.item);
                if (item != null) drops.add(new ItemStack(item, count));
            }
        }
        return drops;
    }

    private static void tryPushOutputBelow(Level level, BlockPos pos, PlanterBlockEntity be) {
        BlockPos below = pos.below();
        if (!(level.getBlockEntity(below) instanceof Container target)) return;

        boolean changed = false;
        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) {
            ItemStack stack = be.inventory.get(slot);
            if (stack.isEmpty()) continue;

            int[] slots = target instanceof WorldlyContainer wc
                    ? wc.getSlotsForFace(Direction.UP)
                    : range(target.getContainerSize());

            for (int targetSlot : slots) {
                if (target instanceof WorldlyContainer wc
                        && !wc.canPlaceItemThroughFace(targetSlot, stack, Direction.UP)) continue;

                ItemStack targetStack = target.getItem(targetSlot);
                if (targetStack.isEmpty()) {
                    target.setItem(targetSlot, stack.copy());
                    be.inventory.set(slot, ItemStack.EMPTY);
                    changed = true;
                    break;
                } else if (targetStack.is(stack.getItem())) {
                    int space = targetStack.getMaxStackSize() - targetStack.getCount();
                    if (space <= 0) continue;
                    int toMove = Math.min(space, stack.getCount());
                    targetStack.grow(toMove);
                    stack.shrink(toMove);
                    if (stack.isEmpty()) be.inventory.set(slot, ItemStack.EMPTY);
                    changed = true;
                    break;
                }
            }
        }

        if (changed) {
            be.setChanged();
            target.setChanged();
        }
    }

    public boolean hasOutputSpace() {
        List<ItemStack> drops = getHarvestDrops(inventory.get(SLOT_PLANT));

        Map<Integer, Integer> simAmounts = new HashMap<>();
        Map<Integer, Item> simItems = new HashMap<>();
        Map<Integer, Integer> simCapacity = new HashMap<>();

        for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END; slot++) {
            ItemStack s = inventory.get(slot);
            simAmounts.put(slot, s.getCount());
            simItems.put(slot, s.isEmpty() ? null : s.getItem());
            simCapacity.put(slot, s.isEmpty() ? 64 : s.getMaxStackSize());
        }

        for (ItemStack drop : drops) {
            int remaining = drop.getCount();

            for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && remaining > 0; slot++) {
                Item here = simItems.get(slot);
                if (here != null && here == drop.getItem()) {
                    int space = simCapacity.get(slot) - simAmounts.get(slot);
                    int toAdd = Math.min(space, remaining);
                    simAmounts.merge(slot, toAdd, Integer::sum);
                    remaining -= toAdd;
                }
            }

            for (int slot = SLOT_OUTPUT_START; slot <= SLOT_OUTPUT_END && remaining > 0; slot++) {
                if (simItems.get(slot) == null) {
                    simItems.put(slot, drop.getItem());
                    simAmounts.put(slot, remaining);
                    remaining = 0;
                }
            }

            if (remaining > 0) return false;
        }
        return true;
    }

    private static int[] range(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) arr[i] = i;
        return arr;
    }

    public float getGrowthProgress() {
        return growthProgress / 100.0F;
    }

    public int getGrowthStage() {
        if (isTree()) return growthProgress > 50 ? 1 : 0;
        return Math.min(8, (int) (growthProgress / 12.5F));
    }

    public boolean isReadyToHarvest() {
        return readyToHarvest;
    }

    public void drops() {
        Containers.dropContents(level, worldPosition, this);
    }

    // --- WorldlyContainer ---

    @Override
    public int[] getSlotsForFace(Direction side) {
        if (side == Direction.DOWN) {
            int[] slots = new int[SLOT_OUTPUT_END - SLOT_OUTPUT_START + 1];
            for (int i = 0; i < slots.length; i++) slots[i] = SLOT_OUTPUT_START + i;
            return slots;
        }
        if (side.getAxis().isHorizontal()) return new int[]{ SLOT_FERTILIZER };
        return new int[0];
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (side == null || !side.getAxis().isHorizontal()) return false;
        return slot == SLOT_FERTILIZER && PlantablesConfig.isValidFertilizer(RegistryHelper.getItemId(stack));
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
        String id = RegistryHelper.getItemId(stack);
        return switch (slot) {
            case SLOT_PLANT -> PlantablesConfig.isValidSeed(id) || PlantablesConfig.isValidSapling(id);
            case SLOT_SOIL -> PlantablesConfig.isValidSoil(id);
            case SLOT_FERTILIZER -> PlantablesConfig.isValidFertilizer(id);
            default -> false;
        };
    }

    // --- Serialization ---

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
        output.putInt("growthProgress", growthProgress);
        output.putInt("growthTicks", growthTicks);
        output.putBoolean("readyToHarvest", readyToHarvest);
        output.putInt("lastGrowthStage", lastGrowthStage);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
        growthProgress = input.getIntOr("growthProgress", 0);
        growthTicks = input.getIntOr("growthTicks", 0);
        readyToHarvest = input.getBooleanOr("readyToHarvest", false);
        lastGrowthStage = input.getIntOr("lastGrowthStage", -1);
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
        return new PlanterMenu(id, playerInv, this.worldPosition);
    }
}