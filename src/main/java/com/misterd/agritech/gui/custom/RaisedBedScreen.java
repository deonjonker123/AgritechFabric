package com.misterd.agritech.gui.custom;

import com.misterd.agritech.compat.jei.ATJeiPlugin;
import com.misterd.agritech.compat.jei.RaisedBedRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class RaisedBedScreen extends AbstractContainerScreen<RaisedBedMenu> {
    private static final Identifier GUI_TEXTURE =
            Identifier.fromNamespaceAndPath("agritech", "textures/gui/raised_bed_gui.png");

    private static final int GUI_HEIGHT = 136;

    public RaisedBedScreen(RaisedBedMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, GUI_HEIGHT);
        this.inventoryLabelY = GUI_HEIGHT - 94;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE,
                this.leftPos, this.topPos, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);

        float growthProgress = this.menu.blockEntity.getGrowthProgress();
        if (growthProgress > 0.0F) {
            int barHeight = (int) (18 * growthProgress);
            int barY = this.topPos + 18 + 18 - barHeight;
            graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE,
                    this.leftPos + 122, barY,
                    176.0F, (float) (18 - barHeight),
                    6, barHeight, 256, 256);
        }

        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (mouseX >= this.leftPos + 122 && mouseX <= this.leftPos + 128
                && mouseY >= this.topPos + 18 && mouseY <= this.topPos + 37) {
            float progress = this.menu.blockEntity.getGrowthProgress();
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("tooltip.agritech.growth_progress"),
                    Component.literal(String.format("%.1f%%", progress * 100.0F))
                            .withStyle(ChatFormatting.GREEN),
                    Component.translatable("tooltip.agritech.view_recipes")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            ), mouseX, mouseY);
            return;
        }
        if (isHovering(62, 19, 16, 16, mouseX, mouseY) && menu.slots.get(0).getItem().isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("tooltip.agritech.slot.plant")
            ), mouseX, mouseY);
            return;
        }
        if (isHovering(98, 19, 16, 16, mouseX, mouseY) && menu.slots.get(1).getItem().isEmpty()) {
            graphics.setComponentTooltipForNextFrame(this.font, List.of(
                    Component.translatable("tooltip.agritech.slot.soil")
            ), mouseX, mouseY);
            return;
        }
        super.extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            double mx = event.x();
            double my = event.y();
            if (mx >= this.leftPos + 122 && mx <= this.leftPos + 128
                    && my >= this.topPos + 18 && my <= this.topPos + 37) {
                if (this.minecraft != null && this.minecraft.player != null) {
                    IJeiRuntime runtime = ATJeiPlugin.getJeiRuntime();
                    if (runtime != null) {
                        runtime.getRecipesGui().showTypes(List.of(RaisedBedRecipeCategory.RAISED_BED_RECIPE_TYPE));
                    }
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}