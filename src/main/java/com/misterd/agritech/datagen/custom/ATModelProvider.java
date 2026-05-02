package com.misterd.agritech.datagen.custom;

import com.misterd.agritech.Agritech;
import com.misterd.agritech.block.ATBlocks;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class ATModelProvider extends FabricModelProvider {
    public ATModelProvider(FabricPackOutput output) {
        super(output);
    }

    private static final PropertyDispatch<VariantMutator> ROTATION_HORIZONTAL_FACING =
            PropertyDispatch.modify(BlockStateProperties.HORIZONTAL_FACING)
                    .select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
                    .select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
                    .select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
                    .select(Direction.NORTH, BlockModelGenerators.NOP);

    @Override
    public void generateBlockStateModels(BlockModelGenerators g) {
        g.createNonTemplateModelBlock(ATBlocks.ACACIA_PLANTER);
        g.createNonTemplateModelBlock(ATBlocks.BAMBOO_PLANTER);
        g.createNonTemplateModelBlock(ATBlocks.BIRCH_PLANTER);
        g.createNonTemplateModelBlock(ATBlocks.CHERRY_PLANTER);
        g.createNonTemplateModelBlock(ATBlocks.CRIMSON_PLANTER);
        g.createNonTemplateModelBlock(ATBlocks.DARK_OAK_PLANTER);
        g.createNonTemplateModelBlock(ATBlocks.JUNGLE_PLANTER);
        g.createNonTemplateModelBlock(ATBlocks.MANGROVE_PLANTER);
        g.createNonTemplateModelBlock(ATBlocks.OAK_PLANTER);
        g.createNonTemplateModelBlock(ATBlocks.PALE_OAK_PLANTER);
        g.createNonTemplateModelBlock(ATBlocks.SPRUCE_PLANTER);
        g.createNonTemplateModelBlock(ATBlocks.WARPED_PLANTER);

        g.createNonTemplateModelBlock(ATBlocks.ACACIA_RAISED_BED);
        g.createNonTemplateModelBlock(ATBlocks.BAMBOO_RAISED_BED);
        g.createNonTemplateModelBlock(ATBlocks.BIRCH_RAISED_BED);
        g.createNonTemplateModelBlock(ATBlocks.CHERRY_RAISED_BED);
        g.createNonTemplateModelBlock(ATBlocks.CRIMSON_RAISED_BED);
        g.createNonTemplateModelBlock(ATBlocks.DARK_OAK_RAISED_BED);
        g.createNonTemplateModelBlock(ATBlocks.JUNGLE_RAISED_BED);
        g.createNonTemplateModelBlock(ATBlocks.MANGROVE_RAISED_BED);
        g.createNonTemplateModelBlock(ATBlocks.OAK_RAISED_BED);
        g.createNonTemplateModelBlock(ATBlocks.PALE_OAK_RAISED_BED);
        g.createNonTemplateModelBlock(ATBlocks.SPRUCE_RAISED_BED);
        g.createNonTemplateModelBlock(ATBlocks.WARPED_RAISED_BED);

        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.ACACIA_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/acacia_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.BAMBOO_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/bamboo_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.BIRCH_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/birch_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.CHERRY_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/cherry_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.CRIMSON_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/crimson_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.DARK_OAK_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/dark_oak_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.JUNGLE_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/jungle_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.MANGROVE_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/mangrove_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.OAK_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/oak_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.PALE_OAK_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/pale_oak_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.SPRUCE_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/spruce_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
        g.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ATBlocks.WARPED_CRATE,
                        BlockModelGenerators.plainVariant(Identifier.fromNamespaceAndPath(Agritech.MODID, "block/warped_crate")))
                .with(ROTATION_HORIZONTAL_FACING));
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {}
}