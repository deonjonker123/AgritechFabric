package com.misterd.agritech.compat.jade;

import com.misterd.agritech.block.custom.PlanterBlock;
import com.misterd.agritech.block.custom.RaisedBedBlock;
import com.misterd.agritech.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritech.blockentity.custom.RaisedBedBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class ATJadePlugin implements IWailaPlugin {
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(PlanterProvider.INSTANCE, PlanterBlockEntity.class);
        registration.registerBlockDataProvider(RaisedBedProvider.INSTANCE, RaisedBedBlockEntity.class);
    }

    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(PlanterClientProvider.INSTANCE, PlanterBlock.class);
        registration.registerBlockComponent(RaisedBedClientProvider.INSTANCE, RaisedBedBlock.class);
    }
}
