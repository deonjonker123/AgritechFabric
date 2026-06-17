package com.misterd.agritech.compat.jade;

import com.misterd.agritech.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritech.datamap.ATDataMaps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum PlanterProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    static final Identifier UID = Identifier.fromNamespaceAndPath("agritech", "planter_info");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity be = accessor.getBlockEntity();
        if (be instanceof PlanterBlockEntity planter) {
            appendBasicPlanterData(data, planter, accessor.getBlockState());
        }
    }

    private void appendBasicPlanterData(CompoundTag data, PlanterBlockEntity planter, BlockState state) {
        ItemStack seedStack = planter.getItem(PlanterBlockEntity.SLOT_PLANT);
        ItemStack soilStack = planter.getItem(PlanterBlockEntity.SLOT_SOIL);
        if (seedStack.isEmpty() || soilStack.isEmpty()) {
            data.putBoolean("hasCrop", false);
            return;
        }

        data.putBoolean("hasCrop", true);
        data.putString("cropName", seedStack.getDisplayName().getString());
        data.putInt("currentStage", planter.getGrowthStage());
        data.putInt("maxStage", planter.isTree() ? 1 : 8);
        data.putFloat("progressPercent", planter.getGrowthProgress() * 100.0F);
        data.putString("soilName", soilStack.getDisplayName().getString());
        data.putFloat("growthModifier", planter.getSoilGrowthModifier(soilStack));

        ItemStack fertStack = planter.getItem(PlanterBlockEntity.SLOT_FERTILIZER);
        if (!fertStack.isEmpty()) {
            var fertData = ATDataMaps.getFertilizer(fertStack.getItem());
            data.putBoolean("hasFertilizer", true);
            data.putString("fertilizerName", fertStack.getDisplayName().getString());
            data.putFloat("fertilizerSpeedModifier", fertData != null ? fertData.speedMultiplier() : 1.0F);
            data.putFloat("fertilizerYieldModifier", fertData != null ? fertData.yieldMultiplier() : 1.0F);
        } else {
            data.putBoolean("hasFertilizer", false);
        }
    }
}