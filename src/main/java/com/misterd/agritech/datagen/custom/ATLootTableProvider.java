package com.misterd.agritech.datagen.custom;

import com.misterd.agritech.block.ATBlocks;
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

        dropSelf(ATBlocks.ACACIA_RAISED_BED);
        dropSelf(ATBlocks.BAMBOO_RAISED_BED);
        dropSelf(ATBlocks.BIRCH_RAISED_BED);
        dropSelf(ATBlocks.CHERRY_RAISED_BED);
        dropSelf(ATBlocks.CRIMSON_RAISED_BED);
        dropSelf(ATBlocks.DARK_OAK_RAISED_BED);
        dropSelf(ATBlocks.JUNGLE_RAISED_BED);
        dropSelf(ATBlocks.MANGROVE_RAISED_BED);
        dropSelf(ATBlocks.OAK_RAISED_BED);
        dropSelf(ATBlocks.PALE_OAK_RAISED_BED);
        dropSelf(ATBlocks.SPRUCE_RAISED_BED);
        dropSelf(ATBlocks.WARPED_RAISED_BED);

        dropSelf(ATBlocks.ACACIA_CRATE);
        dropSelf(ATBlocks.BAMBOO_CRATE);
        dropSelf(ATBlocks.BIRCH_CRATE);
        dropSelf(ATBlocks.CHERRY_CRATE);
        dropSelf(ATBlocks.CRIMSON_CRATE);
        dropSelf(ATBlocks.DARK_OAK_CRATE);
        dropSelf(ATBlocks.JUNGLE_CRATE);
        dropSelf(ATBlocks.MANGROVE_CRATE);
        dropSelf(ATBlocks.OAK_CRATE);
        dropSelf(ATBlocks.PALE_OAK_CRATE);
        dropSelf(ATBlocks.SPRUCE_CRATE);
        dropSelf(ATBlocks.WARPED_CRATE);
    }
}