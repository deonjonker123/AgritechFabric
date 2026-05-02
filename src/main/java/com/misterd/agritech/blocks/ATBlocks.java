package com.misterd.agritech.blocks;

import com.misterd.agritech.Agritech;
import com.misterd.agritech.blocks.custom.AcaciaPlanterBlock;
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
    // Planters
    public static final Block ACACIA_PLANTER = registerBlock("acacia_planter",
            properties -> new AcaciaPlanterBlock(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    public static final Block BAMBOO_PLANTER = registerBlock("bamboo_planter",
            properties -> new Block(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    public static final Block BIRCH_PLANTER = registerBlock("birch_planter",
            properties -> new Block(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    public static final Block CHERRY_PLANTER = registerBlock("cherry_planter",
            properties -> new Block(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    public static final Block CRIMSON_PLANTER = registerBlock("crimson_planter",
            properties -> new Block(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    public static final Block DARK_OAK_PLANTER = registerBlock("dark_oak_planter",
            properties -> new Block(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    public static final Block JUNGLE_PLANTER = registerBlock("jungle_planter",
            properties -> new Block(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    public static final Block MANGROVE_PLANTER = registerBlock("mangrove_planter",
            properties -> new Block(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    public static final Block OAK_PLANTER = registerBlock("oak_planter",
            properties -> new Block(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    public static final Block PALE_OAK_PLANTER = registerBlock("pale_oak_planter",
            properties -> new Block(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    public static final Block SPRUCE_PLANTER = registerBlock("spruce_planter",
            properties -> new Block(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    public static final Block WARPED_PLANTER = registerBlock("warped_planter",
            properties -> new Block(properties
                    .noOcclusion()
                    .strength(2F, 6F)
                    .sound(SoundType.WOOD)));

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> function) {
        Block toRegister = function.apply(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Agritech.MODID, name))));
        registerBlockItem(name, toRegister);
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(Agritech.MODID, name), toRegister);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(Agritech.MODID, name),
                new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix()
                        .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Agritech.MODID, name)))));
    }

    public static void registerATBlocks() {
        Agritech.LOGGER.info("Registering Agritech Blocks for " + Agritech.MODID);
    }
}
