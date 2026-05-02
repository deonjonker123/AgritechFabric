package com.misterd.agritech.block;

import com.misterd.agritech.Agritech;
import com.misterd.agritech.block.custom.PlanterBlock;
import com.misterd.agritech.block.custom.RaisedBedBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ATBlocks {
    public static final Block ACACIA_PLANTER = registerBlock("acacia_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block BAMBOO_PLANTER = registerBlock("bamboo_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block BIRCH_PLANTER = registerBlock("birch_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block CHERRY_PLANTER = registerBlock("cherry_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block CRIMSON_PLANTER = registerBlock("crimson_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block DARK_OAK_PLANTER = registerBlock("dark_oak_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block JUNGLE_PLANTER = registerBlock("jungle_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block MANGROVE_PLANTER = registerBlock("mangrove_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block OAK_PLANTER = registerBlock("oak_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block PALE_OAK_PLANTER = registerBlock("pale_oak_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block SPRUCE_PLANTER = registerBlock("spruce_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block WARPED_PLANTER = registerBlock("warped_planter",
            p -> new PlanterBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));

    public static final Block ACACIA_RAISED_BED = registerBlock("acacia_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block BAMBOO_RAISED_BED = registerBlock("bamboo_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block BIRCH_RAISED_BED = registerBlock("birch_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block CHERRY_RAISED_BED = registerBlock("cherry_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block CRIMSON_RAISED_BED = registerBlock("crimson_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block DARK_OAK_RAISED_BED = registerBlock("dark_oak_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block JUNGLE_RAISED_BED = registerBlock("jungle_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block MANGROVE_RAISED_BED = registerBlock("mangrove_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block OAK_RAISED_BED = registerBlock("oak_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block PALE_OAK_RAISED_BED = registerBlock("pale_oak_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block SPRUCE_RAISED_BED= registerBlock("spruce_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));
    public static final Block WARPED_RAISED_BED = registerBlock("warped_raised_bed",
            p -> new RaisedBedBlock(p.noOcclusion().strength(2F, 6F).sound(SoundType.WOOD)));

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> function) {
        Block block = function.apply(BlockBehaviour.Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK,
                        Identifier.fromNamespaceAndPath(Agritech.MODID, name))));
        registerBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK,
                Identifier.fromNamespaceAndPath(Agritech.MODID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(BuiltInRegistries.ITEM,
                Identifier.fromNamespaceAndPath(Agritech.MODID, name),
                new BlockItem(block, new Item.Properties()
                        .useBlockDescriptionPrefix()
                        .setId(ResourceKey.create(Registries.ITEM,
                                Identifier.fromNamespaceAndPath(Agritech.MODID, name)))));
    }

    public static void registerATBlocks() {}
}