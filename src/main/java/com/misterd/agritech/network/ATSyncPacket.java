package com.misterd.agritech.network;

import com.misterd.agritech.Agritech;
import com.misterd.agritech.datamap.ATDataMaps;
import com.misterd.agritech.recipe.PlantIndex;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ATSyncPacket(PlantIndex plants, ATDataMaps.Snapshot dataMaps) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ATSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Agritech.MODID, "sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ATSyncPacket> STREAM_CODEC = StreamCodec.composite(
            PlantIndex.STREAM_CODEC, ATSyncPacket::plants,
            ATDataMaps.Snapshot.STREAM_CODEC, ATSyncPacket::dataMaps,
            ATSyncPacket::new
    );

    @Override
    public CustomPacketPayload.Type<ATSyncPacket> type() {
        return TYPE;
    }
}