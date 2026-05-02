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

public class Config {
    private static final Logger LOGGER = LoggerFactory.getLogger("agritech");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Fertilizers
    public static double fertilizerBoneMealSpeedMultiplier = 1.2;
    public static double fertilizerBoneMealYieldMultiplier = 1.2;

    // Planter
    public static int planterBaseProcessingTime = 1200;

    // Raised Bed
    public static double raisedBedSkyDaySpeedMultiplier = 1.25;

    // Basket
    public static int basketPickupIntervalTicks = 20;

    public static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("agritech/agritech.json");

        if (!Files.exists(configPath)) {
            save(configPath);
            LOGGER.info("Created default agritech config at {}", configPath);
            return;
        }

        try {
            String json = Files.readString(configPath);
            ConfigData data = GSON.fromJson(json, ConfigData.class);
            if (data == null) throw new JsonSyntaxException("Empty config file");
            applyData(data);
            LOGGER.info("Loaded agritech config from {}", configPath);
        } catch (JsonSyntaxException | IOException e) {
            LOGGER.error("Failed to load agritech config, using defaults: {}", e.getMessage());
        }
    }

    private static void applyData(ConfigData d) {
        if (d.fertilizerBoneMealSpeedMultiplier > 0) fertilizerBoneMealSpeedMultiplier = d.fertilizerBoneMealSpeedMultiplier;
        if (d.fertilizerBoneMealYieldMultiplier > 0) fertilizerBoneMealYieldMultiplier = d.fertilizerBoneMealYieldMultiplier;
        if (d.planterBaseProcessingTime > 0) planterBaseProcessingTime = d.planterBaseProcessingTime;
        if (d.raisedBedSkyDaySpeedMultiplier > 0) raisedBedSkyDaySpeedMultiplier = d.raisedBedSkyDaySpeedMultiplier;
        if (d.basketPickupIntervalTicks > 0) basketPickupIntervalTicks = d.basketPickupIntervalTicks;
    }

    private static void save(Path configPath) {
        try {
            Files.createDirectories(configPath.getParent());
            ConfigData data = new ConfigData();
            data.fertilizerBoneMealSpeedMultiplier = fertilizerBoneMealSpeedMultiplier;
            data.fertilizerBoneMealYieldMultiplier = fertilizerBoneMealYieldMultiplier;
            data.planterBaseProcessingTime = planterBaseProcessingTime;
            data.raisedBedSkyDaySpeedMultiplier = raisedBedSkyDaySpeedMultiplier;
            data.basketPickupIntervalTicks = basketPickupIntervalTicks;
            Files.writeString(configPath, GSON.toJson(data));
        } catch (IOException e) {
            LOGGER.error("Failed to save agritech config: {}", e.getMessage());
        }
    }

    private static class ConfigData {
        double fertilizerBoneMealSpeedMultiplier = 1.2;
        double fertilizerBoneMealYieldMultiplier = 1.2;
        int planterBaseProcessingTime = 1200;
        double raisedBedSkyDaySpeedMultiplier = 1.25;
        int basketPickupIntervalTicks = 20;
    }
}