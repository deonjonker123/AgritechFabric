package com.misterd.agritech.recipe;

import com.misterd.agritech.Agritech;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ATRecipeTypes {

    public static final RecipeBookCategory CROP_RECIPE_BOOK_CATEGORY =
            registerBookCategory("crop");

    public static final RecipeBookCategory TREE_RECIPE_BOOK_CATEGORY =
            registerBookCategory("tree");

    public static final RecipeType<CropRecipe> CROP_TYPE =
            registerType("crop", new RecipeType<CropRecipe>() {
                @Override
                public String toString() { return Agritech.MODID + ":crop"; }
            });

    public static final RecipeType<TreeRecipe> TREE_TYPE =
            registerType("tree", new RecipeType<TreeRecipe>() {
                @Override
                public String toString() { return Agritech.MODID + ":tree"; }
            });

    public static final RecipeSerializer<CropRecipe> CROP_SERIALIZER =
            registerSerializer("crop", new RecipeSerializer<>(CropRecipe.CODEC, CropRecipe.STREAM_CODEC));

    public static final RecipeSerializer<TreeRecipe> TREE_SERIALIZER =
            registerSerializer("tree", new RecipeSerializer<>(TreeRecipe.CODEC, TreeRecipe.STREAM_CODEC));

    private static RecipeBookCategory registerBookCategory(String name) {
        return Registry.register(
                BuiltInRegistries.RECIPE_BOOK_CATEGORY,
                Identifier.fromNamespaceAndPath(Agritech.MODID, name),
                new RecipeBookCategory()
        );
    }

    private static <T extends RecipeType<?>> T registerType(String name, T type) {
        return Registry.register(
                BuiltInRegistries.RECIPE_TYPE,
                Identifier.fromNamespaceAndPath(Agritech.MODID, name),
                type
        );
    }

    private static <T extends RecipeSerializer<?>> T registerSerializer(String name, T serializer) {
        return Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(Agritech.MODID, name),
                serializer
        );
    }

    public static void register() {
    }
}