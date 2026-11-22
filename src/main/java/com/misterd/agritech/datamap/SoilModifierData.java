package com.misterd.agritech.datamap;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SoilModifierData(float growthModifier) {
    public static final Codec<SoilModifierData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("growth_modifier").forGetter(SoilModifierData::growthModifier)
    ).apply(instance, SoilModifierData::new));

    public static final StreamCodec<ByteBuf, SoilModifierData> STREAM_CODEC =
            ByteBufCodecs.FLOAT.map(SoilModifierData::new, SoilModifierData::growthModifier);
}