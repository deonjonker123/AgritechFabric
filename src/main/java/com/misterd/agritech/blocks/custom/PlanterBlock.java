package com.misterd.agritech.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PlanterBlock extends Block {
    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 3, 11, 3),
            Block.box(13, 0, 1, 15, 11, 3),
            Block.box(1, 0, 13, 3, 11, 15),
            Block.box(13, 0, 13, 15, 11, 15),
            Block.box(2, 2, 2, 14, 10, 3),
            Block.box(2, 2, 13, 14, 10, 14),
            Block.box(2, 2, 3, 3, 10, 13),
            Block.box(13, 2, 3, 14, 10, 13),
            Block.box(3, 2, 3, 13, 3, 13)
    );

    public PlanterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
