package com.misterd.agritech.blockentity;

import com.misterd.agritech.Agritech;
import com.misterd.agritech.block.ATBlocks;
import com.misterd.agritech.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritech.blockentity.custom.RaisedBedBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ATBlockEntities {

    public static final BlockEntityType<PlanterBlockEntity> PLANTER_BE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(Agritech.MODID, "planter_be"),
                    FabricBlockEntityTypeBuilder.create(PlanterBlockEntity::new,
                            ATBlocks.ACACIA_PLANTER,
                            ATBlocks.BAMBOO_PLANTER,
                            ATBlocks.BIRCH_PLANTER,
                            ATBlocks.CHERRY_PLANTER,
                            ATBlocks.CRIMSON_PLANTER,
                            ATBlocks.DARK_OAK_PLANTER,
                            ATBlocks.JUNGLE_PLANTER,
                            ATBlocks.MANGROVE_PLANTER,
                            ATBlocks.OAK_PLANTER,
                            ATBlocks.PALE_OAK_PLANTER,
                            ATBlocks.SPRUCE_PLANTER,
                            ATBlocks.WARPED_PLANTER)
                            .build());

    public static final BlockEntityType<RaisedBedBlockEntity> RAISED_BED_BE =
            Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, Identifier.fromNamespaceAndPath(Agritech.MODID, "raised_bed_be"),
                    FabricBlockEntityTypeBuilder.create(RaisedBedBlockEntity::new,
                                    ATBlocks.ACACIA_RAISED_BED,
                                    ATBlocks.BAMBOO_RAISED_BED,
                                    ATBlocks.BIRCH_RAISED_BED,
                                    ATBlocks.CHERRY_RAISED_BED,
                                    ATBlocks.CRIMSON_RAISED_BED,
                                    ATBlocks.DARK_OAK_RAISED_BED,
                                    ATBlocks.JUNGLE_RAISED_BED,
                                    ATBlocks.MANGROVE_RAISED_BED,
                                    ATBlocks.OAK_RAISED_BED,
                                    ATBlocks.PALE_OAK_RAISED_BED,
                                    ATBlocks.SPRUCE_RAISED_BED,
                                    ATBlocks.WARPED_RAISED_BED)
                            .build());

    public static void registerBlockEntities() {

    }
}
