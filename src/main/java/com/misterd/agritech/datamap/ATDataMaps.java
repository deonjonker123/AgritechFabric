package com.misterd.agritech.datamap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.misterd.agritech.Agritech;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ATDataMaps {

    private static Map<Item, FertilizerData> fertilizers = Map.of();
    private static List<TagEntry<FertilizerData>> fertilizerTags = List.of();

    private static Map<Item, SoilModifierData> soilModifiers = Map.of();
    private static List<TagEntry<SoilModifierData>> soilModifierTags = List.of();

    public static FertilizerData getFertilizer(Item item) {
        FertilizerData direct = fertilizers.get(item);
        if (direct != null) return direct;
        for (TagEntry<FertilizerData> entry : fertilizerTags) {
            if (item.builtInRegistryHolder().is(entry.tag())) return entry.value();
        }
        return null;
    }

    public static SoilModifierData getSoilModifier(Item item) {
        SoilModifierData direct = soilModifiers.get(item);
        if (direct != null) return direct;
        for (TagEntry<SoilModifierData> entry : soilModifierTags) {
            if (item.builtInRegistryHolder().is(entry.tag())) return entry.value();
        }
        return null;
    }

    public static void register() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new DataMapLoader<>(
                        Identifier.fromNamespaceAndPath(Agritech.MODID, "fertilizers"),
                        Identifier.fromNamespaceAndPath(Agritech.MODID, "data_maps/item/fertilizers.json"),
                        FertilizerData.CODEC,
                        (direct, tags) -> { fertilizers = direct; fertilizerTags = tags; }
                )
        );
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new DataMapLoader<>(
                        Identifier.fromNamespaceAndPath(Agritech.MODID, "soil_modifiers"),
                        Identifier.fromNamespaceAndPath(Agritech.MODID, "data_maps/item/soil_modifiers.json"),
                        SoilModifierData.CODEC,
                        (direct, tags) -> { soilModifiers = direct; soilModifierTags = tags; }
                )
        );
    }

    public record Snapshot(Map<Item, FertilizerData> fertilizers, Map<Identifier, FertilizerData> fertilizerTags,
                           Map<Item, SoilModifierData> soils, Map<Identifier, SoilModifierData> soilTags) {

        public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> STREAM_CODEC = StreamCodec.composite(
                mapCodec(ByteBufCodecs.registry(Registries.ITEM), FertilizerData.STREAM_CODEC), Snapshot::fertilizers,
                mapCodec(Identifier.STREAM_CODEC, FertilizerData.STREAM_CODEC), Snapshot::fertilizerTags,
                mapCodec(ByteBufCodecs.registry(Registries.ITEM), SoilModifierData.STREAM_CODEC), Snapshot::soils,
                mapCodec(Identifier.STREAM_CODEC, SoilModifierData.STREAM_CODEC), Snapshot::soilTags,
                Snapshot::new
        );

        private static <K, V> StreamCodec<RegistryFriendlyByteBuf, Map<K, V>> mapCodec(
                StreamCodec<? super RegistryFriendlyByteBuf, K> key, StreamCodec<? super RegistryFriendlyByteBuf, V> value) {
            return ByteBufCodecs.map(HashMap::new, key, value);
        }
    }

    public static Snapshot snapshot() {
        return new Snapshot(new HashMap<>(fertilizers), tagsToMap(fertilizerTags),
                new HashMap<>(soilModifiers), tagsToMap(soilModifierTags));
    }

    public static void applySnapshot(Snapshot snapshot) {
        fertilizers = Map.copyOf(snapshot.fertilizers());
        fertilizerTags = mapToTags(snapshot.fertilizerTags());
        soilModifiers = Map.copyOf(snapshot.soils());
        soilModifierTags = mapToTags(snapshot.soilTags());
    }

    private static <T> Map<Identifier, T> tagsToMap(List<TagEntry<T>> tags) {
        Map<Identifier, T> out = new LinkedHashMap<>();
        for (TagEntry<T> entry : tags) out.put(entry.tag().location(), entry.value());
        return out;
    }

    private static <T> List<TagEntry<T>> mapToTags(Map<Identifier, T> map) {
        List<TagEntry<T>> out = new ArrayList<>();
        map.forEach((id, value) -> out.add(new TagEntry<>(TagKey.create(Registries.ITEM, id), value)));
        return List.copyOf(out);
    }

    private record TagEntry<T>(TagKey<Item> tag, T value) {}

    private static class DataMapLoader<T> implements SimpleSynchronousResourceReloadListener {

        private final Identifier fabricId;
        private final Identifier file;
        private final Codec<T> codec;
        private final BiConsumer<Map<Item, T>, List<TagEntry<T>>> sink;

        DataMapLoader(Identifier fabricId, Identifier file, Codec<T> codec, BiConsumer<Map<Item, T>, List<TagEntry<T>>> sink) {
            this.fabricId = fabricId;
            this.file = file;
            this.codec = codec;
            this.sink = sink;
        }

        @Override
        public Identifier getFabricId() {
            return fabricId;
        }

        @Override
        public void onResourceManagerReload(ResourceManager manager) {
            Map<Item, T> direct = new LinkedHashMap<>();
            List<TagEntry<T>> tags = new ArrayList<>();

            for (Resource resource : manager.getResourceStack(file)) {
                try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    apply(json, direct, tags);
                } catch (IOException | RuntimeException e) {
                    Agritech.LOGGER.error("Failed to load data map {} from pack {}", file, resource.sourcePackId(), e);
                }
            }

            sink.accept(Map.copyOf(direct), List.copyOf(tags));
        }

        private void apply(JsonObject json, Map<Item, T> direct, List<TagEntry<T>> tags) {
            if (json.has("replace") && json.get("replace").getAsBoolean()) {
                direct.clear();
                tags.clear();
            }

            if (json.has("values")) {
                for (Map.Entry<String, JsonElement> valueEntry : json.getAsJsonObject("values").entrySet()) {
                    String key = valueEntry.getKey();
                    T value = codec.parse(JsonOps.INSTANCE, valueEntry.getValue()).getOrThrow();

                    if (key.startsWith("#")) {
                        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, Identifier.parse(key.substring(1)));
                        tags.removeIf(t -> t.tag().equals(tagKey));
                        tags.add(new TagEntry<>(tagKey, value));
                    } else {
                        Identifier id = Identifier.parse(key);
                        if (BuiltInRegistries.ITEM.containsKey(id)) {
                            direct.put(BuiltInRegistries.ITEM.getValue(id), value);
                        } else {
                            Agritech.LOGGER.debug("Skipping unknown item '{}' in data map {}", key, file);
                        }
                    }
                }
            }

            if (json.has("remove")) {
                for (JsonElement removeElement : json.getAsJsonArray("remove")) {
                    String key = removeElement.getAsString();
                    if (key.startsWith("#")) {
                        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, Identifier.parse(key.substring(1)));
                        tags.removeIf(t -> t.tag().equals(tagKey));
                    } else {
                        direct.remove(BuiltInRegistries.ITEM.getValue(Identifier.parse(key)));
                    }
                }
            }
        }
    }
}