package com.misterd.agritech.datagen.custom;

import com.misterd.agritech.block.ATBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

public class ATRecipeProvider extends FabricRecipeProvider {
    public ATRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, BootstrapContext<Recipe<?>> output, BootstrapContext<Advancement> advancementOutput) {
        return new RecipeProvider(output, advancementOutput) {
            @Override
            public void buildRecipes() {
                shaped(RecipeCategory.MISC, ATBlocks.ACACIA_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.ACACIA_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_acaia_log", has(Items.ACACIA_LOG)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.BAMBOO_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.BAMBOO_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_bamboo_block", has(Items.BAMBOO_BLOCK)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.BIRCH_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.BIRCH_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_birch_log", has(Items.BIRCH_LOG)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.CHERRY_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.CHERRY_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_cherry_log", has(Items.CHERRY_LOG)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.CRIMSON_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.CRIMSON_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_crimson_stem", has(Items.CRIMSON_STEM)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.DARK_OAK_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.DARK_OAK_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_dark_oak_log", has(Items.DARK_OAK_LOG)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.JUNGLE_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.JUNGLE_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_jungle_log", has(Items.JUNGLE_LOG)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.MANGROVE_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.MANGROVE_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_mangrove_log", has(Items.MANGROVE_LOG)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.OAK_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.OAK_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_oak_log", has(Items.OAK_LOG)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.OAK_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', ItemTags.PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_oak_log", has(Items.OAK_LOG))
                        .save(output, "agritech:zzz_oak_planter_from_any_wood");

                shaped(RecipeCategory.MISC, ATBlocks.PALE_OAK_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.PALE_OAK_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_pale_oak_log", has(Items.PALE_OAK_LOG)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.POPLAR_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.POPLAR_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_poplar_log", has(Items.POPLAR_LOG)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.SPRUCE_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.SPRUCE_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_spruce_log", has(Items.SPRUCE_LOG)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.WARPED_PLANTER)
                        .pattern("PHP").pattern("PPP")
                        .define('P', Items.WARPED_PLANKS)
                        .define('H', Items.HOPPER)
                        .unlockedBy("has_warped_stem", has(Items.WARPED_STEM)).save(output);

                // Raised Beds
                shaped(RecipeCategory.MISC, ATBlocks.ACACIA_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.ACACIA_PLANKS).define('D', Items.ACACIA_SLAB)
                        .unlockedBy("has_acacia_planks", has(Items.ACACIA_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.BAMBOO_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.BAMBOO_PLANKS).define('D', Items.BAMBOO_SLAB)
                        .unlockedBy("has_bamboo_planks", has(Items.BAMBOO_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.BIRCH_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.BIRCH_PLANKS).define('D', Items.BIRCH_SLAB)
                        .unlockedBy("has_birch_planks", has(Items.BIRCH_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.CHERRY_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.CHERRY_PLANKS).define('D', Items.CHERRY_SLAB)
                        .unlockedBy("has_cherry_planks", has(Items.CHERRY_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.CRIMSON_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.CRIMSON_PLANKS).define('D', Items.CRIMSON_SLAB)
                        .unlockedBy("has_crimson_planks", has(Items.CRIMSON_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.DARK_OAK_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.DARK_OAK_PLANKS).define('D', Items.DARK_OAK_SLAB)
                        .unlockedBy("has_dark_oak_planks", has(Items.DARK_OAK_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.JUNGLE_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.JUNGLE_PLANKS).define('D', Items.JUNGLE_SLAB)
                        .unlockedBy("has_jungle_planks", has(Items.JUNGLE_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.MANGROVE_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.MANGROVE_PLANKS).define('D', Items.MANGROVE_SLAB)
                        .unlockedBy("has_mangrove_planks", has(Items.MANGROVE_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.OAK_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.OAK_PLANKS).define('D', Items.OAK_SLAB)
                        .unlockedBy("has_oak_planks", has(Items.OAK_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.OAK_RAISED_BED)
                        .pattern("PDP")
                        .define('P', ItemTags.PLANKS).define('D', ItemTags.WOODEN_SLABS)
                        .unlockedBy("has_oak_log", has(Items.OAK_LOG))
                        .save(output, "agritech:zzz_oak_raised_bed_from_any_wood");

                shaped(RecipeCategory.MISC, ATBlocks.PALE_OAK_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.PALE_OAK_PLANKS).define('D', Items.PALE_OAK_SLAB)
                        .unlockedBy("has_pale_oak_planks", has(Items.PALE_OAK_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.POPLAR_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.POPLAR_PLANKS).define('D', Items.POPLAR_SLAB)
                        .unlockedBy("has_poplar_planks", has(Items.POPLAR_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.SPRUCE_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.SPRUCE_PLANKS).define('D', Items.SPRUCE_SLAB)
                        .unlockedBy("has_spruce_planks", has(Items.SPRUCE_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.WARPED_RAISED_BED)
                        .pattern("PDP")
                        .define('P', Items.WARPED_PLANKS).define('D', Items.WARPED_SLAB)
                        .unlockedBy("has_warped_planks", has(Items.WARPED_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.ACACIA_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.ACACIA_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.ACACIA_SLAB)
                        .unlockedBy("has_acacia_planks", has(Items.ACACIA_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.BAMBOO_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.BAMBOO_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.BAMBOO_SLAB)
                        .unlockedBy("has_bamboo_planks", has(Items.BAMBOO_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.BIRCH_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.BIRCH_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.BIRCH_SLAB)
                        .unlockedBy("has_birch_planks", has(Items.BIRCH_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.CHERRY_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.CHERRY_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.CHERRY_SLAB)
                        .unlockedBy("has_cherry_planks", has(Items.CHERRY_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.CRIMSON_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.CRIMSON_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.CRIMSON_SLAB)
                        .unlockedBy("has_crimson_planks", has(Items.CRIMSON_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.DARK_OAK_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.DARK_OAK_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.DARK_OAK_SLAB)
                        .unlockedBy("has_dark_oak_planks", has(Items.DARK_OAK_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.JUNGLE_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.JUNGLE_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.JUNGLE_SLAB)
                        .unlockedBy("has_jungle_planks", has(Items.JUNGLE_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.MANGROVE_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.MANGROVE_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.MANGROVE_SLAB)
                        .unlockedBy("has_mangrove_planks", has(Items.MANGROVE_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.OAK_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.OAK_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.OAK_SLAB)
                        .unlockedBy("has_oak_planks", has(Items.OAK_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.OAK_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', ItemTags.PLANKS).define('C', Items.CHEST)
                        .define('S', ItemTags.WOODEN_SLABS)
                        .unlockedBy("has_oak_planks", has(Items.OAK_PLANKS))
                        .save(output, "agritech:zzz_oak_crate_from_any_wood");

                shaped(RecipeCategory.MISC, ATBlocks.PALE_OAK_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.PALE_OAK_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.PALE_OAK_SLAB)
                        .unlockedBy("has_pale_oak_planks", has(Items.PALE_OAK_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.POPLAR_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.POPLAR_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.POPLAR_SLAB)
                        .unlockedBy("has_poplar_planks", has(Items.POPLAR_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.SPRUCE_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.SPRUCE_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.SPRUCE_SLAB)
                        .unlockedBy("has_spruce_planks", has(Items.SPRUCE_PLANKS)).save(output);

                shaped(RecipeCategory.MISC, ATBlocks.WARPED_CRATE)
                        .pattern("P P").pattern("PCP").pattern("PSP")
                        .define('P', Items.WARPED_PLANKS).define('C', Items.CHEST)
                        .define('S', Items.WARPED_SLAB)
                        .unlockedBy("has_warped_planks", has(Items.WARPED_PLANKS)).save(output);
            }
        };
    }

    @Override
    public String getName() {
        return "Agritech Recipes";
    }
}