package com.misterd.agritech.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "agritech")
public class ATConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 20, max = 72000)
    public int planterBaseProcessingTime = 1200;

    @ConfigEntry.Gui.Tooltip
    public double raisedBedSkyDaySpeedMultiplier = 1.25;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 1, max = 1200)
    public int cratePickupIntervalTicks = 20;

    public static ATConfig get() {
        return AutoConfig.getConfigHolder(ATConfig.class).getConfig();
    }

    public static void register() {
        AutoConfig.register(ATConfig.class, GsonConfigSerializer::new);
    }
}