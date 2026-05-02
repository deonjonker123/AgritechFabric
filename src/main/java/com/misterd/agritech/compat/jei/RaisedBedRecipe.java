package com.misterd.agritech.compat.jei;

import com.misterd.agritech.config.PlantablesConfig;
import com.misterd.agritech.util.RegistryHelper;
import com.mojang.logging.LogUtils;
import mezz.jei.api.recipe.category.extensions.IRecipeCategoryExtension;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

public class RaisedBedRecipe implements IRecipeCategoryExtension {
    private final Ingredient plantableIngredient;
    private final Ingredient soilIngredient;
    private final List<ItemStack> outputs;
    private final List<PlantablesConfig.DropInfo> dropInfos;
    private final PlantableType type;

    public RaisedBedRecipe(Ingredient plantableIngredient, Ingredient soilIngredient, List<ItemStack> outputs, List<PlantablesConfig.DropInfo> dropInfos, PlantableType type) {
        this.plantableIngredient = plantableIngredient;
        this.soilIngredient = soilIngredient;
        this.outputs = outputs;
        this.dropInfos = dropInfos;
        this.type = type;
    }

    public Ingredient getPlantableIngredient() { return plantableIngredient; }
    public Ingredient getSeedIngredient() { return plantableIngredient; }
    public Ingredient getSaplingIngredient() { return plantableIngredient; }
    public Ingredient getSoilIngredient() { return soilIngredient; }
    public List<ItemStack> getOutputs() { return outputs; }
    public List<PlantablesConfig.DropInfo> getDropInfos() { return dropInfos; }
    public PlantableType getType() { return type; }

    public static RaisedBedRecipe createCrop(String seedId, String soilId) {
        return create(seedId, soilId, PlantableType.CROP);
    }

    public static RaisedBedRecipe createTree(String saplingId, String soilId) {
        return create(saplingId, soilId, PlantableType.TREE);
    }

    private static RaisedBedRecipe create(String plantableId, String soilId, PlantableType type) {
        String plantableLabel = type == PlantableType.CROP ? "Seed" : "Sapling";
        String plantableLower = plantableLabel.toLowerCase();

        Item plantableItem = RegistryHelper.getItem(plantableId);
        if (plantableItem == null) {
            LogUtils.getLogger().error("Failed to create raised bed recipe: {} item not found for ID: {}", plantableLabel, plantableId);
            throw new IllegalArgumentException(plantableLabel + " item not found for ID: " + plantableId);
        }

        Ingredient plantableIngredient = Ingredient.of(plantableItem);
        Ingredient soilIngredient;

        if (soilId.equals("minecraft:water_bucket")) {
            soilIngredient = Ingredient.of(Items.WATER_BUCKET);
        } else {
            Block soilBlock = RegistryHelper.getBlock(soilId);
            if (soilBlock == null) {
                LogUtils.getLogger().error("Failed to create raised bed recipe: Soil block not found for ID: {}", soilId);
                throw new IllegalArgumentException("Soil block not found for ID: " + soilId);
            }
            soilIngredient = Ingredient.of(soilBlock.asItem());
        }

        List<PlantablesConfig.DropInfo> drops = type == PlantableType.CROP
                ? PlantablesConfig.getCropDrops(plantableId)
                : PlantablesConfig.getTreeDrops(plantableId);

        List<ItemStack> outputs = new ArrayList<>();
        List<PlantablesConfig.DropInfo> validDropInfos = new ArrayList<>();

        for (PlantablesConfig.DropInfo dropInfo : drops) {
            if (dropInfo.chance <= 0.0F) continue;
            Item dropItem = RegistryHelper.getItem(dropInfo.item);
            if (dropItem == null) {
                LogUtils.getLogger().error("Drop item not found for ID: {} in recipe for {} {}",
                        dropInfo.item, plantableLower, plantableId);
                throw new IllegalArgumentException("Drop item not found for ID: " + dropInfo.item
                        + " in recipe for " + plantableLower + " " + plantableId);
            }
            outputs.add(new ItemStack(dropItem, (dropInfo.minCount + dropInfo.maxCount) / 2));
            validDropInfos.add(dropInfo);
        }

        return new RaisedBedRecipe(plantableIngredient, soilIngredient, outputs, validDropInfos, type);
    }

    public enum PlantableType {
        CROP,
        TREE
    }
}