package com.misterd.agritech.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PlantablesConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("agritech");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, CropInfo> crops = new HashMap<>();
    private static Map<String, TreeInfo> trees = new HashMap<>();
    private static Map<String, SoilInfo> soils = new HashMap<>();
    private static Map<String, FertilizerInfo> fertilizers = new HashMap<>();

    public static void loadConfig() {
        LOGGER.info("PlantablesConfig.loadConfig() invoked.");
        Path configPath = FabricLoader.getInstance().getConfigDir()
                .resolve("agritech/plantables.json");

        if (!Files.exists(configPath)) {
            createDefaultConfig(configPath);
        }

        try {
            String json = Files.readString(configPath);
            PlantablesConfigData configData = GSON.fromJson(json, PlantablesConfigData.class);
            processConfig(configData);
        } catch (JsonSyntaxException | IOException e) {
            LOGGER.error("Failed to load plantables config: {}", e.getMessage());
            LOGGER.info("Loading default plantables configuration instead");
            processConfig(getDefaultConfig());
        }

        PlantablesOverrideConfig.loadOverrides(crops, trees, soils, soils, fertilizers);
    }

    private static void createDefaultConfig(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(getDefaultConfig()));
        } catch (IOException e) {
            LOGGER.error("Failed to create default plantables config: {}", e.getMessage());
        }
    }

    private static PlantablesConfigData getDefaultConfig() {
        LOGGER.info("Generating default plantables config.");
        PlantablesConfigData config = new PlantablesConfigData();

        List<CropEntry> defaultCrops = new ArrayList<>();
        addVanillaCrops(defaultCrops);
        if (FabricLoader.getInstance().isModLoaded("farmersdelight")) {
            LOGGER.info("Adding Farmer's Delight crops to Agritech config");
            addFarmersDelightCrops(defaultCrops);
        }
        if (FabricLoader.getInstance().isModLoaded("croptopia")) {
            LOGGER.info("Adding Croptopia crops to Agritech config");
            addCroptopiaCrops(defaultCrops);
        }
        if (FabricLoader.getInstance().isModLoaded("cobblemon")) {
            LOGGER.info("Adding Cobblemon crops to Agritech config");
            addCobblemonCrops(defaultCrops);
        }
        config.allowedCrops = defaultCrops;

        List<TreeEntry> defaultTrees = new ArrayList<>();
        addVanillaTrees(defaultTrees);
        if (FabricLoader.getInstance().isModLoaded("croptopia")) {
            LOGGER.info("Adding Croptopia trees to Agritech config");
            addCroptopiaTrees(defaultTrees);
        }
        if (FabricLoader.getInstance().isModLoaded("cobblemon")) {
            LOGGER.info("Adding Cobblemon trees to Agritech config");
            addCobblemonTrees(defaultTrees);
        }
        config.allowedTrees = defaultTrees;

        List<SoilEntry> defaultSoils = new ArrayList<>();
        addVanillaSoils(defaultSoils);
        if (FabricLoader.getInstance().isModLoaded("farmersdelight")) {
            LOGGER.info("Adding Farmer's Delight soils to Agritech config");
            addFarmersDelightSoils(defaultSoils);
        }
        config.allowedSoils = defaultSoils;

        List<FertilizerEntry> defaultFertilizers = new ArrayList<>();
        addVanillaFertilizers(defaultFertilizers);
        config.allowedFertilizers = defaultFertilizers;

        return config;
    }

    private static final List<String> STANDARD_FARMLAND_SOILS = List.of(
            "minecraft:farmland",
            "farmersdelight:rich_soil_farmland"
    );

    private static final List<String> STANDARD_GROUND_SOILS = List.of(
            "minecraft:farmland",
            "minecraft:dirt",
            "minecraft:grass_block",
            "minecraft:rooted_dirt",
            "minecraft:coarse_dirt",
            "minecraft:podzol",
            "minecraft:mycelium",
            "minecraft:mud",
            "minecraft:moss_block",
            "minecraft:muddy_mangrove_roots",
            "farmersdelight:rich_soil_farmland"
    );

    private static final List<String> STANDARD_TREE_SOILS = List.of(
            "minecraft:dirt",
            "minecraft:grass_block",
            "minecraft:podzol",
            "minecraft:coarse_dirt",
            "minecraft:mycelium",
            "minecraft:rooted_dirt",
            "minecraft:moss_block",
            "minecraft:pale_moss_block",
            "minecraft:farmland",
            "minecraft:mud",
            "minecraft:muddy_mangrove_roots",
            "farmersdelight:rich_soil_farmland",
            "farmersdelight:organic_compost"
    );

    private static CropEntry makeCrop(String seed, List<String> validSoils, DropEntry... drops) {
        CropEntry crop = new CropEntry();
        crop.seed = seed;
        crop.validSoils = new ArrayList<>(validSoils);
        crop.drops = new ArrayList<>(List.of(drops));
        return crop;
    }

    private static DropEntry makeDrop(String item, int min, int max, float chance) {
        DropEntry drop = new DropEntry();
        drop.item = item;
        drop.count = new CountRange(min, max);
        drop.chance = chance;
        return drop;
    }

    private static DropEntry makeDrop(String item, int min, int max) {
        return makeDrop(item, min, max, 1.0F);
    }

    private static TreeEntry makeTree(String sapling, List<String> validSoils, DropEntry... drops) {
        TreeEntry tree = new TreeEntry();
        tree.sapling = sapling;
        tree.validSoils = new ArrayList<>(validSoils);
        tree.drops = new ArrayList<>(List.of(drops));
        return tree;
    }

    private static void addVanillaCrops(List<CropEntry> crops) {
        crops.add(makeCrop("minecraft:wheat_seeds", STANDARD_FARMLAND_SOILS,
                makeDrop("minecraft:wheat", 1, 1),
                makeDrop("minecraft:wheat_seeds", 1, 2, 0.5F)));

        crops.add(makeCrop("minecraft:beetroot_seeds", STANDARD_FARMLAND_SOILS,
                makeDrop("minecraft:beetroot", 1, 1),
                makeDrop("minecraft:beetroot_seeds", 1, 2, 0.5F)));

        crops.add(makeCrop("minecraft:carrot", STANDARD_FARMLAND_SOILS,
                makeDrop("minecraft:carrot", 2, 5)));

        crops.add(makeCrop("minecraft:potato", STANDARD_FARMLAND_SOILS,
                makeDrop("minecraft:potato", 2, 5),
                makeDrop("minecraft:poisonous_potato", 1, 1, 0.02F)));

        crops.add(makeCrop("minecraft:melon_seeds", STANDARD_FARMLAND_SOILS,
                makeDrop("minecraft:melon_slice", 3, 7)));

        crops.add(makeCrop("minecraft:pumpkin_seeds", STANDARD_FARMLAND_SOILS,
                makeDrop("minecraft:pumpkin", 1, 1)));

        crops.add(makeCrop("minecraft:sugar_cane",
                List.of("minecraft:dirt", "minecraft:grass_block", "minecraft:sand", "minecraft:red_sand"),
                makeDrop("minecraft:sugar_cane", 1, 3)));

        crops.add(makeCrop("minecraft:cactus",
                List.of("minecraft:sand", "minecraft:red_sand"),
                makeDrop("minecraft:cactus", 1, 3),
                makeDrop("minecraft:cactus_flower", 1, 1, 0.25F)));

        crops.add(makeCrop("minecraft:bamboo",
                List.of("minecraft:dirt", "minecraft:grass_block", "minecraft:rooted_dirt",
                        "minecraft:coarse_dirt", "minecraft:podzol", "minecraft:mycelium",
                        "minecraft:mud", "minecraft:moss_block", "minecraft:muddy_mangrove_roots",
                        "farmersdelight:rich_soil", "farmersdelight:organic_compost"),
                makeDrop("minecraft:bamboo", 2, 4)));

        crops.add(makeCrop("minecraft:sweet_berries",
                List.of("minecraft:farmland", "minecraft:dirt", "minecraft:grass_block",
                        "minecraft:rooted_dirt", "minecraft:coarse_dirt", "minecraft:podzol",
                        "minecraft:mycelium", "minecraft:mud", "minecraft:moss_block",
                        "minecraft:muddy_mangrove_roots", "minecraft:pale_moss_block",
                        "farmersdelight:rich_soil_farmland", "farmersdelight:rich_soil",
                        "farmersdelight:organic_compost"),
                makeDrop("minecraft:sweet_berries", 2, 4)));

        crops.add(makeCrop("minecraft:glow_berries",
                List.of("minecraft:moss_block", "minecraft:pale_moss_block"),
                makeDrop("minecraft:glow_berries", 2, 4)));

        crops.add(makeCrop("minecraft:nether_wart",
                List.of("minecraft:soul_sand"),
                makeDrop("minecraft:nether_wart", 1, 3)));

        crops.add(makeCrop("minecraft:chorus_flower",
                List.of("minecraft:end_stone"),
                makeDrop("minecraft:chorus_fruit", 1, 3),
                makeDrop("minecraft:chorus_flower", 1, 1, 0.02F)));

        crops.add(makeCrop("minecraft:kelp",
                List.of("minecraft:water_bucket"),
                makeDrop("minecraft:kelp", 1, 2)));

        crops.add(makeCrop("minecraft:lily_pad",
                List.of("minecraft:water_bucket"),
                makeDrop("minecraft:lily_pad", 1, 1)));

        crops.add(makeCrop("minecraft:spore_blossom",
                List.of("minecraft:moss_block"),
                makeDrop("minecraft:spore_blossom", 1, 1)));

        crops.add(makeCrop("minecraft:moss_block",
                List.of("minecraft:stone"),
                makeDrop("minecraft:moss_block", 1, 2),
                makeDrop("minecraft:moss_carpet", 1, 1, 0.1F),
                makeDrop("minecraft:wheat_seeds", 1, 1, 0.1F)));

        crops.add(makeCrop("minecraft:pale_moss_block",
                List.of("minecraft:stone"),
                makeDrop("minecraft:pale_moss_block", 1, 2),
                makeDrop("minecraft:pale_moss_carpet", 1, 1, 0.1F)));

        crops.add(makeCrop("minecraft:brown_mushroom",
                List.of("minecraft:mycelium", "minecraft:podzol",
                        "farmersdelight:rich_soil", "farmersdelight:organic_compost"),
                makeDrop("minecraft:brown_mushroom", 1, 1)));

        crops.add(makeCrop("minecraft:red_mushroom",
                List.of("minecraft:mycelium", "minecraft:podzol",
                        "farmersdelight:rich_soil", "farmersdelight:organic_compost"),
                makeDrop("minecraft:red_mushroom", 1, 1)));

        crops.add(makeCrop("minecraft:cocoa_beans",
                List.of("minecraft:jungle_log", "minecraft:jungle_wood",
                        "minecraft:stripped_jungle_log", "minecraft:stripped_jungle_wood"),
                makeDrop("minecraft:cocoa_beans", 1, 3)));

        crops.add(makeCrop("minecraft:pitcher_pod", STANDARD_FARMLAND_SOILS,
                makeDrop("minecraft:pitcher_plant", 1, 1)));

        crops.add(makeCrop("minecraft:torchflower_seeds", STANDARD_FARMLAND_SOILS,
                makeDrop("minecraft:torchflower", 1, 1)));

        for (String flower : new String[]{
                "minecraft:allium", "minecraft:azure_bluet", "minecraft:blue_orchid",
                "minecraft:cornflower", "minecraft:dandelion", "minecraft:lily_of_the_valley",
                "minecraft:oxeye_daisy", "minecraft:poppy", "minecraft:red_tulip",
                "minecraft:orange_tulip", "minecraft:white_tulip", "minecraft:pink_tulip",
                "minecraft:wither_rose", "minecraft:lilac", "minecraft:peony",
                "minecraft:rose_bush", "minecraft:sunflower",
                "minecraft:closed_eyeblossom", "minecraft:open_eyeblossom"
        }) {
            crops.add(makeCrop(flower, STANDARD_GROUND_SOILS, makeDrop(flower, 1, 1)));
        }
    }

    private static void addFarmersDelightCrops(List<CropEntry> crops) {
        crops.add(makeCrop("farmersdelight:cabbage_seeds", STANDARD_FARMLAND_SOILS,
                makeDrop("farmersdelight:cabbage", 1, 1),
                makeDrop("farmersdelight:cabbage_seeds", 1, 2)));

        crops.add(makeCrop("farmersdelight:tomato_seeds", STANDARD_FARMLAND_SOILS,
                makeDrop("farmersdelight:tomato", 1, 2)));

        crops.add(makeCrop("farmersdelight:onion", STANDARD_FARMLAND_SOILS,
                makeDrop("farmersdelight:onion", 1, 3)));

        crops.add(makeCrop("farmersdelight:rice", STANDARD_FARMLAND_SOILS,
                makeDrop("farmersdelight:rice_panicle", 1, 1)));
    }

    private static void addCroptopiaCrops(List<CropEntry> crops) {
        for (String crop : new String[]{
                "artichoke", "asparagus", "barley", "basil", "bellpepper", "blackbean",
                "blackberry", "blueberry", "broccoli", "cabbage", "cantaloupe", "cauliflower",
                "celery", "chile_pepper", "coffee", "corn", "cranberry", "cucumber", "currant",
                "eggplant", "elderberry", "garlic", "ginger", "grape", "greenbean", "greenonion",
                "honeydew", "hops", "kale", "kiwi", "leek", "lettuce", "mustard", "oat", "olive",
                "onion", "peanut", "pepper", "pineapple", "radish", "raspberry", "rhubarb", "rice",
                "rutabaga", "saguaro", "soybean", "spinach", "squash", "strawberry", "sweetpotato",
                "tea", "tomatillo", "tomato", "turmeric", "turnip", "yam", "zucchini"
        }) {
            crops.add(makeCrop("croptopia:" + crop + "_seed", STANDARD_FARMLAND_SOILS,
                    makeDrop("croptopia:" + crop, 2, 4),
                    makeDrop("croptopia:" + crop + "_seed", 1, 1, 0.2F)));
        }
        crops.add(makeCrop("croptopia:vanilla_seeds", STANDARD_FARMLAND_SOILS,
                makeDrop("croptopia:vanilla", 2, 4),
                makeDrop("croptopia:vanilla_seeds", 1, 1, 0.2F)));
    }

    private static void addCobblemonCrops(List<CropEntry> crops) {
        crops.add(makeCrop("cobblemon:revival_herb", STANDARD_FARMLAND_SOILS,
                makeDrop("cobblemon:revival_herb", 1, 2),
                makeDrop("cobblemon:pep_up_flower", 1, 1)));

        crops.add(makeCrop("cobblemon:vivichoke_seeds", STANDARD_FARMLAND_SOILS,
                makeDrop("cobblemon:vivichoke", 1, 1),
                makeDrop("cobblemon:vivichoke_seeds", 1, 1)));

        crops.add(makeCrop("cobblemon:big_root", STANDARD_FARMLAND_SOILS,
                makeDrop("cobblemon:big_root", 1, 1),
                makeDrop("cobblemon:energy_root", 1, 1, 0.5F)));

        crops.add(makeCrop("cobblemon:galarica_nuts", STANDARD_FARMLAND_SOILS,
                makeDrop("cobblemon:galarica_nuts", 1, 1)));

        crops.add(makeCrop("cobblemon:hearty_grains", STANDARD_FARMLAND_SOILS,
                makeDrop("cobblemon:hearty_grains", 1, 2)));

        crops.add(makeCrop("cobblemon:medicinal_leek",
                List.of("minecraft:water_bucket"),
                makeDrop("cobblemon:medicinal_leek", 1, 1)));

        for (String berry : new String[]{
                "aguav", "apicot", "aspear", "babiri", "belue", "bluk", "charti", "cheri",
                "chesto", "chilan", "chople", "coba", "colbur", "cornn", "custap", "durin",
                "eggant", "enigma", "figy", "ganlon", "grepa", "haban", "hondew", "hopo",
                "iapapa", "jaboca", "kasib", "kebia", "kee", "kelpsy", "lansat", "leppa",
                "liechi", "lum", "mago", "magost", "maranga", "micle", "nanab", "nomel",
                "occa", "oran", "pamtre", "passho", "payapa", "pecha", "persim", "petaya",
                "pinap", "pomeg", "qualot", "rabuta", "rawst", "razz", "rindo", "roseli",
                "rowap", "salac", "shuca", "sitrus", "spelon", "starf", "tamato", "tanga",
                "touga", "wacan", "watmel", "wepear", "wiki", "yache"
        }) {
            crops.add(makeCrop("cobblemon:" + berry + "_berry", STANDARD_FARMLAND_SOILS,
                    makeDrop("cobblemon:" + berry + "_berry", 1, 3)));
        }

        for (String color : new String[]{"red", "blue", "cyan", "pink", "green", "white"}) {
            crops.add(makeCrop("cobblemon:" + color + "_mint_seeds", STANDARD_FARMLAND_SOILS,
                    makeDrop("cobblemon:" + color + "_mint_leaf", 1, 4)));
        }
    }

    private static void addVanillaTrees(List<TreeEntry> trees) {
        trees.add(makeTree("minecraft:oak_sapling", STANDARD_TREE_SOILS,
                makeDrop("minecraft:oak_log", 2, 6),
                makeDrop("minecraft:oak_sapling", 1, 2, 0.5F),
                makeDrop("minecraft:stick", 1, 2, 0.5F),
                makeDrop("minecraft:apple", 1, 1, 0.4F)));

        trees.add(makeTree("minecraft:birch_sapling", STANDARD_TREE_SOILS,
                makeDrop("minecraft:birch_log", 2, 6),
                makeDrop("minecraft:birch_sapling", 1, 2, 0.5F),
                makeDrop("minecraft:stick", 1, 2, 0.5F)));

        trees.add(makeTree("minecraft:spruce_sapling", STANDARD_TREE_SOILS,
                makeDrop("minecraft:spruce_log", 4, 8),
                makeDrop("minecraft:spruce_sapling", 1, 2, 0.5F),
                makeDrop("minecraft:stick", 1, 2, 0.5F)));

        trees.add(makeTree("minecraft:jungle_sapling", STANDARD_TREE_SOILS,
                makeDrop("minecraft:jungle_log", 2, 6),
                makeDrop("minecraft:jungle_sapling", 1, 2, 0.4F),
                makeDrop("minecraft:stick", 1, 2, 0.5F),
                makeDrop("minecraft:cocoa_beans", 1, 2, 0.2F)));

        trees.add(makeTree("minecraft:acacia_sapling", STANDARD_TREE_SOILS,
                makeDrop("minecraft:acacia_log", 2, 6),
                makeDrop("minecraft:acacia_sapling", 1, 2, 0.5F),
                makeDrop("minecraft:stick", 1, 2, 0.5F)));

        trees.add(makeTree("minecraft:dark_oak_sapling", STANDARD_TREE_SOILS,
                makeDrop("minecraft:dark_oak_log", 4, 8),
                makeDrop("minecraft:dark_oak_sapling", 1, 2, 0.5F),
                makeDrop("minecraft:stick", 1, 2, 0.5F),
                makeDrop("minecraft:apple", 1, 2, 0.3F)));

        trees.add(makeTree("minecraft:pale_oak_sapling", STANDARD_TREE_SOILS,
                makeDrop("minecraft:pale_oak_log", 4, 8),
                makeDrop("minecraft:pale_oak_sapling", 1, 2, 0.5F),
                makeDrop("minecraft:stick", 1, 2, 0.5F),
                makeDrop("minecraft:pale_hanging_moss", 1, 2, 0.3F)));

        trees.add(makeTree("minecraft:mangrove_propagule",
                List.of("minecraft:mud", "minecraft:muddy_mangrove_roots", "minecraft:dirt",
                        "minecraft:coarse_dirt", "minecraft:grass_block", "minecraft:podzol",
                        "minecraft:mycelium"),
                makeDrop("minecraft:mangrove_log", 2, 6),
                makeDrop("minecraft:mangrove_propagule", 1, 2, 0.5F),
                makeDrop("minecraft:stick", 1, 2, 0.5F),
                makeDrop("minecraft:mangrove_roots", 1, 1, 0.3F)));

        trees.add(makeTree("minecraft:cherry_sapling", STANDARD_TREE_SOILS,
                makeDrop("minecraft:cherry_log", 2, 6),
                makeDrop("minecraft:cherry_sapling", 1, 2, 0.5F),
                makeDrop("minecraft:stick", 1, 2, 0.5F)));

        List<String> azaleaSoils = List.of(
                "minecraft:dirt", "minecraft:grass_block", "minecraft:podzol",
                "minecraft:coarse_dirt", "minecraft:rooted_dirt", "minecraft:moss_block",
                "minecraft:mycelium");

        trees.add(makeTree("minecraft:azalea", azaleaSoils,
                makeDrop("minecraft:oak_log", 2, 6),
                makeDrop("minecraft:azalea", 1, 1, 0.5F),
                makeDrop("minecraft:stick", 1, 2, 0.5F),
                makeDrop("minecraft:moss_block", 1, 2, 0.2F)));

        trees.add(makeTree("minecraft:flowering_azalea", azaleaSoils,
                makeDrop("minecraft:oak_log", 2, 6),
                makeDrop("minecraft:flowering_azalea", 1, 1, 0.5F),
                makeDrop("minecraft:stick", 1, 2, 0.5F),
                makeDrop("minecraft:moss_block", 1, 1, 0.2F)));

        List<String> fungalSoils = List.of("minecraft:crimson_nylium", "minecraft:warped_nylium");

        trees.add(makeTree("minecraft:crimson_fungus", fungalSoils,
                makeDrop("minecraft:crimson_stem", 2, 6),
                makeDrop("minecraft:nether_wart_block", 4, 8),
                makeDrop("minecraft:weeping_vines", 1, 2),
                makeDrop("minecraft:shroomlight", 2, 4)));

        trees.add(makeTree("minecraft:warped_fungus", fungalSoils,
                makeDrop("minecraft:warped_stem", 2, 6),
                makeDrop("minecraft:warped_wart_block", 4, 8),
                makeDrop("minecraft:twisting_vines", 1, 2),
                makeDrop("minecraft:shroomlight", 2, 4)));
    }

    private static void addCroptopiaTrees(List<TreeEntry> trees) {
        record T(String sapling, String log, String fruit) {}
        for (T t : new T[]{
                new T("almond",      "dark_oak_log", "almond"),
                new T("apple",       "oak_log",      "apple"),
                new T("apricot",     "oak_log",      "apricot"),
                new T("avocado",     "spruce_log",   "avocado"),
                new T("banana",      "jungle_log",   "banana"),
                new T("cashew",      "dark_oak_log", "cashew"),
                new T("cherry",      "oak_log",      "cherry"),
                new T("coconut",     "jungle_log",   "coconut"),
                new T("date",        "jungle_log",   "date"),
                new T("dragonfruit", "jungle_log",   "dragonfruit"),
                new T("fig",         "jungle_log",   "fig"),
                new T("grapefruit",  "jungle_log",   "grapefruit"),
                new T("kumquat",     "jungle_log",   "kumquat"),
                new T("lemon",       "oak_log",      "lemon"),
                new T("lime",        "oak_log",      "lime"),
                new T("mango",       "jungle_log",   "mango"),
                new T("nectarine",   "oak_log",      "nectarine"),
                new T("nutmeg",      "jungle_log",   "nutmeg"),
                new T("orange",      "oak_log",      "orange"),
                new T("peach",       "oak_log",      "peach"),
                new T("pear",        "oak_log",      "pear"),
                new T("pecan",       "dark_oak_log", "pecan"),
                new T("persimmon",   "oak_log",      "persimmon"),
                new T("plum",        "oak_log",      "plum"),
                new T("starfruit",   "oak_log",      "starfruit"),
                new T("walnut",      "dark_oak_log", "walnut")
        }) {
            String fruit = t.fruit().equals("apple") ? "minecraft:apple" : "croptopia:" + t.fruit();
            trees.add(makeTree("croptopia:" + t.sapling() + "_sapling", STANDARD_TREE_SOILS,
                    makeDrop("minecraft:" + t.log(), 4, 6),
                    makeDrop("croptopia:" + t.sapling() + "_sapling", 1, 1, 0.3F),
                    makeDrop(fruit, 2, 4),
                    makeDrop("minecraft:stick", 1, 2, 0.5F)));
        }
        trees.add(makeTree("croptopia:cinnamon_sapling", STANDARD_TREE_SOILS,
                makeDrop("croptopia:cinnamon_log", 4, 6),
                makeDrop("croptopia:cinnamon_sapling", 1, 1, 0.3F),
                makeDrop("minecraft:stick", 1, 2, 0.5F)));
    }

    private static void addCobblemonTrees(List<TreeEntry> trees) {
        for (String color : new String[]{"red", "yellow", "green", "blue", "pink", "black", "white"}) {
            trees.add(makeTree("cobblemon:" + color + "_apricorn_seed", STANDARD_TREE_SOILS,
                    makeDrop("cobblemon:apricorn_log", 4, 6),
                    makeDrop("cobblemon:" + color + "_apricorn_seed", 1, 1, 0.3F),
                    makeDrop("cobblemon:" + color + "_apricorn", 2, 4),
                    makeDrop("minecraft:stick", 1, 2, 0.5F)));
        }
        trees.add(makeTree("cobblemon:saccharine_sapling", STANDARD_TREE_SOILS,
                makeDrop("cobblemon:saccharine_log", 4, 6),
                makeDrop("cobblemon:saccharine_sapling", 1, 1, 0.3F),
                makeDrop("minecraft:stick", 1, 2, 0.5F)));
    }

    private static void addVanillaSoils(List<SoilEntry> soils) {
        record S(String id, float mod) {}
        for (S s : new S[]{
                new S("minecraft:dirt",                 0.475F),
                new S("minecraft:coarse_dirt",          0.475F),
                new S("minecraft:podzol",               0.475F),
                new S("minecraft:mycelium",             0.475F),
                new S("minecraft:mud",                  0.5F),
                new S("minecraft:muddy_mangrove_roots", 0.5F),
                new S("minecraft:rooted_dirt",          0.475F),
                new S("minecraft:moss_block",           0.475F),
                new S("minecraft:pale_moss_block",      0.475F),
                new S("minecraft:farmland",             0.5F),
                new S("minecraft:sand",                 0.5F),
                new S("minecraft:red_sand",             0.5F),
                new S("minecraft:grass_block",          0.475F),
                new S("minecraft:soul_sand",            0.5F),
                new S("minecraft:end_stone",            0.5F),
                new S("minecraft:jungle_log",           0.5F),
                new S("minecraft:jungle_wood",          0.5F),
                new S("minecraft:stripped_jungle_log",  0.5F),
                new S("minecraft:stripped_jungle_wood", 0.5F),
                new S("minecraft:water_bucket",         0.5F),
                new S("minecraft:crimson_nylium",       0.6F),
                new S("minecraft:warped_nylium",        0.6F),
                new S("minecraft:stone",                0.6F)
        }) {
            SoilEntry e = new SoilEntry();
            e.soil = s.id();
            e.growthModifier = s.mod();
            soils.add(e);
        }
    }

    private static void addFarmersDelightSoils(List<SoilEntry> soils) {
        for (String id : new String[]{
                "farmersdelight:rich_soil",
                "farmersdelight:rich_soil_farmland",
                "farmersdelight:organic_compost"
        }) {
            SoilEntry e = new SoilEntry();
            e.soil = id;
            e.growthModifier = 0.525F;
            soils.add(e);
        }
    }

    private static void addVanillaFertilizers(List<FertilizerEntry> fertilizers) {
        FertilizerEntry boneMeal = new FertilizerEntry();
        boneMeal.item = "minecraft:bone_meal";
        boneMeal.speedMultiplier = (float) Config.fertilizerBoneMealSpeedMultiplier;
        boneMeal.yieldMultiplier = (float) Config.fertilizerBoneMealYieldMultiplier;
        fertilizers.add(boneMeal);
    }

    private static void processConfig(PlantablesConfigData configData) {
        crops.clear();
        trees.clear();
        soils.clear();
        fertilizers.clear();

        if (configData.allowedCrops != null) {
            for (CropEntry entry : configData.allowedCrops) {
                if (entry.seed != null && !entry.seed.isEmpty()) {
                    crops.put(entry.seed, createCropInfo(entry.validSoils, entry.soil, entry.drops));
                }
            }
        }

        if (configData.allowedTrees != null) {
            for (TreeEntry entry : configData.allowedTrees) {
                if (entry.sapling != null && !entry.sapling.isEmpty()) {
                    trees.put(entry.sapling, createTreeInfo(entry.validSoils, entry.soil, entry.drops));
                }
            }
        }

        if (configData.allowedSoils != null) {
            for (SoilEntry entry : configData.allowedSoils) {
                if (entry.soil != null && !entry.soil.isEmpty()) {
                    soils.put(entry.soil, new SoilInfo(entry.growthModifier));
                }
            }
        }

        if (configData.allowedFertilizers != null) {
            for (FertilizerEntry entry : configData.allowedFertilizers) {
                if (entry.item != null && !entry.item.isEmpty()) {
                    fertilizers.put(entry.item, new FertilizerInfo(entry.speedMultiplier, entry.yieldMultiplier));
                }
            }
        }

        LOGGER.info("Loaded {} crops, {} trees, {} soils, and {} fertilizers from config",
                crops.size(), trees.size(), soils.size(), fertilizers.size());
    }

    private static CropInfo createCropInfo(List<String> validSoils, String soil, List<DropEntry> drops) {
        CropInfo info = new CropInfo();
        info.drops = new ArrayList<>();
        if (validSoils != null && !validSoils.isEmpty()) {
            info.validSoils.addAll(validSoils);
        } else if (soil != null && !soil.isEmpty()) {
            info.validSoils.add(soil);
        }
        if (drops != null) {
            for (DropEntry d : drops) {
                info.drops.add(new DropInfo(d.item,
                        d.count != null ? d.count.min : 1,
                        d.count != null ? d.count.max : 1,
                        d.chance));
            }
        }
        return info;
    }

    private static TreeInfo createTreeInfo(List<String> validSoils, String soil, List<DropEntry> drops) {
        TreeInfo info = new TreeInfo();
        info.drops = new ArrayList<>();
        if (validSoils != null && !validSoils.isEmpty()) {
            info.validSoils.addAll(validSoils);
        } else if (soil != null && !soil.isEmpty()) {
            info.validSoils.add(soil);
        }
        if (drops != null) {
            for (DropEntry d : drops) {
                info.drops.add(new DropInfo(d.item,
                        d.count != null ? d.count.min : 1,
                        d.count != null ? d.count.max : 1,
                        d.chance));
            }
        }
        return info;
    }

    public static boolean isValidSeed(String itemId) { return crops.containsKey(itemId); }
    public static boolean isValidSapling(String itemId) { return trees.containsKey(itemId); }
    public static boolean isValidSoil(String blockId) { return soils.containsKey(blockId); }
    public static boolean isValidFertilizer(String itemId) { return fertilizers.containsKey(itemId); }

    public static boolean isSoilValidForSeed(String soilId, String seedId) {
        CropInfo info = crops.get(seedId);
        return info != null && info.validSoils.contains(soilId);
    }

    public static boolean isSoilValidForSapling(String soilId, String saplingId) {
        TreeInfo info = trees.get(saplingId);
        return info != null && info.validSoils.contains(soilId);
    }

    public static List<DropInfo> getCropDrops(String seedId) {
        CropInfo info = crops.get(seedId);
        return info != null ? info.drops : Collections.emptyList();
    }

    public static List<DropInfo> getTreeDrops(String saplingId) {
        TreeInfo info = trees.get(saplingId);
        return info != null ? info.drops : Collections.emptyList();
    }

    public static float getSoilGrowthModifier(String blockId) {
        SoilInfo info = soils.get(blockId);
        return info != null ? info.growthModifier : 1.0F;
    }

    public static FertilizerInfo getFertilizerInfo(String itemId) {
        return fertilizers.get(itemId);
    }

    public static int getBaseSaplingGrowthTime() {
        return Config.planterBaseProcessingTime;
    }

    public static Map<String, List<String>> getAllSeedToSoilMappings() {
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, CropInfo> entry : crops.entrySet()) {
            if (!entry.getValue().validSoils.isEmpty()) {
                result.put(entry.getKey(), new ArrayList<>(entry.getValue().validSoils));
            }
        }
        return result;
    }

    public static Map<String, List<String>> getAllSaplingToSoilMappings() {
        Map<String, List<String>> result = new HashMap<>();
        for (Map.Entry<String, TreeInfo> entry : trees.entrySet()) {
            if (!entry.getValue().validSoils.isEmpty()) {
                result.put(entry.getKey(), new ArrayList<>(entry.getValue().validSoils));
            }
        }
        return result;
    }

    // --- Data classes ---

    public static class PlantablesConfigData {
        public List<CropEntry> allowedCrops;
        public List<TreeEntry> allowedTrees;
        public List<SoilEntry> allowedSoils;
        public List<FertilizerEntry> allowedFertilizers;
    }

    public static class CropEntry {
        public String seed;
        public String soil;
        public List<String> validSoils;
        public List<DropEntry> drops;
    }

    public static class TreeEntry {
        public String sapling;
        public String soil;
        public List<String> validSoils;
        public List<DropEntry> drops;
    }

    public static class SoilEntry {
        public String soil;
        public float growthModifier;
    }

    public static class FertilizerEntry {
        public String item;
        public float speedMultiplier = 1.2F;
        public float yieldMultiplier = 1.2F;
    }

    public static class DropEntry {
        public String item;
        public CountRange count;
        public float chance = 1.0F;
    }

    public static class CountRange {
        public int min;
        public int max;

        public CountRange() { this.min = 1; this.max = 1; }
        public CountRange(int min, int max) { this.min = min; this.max = max; }
    }

    public static class CropInfo {
        public List<DropInfo> drops;
        public List<String> validSoils = new ArrayList<>();
    }

    public static class TreeInfo {
        public List<DropInfo> drops;
        public List<String> validSoils = new ArrayList<>();
    }

    public static class SoilInfo {
        public final float growthModifier;
        public SoilInfo(float growthModifier) { this.growthModifier = growthModifier; }
    }

    public static class FertilizerInfo {
        public final float speedMultiplier;
        public final float yieldMultiplier;
        public FertilizerInfo(float speedMultiplier, float yieldMultiplier) {
            this.speedMultiplier = speedMultiplier;
            this.yieldMultiplier = yieldMultiplier;
        }
    }

    public static class DropInfo {
        public final String item;
        public final int minCount;
        public final int maxCount;
        public final float chance;
        public DropInfo(String item, int minCount, int maxCount, float chance) {
            this.item = item;
            this.minCount = minCount;
            this.maxCount = maxCount;
            this.chance = chance;
        }
    }
}