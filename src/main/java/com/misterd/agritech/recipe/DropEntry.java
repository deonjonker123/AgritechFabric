package com.misterd.agritech.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record DropEntry(Item item, int min, int max, float chance) {

    public static final Codec<DropEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(DropEntry::item),
            Codec.INT.fieldOf("min").forGetter(DropEntry::min),
            Codec.INT.fieldOf("max").forGetter(DropEntry::max),
            Codec.FLOAT.fieldOf("chance").forGetter(DropEntry::chance)
    ).apply(instance, DropEntry::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DropEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.ITEM), DropEntry::item,
            ByteBufCodecs.INT, DropEntry::min,
            ByteBufCodecs.INT, DropEntry::max,
            ByteBufCodecs.FLOAT, DropEntry::chance,
            DropEntry::new
    );

    public static List<ItemStack> roll(List<DropEntry> entries, RandomSource random, float yieldMultiplier) {
        List<ItemStack> drops = new ArrayList<>();
        for (DropEntry entry : entries) {
            if (random.nextFloat() >= entry.chance()) continue;
            int count = entry.max() > entry.min()
                    ? entry.min() + random.nextInt(entry.max() - entry.min() + 1)
                    : entry.min();
            if (yieldMultiplier != 1.0F) count = Math.max(1, Math.round(count * yieldMultiplier));
            if (count > 0) drops.add(new ItemStack(entry.item(), count));
        }
        return drops;
    }
}