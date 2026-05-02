package com.misterd.agritech.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum RaisedBedClientProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return RaisedBedProvider.UID;
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
        boolean hasSkyBoost = data.getBooleanOr("hasSkyBoost", false);
        float skyBoostModifier = data.getFloatOr("skyBoostModifier", 1f);

        if (progressPercent >= 100.0F) {
            tooltip.add(Component.translatable("jade.agritech.crop_ready", cropName).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        } else {
            tooltip.add(Component.translatable("jade.agritech.crop_progress", cropName, currentStage, maxStage, Math.round(progressPercent)).withStyle(ChatFormatting.DARK_GREEN));
        }

        tooltip.add(Component.translatable("jade.agritech.soil_info", soilName, String.format("%.2fx", growthModifier)).withStyle(ChatFormatting.GRAY));

        if (hasSkyBoost) {
            tooltip.add(Component.translatable("jade.agritech.sky_boost", String.format("%.2fx", skyBoostModifier)).withStyle(ChatFormatting.GOLD));
        }
    }
}