package com.misterd.agritech.datagen.custom;

import com.misterd.agritech.blocks.ATBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

public class ATLootTableProvider extends FabricBlockLootSubProvider {
    public ATLootTableProvider(FabricPackOutput packOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(packOutput, registriesFuture);
    }

    @Override
    public void generate() {
        dropSelf(ATBlocks.ACACIA_PLANTER);
        dropSelf(ATBlocks.BAMBOO_PLANTER);
        dropSelf(ATBlocks.BIRCH_PLANTER);
        dropSelf(ATBlocks.CHERRY_PLANTER);
        dropSelf(ATBlocks.CRIMSON_PLANTER);
        dropSelf(ATBlocks.DARK_OAK_PLANTER);
        dropSelf(ATBlocks.JUNGLE_PLANTER);
        dropSelf(ATBlocks.MANGROVE_PLANTER);
        dropSelf(ATBlocks.OAK_PLANTER);
        dropSelf(ATBlocks.PALE_OAK_PLANTER);
        dropSelf(ATBlocks.SPRUCE_PLANTER);
        dropSelf(ATBlocks.WARPED_PLANTER);
    }
}
