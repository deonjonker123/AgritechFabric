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

                        output.accept(ATItems.CLOCHE);
                    }).build());

    public static void registerCreativeModeTabs() {

    }
}
