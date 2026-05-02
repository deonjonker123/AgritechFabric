package com.misterd.agritech.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ATNetwork {
    public static void registerATNetwork() {
        PayloadTypeRegistry.serverboundPlay().register(
                CrateCollectionTogglePacket.TYPE,
                CrateCollectionTogglePacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(
                CrateCollectionTogglePacket.TYPE,
                (packet, context) -> CrateCollectionTogglePacket.handle(packet, context.player()));
    }
}