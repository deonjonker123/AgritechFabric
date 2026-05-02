package com.misterd.agritech.datagen.custom;

import com.misterd.agritech.block.ATBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;

public class ATBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public ATBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE)
                .add(ATBlocks.ACACIA_PLANTER)
                .add(ATBlocks.BAMBOO_PLANTER)
                .add(ATBlocks.BIRCH_PLANTER)
                .add(ATBlocks.CHERRY_PLANTER)
                .add(ATBlocks.CRIMSON_PLANTER)
                .add(ATBlocks.DARK_OAK_PLANTER)
                .add(ATBlocks.JUNGLE_PLANTER)
                .add(ATBlocks.MANGROVE_PLANTER)
                .add(ATBlocks.OAK_PLANTER)
                .add(ATBlocks.PALE_OAK_PLANTER)
                .add(ATBlocks.SPRUCE_PLANTER)
                .add(ATBlocks.WARPED_PLANTER);
    }
}
