package com.misterd.agritech;

import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.client.ber.PlanterBlockEntityRenderer;
import com.misterd.agritech.client.ber.RaisedBedBlockEntityRenderer;
import com.misterd.agritech.gui.ATMenuTypes;
import com.misterd.agritech.gui.custom.PlanterScreen;
import com.misterd.agritech.gui.custom.RaisedBedScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class AgritechClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ATBlockEntities.PLANTER_BE, PlanterBlockEntityRenderer::new);
        BlockEntityRenderers.register(ATBlockEntities.RAISED_BED_BE, RaisedBedBlockEntityRenderer::new);
        MenuScreens.register(ATMenuTypes.PLANTER_MENU, PlanterScreen::new);
        MenuScreens.register(ATMenuTypes.RAISED_BED_MENU, RaisedBedScreen::new);
    }
}
