package com.misterd.agritech.recipe;

import com.misterd.agritech.Agritech;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ATRecipe {
    private static <T extends RecipeSerializer<?>> T register(String name, T serializer) {
        return Registry.register(
                BuiltInRegistries.RECIPE_SERIALIZER,
                Identifier.fromNamespaceAndPath(Agritech.MODID, name),
                serializer
        );
    }

    public static void register() {
        ATRecipeTypes.register();
    }
}
