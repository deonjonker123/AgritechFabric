package com.misterd.agritech.blockentity.custom;

import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.config.ATConfig;
import com.misterd.agritech.datamap.ATDataMaps;
import com.misterd.agritech.gui.custom.RaisedBedMenu;
import com.misterd.agritech.recipe.DropEntry;
import com.misterd.agritech.recipe.PlantIndex;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
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
import org.jspecify.annotations.Nullable;

public class RaisedBedBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int SLOT_PLANT = 0;
    public static final int SLOT_SOIL = 1;
    public static final int SIZE = 2;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    private int growthProgress = 0;
    private int growthTicks = 0;
    private boolean readyToHarvest = false;
    private int lastSyncedStep = -1;

    public RaisedBedBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATBlockEntities.RAISED_BED_BE, pos, blockState);
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

    public static void tick(Level level, BlockPos pos, BlockState state, RaisedBedBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        ItemStack plantStack = be.inventory.get(SLOT_PLANT);
        ItemStack soilStack = be.inventory.get(SLOT_SOIL);
        PlantIndex.Plant plant = be.plants().find(plantStack);

        if (plant == null || !plant.acceptsSoil(soilStack)) {
            be.resetGrowth();
            return;
        }

        if (!be.readyToHarvest) {
            float totalMod = be.getSoilGrowthModifier(soilStack) * getSkyDayModifier(level, pos);
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
            be.harvestPlant(serverLevel, pos, plant);
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

    private static float getSkyDayModifier(Level level, BlockPos pos) {
        boolean canSeeSky = level.canSeeSky(pos.above());
        boolean isDay = level.isBrightOutside();
        if (canSeeSky && isDay) return (float) ATConfig.get().raisedBedSkyDaySpeedMultiplier;
        return 1.0F;
    }

    public float getSoilGrowthModifier(ItemStack soilStack) {
        if (soilStack.isEmpty()) return 1.0F;
        var data = ATDataMaps.getSoilModifier(soilStack.getItem());
        return data != null ? data.growthModifier() : 1.0F;
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

        lastSyncedStep = visualStep();
        setChanged();
    }

    private void resetGrowth() {
        if (growthProgress == 0 && growthTicks == 0 && !readyToHarvest) return;
        growthProgress = 0;
        growthTicks = 0;
        readyToHarvest = false;
        lastSyncedStep = -1;
        sync();
    }

    public boolean isReadyToHarvest() {
        return readyToHarvest;
    }

    private void harvestPlant(ServerLevel level, BlockPos pos, PlantIndex.Plant plant) {
        for (ItemStack drop : DropEntry.roll(plant.drops(), level.getRandom(), 1.0F)) {
            double x = pos.getX() + 0.25 + level.getRandom().nextDouble() * 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.25 + level.getRandom().nextDouble() * 0.5;
            ItemEntity entity = new ItemEntity(level, x, y, z, drop);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
        resetGrowth();
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

    public void drops() {
        Containers.dropContents(level, worldPosition, this);
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
        return Component.translatable("gui.agritech.raised_bed_menu");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new RaisedBedMenu(id, playerInv, this);
    }
}