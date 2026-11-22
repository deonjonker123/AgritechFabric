package com.misterd.agritech.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ClocheItem extends Item {
    public ClocheItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> consumer, TooltipFlag flag) {
        consumer.accept(Component.translatable("item.agritech.cloche.tooltip.line1"));
        consumer.accept(Component.translatable("item.agritech.cloche.tooltip.line2"));
        consumer.accept(Component.translatable("item.agritech.cloche.tooltip.line3"));
        super.appendHoverText(stack, context, display, consumer, flag);
    }
}