package com.misterd.agritech.gui.custom;

import com.misterd.agritech.network.CrateCollectionTogglePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CrateScreen extends AbstractContainerScreen<CrateMenu> {
    private static final Identifier GUI_TEXTURE =
            Identifier.fromNamespaceAndPath("agritech", "textures/gui/crate_gui.png");

    private static final WidgetSprites COLLECTING_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath("agritech", "enable_collection_btn"),
            Identifier.fromNamespaceAndPath("agritech", "enable_collection_btn"),
            Identifier.fromNamespaceAndPath("agritech", "enable_collection_btn"),
            Identifier.fromNamespaceAndPath("agritech", "enable_collection_btn")
    );
    private static final WidgetSprites NOT_COLLECTING_SPRITES = new WidgetSprites(
            Identifier.fromNamespaceAndPath("agritech", "disable_collection_btn"),
            Identifier.fromNamespaceAndPath("agritech", "disable_collection_btn"),
            Identifier.fromNamespaceAndPath("agritech", "disable_collection_btn"),
            Identifier.fromNamespaceAndPath("agritech", "disable_collection_btn")
    );

    private static final int GUI_HEIGHT = 226;
    private boolean collecting;

    public CrateScreen(CrateMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, GUI_HEIGHT);
        this.inventoryLabelY = GUI_HEIGHT - 94;
        this.collecting = menu.isCollecting();
    }

    @Override
    protected void init() {
        super.init();

        addRenderableWidget(new ImageButton(
                leftPos + 160, topPos + 6, 9, 10,
                collecting ? COLLECTING_SPRITES : NOT_COLLECTING_SPRITES,
                btn -> toggleCollecting()
        )).setTooltip(Tooltip.create(Component.translatable(
                collecting ? "tooltip.agritech.crate.collecting"
                        : "tooltip.agritech.crate.not_collecting")));
    }

    private void toggleCollecting() {
        collecting = !collecting;
        ClientPlayNetworking.send(new CrateCollectionTogglePacket(
                menu.blockEntity.getBlockPos(), collecting));
        init();
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE,
                this.leftPos, this.topPos, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }
}