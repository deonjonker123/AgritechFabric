package com.misterd.agritech.datagen.custom;

import com.misterd.agritech.block.ATBlocks;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ATModelProvider extends FabricModelProvider {
    public ATModelProvider(FabricPackOutput output) {
        super(output);
    }

    private static final PropertyDispatch<VariantMutator> ROTATION_HORIZONTAL_FACING = PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
            .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
            .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
            .select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
            .select(Direction.NORTH, BlockModelGenerators.NOP);

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.ACACIA_PLANTER);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.BAMBOO_PLANTER);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.BIRCH_PLANTER);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.CHERRY_PLANTER);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.CRIMSON_PLANTER);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.DARK_OAK_PLANTER);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.JUNGLE_PLANTER);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.MANGROVE_PLANTER);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.OAK_PLANTER);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.PALE_OAK_PLANTER);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.SPRUCE_PLANTER);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.WARPED_PLANTER);

        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.ACACIA_RAISED_BED);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.BAMBOO_RAISED_BED);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.BIRCH_RAISED_BED);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.CHERRY_RAISED_BED);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.CRIMSON_RAISED_BED);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.DARK_OAK_RAISED_BED);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.JUNGLE_RAISED_BED);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.MANGROVE_RAISED_BED);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.OAK_RAISED_BED);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.PALE_OAK_RAISED_BED);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.SPRUCE_RAISED_BED);
        blockModelGenerators.createNonTemplateModelBlock(ATBlocks.WARPED_RAISED_BED);
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {

    }
}
