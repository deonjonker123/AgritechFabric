package com.misterd.agritech.blockentity.custom;

import com.misterd.agritech.Agritech;
import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.config.ATConfig;
import com.misterd.agritech.datamap.ATDataMaps;
import com.misterd.agritech.gui.custom.PlanterMenu;
import com.misterd.agritech.recipe.ATRecipeTypes;
import com.misterd.agritech.recipe.CropRecipe;
import com.misterd.agritech.recipe.DropEntry;
import com.misterd.agritech.recipe.TreeRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
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

    private @Nullable CropRecipe cachedCropRecipe = null;
    private @Nullable TreeRecipe cachedTreeRecipe = null;
    private @Nullable Item cachedSeedItem = null;
    private Set<Item> cachedValidSoils = null;
    private int soilCacheRevision = -1;
    private int cachedRevision = -1;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    private int growthProgress = 0;
    private int growthTicks = 0;
    private boolean readyToHarvest = false;
    private int lastGrowthStage = -1;

    public PlanterBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATBlockEntities.PLANTER_BE, pos, blockState);
    }

    private void invalidateRecipeCache() {
        cachedCropRecipe = null;
        cachedTreeRecipe = null;
        cachedSeedItem = null;
        cachedRevision = -1;
    }

    @Nullable
    private RecipeManager getRecipes() {
        if (level instanceof ServerLevel serverLevel) return serverLevel.recipeAccess();
        return null;
    }

    private void refreshRecipeCacheIfNeeded(ItemStack seed) {
        if (seed.isEmpty()) {
            invalidateRecipeCache();
            return;
        }
        Item seedItem = seed.getItem();
        if (seedItem == cachedSeedItem && cachedRevision == Agritech.RECIPE_REVISION) return;

        invalidateRecipeCache();
        RecipeManager rm = getRecipes();
        if (rm == null) return;

        cachedSeedItem = seedItem;
        cachedRevision = Agritech.RECIPE_REVISION;
        SingleRecipeInput input = new SingleRecipeInput(seed);

        Optional<RecipeHolder<CropRecipe>> crop = rm.getRecipeFor(ATRecipeTypes.CROP_TYPE, input, level);
        if (crop.isPresent()) { cachedCropRecipe = crop.get().value(); return; }

        Optional<RecipeHolder<TreeRecipe>> tree = rm.getRecipeFor(ATRecipeTypes.TREE_TYPE, input, level);
        tree.ifPresent(h -> cachedTreeRecipe = h.value());
    }

    private Optional<CropRecipe> findCropRecipe(ItemStack seed) {
        if (seed.isEmpty()) return Optional.empty();
        refreshRecipeCacheIfNeeded(seed);
        return Optional.ofNullable(cachedCropRecipe);
    }

    private Optional<TreeRecipe> findTreeRecipe(ItemStack sapling) {
        if (sapling.isEmpty()) return Optional.empty();
        refreshRecipeCacheIfNeeded(sapling);
        return Optional.ofNullable(cachedTreeRecipe);
    }

    public boolean isValidPlant(ItemStack stack) {
        if (level == null) return false;
        return findCropRecipe(stack).isPresent() || findTreeRecipe(stack).isPresent();
    }

    private Set<Item> getValidSoils() {
        if (cachedValidSoils != null && soilCacheRevision == Agritech.RECIPE_REVISION) {
            return cachedValidSoils;
        }
        RecipeManager rm = getRecipes();
        if (rm == null) return Set.of();
        Set<Item> soils = new HashSet<>();
        for (RecipeHolder<?> holder : rm.getRecipes()) {
            if (holder.value().getType() == ATRecipeTypes.CROP_TYPE) {
                for (Ingredient ing : ((CropRecipe) holder.value()).getSoils()) {
                    ing.items().map(Holder::value).forEach(soils::add);
                }
            } else if (holder.value().getType() == ATRecipeTypes.TREE_TYPE) {
                for (Ingredient ing : ((TreeRecipe) holder.value()).getSoils()) {
                    ing.items().map(Holder::value).forEach(soils::add);
                }
            }
        }
        cachedValidSoils = soils;
        soilCacheRevision = Agritech.RECIPE_REVISION;
        return cachedValidSoils;
    }

    public boolean isValidSoilForAnyRecipe(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return getValidSoils().contains(stack.getItem());
    }

    public boolean isValidPlantSoilCombination(ItemStack plant, ItemStack soil) {
        if (plant.isEmpty() || soil.isEmpty()) return false;
        Optional<CropRecipe> crop = findCropRecipe(plant);
        if (crop.isPresent()) return crop.get().matchesSoil(soil);
        Optional<TreeRecipe> tree = findTreeRecipe(plant);
        return tree.isPresent() && tree.get().matchesSoil(soil);
    }

    public boolean isTree() {
        return findTreeRecipe(inventory.get(SLOT_PLANT)).isPresent();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PlanterBlockEntity be) {
        if (level.isClientSide()) return;

        ItemStack plantStack = be.inventory.get(SLOT_PLANT);
        ItemStack soilStack = be.inventory.get(SLOT_SOIL);

        if (plantStack.isEmpty() || soilStack.isEmpty()) {
            be.resetGrowth();
            return;
        }

        if (!be.isValidPlantSoilCombination(plantStack, soilStack)) {
            be.resetGrowth();
            return;
        }

        if (!be.readyToHarvest) {
            float soilMod = be.getSoilGrowthModifier(soilStack);
            float fertMod = be.getFertilizerGrowthModifier();
            float totalMod = soilMod * fertMod;
            int baseTime = ATConfig.get().planterBaseProcessingTime;
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
                boolean stageChanged = stage != be.lastGrowthStage;
                if (stageChanged) {
                    be.lastGrowthStage = stage;
                }
                if (stageChanged || be.growthTicks % 10 == 0) {
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

        float soilMod = getSoilGrowthModifier(soilStack);
        int adjustedTime = Math.max(1, Math.round(ATConfig.get().planterBaseProcessingTime / soilMod));

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

        List<DropEntry> entries;
        Optional<CropRecipe> crop = findCropRecipe(plantStack);
        if (crop.isPresent()) {
            entries = crop.get().getDrops();
        } else {
            Optional<TreeRecipe> tree = findTreeRecipe(plantStack);
            if (tree.isEmpty()) return drops;
            entries = tree.get().getDrops();
        }

        Random rng = new Random();
        for (DropEntry entry : entries) {
            if (rng.nextFloat() <= entry.chance()) {
                int count = entry.max() > entry.min()
                        ? entry.min() + rng.nextInt(entry.max() - entry.min() + 1)
                        : entry.min();
                drops.add(new ItemStack(entry.item(), count));
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