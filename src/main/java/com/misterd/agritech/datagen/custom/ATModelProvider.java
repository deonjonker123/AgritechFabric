package com.misterd.agritech.datagen.custom;

import com.misterd.agritech.Agritech;
import com.misterd.agritech.block.ATBlocks;
import com.misterd.agritech.block.custom.PlanterBlock;
import com.misterd.agritech.item.ATItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
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

    private static final Identifier CLOCHE_DOME_MODEL = Identifier.fromNamespaceAndPath(Agritech.MODID, "block/cloche_dome");

    private static void planter(BlockModelGenerators g, Block block) {
        Identifier model = ModelLocationUtils.getModelLocation(block);
        g.blockStateOutput.accept(MultiPartGenerator.multiPart(block)
                .with(BlockModelGenerators.plainVariant(model))
                .with(BlockModelGenerators.condition().term(PlanterBlock.CLOCHED, true),
                        BlockModelGenerators.plainVariant(CLOCHE_DOME_MODEL)));
        g.registerSimpleItemModel(block, model);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators g) {
        planter(g, ATBlocks.ACACIA_PLANTER);
        planter(g, ATBlocks.BAMBOO_PLANTER);
        planter(g, ATBlocks.BIRCH_PLANTER);
        planter(g, ATBlocks.CHERRY_PLANTER);
        planter(g, ATBlocks.CRIMSON_PLANTER);
        planter(g, ATBlocks.DARK_OAK_PLANTER);
        planter(g, ATBlocks.JUNGLE_PLANTER);
        planter(g, ATBlocks.MANGROVE_PLANTER);
        planter(g, ATBlocks.OAK_PLANTER);
        planter(g, ATBlocks.PALE_OAK_PLANTER);
        planter(g, ATBlocks.SPRUCE_PLANTER);
        planter(g, ATBlocks.WARPED_PLANTER);

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
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(ATItems.CLOCHE, ModelTemplates.FLAT_ITEM);
    }
}