package com.misterd.agritech.blockentity.custom;

import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.config.Config;
import com.misterd.agritech.config.PlantablesConfig;
import com.misterd.agritech.gui.custom.RaisedBedMenu;
import com.misterd.agritech.util.RegistryHelper;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RaisedBedBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int SLOT_PLANT = 0;
    public static final int SLOT_SOIL = 1;
    public static final int SIZE = 2;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(SIZE, ItemStack.EMPTY);

    private int growthProgress = 0;
    private int growthTicks = 0;
    private boolean readyToHarvest = false;
    private int lastGrowthStage = -1;

    public RaisedBedBlockEntity(BlockPos pos, BlockState blockState) {
        super(ATBlockEntities.RAISED_BED_BE, pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RaisedBedBlockEntity be) {
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
            float skyMod = getSkyDayModifier(level, pos);
            float totalMod = soilMod * skyMod;
            int adjustedTime = Math.max(1, Math.round(Config.planterBaseProcessingTime / totalMod));

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

        if (be.readyToHarvest) {
            be.harvestPlant(level, pos);
        }
    }

    private static float getSkyDayModifier(Level level, BlockPos pos) {
        boolean canSeeSky = level.canSeeSky(pos.above());
        boolean isDay = level.isBrightOutside();
        if (canSeeSky && isDay) return (float) Config.raisedBedSkyDaySpeedMultiplier;
        return 1.0F;
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

    public boolean isReadyToHarvest() {
        return readyToHarvest;
    }

    public void harvestPlant(Level level, BlockPos pos) {
        if (!readyToHarvest) return;

        List<ItemStack> drops = getHarvestDrops(inventory.get(SLOT_PLANT));
        for (ItemStack drop : drops) {
            double x = pos.getX() + 0.25 + level.getRandom().nextDouble() * 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.25 + level.getRandom().nextDouble() * 0.5;
            ItemEntity entity = new ItemEntity(level, x, y, z, drop);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }

        resetGrowth();
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

    public float getGrowthProgress() {
        return growthProgress / 100.0F;
    }

    public int getGrowthStage() {
        if (isTree()) return growthProgress > 50 ? 1 : 0;
        return Math.min(8, (int) (growthProgress / 12.5F));
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

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        String id = RegistryHelper.getItemId(stack);
        return switch (slot) {
            case SLOT_PLANT -> PlantablesConfig.isValidSeed(id) || PlantablesConfig.isValidSapling(id);
            case SLOT_SOIL -> PlantablesConfig.isValidSoil(id);
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
        return Component.translatable("gui.agritech.raised_bed_menu");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return new RaisedBedMenu(id, playerInv, this);
    }
}