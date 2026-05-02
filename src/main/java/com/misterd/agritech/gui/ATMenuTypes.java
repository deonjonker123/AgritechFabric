package com.misterd.agritech.gui;

import com.misterd.agritech.Agritech;
import com.misterd.agritech.gui.custom.CrateMenu;
import com.misterd.agritech.gui.custom.PlanterMenu;
import com.misterd.agritech.gui.custom.RaisedBedMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.MenuType;

public class ATMenuTypes {
    private static final StreamCodec<RegistryFriendlyByteBuf, BlockPos> BLOCK_POS_CODEC =
            BlockPos.STREAM_CODEC.cast();

    public static final MenuType<PlanterMenu> PLANTER_MENU =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Agritech.MODID, "planter_menu"),
                    new ExtendedMenuType<>((id, inv, pos) -> new PlanterMenu(id, inv, pos), BLOCK_POS_CODEC));

    public static final MenuType<RaisedBedMenu> RAISED_BED_MENU =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Agritech.MODID, "raised_bed_menu"),
                    new ExtendedMenuType<>((id, inv, pos) -> new RaisedBedMenu(id, inv, pos), BLOCK_POS_CODEC));

    public static final MenuType<CrateMenu> CRATE_MENU =
            Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath(Agritech.MODID, "crate_menu"),
                    new ExtendedMenuType<>((id, inv, pos) -> new CrateMenu(id, inv, pos), BLOCK_POS_CODEC));

    public static void registerATMenuTypes() {}
}