package com.misterd.agritech.item;

import com.misterd.agritech.Agritech;
import com.misterd.agritech.block.ATBlocks;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ATCreativeTab {
    public static final CreativeModeTab AGRITECH = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(Agritech.MODID, "agritech_creativetab"),
            FabricCreativeModeTab.builder().icon(() -> new ItemStack(ATBlocks.OAK_PLANTER))
                    .title(Component.translatable("creativetab.agritech"))
                    .displayItems((parameters, output) -> {
                        output.accept(ATBlocks.ACACIA_PLANTER);
                        output.accept(ATBlocks.BAMBOO_PLANTER);
                        output.accept(ATBlocks.BIRCH_PLANTER);
                        output.accept(ATBlocks.CHERRY_PLANTER);
                        output.accept(ATBlocks.CRIMSON_PLANTER);
                        output.accept(ATBlocks.DARK_OAK_PLANTER);
                        output.accept(ATBlocks.JUNGLE_PLANTER);
                        output.accept(ATBlocks.MANGROVE_PLANTER);
                        output.accept(ATBlocks.OAK_PLANTER);
                        output.accept(ATBlocks.PALE_OAK_PLANTER);
                        output.accept(ATBlocks.SPRUCE_PLANTER);
                        output.accept(ATBlocks.WARPED_PLANTER);

                        output.accept(ATBlocks.ACACIA_RAISED_BED);
                        output.accept(ATBlocks.BAMBOO_RAISED_BED);
                        output.accept(ATBlocks.BIRCH_RAISED_BED);
                        output.accept(ATBlocks.CHERRY_RAISED_BED);
                        output.accept(ATBlocks.CRIMSON_RAISED_BED);
                        output.accept(ATBlocks.DARK_OAK_RAISED_BED);
                        output.accept(ATBlocks.JUNGLE_RAISED_BED);
                        output.accept(ATBlocks.MANGROVE_RAISED_BED);
                        output.accept(ATBlocks.OAK_RAISED_BED);
                        output.accept(ATBlocks.PALE_OAK_RAISED_BED);
                        output.accept(ATBlocks.SPRUCE_RAISED_BED);
                        output.accept(ATBlocks.WARPED_RAISED_BED);

                        output.accept(ATBlocks.ACACIA_CRATE);
                        output.accept(ATBlocks.BAMBOO_CRATE);
                        output.accept(ATBlocks.BIRCH_CRATE);
                        output.accept(ATBlocks.CHERRY_CRATE);
                        output.accept(ATBlocks.CRIMSON_CRATE);
                        output.accept(ATBlocks.DARK_OAK_CRATE);
                        output.accept(ATBlocks.JUNGLE_CRATE);
                        output.accept(ATBlocks.MANGROVE_CRATE);
                        output.accept(ATBlocks.OAK_CRATE);
                        output.accept(ATBlocks.PALE_OAK_CRATE);
                        output.accept(ATBlocks.SPRUCE_CRATE);
                        output.accept(ATBlocks.WARPED_CRATE);
                    }).build());

    public static void registerCreativeModeTabs() {}
}