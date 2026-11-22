package com.misterd.agritech;

import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.client.DataMapTooltipHandler;
import com.misterd.agritech.client.ber.PlanterBlockEntityRenderer;
import com.misterd.agritech.client.ber.RaisedBedBlockEntityRenderer;
import com.misterd.agritech.datamap.ATDataMaps;
import com.misterd.agritech.gui.ATMenuTypes;
import com.misterd.agritech.gui.custom.CrateScreen;
import com.misterd.agritech.gui.custom.PlanterScreen;
import com.misterd.agritech.gui.custom.RaisedBedScreen;
import com.misterd.agritech.network.ATSyncPacket;
import com.misterd.agritech.recipe.PlantIndex;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class AgritechClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(ATBlockEntities.PLANTER_BE, PlanterBlockEntityRenderer::new);
        BlockEntityRenderers.register(ATBlockEntities.RAISED_BED_BE, RaisedBedBlockEntityRenderer::new);
        MenuScreens.register(ATMenuTypes.PLANTER_MENU, PlanterScreen::new);
        MenuScreens.register(ATMenuTypes.RAISED_BED_MENU, RaisedBedScreen::new);
        MenuScreens.register(ATMenuTypes.CRATE_MENU, CrateScreen::new);

        ClientPlayNetworking.registerGlobalReceiver(ATSyncPacket.TYPE, (payload, context) -> {
            PlantIndex.setClient(payload.plants());
            ATDataMaps.applySnapshot(payload.dataMaps());
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PlantIndex.setClient(PlantIndex.EMPTY));

        DataMapTooltipHandler.register();
    }
}