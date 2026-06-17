package com.misterd.agritech.compat.jei;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.misterd.agritech.Agritech;
import com.misterd.agritech.block.ATBlocks;
import com.misterd.agritech.recipe.ATRecipeTypes;
import com.misterd.agritech.recipe.CropRecipe;
import com.misterd.agritech.recipe.TreeRecipe;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JeiPlugin
public class ATJeiPlugin implements IModPlugin {

    private static final Identifier PLUGIN_ID = Identifier.fromNamespaceAndPath(Agritech.MODID, "jei_plugin");
    private static IJeiRuntime jeiRuntime;
    private static boolean planterRegistered = false;
    private static boolean raisedBedRegistered = false;

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
        List<PlanterRecipe> planter = buildPlanterRecipes();
        planterRegistered = !planter.isEmpty();
        if (planterRegistered) {
            registration.addRecipes(PlanterRecipeCategory.PLANTER_RECIPE_TYPE, planter);
        }

        List<RaisedBedRecipe> raisedBed = buildRaisedBedRecipes();
        raisedBedRegistered = !raisedBed.isEmpty();
        if (raisedBedRegistered) {
            registration.addRecipes(RaisedBedRecipeCategory.RAISED_BED_RECIPE_TYPE, raisedBed);
        }
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(PlanterRecipeCategory.PLANTER_RECIPE_TYPE, ATBlocks.OAK_PLANTER);
        registration.addCraftingStation(RaisedBedRecipeCategory.RAISED_BED_RECIPE_TYPE, ATBlocks.OAK_RAISED_BED);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        jeiRuntime = runtime;
        if (Minecraft.getInstance().getConnection() != null) {
            if (!planterRegistered) {
                List<PlanterRecipe> planter = buildPlanterRecipes();
                if (!planter.isEmpty()) {
                    runtime.getRecipeManager().addRecipes(PlanterRecipeCategory.PLANTER_RECIPE_TYPE, planter);
                    LogUtils.getLogger().info("[AT JEI] Injected {} planter recipes from onRuntimeAvailable", planter.size());
                }
            }
            if (!raisedBedRegistered) {
                List<RaisedBedRecipe> raisedBed = buildRaisedBedRecipes();
                if (!raisedBed.isEmpty()) {
                    runtime.getRecipeManager().addRecipes(RaisedBedRecipeCategory.RAISED_BED_RECIPE_TYPE, raisedBed);
                    LogUtils.getLogger().info("[AT JEI] Injected {} raised bed recipes from onRuntimeAvailable", raisedBed.size());
                }
            }
        }
    }

    public static IJeiRuntime getJeiRuntime() {
        return jeiRuntime;
    }

    private static DynamicOps<JsonElement> getOps() {
        return RegistryOps.create(JsonOps.INSTANCE, Minecraft.getInstance().getConnection().registryAccess());
    }

    static List<PlanterRecipe> buildPlanterRecipes() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            LogUtils.getLogger().warn("[AT JEI] No connection — deferred");
            return List.of();
        }
        List<PlanterRecipe> recipes = new ArrayList<>();
        DynamicOps<JsonElement> ops = getOps();

        FabricLoader.getInstance().getModContainer(Agritech.MODID)
                .flatMap(c -> c.findPath("data/" + Agritech.MODID + "/recipe"))
                .ifPresent(recipePath -> {
                    try {
                        walkPlanterRecipes(recipePath, ops, recipes);
                    } catch (Exception e) {
                        LogUtils.getLogger().error("[AT JEI] Failed to walk bundled planter recipe directory: {}", e.getMessage());
                    }
                });

        walkDatapackRecipes(mc, ops, recipes, true, false);
        injectServerRecipes(mc, recipes, true, false);

        LogUtils.getLogger().info("[AT JEI] Built {} planter recipes", recipes.size());
        return recipes;
    }

    static List<RaisedBedRecipe> buildRaisedBedRecipes() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) {
            LogUtils.getLogger().warn("[AT JEI] No connection — deferred");
            return List.of();
        }
        List<RaisedBedRecipe> recipes = new ArrayList<>();
        DynamicOps<JsonElement> ops = getOps();

        FabricLoader.getInstance().getModContainer(Agritech.MODID)
                .flatMap(c -> c.findPath("data/" + Agritech.MODID + "/recipe"))
                .ifPresent(recipePath -> {
                    try {
                        walkRaisedBedRecipes(recipePath, ops, recipes);
                    } catch (Exception e) {
                        LogUtils.getLogger().error("[AT JEI] Failed to walk bundled raised bed recipe directory: {}", e.getMessage());
                    }
                });

        walkDatapackRecipes(mc, ops, recipes, false, true);
        injectServerRecipes(mc, recipes, false, true);

        LogUtils.getLogger().info("[AT JEI] Built {} raised bed recipes", recipes.size());
        return recipes;
    }

    private static void walkDatapackRecipes(Minecraft mc, DynamicOps<JsonElement> ops,
                                            List<?> recipes, boolean planter, boolean raisedBed) {
        var server = mc.getSingleplayerServer();
        if (server == null) return;
        try {
            Path datapackDir = server.getWorldPath(LevelResource.DATAPACK_DIR);
            if (!Files.isDirectory(datapackDir)) return;
            try (var dpStream = Files.list(datapackDir)) {
                dpStream.forEach(dp -> {
                    try {
                        if (Files.isDirectory(dp)) {
                            Path recipePath = dp.resolve("data").resolve(Agritech.MODID).resolve("recipe");
                            if (Files.exists(recipePath)) {
                                if (planter) walkPlanterRecipes(recipePath, ops, castPlanterList(recipes));
                                if (raisedBed) walkRaisedBedRecipes(recipePath, ops, castRaisedBedList(recipes));
                            }
                        } else if (dp.toString().endsWith(".zip")) {
                            try (FileSystem fs = FileSystems.newFileSystem(dp, Map.of())) {
                                Path recipePath = fs.getPath("/data/" + Agritech.MODID + "/recipe");
                                if (Files.exists(recipePath)) {
                                    if (planter) walkPlanterRecipes(recipePath, ops, castPlanterList(recipes));
                                    if (raisedBed) walkRaisedBedRecipes(recipePath, ops, castRaisedBedList(recipes));
                                }
                            }
                        }
                    } catch (Exception e) {
                        LogUtils.getLogger().error("[AT JEI] Failed to walk datapack {}: {}", dp.getFileName(), e.getMessage());
                    }
                });
            }
        } catch (Exception e) {
            LogUtils.getLogger().error("[AT JEI] Failed to access datapack dir: {}", e.getMessage());
        }
    }

    private static void injectServerRecipes(Minecraft mc, List<?> recipes, boolean planter, boolean raisedBed) {
        var server = mc.getSingleplayerServer();
        if (server == null) return;
        try {
            server.getRecipeManager().getRecipes().forEach(holder -> {
                try {
                    if (planter) {
                        List<PlanterRecipe> pl = castPlanterList(recipes);
                        if (holder.value().getType() == ATRecipeTypes.CROP_TYPE) {
                            PlanterRecipe pr = PlanterRecipe.fromCrop((CropRecipe) holder.value());
                            if (pl.stream().noneMatch(r -> r.getPlant().equals(pr.getPlant()))) pl.add(pr);
                        } else if (holder.value().getType() == ATRecipeTypes.TREE_TYPE) {
                            PlanterRecipe pr = PlanterRecipe.fromTree((TreeRecipe) holder.value());
                            if (pl.stream().noneMatch(r -> r.getPlant().equals(pr.getPlant()))) pl.add(pr);
                        }
                    }
                    if (raisedBed) {
                        List<RaisedBedRecipe> rb = castRaisedBedList(recipes);
                        if (holder.value().getType() == ATRecipeTypes.CROP_TYPE) {
                            RaisedBedRecipe rr = RaisedBedRecipe.fromCrop((CropRecipe) holder.value());
                            if (rb.stream().noneMatch(r -> r.getPlant().equals(rr.getPlant()))) rb.add(rr);
                        } else if (holder.value().getType() == ATRecipeTypes.TREE_TYPE) {
                            RaisedBedRecipe rr = RaisedBedRecipe.fromTree((TreeRecipe) holder.value());
                            if (rb.stream().noneMatch(r -> r.getPlant().equals(rr.getPlant()))) rb.add(rr);
                        }
                    }
                } catch (Exception e) {
                    LogUtils.getLogger().error("[AT JEI] Failed to process server recipe: {}", e.getMessage());
                }
            });
        } catch (Exception e) {
            LogUtils.getLogger().error("[AT JEI] Failed to read server RecipeManager: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static List<PlanterRecipe> castPlanterList(List<?> list) { return (List<PlanterRecipe>) list; }

    @SuppressWarnings("unchecked")
    private static List<RaisedBedRecipe> castRaisedBedList(List<?> list) { return (List<RaisedBedRecipe>) list; }

    private static void walkPlanterRecipes(Path recipePath, DynamicOps<JsonElement> ops, List<PlanterRecipe> recipes) throws Exception {
        try (var stream = Files.walk(recipePath)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                try (var reader = Files.newBufferedReader(p)) {
                    JsonElement json = JsonParser.parseReader(reader);
                    if (!json.isJsonObject()) return;
                    var obj = json.getAsJsonObject();
                    var typeEl = obj.get("type");
                    if (typeEl == null) return;
                    String type = typeEl.getAsString();
                    if ((Agritech.MODID + ":crop").equals(type)) {
                        CropRecipe.CODEC.codec().parse(ops, obj)
                                .result().ifPresent(crop -> recipes.add(PlanterRecipe.fromCrop(crop)));
                    } else if ((Agritech.MODID + ":tree").equals(type)) {
                        TreeRecipe.CODEC.codec().parse(ops, obj)
                                .result().ifPresent(tree -> recipes.add(PlanterRecipe.fromTree(tree)));
                    }
                } catch (Exception e) {
                    LogUtils.getLogger().error("[AT JEI] Failed to parse {}: {}", p, e.getMessage());
                }
            });
        }
    }

    private static void walkRaisedBedRecipes(Path recipePath, DynamicOps<JsonElement> ops, List<RaisedBedRecipe> recipes) throws Exception {
        try (var stream = Files.walk(recipePath)) {
            stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                try (var reader = Files.newBufferedReader(p)) {
                    JsonElement json = JsonParser.parseReader(reader);
                    if (!json.isJsonObject()) return;
                    var obj = json.getAsJsonObject();
                    var typeEl = obj.get("type");
                    if (typeEl == null) return;
                    String type = typeEl.getAsString();
                    if ((Agritech.MODID + ":crop").equals(type)) {
                        CropRecipe.CODEC.codec().parse(ops, obj)
                                .result().ifPresent(crop -> recipes.add(RaisedBedRecipe.fromCrop(crop)));
                    } else if ((Agritech.MODID + ":tree").equals(type)) {
                        TreeRecipe.CODEC.codec().parse(ops, obj)
                                .result().ifPresent(tree -> recipes.add(RaisedBedRecipe.fromTree(tree)));
                    }
                } catch (Exception e) {
                    LogUtils.getLogger().error("[AT JEI] Failed to parse {}: {}", p, e.getMessage());
                }
            });
        }
    }
}