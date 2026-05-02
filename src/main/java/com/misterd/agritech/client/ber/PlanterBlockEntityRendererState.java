package com.misterd.agritech.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.level.Level;

public class PlanterBlockEntityRendererState extends BlockEntityRenderState {
    public Level level;

    final ItemStackRenderState itemStackRenderState =  new ItemStackRenderState();
}
