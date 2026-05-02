package com.misterd.agritech.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;

public class AcaciaPlanterBlock extends PlanterBlock{
    public static final MapCodec<AcaciaPlanterBlock> CODEC = simpleCodec(AcaciaPlanterBlock::new);

    public AcaciaPlanterBlock(Properties properties) {
        super(properties);
    }

    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
