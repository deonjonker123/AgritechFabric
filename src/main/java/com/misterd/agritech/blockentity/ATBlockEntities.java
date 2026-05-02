package com.misterd.agritech.blockentity;

import com.misterd.agritech.Agritech;
import com.misterd.agritech.block.ATBlocks;
import com.misterd.agritech.blockentity.custom.PlanterBlockEntity;
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
                            ATBlocks.SPRUCE_PLANTER,
                            ATBlocks.WARPED_PLANTER).build());

    public static void registerBlockEntities() {

    }
}
