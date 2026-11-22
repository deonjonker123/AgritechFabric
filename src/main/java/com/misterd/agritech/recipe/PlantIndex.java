package com.misterd.agritech.recipe;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PlantIndex {
    public record Plant(boolean tree, Set<Item> soils, List<DropEntry> drops) {
        public boolean acceptsSoil(ItemStack soil) {
            return !soil.isEmpty() && soils.contains(soil.getItem());
        }
    }

    public static final PlantIndex EMPTY = new PlantIndex(Map.of());

    private static volatile @Nullable RecipeManager serverSource = null;
    private static volatile PlantIndex server = EMPTY;
    private static volatile PlantIndex client = EMPTY;

    private final Map<Item, Plant> plants;
    private final Set<Item> allSoils;

    private PlantIndex(Map<Item, Plant> plants) {
        this.plants = Map.copyOf(plants);
        Set<Item> soils = new HashSet<>();
        for (Plant plant : plants.values()) soils.addAll(plant.soils());
        this.allSoils = Set.copyOf(soils);
    }

    public static PlantIndex get(@Nullable Level level) {
        if (level instanceof ServerLevel serverLevel) return forServer(serverLevel.recipeAccess());
        return client;
    }

    public static PlantIndex forServer(RecipeManager rm) {
        if (rm == serverSource) return server;
        synchronized (PlantIndex.class) {
            if (rm != serverSource) {
                server = build(rm);
                serverSource = rm;
            }
            return server;
        }
    }

    public static void setClient(PlantIndex index) {
        client = index;
    }

    private static PlantIndex build(RecipeManager rm) {
        Map<Item, Plant> plants = new HashMap<>();
        for (RecipeHolder<?> holder : rm.getRecipes()) {
            if (holder.value() instanceof CropRecipe crop) {
                add(plants, crop.getSeed(), false, crop.getSoils(), crop.getDrops());
            }
        }
        for (RecipeHolder<?> holder : rm.getRecipes()) {
            if (holder.value() instanceof TreeRecipe tree) {
                add(plants, tree.getSapling(), true, tree.getSoils(), tree.getDrops());
            }
        }
        return new PlantIndex(plants);
    }

    private static void add(Map<Item, Plant> plants, Ingredient plant, boolean tree,
                            List<Ingredient> soilIngredients, List<DropEntry> drops) {
        Set<Item> soils = new HashSet<>();
        for (Ingredient ing : soilIngredients) ing.items().map(Holder::value).forEach(soils::add);
        Plant entry = new Plant(tree, Set.copyOf(soils), List.copyOf(drops));
        plant.items().map(Holder::value).forEach(item -> plants.putIfAbsent(item, entry));
    }

    public @Nullable Plant find(ItemStack stack) {
        return stack.isEmpty() ? null : plants.get(stack.getItem());
    }

    public boolean isPlant(ItemStack stack) {
        return find(stack) != null;
    }

    public boolean isSoil(ItemStack stack) {
        return !stack.isEmpty() && allSoils.contains(stack.getItem());
    }

    public boolean isValidCombination(ItemStack plant, ItemStack soil) {
        Plant p = find(plant);
        return p != null && p.acceptsSoil(soil);
    }

    private static final StreamCodec<RegistryFriendlyByteBuf, Plant> PLANT_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, Plant::tree,
            ByteBufCodecs.registry(Registries.ITEM).apply(ByteBufCodecs.collection(HashSet<Item>::new)), (Plant p) -> new HashSet<>(p.soils()),
            (tree, soils) -> new Plant(tree, Set.copyOf(soils), List.of())
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PlantIndex> STREAM_CODEC =
            ByteBufCodecs.map(HashMap<Item, Plant>::new, ByteBufCodecs.registry(Registries.ITEM), PLANT_STREAM_CODEC)
                    .map(PlantIndex::new, index -> new HashMap<>(index.plants));
}