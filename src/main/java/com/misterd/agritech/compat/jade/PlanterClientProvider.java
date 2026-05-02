package com.misterd.agritech.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum PlanterClientProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return PlanterProvider.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (!data.getBooleanOr("hasCrop", false)) return;

        String cropName = data.getStringOr("cropName", "");
        int currentStage = data.getIntOr("currentStage", 0);
        int maxStage = data.getIntOr("maxStage", 0);
        float progressPercent = data.getFloatOr("progressPercent", 0f);
        String soilName = data.getStringOr("soilName", "");
        float growthModifier = data.getFloatOr("growthModifier", 1f);

        if (progressPercent >= 100.0F) {
            tooltip.add(Component.translatable("jade.agritech.crop_ready", cropName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        } else {
            tooltip.add(Component.translatable("jade.agritech.crop_progress", cropName, currentStage, maxStage, Math.round(progressPercent)).withStyle(ChatFormatting.DARK_GREEN));
        }

        tooltip.add(Component.translatable("jade.agritech.soil_info", soilName, String.format("%.2fx", growthModifier)).withStyle(ChatFormatting.GRAY));

        if (data.getBooleanOr("hasFertilizer", false)) {
            float fertSpeed = data.getFloatOr("fertilizerSpeedModifier", 1f);
            float fertYield = data.getFloatOr("fertilizerYieldModifier", 1f);
            tooltip.add(Component.translatable("jade.agritech.fertilizer_info",
                            data.getStringOr("fertilizerName", ""), String.format("%.2fx", fertSpeed), String.format("%.2fx", fertYield)).withStyle(ChatFormatting.GOLD));
        }

        if (data.getBooleanOr("isCloched", false)) {
            float clocheSpeed = data.getFloatOr("clocheSpeedModifier", 1f);
            float clocheYield = data.getFloatOr("clocheYieldModifier", 1f);
        }
    }
}