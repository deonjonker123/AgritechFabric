package com.misterd.agritech.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record FertilizerData(float speedMultiplier, float yieldMultiplier) {
    public static final Codec<FertilizerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("speed_multiplier").forGetter(FertilizerData::speedMultiplier),
            Codec.FLOAT.fieldOf("yield_multiplier").forGetter(FertilizerData::yieldMultiplier)
    ).apply(instance, FertilizerData::new));

    public static final StreamCodec<ByteBuf, FertilizerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, FertilizerData::speedMultiplier,
            ByteBufCodecs.FLOAT, FertilizerData::yieldMultiplier,
            FertilizerData::new
    );
}