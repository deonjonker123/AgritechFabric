package com.misterd.agritech;

import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.client.ber.PlanterBlockEntityRenderer;
import com.misterd.agritech.gui.ATMenuTypes;
import com.misterd.agritech.gui.custom.PlanterScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class AgritechClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ATBlockEntities.PLANTER_BE, PlanterBlockEntityRenderer::new);
        MenuScreens.register(ATMenuTypes.PLANTER_MENU, PlanterScreen::new);
    }
}
