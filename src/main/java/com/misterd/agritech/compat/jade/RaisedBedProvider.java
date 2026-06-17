package com.misterd.agritech.compat.jade;

import com.misterd.agritech.blockentity.custom.RaisedBedBlockEntity;
import com.misterd.agritech.config.ATConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum RaisedBedProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    static final Identifier UID = Identifier.fromNamespaceAndPath("agritech", "raised_bed_info");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (!(be instanceof RaisedBedBlockEntity bed)) return;

        ItemStack seedStack = bed.getItem(RaisedBedBlockEntity.SLOT_PLANT);
        ItemStack soilStack = bed.getItem(RaisedBedBlockEntity.SLOT_SOIL);
        if (seedStack.isEmpty() || soilStack.isEmpty()) {
            data.putBoolean("hasCrop", false);
            return;
        }

        Level level = bed.getLevel();
        BlockPos pos = bed.getBlockPos();
        boolean canSeeSky = level.canSeeSky(pos.above());
        boolean isDay = level.isBrightOutside();
        boolean hasSkyBoost = canSeeSky && isDay;

        data.putBoolean("hasCrop", true);
        data.putString("cropName", seedStack.getDisplayName().getString());
        data.putInt("currentStage", bed.getGrowthStage());
        data.putInt("maxStage", bed.isTree() ? 1 : 8);
        data.putFloat("progressPercent", bed.getGrowthProgress() * 100.0F);
        data.putString("soilName", soilStack.getDisplayName().getString());
        data.putFloat("growthModifier", bed.getSoilGrowthModifier(soilStack));
        data.putBoolean("hasSkyBoost", hasSkyBoost);
        data.putFloat("skyBoostModifier", (float) ATConfig.get().raisedBedSkyDaySpeedMultiplier);
    }
}