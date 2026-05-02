package com.misterd.agritech.compat.jei;

import com.misterd.agritech.block.ATBlocks;
import com.misterd.agritech.config.PlantablesConfig;
import com.misterd.agritech.util.RegistryHelper;
import com.mojang.logging.LogUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class ATJeiPlugin implements IModPlugin {
    private static final Identifier PLUGIN_ID = Identifier.fromNamespaceAndPath("agritech", "jei_plugin");
    private static IJeiRuntime jeiRuntime;

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new PlanterRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new RaisedBedRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PlanterRecipeCategory.PLANTER_RECIPE_TYPE, generatePlanterRecipes());
        registration.addRecipes(RaisedBedRecipeCategory.RAISED_BED_RECIPE_TYPE, generateRaisedBedRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(PlanterRecipeCategory.PLANTER_RECIPE_TYPE, ATBlocks.OAK_PLANTER);
        registration.addCraftingStation(RaisedBedRecipeCategory.RAISED_BED_RECIPE_TYPE, ATBlocks.OAK_RAISED_BED);
    }

    private List<PlanterRecipe> generatePlanterRecipes() {
        List<PlanterRecipe> recipes = new ArrayList<>();
        recipes.addAll(generateCropRecipes());
        recipes.addAll(generateTreeRecipes());
        LogUtils.getLogger().info("Generated {} total planter recipes for JEI", recipes.size());
        return recipes;
    }

    private List<RaisedBedRecipe> generateRaisedBedRecipes() {
        List<RaisedBedRecipe> recipes = new ArrayList<>();
        recipes.addAll(generateRaisedBedCropRecipes());
        recipes.addAll(generateRaisedBedTreeRecipes());
        LogUtils.getLogger().info("Generated {} total raised bed recipes for JEI", recipes.size());
        return recipes;
    }

    private List<PlanterRecipe> generateCropRecipes() {
        List<PlanterRecipe> recipes = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : PlantablesConfig.getAllSeedToSoilMappings().entrySet()) {
            String seedId = entry.getKey();
            for (String soilId : entry.getValue()) {
                try {
                    if (!soilId.equals("minecraft:water_bucket") && RegistryHelper.getBlock(soilId) == null) {
                        LogUtils.getLogger().error("Invalid soil block in config: {} for seed {}", soilId, seedId);
                        continue;
                    }
                    PlanterRecipe recipe = PlanterRecipe.createCrop(seedId, soilId);
                    if (recipe != null && !recipe.getOutputs().isEmpty()) recipes.add(recipe);
                } catch (Exception e) {
                    LogUtils.getLogger().error("Error creating recipe for seed {} and soil {}: {}", seedId, soilId, e.getMessage(), e);
                }
            }
        }
        LogUtils.getLogger().info("Generated {} crop planter recipes for JEI", recipes.size());
        return recipes;
    }

    private List<PlanterRecipe> generateTreeRecipes() {
        List<PlanterRecipe> recipes = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : PlantablesConfig.getAllSaplingToSoilMappings().entrySet()) {
            String saplingId = entry.getKey();
            for (String soilId : entry.getValue()) {
                try {
                    if (!soilId.equals("minecraft:water_bucket") && RegistryHelper.getBlock(soilId) == null) {
                        LogUtils.getLogger().error("Invalid soil block in config: {} for sapling {}", soilId, saplingId);
                        continue;
                    }
                    PlanterRecipe recipe = PlanterRecipe.createTree(saplingId, soilId);
                    if (recipe != null && !recipe.getOutputs().isEmpty()) recipes.add(recipe);
                } catch (Exception e) {
                    LogUtils.getLogger().error("Error creating recipe for sapling {} and soil {}: {}", saplingId, soilId, e.getMessage(), e);
                }
            }
        }
        LogUtils.getLogger().info("Generated {} tree planter recipes for JEI", recipes.size());
        return recipes;
    }

    private List<RaisedBedRecipe> generateRaisedBedCropRecipes() {
        List<RaisedBedRecipe> recipes = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : PlantablesConfig.getAllSeedToSoilMappings().entrySet()) {
            String seedId = entry.getKey();
            for (String soilId : entry.getValue()) {
                try {
                    if (!soilId.equals("minecraft:water_bucket") && RegistryHelper.getBlock(soilId) == null) continue;
                    RaisedBedRecipe recipe = RaisedBedRecipe.createCrop(seedId, soilId);
                    if (recipe != null && !recipe.getDropInfos().isEmpty()) recipes.add(recipe);
                } catch (Exception e) {
                    LogUtils.getLogger().error("Error creating raised bed recipe for seed {} and soil {}: {}", seedId, soilId, e.getMessage(), e);
                }
            }
        }
        LogUtils.getLogger().info("Generated {} crop raised bed recipes for JEI", recipes.size());
        return recipes;
    }

    private List<RaisedBedRecipe> generateRaisedBedTreeRecipes() {
        List<RaisedBedRecipe> recipes = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : PlantablesConfig.getAllSaplingToSoilMappings().entrySet()) {
            String saplingId = entry.getKey();
            for (String soilId : entry.getValue()) {
                try {
                    if (!soilId.equals("minecraft:water_bucket") && RegistryHelper.getBlock(soilId) == null) continue;
                    RaisedBedRecipe recipe = RaisedBedRecipe.createTree(saplingId, soilId);
                    if (recipe != null && !recipe.getDropInfos().isEmpty()) recipes.add(recipe);
                } catch (Exception e) {
                    LogUtils.getLogger().error("Error creating raised bed recipe for sapling {} and soil {}: {}", saplingId, soilId, e.getMessage(), e);
                }
            }
        }
        LogUtils.getLogger().info("Generated {} tree raised bed recipes for JEI", recipes.size());
        return recipes;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        ATJeiPlugin.jeiRuntime = runtime;
    }

    public static IJeiRuntime getJeiRuntime() {
        return jeiRuntime;
    }
}