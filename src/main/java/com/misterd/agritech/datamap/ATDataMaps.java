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
                        "data_maps/item/fertilizers.json",
                        FertilizerData.CODEC,
                        (direct, tags) -> { fertilizers = direct; fertilizerTags = tags; }
                )
        );
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(
                new DataMapLoader<>(
                        Identifier.fromNamespaceAndPath(Agritech.MODID, "soil_modifiers"),
                        "data_maps/item/soil_modifiers.json",
                        SoilModifierData.CODEC,
                        (direct, tags) -> { soilModifiers = direct; soilModifierTags = tags; }
                )
        );
    }

    private record TagEntry<T>(TagKey<Item> tag, T value) {}

    private static class DataMapLoader<T> implements SimpleSynchronousResourceReloadListener {

        private final Identifier fabricId;
        private final String relativePath;
        private final Codec<T> codec;
        private final BiConsumer<Map<Item, T>, List<TagEntry<T>>> sink;

        DataMapLoader(Identifier fabricId, String relativePath, Codec<T> codec, BiConsumer<Map<Item, T>, List<TagEntry<T>>> sink) {
            this.fabricId = fabricId;
            this.relativePath = relativePath;
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

            String folder = relativePath.substring(0, relativePath.lastIndexOf('/'));
            Map<Identifier, Resource> resources = manager.listResources(
                    folder, id -> id.getPath().equals(relativePath));

            for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                try (Reader reader = new InputStreamReader(entry.getValue().open(), StandardCharsets.UTF_8)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    apply(json, direct, tags);
                } catch (IOException | RuntimeException e) {
                    Agritech.LOGGER.error("Failed to load data map file {}", entry.getKey(), e);
                }
            }

            sink.accept(direct, tags);
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
                        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(key));
                        if (item != null) {
                            direct.put(item, value);
                        } else {
                            Agritech.LOGGER.warn("Unknown item '{}' in data map file", key);
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