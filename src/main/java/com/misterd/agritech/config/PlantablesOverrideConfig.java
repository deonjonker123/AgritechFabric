package com.misterd.agritech.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlantablesOverrideConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("agritech");

    private static final Pattern TABLE_PATTERN = Pattern.compile("\\[(\\w+)\\.([\\w]+)\\]");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*(.+)");
    private static final Pattern ARRAY_PATTERN = Pattern.compile("\\[\\s*(.*)\\s*\\]");
    private static final Pattern STRING_PATTERN = Pattern.compile("\"([^\"]*)\"");

    private static final Map<String, Integer> cropLineNumbers = new HashMap<>();
    private static final Map<String, Integer> treeLineNumbers = new HashMap<>();
    private static final Map<String, Integer> soilLineNumbers = new HashMap<>();

    public static void loadOverrides(
            Map<String, PlantablesConfig.CropInfo> crops,
            Map<String, PlantablesConfig.TreeInfo> trees,
            Map<String, PlantablesConfig.SoilInfo> cropSoils,
            Map<String, PlantablesConfig.SoilInfo> treeSoils,
            Map<String, PlantablesConfig.FertilizerInfo> fertilizers) {

        Path configDir = FabricLoader.getInstance().getConfigDir()
                .resolve("agritech/plantables_overrides");
        Path overridePath = configDir.resolve("plantables_config_overrides.toml");

        if (!Files.exists(overridePath)) {
            createDefaultOverrideFile(configDir, overridePath);
            return;
        }

        try {
            LOGGER.info("Loading plantables overrides from {}", overridePath);
            cropLineNumbers.clear();
            treeLineNumbers.clear();
            soilLineNumbers.clear();

            Map<String, Map<String, Map<String, Object>>> tables = parseTomlFile(overridePath);
            int cropCount = processCropEntries(tables.getOrDefault("crops", Collections.emptyMap()), crops);
            int treeCount = processTreeEntries(tables.getOrDefault("trees", Collections.emptyMap()), trees);
            int soilCount = processSoilEntries(tables.getOrDefault("soils", Collections.emptyMap()), cropSoils, treeSoils);
            int fertilizerCount = processFertilizerEntries(tables.getOrDefault("fertilizers", Collections.emptyMap()), fertilizers);
            LOGGER.info("Loaded {} crop, {} tree, {} soil, and {} fertilizer overrides",
                    cropCount, treeCount, soilCount, fertilizerCount);
        } catch (Exception e) {
            LOGGER.error("Failed to load plantables_config_overrides.toml: {}", e.getMessage());
        }
    }

    private static Map<String, Map<String, Map<String, Object>>> parseTomlFile(Path filePath) throws IOException {
        Map<String, Map<String, Map<String, Object>>> result = new HashMap<>();
        String currentSection = null;
        Map<String, Map<String, Object>> currentSectionMap = null;
        Map<String, Object> currentTableMap = null;
        StringBuilder multilineValue = null;
        String pendingKey = null;
        int lineNumber = 0;

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                int commentPos = findUnquotedChar(line, '#');
                if (commentPos >= 0) line = line.substring(0, commentPos);
                line = line.trim();
                if (line.isEmpty()) continue;

                if (multilineValue != null) {
                    multilineValue.append(line);
                    if (countOccurrences(multilineValue.toString(), '[') == countOccurrences(multilineValue.toString(), ']')
                            && countOccurrences(multilineValue.toString(), '{') == countOccurrences(multilineValue.toString(), '}')) {
                        currentTableMap.put(pendingKey, parseValue(multilineValue.toString()));
                        multilineValue = null;
                        pendingKey = null;
                    }
                    continue;
                }

                Matcher tableMatcher = TABLE_PATTERN.matcher(line);
                if (tableMatcher.matches()) {
                    currentSection = tableMatcher.group(1);
                    String currentTable = tableMatcher.group(2);

                    switch (currentSection) {
                        case "crops" -> cropLineNumbers.put(currentTable, lineNumber);
                        case "trees" -> treeLineNumbers.put(currentTable, lineNumber);
                        case "soils" -> soilLineNumbers.put(currentTable, lineNumber);
                    }

                    currentSectionMap = result.computeIfAbsent(currentSection, k -> new HashMap<>());
                    currentTableMap = currentSectionMap.computeIfAbsent(currentTable, k -> new HashMap<>());
                    continue;
                }

                if (currentTableMap == null) continue;

                Matcher kvMatcher = KEY_VALUE_PATTERN.matcher(line);
                if (kvMatcher.matches()) {
                    String key = kvMatcher.group(1);
                    String valueStr = kvMatcher.group(2).trim();
                    boolean balanced = countOccurrences(valueStr, '[') == countOccurrences(valueStr, ']')
                            && countOccurrences(valueStr, '{') == countOccurrences(valueStr, '}');
                    if ((!valueStr.startsWith("[") || valueStr.endsWith("]")) && balanced) {
                        currentTableMap.put(key, parseValue(valueStr));
                    } else {
                        multilineValue = new StringBuilder(valueStr);
                        pendingKey = key;
                    }
                }
            }
        }

        return result;
    }

    private static int findUnquotedChar(String str, char target) {
        boolean inQuotes = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            else if (c == target && !inQuotes) return i;
        }
        return -1;
    }

    private static int countOccurrences(String str, char target) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == target) count++;
        }
        return count;
    }

    private static Object parseValue(String valueStr) {
        if (valueStr.startsWith("[") && valueStr.endsWith("]") && valueStr.contains("{")) {
            List<Map<String, Object>> items = new ArrayList<>();
            String content = valueStr.substring(1, valueStr.length() - 1).trim();
            int startIdx = 0;
            while (startIdx < content.length()) {
                int openBrace = content.indexOf('{', startIdx);
                if (openBrace == -1) break;
                int closeBrace = findMatchingCloseBrace(content, openBrace);
                if (closeBrace == -1) break;
                items.add(parseObject(content.substring(openBrace + 1, closeBrace).trim()));
                startIdx = closeBrace + 1;
            }
            return items;
        }

        if (valueStr.startsWith("[") && valueStr.endsWith("]")) {
            Matcher m = ARRAY_PATTERN.matcher(valueStr);
            if (m.matches()) {
                List<String> items = new ArrayList<>();
                Matcher sm = STRING_PATTERN.matcher(m.group(1));
                while (sm.find()) items.add(sm.group(1));
                return items;
            }
        }

        if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
            return valueStr.substring(1, valueStr.length() - 1);
        }

        try {
            return valueStr.contains(".") ? Double.parseDouble(valueStr) : Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            if (valueStr.equalsIgnoreCase("true")) return true;
            if (valueStr.equalsIgnoreCase("false")) return false;
            return valueStr;
        }
    }

    private static int findMatchingCloseBrace(String content, int openBracePos) {
        int depth = 0;
        for (int i = openBracePos; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') depth++;
            else if (c == '}' && --depth == 0) return i;
        }
        return -1;
    }

    private static Map<String, Object> parseObject(String objectStr) {
        Map<String, Object> result = new HashMap<>();
        for (String part : objectStr.split(",")) {
            part = part.trim();
            if (part.isEmpty()) continue;
            String[] kv = part.split("=", 2);
            if (kv.length != 2) continue;
            String key = kv[0].trim();
            String valueStr = kv[1].trim();
            Object value;
            if (valueStr.startsWith("\"") && valueStr.endsWith("\"")) {
                value = valueStr.substring(1, valueStr.length() - 1);
            } else if (valueStr.equals("true")) {
                value = true;
            } else if (valueStr.equals("false")) {
                value = false;
            } else {
                try {
                    value = valueStr.contains(".") ? Double.parseDouble(valueStr) : Integer.parseInt(valueStr);
                } catch (NumberFormatException e) {
                    value = valueStr;
                }
            }
            result.put(key, value);
        }
        return result;
    }

    private static String lineInfo(Map<String, Integer> lineNumbers, String name) {
        int n = lineNumbers.getOrDefault(name, -1);
        return n > 0 ? " (line " + n + ")" : "";
    }

    private static int processCropEntries(Map<String, Map<String, Object>> entries, Map<String, PlantablesConfig.CropInfo> crops) {
        return processPlantEntries(entries, crops, null, cropLineNumbers, "Crop", "seed");
    }

    private static int processTreeEntries(Map<String, Map<String, Object>> entries, Map<String, PlantablesConfig.TreeInfo> trees) {
        return processPlantEntries(entries, null, trees, treeLineNumbers, "Tree", "sapling");
    }

    private static int processPlantEntries(
            Map<String, Map<String, Object>> plantEntries,
            Map<String, PlantablesConfig.CropInfo> crops,
            Map<String, PlantablesConfig.TreeInfo> trees,
            Map<String, Integer> lineNumbers,
            String plantType,
            String seedKey) {

        int count = 0;
        boolean isCrop = crops != null;

        for (Map.Entry<String, Map<String, Object>> entry : plantEntries.entrySet()) {
            String plantName = entry.getKey();
            Map<String, Object> plantConfig = entry.getValue();

            try {
                Object seedObj = plantConfig.get(seedKey);
                if (seedObj == null) {
                    LOGGER.warn("{} override '{}'{} is missing a {} ID, skipping",
                            plantType, plantName, lineInfo(lineNumbers, plantName), seedKey);
                    continue;
                }

                String seedId = seedObj.toString();

                List<String> existingValidSoils = isCrop
                        ? (crops.containsKey(seedId) ? crops.get(seedId).validSoils : new ArrayList<>())
                        : (trees.containsKey(seedId) ? trees.get(seedId).validSoils : new ArrayList<>());

                List<String> validSoils = new ArrayList<>(existingValidSoils);
                Object soilsObj = plantConfig.get("soil");
                if (soilsObj instanceof List<?> soilList) {
                    for (Object soilObj : soilList) {
                        String soilId = soilObj.toString();
                        if (!validSoils.contains(soilId)) validSoils.add(soilId);
                    }
                }

                if (validSoils.isEmpty()) {
                    LOGGER.warn("{} override '{}'{} has no valid soils, skipping",
                            plantType, plantName, lineInfo(lineNumbers, plantName));
                    continue;
                }

                List<PlantablesConfig.DropInfo> drops = null;
                if (plantConfig.containsKey("drops")) {
                    drops = processDrops(plantConfig, plantName, lineNumbers, plantType);
                    if (drops.isEmpty()) {
                        LOGGER.warn("{} override '{}'{} has no valid drops, skipping",
                                plantType, plantName, lineInfo(lineNumbers, plantName));
                        continue;
                    }
                }

                if (isCrop) {
                    PlantablesConfig.CropInfo cropInfo = crops.getOrDefault(seedId, new PlantablesConfig.CropInfo());
                    cropInfo.validSoils = validSoils;
                    if (drops != null) cropInfo.drops = drops;
                    crops.put(seedId, cropInfo);
                } else {
                    PlantablesConfig.TreeInfo treeInfo = trees.getOrDefault(seedId, new PlantablesConfig.TreeInfo());
                    treeInfo.validSoils = validSoils;
                    if (drops != null) treeInfo.drops = drops;
                    trees.put(seedId, treeInfo);
                }

                count++;
                LOGGER.info("Applied {} override for '{}'", plantType.toLowerCase(), plantName);

            } catch (Exception e) {
                LOGGER.error("Error processing {} override '{}'{}: {}",
                        plantType.toLowerCase(), plantName, lineInfo(lineNumbers, plantName), e.getMessage());
            }
        }

        return count;
    }

    private static List<PlantablesConfig.DropInfo> processDrops(
            Map<String, Object> plantConfig,
            String plantName,
            Map<String, Integer> lineNumbers,
            String plantType) {

        Object dropsObj = plantConfig.get("drops");
        if (!(dropsObj instanceof List<?> dropsList)) return Collections.emptyList();

        int defaultMin = plantConfig.get("min_count") instanceof Number n ? n.intValue() : 1;
        int defaultMax = plantConfig.get("max_count") instanceof Number n ? n.intValue() : 1;
        float defaultChance = plantConfig.get("chance") instanceof Number n ? n.floatValue() : 1.0F;

        List<PlantablesConfig.DropInfo> drops = new ArrayList<>();

        for (Object dropObj : dropsList) {
            int minCount = defaultMin;
            int maxCount = defaultMax;
            float chance = defaultChance;
            String dropId;

            if (dropObj instanceof Map<?, ?> rawMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dropMap = (Map<String, Object>) rawMap;
                Object itemObj = dropMap.get("item");
                if (itemObj == null) {
                    LOGGER.warn("{} override '{}'{} has drop without item ID, skipping",
                            plantType, plantName, lineInfo(lineNumbers, plantName));
                    continue;
                }
                dropId = itemObj.toString();
                if (dropMap.get("min_count") instanceof Number n) minCount = n.intValue();
                if (dropMap.get("max_count") instanceof Number n) maxCount = n.intValue();
                if (dropMap.get("chance") instanceof Number n) chance = n.floatValue();
            } else {
                dropId = dropObj.toString();
            }

            drops.add(new PlantablesConfig.DropInfo(dropId, minCount, maxCount, chance));
        }

        return drops;
    }

    private static int processSoilEntries(
            Map<String, Map<String, Object>> soilEntries,
            Map<String, PlantablesConfig.SoilInfo> cropSoils,
            Map<String, PlantablesConfig.SoilInfo> treeSoils) {

        int count = 0;

        for (Map.Entry<String, Map<String, Object>> entry : soilEntries.entrySet()) {
            String soilName = entry.getKey();
            Map<String, Object> soilConfig = entry.getValue();

            try {
                Object blockObj = soilConfig.get("block");
                if (blockObj == null) {
                    LOGGER.warn("Soil override '{}'{} is missing a block ID, skipping",
                            soilName, lineInfo(soilLineNumbers, soilName));
                    continue;
                }

                String soilId = blockObj.toString();
                float growthModifier = soilConfig.get("growth_modifier") instanceof Number n ? n.floatValue() : 1.0F;
                PlantablesConfig.SoilInfo info = new PlantablesConfig.SoilInfo(growthModifier);
                cropSoils.put(soilId, info);
                treeSoils.put(soilId, info);
                count++;
                LOGGER.info("Applied soil override for '{}'", soilName);

            } catch (Exception e) {
                LOGGER.error("Error processing soil override '{}'{}: {}",
                        soilName, lineInfo(soilLineNumbers, soilName), e.getMessage());
            }
        }

        return count;
    }

    private static int processFertilizerEntries(
            Map<String, Map<String, Object>> fertilizerEntries,
            Map<String, PlantablesConfig.FertilizerInfo> fertilizers) {

        int count = 0;

        for (Map.Entry<String, Map<String, Object>> entry : fertilizerEntries.entrySet()) {
            String fertilizerName = entry.getKey();
            Map<String, Object> fertilizerConfig = entry.getValue();

            try {
                Object itemObj = fertilizerConfig.get("item");
                if (itemObj == null) {
                    LOGGER.warn("Fertilizer override '{}' is missing item ID, skipping", fertilizerName);
                    continue;
                }

                String itemId = itemObj.toString();
                float speed = fertilizerConfig.get("speed_multiplier") instanceof Number n ? n.floatValue() : 1.2F;
                float yield = fertilizerConfig.get("yield_multiplier") instanceof Number n ? n.floatValue() : 1.2F;
                fertilizers.put(itemId, new PlantablesConfig.FertilizerInfo(speed, yield));
                count++;
                LOGGER.info("Applied fertilizer override for '{}'", fertilizerName);

            } catch (Exception e) {
                LOGGER.error("Error processing fertilizer override '{}': {}", fertilizerName, e.getMessage());
            }
        }

        return count;
    }

    private static void createDefaultOverrideFile(Path configDir, Path overridePath) {
        try {
            Files.createDirectories(configDir);
            Files.writeString(overridePath, createTemplate());
            LOGGER.info("Created default plantables_config_overrides.toml at {}", overridePath);
        } catch (IOException e) {
            LOGGER.error("Failed to create plantables_config_overrides.toml: {}", e.getMessage());
        }
    }

    private static String createTemplate() {
        return """
                # Plantables Override Configuration
                # Add custom crops, trees, soils, and fertilizers here without modifying the core config.
                # Entries here will override or extend existing configurations for the same items/blocks.
                #
                # NOTES:
                # - If a crop/tree already exists, its soils list will be EXTENDED, not replaced.
                # - If you omit the 'drops' field, existing drops are preserved.
                # - IDs use resource location format: "minecraft:dirt", not "dirt"
                #
                # [crops.my_crop]
                # seed = "examplemod:my_seeds"
                # soil = ["minecraft:farmland", "examplemod:rich_soil"]
                # drops = [
                #   { item = "examplemod:my_crop", min_count = 1, max_count = 3, chance = 1.0 },
                #   { item = "examplemod:my_seeds", min_count = 1, max_count = 2, chance = 0.3 }
                # ]
                #
                # [trees.my_tree]
                # sapling = "examplemod:my_sapling"
                # soil = ["minecraft:dirt"]
                # drops = [
                #   { item = "examplemod:my_log", min_count = 2, max_count = 6, chance = 1.0 },
                #   { item = "examplemod:my_sapling", min_count = 1, max_count = 2, chance = 0.5 }
                # ]
                #
                # [soils.my_soil]
                # block = "examplemod:my_soil"
                # growth_modifier = 1.5
                #
                # [fertilizers.my_fertilizer]
                # item = "examplemod:my_fertilizer"
                # speed_multiplier = 1.2
                # yield_multiplier = 1.2
                """;
    }
}