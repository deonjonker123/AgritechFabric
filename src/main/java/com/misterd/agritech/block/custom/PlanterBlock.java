package com.misterd.agritech.block.custom;

import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritech.datamap.ATDataMaps;
import com.misterd.agritech.gui.custom.PlanterMenu;
import com.misterd.agritech.mixin.HoeItemAccessor;
import com.misterd.agritech.util.RegistryHelper;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {}

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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlanterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == ATBlockEntities.PLANTER_BE
                ? (lvl, pos, blockState, be) -> PlanterBlockEntity.tick(lvl, pos, blockState, (PlanterBlockEntity) be)
                : null;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack destroyedWith) {
        if (!level.isClientSide() && blockEntity instanceof PlanterBlockEntity planter) {
            planter.drops();
        }
        super.playerDestroy(level, player, pos, state, blockEntity, destroyedWith);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof PlanterBlockEntity planter)) return InteractionResult.FAIL;

        ItemStack heldItem = player.getItemInHand(hand);

        if (player.isCrouching()) {
            return handleCrouchOpen(level, pos, player, planter);
        }
        if (planter.isValidPlant(heldItem)) {
            return handlePlantInsert(state, level, pos, player, planter, heldItem);
        }
        if (planter.isValidSoilForAnyRecipe(heldItem)) {
            return handleSoilInsert(state, level, pos, player, planter, heldItem);
        }
        if (isFertilizer(heldItem)) {
            return handleFertilizer(state, level, pos, player, planter, heldItem);
        }
        if (heldItem.getItem() instanceof HoeItem) {
            return handleHoeTill(state, level, pos, player, planter, heldItem, hand, hitResult);
        }

        if (!level.isClientSide()) openGui(player, planter, pos);
        return InteractionResult.SUCCESS;
    }

    private static boolean isFertilizer(ItemStack stack) {
        return !stack.isEmpty() && ATDataMaps.getFertilizer(stack.getItem()) != null;
    }

    private InteractionResult handleCrouchOpen(Level level, BlockPos pos, Player player, PlanterBlockEntity planter) {
        if (!level.isClientSide()) openGui(player, planter, pos);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handlePlantInsert(BlockState state, Level level, BlockPos pos, Player player, PlanterBlockEntity planter, ItemStack heldItem) {
        if (!planter.getItem(PlanterBlockEntity.SLOT_PLANT).isEmpty()) {
            if (!level.isClientSide()) openGui(player, planter, pos);
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack existingSoil = planter.getItem(PlanterBlockEntity.SLOT_SOIL);
        if (!existingSoil.isEmpty() && !planter.isValidPlantSoilCombination(heldItem, existingSoil)) {
            player.sendOverlayMessage(Component.translatable("message.agritech.invalid_seed_soil_combination").withStyle(ChatFormatting.GOLD));
            return InteractionResult.SUCCESS;
        }

        planter.setItem(PlanterBlockEntity.SLOT_PLANT, heldItem.copyWithCount(1));
        if (!player.getAbilities().instabuild) heldItem.shrink(1);
        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.sendBlockUpdated(pos, state, state, 3);
        planter.setChanged();
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleSoilInsert(BlockState state, Level level, BlockPos pos, Player player, PlanterBlockEntity planter, ItemStack heldItem) {
        if (!planter.getItem(PlanterBlockEntity.SLOT_SOIL).isEmpty()) {
            if (!level.isClientSide()) openGui(player, planter, pos);
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack existingPlant = planter.getItem(PlanterBlockEntity.SLOT_PLANT);
        if (!existingPlant.isEmpty() && !planter.isValidPlantSoilCombination(existingPlant, heldItem)) {
            player.sendOverlayMessage(Component.translatable("message.agritech.invalid_seed_soil_combination").withStyle(ChatFormatting.GOLD));
            return InteractionResult.SUCCESS;
        }

        planter.setItem(PlanterBlockEntity.SLOT_SOIL, heldItem.copyWithCount(1));
        if (!player.getAbilities().instabuild) heldItem.shrink(1);
        level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.sendBlockUpdated(pos, state, state, 3);
        planter.setChanged();
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleFertilizer(BlockState state, Level level, BlockPos pos, Player player, PlanterBlockEntity planter, ItemStack heldItem) {
        if (planter.getItem(PlanterBlockEntity.SLOT_PLANT).isEmpty()
                || planter.getItem(PlanterBlockEntity.SLOT_SOIL).isEmpty()
                || planter.isReadyToHarvest()) {
            if (!level.isClientSide()) openGui(player, planter, pos);
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            var data = ATDataMaps.getFertilizer(heldItem.getItem());
            if (data != null) {
                planter.applyManualFertilizer(data.speedMultiplier());
                if (!player.getAbilities().instabuild) heldItem.shrink(1);
                level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                            6, 0.3, 0.2, 0.3, 0.0);
                }
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleHoeTill(BlockState state, Level level, BlockPos pos, Player player, PlanterBlockEntity planter, ItemStack heldItem, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack soilStack = planter.getItem(PlanterBlockEntity.SLOT_SOIL);
        if (soilStack.isEmpty() || !(soilStack.getItem() instanceof BlockItem soilBlockItem)) {
            return InteractionResult.PASS;
        }

        Block soilBlock = soilBlockItem.getBlock();

        Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> tillables = HoeItemAccessor.getTillables();
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> tillable = tillables.get(soilBlock);

        UseOnContext ctx = new UseOnContext(level, player, hand, heldItem, hitResult);
        if (tillable != null && !tillable.getFirst().test(ctx)) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            Block resultBlock = getTillResult(soilBlock);
            if (resultBlock == null) return InteractionResult.PASS;

            planter.setItem(PlanterBlockEntity.SLOT_SOIL, new ItemStack(resultBlock));
            level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                heldItem.hurtAndBreak(1, player, slot);
            }
            planter.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    private Block getTillResult(Block input) {
        String inputId = RegistryHelper.getBlockId(input);
        return switch (inputId) {
            case "minecraft:dirt", "minecraft:grass_block", "minecraft:rooted_dirt" -> Blocks.FARMLAND;
            case "minecraft:coarse_dirt" -> Blocks.DIRT;
            case "farmersdelight:rich_soil" -> BuiltInRegistries.BLOCK.getValue(Identifier.parse("farmersdelight:rich_soil_farmland"));
            default -> null;
        };
    }

    private void openGui(Player player, PlanterBlockEntity planter, BlockPos pos) {
        player.openMenu(new ExtendedMenuProvider<BlockPos>() {
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player p) {
                return new PlanterMenu(containerId, inventory, pos);
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("gui.agritech.planter_menu");
            }

            @Override
            public BlockPos getScreenOpeningData(ServerPlayer serverPlayer) {
                return pos;
            }
        });
    }
}