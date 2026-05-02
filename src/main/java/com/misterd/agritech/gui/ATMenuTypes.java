package com.misterd.agritech.gui;

import com.misterd.agritech.Agritech;
import com.misterd.agritech.gui.custom.PlanterMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public class ATMenuTypes {
    public static final MenuType<PlanterMenu> PLANTER_MENU =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Agritech.MODID, "planter_menu"),
                    new ExtendedMenuType<>(PlanterMenu::new, BlockPos.STREAM_CODEC));

    public static void registerATMenuTypes() {

    }
}
