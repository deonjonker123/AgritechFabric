package com.misterd.agritech.client;

import com.misterd.agritech.datamap.ATDataMaps;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

public class DataMapTooltipHandler {

    public static void register() {
        ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> {
            Item item = stack.getItem();
            var soilData = ATDataMaps.getSoilModifier(item);
            var fertData = ATDataMaps.getFertilizer(item);

            if (soilData != null) {
                tooltip.add(
                        Component.translatable("tooltip.agritech.soil_type")
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
                tooltip.add(
                        Component.translatable("tooltip.agritech.soil_growth_modifier",
                                        String.format("%.2fx", soilData.growthModifier()))
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
            }

            if (fertData != null) {
                tooltip.add(
                        Component.translatable("tooltip.agritech.fertilizer_type")
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
                tooltip.add(
                        Component.translatable("tooltip.agritech.fertilizer_speed",
                                        String.format("%.2fx", fertData.speedMultiplier()))
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
                tooltip.add(
                        Component.translatable("tooltip.agritech.fertilizer_yield",
                                        String.format("%.2fx", fertData.yieldMultiplier()))
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
            }
        });
    }
}