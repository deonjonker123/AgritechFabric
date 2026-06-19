package com.misterd.agritech.datagen.custom;

import com.misterd.agritech.block.ATBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class ATBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public ATBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    private static ResourceKey<Block> key(Block block) {
        return block.builtInRegistryHolder().key();
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(key(ATBlocks.ACACIA_PLANTER))
                .add(key(ATBlocks.BAMBOO_PLANTER))
                .add(key(ATBlocks.BIRCH_PLANTER))
                .add(key(ATBlocks.CHERRY_PLANTER))
                .add(key(ATBlocks.CRIMSON_PLANTER))
                .add(key(ATBlocks.DARK_OAK_PLANTER))
                .add(key(ATBlocks.JUNGLE_PLANTER))
                .add(key(ATBlocks.MANGROVE_PLANTER))
                .add(key(ATBlocks.OAK_PLANTER))
                .add(key(ATBlocks.PALE_OAK_PLANTER))
                .add(key(ATBlocks.SPRUCE_PLANTER))
                .add(key(ATBlocks.WARPED_PLANTER))

                .add(key(ATBlocks.ACACIA_RAISED_BED))
                .add(key(ATBlocks.BAMBOO_RAISED_BED))
                .add(key(ATBlocks.BIRCH_RAISED_BED))
                .add(key(ATBlocks.CHERRY_RAISED_BED))
                .add(key(ATBlocks.CRIMSON_RAISED_BED))
                .add(key(ATBlocks.DARK_OAK_RAISED_BED))
                .add(key(ATBlocks.JUNGLE_RAISED_BED))
                .add(key(ATBlocks.MANGROVE_RAISED_BED))
                .add(key(ATBlocks.OAK_RAISED_BED))
                .add(key(ATBlocks.PALE_OAK_RAISED_BED))
                .add(key(ATBlocks.SPRUCE_RAISED_BED))
                .add(key(ATBlocks.WARPED_RAISED_BED))

                .add(key(ATBlocks.ACACIA_CRATE))
                .add(key(ATBlocks.BAMBOO_CRATE))
                .add(key(ATBlocks.BIRCH_CRATE))
                .add(key(ATBlocks.CHERRY_CRATE))
                .add(key(ATBlocks.CRIMSON_CRATE))
                .add(key(ATBlocks.DARK_OAK_CRATE))
                .add(key(ATBlocks.JUNGLE_CRATE))
                .add(key(ATBlocks.MANGROVE_CRATE))
                .add(key(ATBlocks.OAK_CRATE))
                .add(key(ATBlocks.PALE_OAK_CRATE))
                .add(key(ATBlocks.SPRUCE_CRATE))
                .add(key(ATBlocks.WARPED_CRATE));
    }
}