package com.misterd.agritech.network;

import com.misterd.agritech.datamap.ATDataMaps;
import com.misterd.agritech.recipe.PlantIndex;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ATNetwork {
    public static void registerATNetwork() {
        PayloadTypeRegistry.serverboundPlay().register(
                CrateCollectionTogglePacket.TYPE,
                CrateCollectionTogglePacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(
                ATSyncPacket.TYPE,
                ATSyncPacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                CrateCollectionTogglePacket.TYPE,
                (packet, context) -> CrateCollectionTogglePacket.handle(packet, context.player()));

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            if (!ServerPlayNetworking.canSend(player, ATSyncPacket.TYPE)) return;
            PlantIndex plants = PlantIndex.forServer(player.level().getServer().getRecipeManager());
            ServerPlayNetworking.send(player, new ATSyncPacket(plants, ATDataMaps.snapshot()));
        });
    }
}