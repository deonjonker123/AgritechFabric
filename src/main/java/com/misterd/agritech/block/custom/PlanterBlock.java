package com.misterd.agritech.block.custom;

import com.misterd.agritech.blockentity.custom.PlanterBlockEntity;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class PlanterBlock extends BaseEntityBlock {
    public static final MapCodec<PlanterBlock> CODEC = simpleCodec(PlanterBlock::new);
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new PlanterBlockEntity(worldPosition, blockState);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack destroyedWith) {
        if (level.getBlockEntity(pos) instanceof PlanterBlockEntity planterBlockEntity) {
            planterBlockEntity.drops();
            level.updateNeighbourForOutputSignal(pos, this);
        }

        super.playerDestroy(level, player, pos, state, blockEntity, destroyedWith);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof PlanterBlockEntity planterBlockEntity) {


            boolean isPlanterEmpty = planterBlockEntity.isEmpty();

            if (isPlanterEmpty && !itemStack.isEmpty()) {
                planterBlockEntity.setTheItem(itemStack);
                itemStack.shrink(1);
                level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
            }

            else {
                player.openMenu(new ExtendedMenuProvider<BlockPos>() {
                    @Override
                    public @Nullable AbstractContainerMenu createMenu (int containerId, Inventory inventory, Player player) {
                        return planterBlockEntity.createMenu(containerId, inventory, player);
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("gui.agritech.planter_menu");
                    }

                    @Override
                    public BlockPos getScreenOpeningData(ServerPlayer serverPlayer) {
                        return planterBlockEntity.getBlockPos();
                    }
                });

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.SUCCESS;
    }
}
