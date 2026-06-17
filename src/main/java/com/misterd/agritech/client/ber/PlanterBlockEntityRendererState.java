package com.misterd.agritech.client.ber;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public class PlanterBlockEntityRendererState extends BlockEntityRenderState {
    public ItemStack soilStack = ItemStack.EMPTY;
    public ItemStack plantStack = ItemStack.EMPTY;
    public boolean isTree = false;
    public float growthProgress = 0f;
    public int growthStage = 0;
    public boolean soilIsWater = false;
    final ItemStackRenderState soilRenderState = new ItemStackRenderState();
    final BlockModelRenderState plantModel = new BlockModelRenderState();
}