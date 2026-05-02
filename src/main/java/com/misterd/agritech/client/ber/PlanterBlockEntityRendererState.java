package com.misterd.agritech.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class PlanterBlockEntityRendererState extends BlockEntityRenderState {
    public ItemStack soilStack = ItemStack.EMPTY;
    public ItemStack plantStack = ItemStack.EMPTY;
    public float growthProgress = 0f;
    public int growthStage = 0;
    public boolean soilIsWater = false;
    public long posSeed = 0L;
    public int[] soilTints = new int[0];
    public int[] plantTints = new int[0];
}