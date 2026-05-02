package com.misterd.agritech.block.custom;

import com.misterd.agritech.blockentity.ATBlockEntities;
import com.misterd.agritech.blockentity.custom.RaisedBedBlockEntity;
import com.misterd.agritech.config.PlantablesConfig;
import com.misterd.agritech.gui.custom.RaisedBedMenu;
import com.misterd.agritech.mixin.HoeItemAccessor;
import com.misterd.agritech.util.RegistryHelper;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class RaisedBedBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(0,  0,  0, 16, 5,  1),
            Block.box(0,  0, 15, 16, 5, 16),
            Block.box(0,  0,  1,  1, 5, 15),
            Block.box(15, 0,  1, 16, 5, 15),
            Block.box(1,  0,  1, 15, 1, 15)
    );

    public static final MapCodec<RaisedBedBlock> CODEC = simpleCodec(RaisedBedBlock::new);

    public RaisedBedBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RaisedBedBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return type == ATBlockEntities.RAISED_BED_BE
                ? (lvl, pos, blockState, be) -> RaisedBedBlockEntity.tick(lvl, pos, blockState, (RaisedBedBlockEntity) be)
                : null;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack destroyedWith) {
        if (!level.isClientSide() && blockEntity instanceof RaisedBedBlockEntity bed) {
            bed.drops();
        }
        super.playerDestroy(level, player, pos, state, blockEntity, destroyedWith);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof RaisedBedBlockEntity bed)) return InteractionResult.FAIL;

        ItemStack heldItem = player.getItemInHand(hand);
        String heldItemId = RegistryHelper.getItemId(heldItem);

        if (player.isCrouching()) {
            return handleCrouchOpen(level, pos, player, bed);
        }
        if (PlantablesConfig.isValidSeed(heldItemId) || PlantablesConfig.isValidSapling(heldItemId)) {
            return handlePlantInsert(state, level, pos, player, bed, heldItem, heldItemId);
        }
        if (PlantablesConfig.isValidSoil(heldItemId)) {
            return handleSoilInsert(state, level, pos, player, bed, heldItem, heldItemId);
        }
        if (PlantablesConfig.isValidFertilizer(heldItemId)) {
            return handleFertilizer(state, level, pos, player, bed, heldItem, heldItemId);
        }
        if (heldItem.getItem() instanceof HoeItem) {
            return handleHoeTill(state, level, pos, player, bed, heldItem, hand, hitResult);
        }

        if (!level.isClientSide()) openGui(player, bed, pos);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleCrouchOpen(Level level, BlockPos pos, Player player, RaisedBedBlockEntity bed) {
        if (!level.isClientSide()) openGui(player, bed, pos);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handlePlantInsert(BlockState state, Level level, BlockPos pos, Player player, RaisedBedBlockEntity bed, ItemStack heldItem, String heldItemId) {
        if (!bed.getItem(RaisedBedBlockEntity.SLOT_PLANT).isEmpty()) {
            if (!level.isClientSide()) openGui(player, bed, pos);
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack existingSoil = bed.getItem(RaisedBedBlockEntity.SLOT_SOIL);
        if (!existingSoil.isEmpty()) {
            String soilId = RegistryHelper.getItemId(existingSoil);
            boolean valid = PlantablesConfig.isValidSeed(heldItemId)
                    ? PlantablesConfig.isSoilValidForSeed(soilId, heldItemId)
                    : PlantablesConfig.isSoilValidForSapling(soilId, heldItemId);
            if (!valid) {
                player.sendSystemMessage(Component.translatable("message.agritech.invalid_seed_soil_combination"));
                return InteractionResult.SUCCESS;
            }
        }

        bed.setItem(RaisedBedBlockEntity.SLOT_PLANT, heldItem.copyWithCount(1));
        if (!player.getAbilities().instabuild) heldItem.shrink(1);
        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.sendBlockUpdated(pos, state, state, 3);
        bed.setChanged();
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleSoilInsert(BlockState state, Level level, BlockPos pos, Player player, RaisedBedBlockEntity bed, ItemStack heldItem, String heldItemId) {
        if (!bed.getItem(RaisedBedBlockEntity.SLOT_SOIL).isEmpty()) {
            if (!level.isClientSide()) openGui(player, bed, pos);
            return InteractionResult.SUCCESS;
        }
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ItemStack existingPlant = bed.getItem(RaisedBedBlockEntity.SLOT_PLANT);
        if (!existingPlant.isEmpty()) {
            String plantId = RegistryHelper.getItemId(existingPlant);
            boolean valid = PlantablesConfig.isValidSeed(plantId)
                    ? PlantablesConfig.isSoilValidForSeed(heldItemId, plantId)
                    : PlantablesConfig.isSoilValidForSapling(heldItemId, plantId);
            if (!valid) {
                player.sendSystemMessage(Component.translatable("message.agritech.invalid_seed_soil_combination"));
                return InteractionResult.SUCCESS;
            }
        }

        bed.setItem(RaisedBedBlockEntity.SLOT_SOIL, heldItem.copyWithCount(1));
        if (!player.getAbilities().instabuild) heldItem.shrink(1);
        level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 0.8F);
        level.sendBlockUpdated(pos, state, state, 3);
        bed.setChanged();
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleFertilizer(BlockState state, Level level, BlockPos pos, Player player, RaisedBedBlockEntity bed, ItemStack heldItem, String heldItemId) {
        if (bed.getItem(RaisedBedBlockEntity.SLOT_PLANT).isEmpty()
                || bed.getItem(RaisedBedBlockEntity.SLOT_SOIL).isEmpty()
                || bed.isReadyToHarvest()) {
            if (!level.isClientSide()) openGui(player, bed, pos);
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            PlantablesConfig.FertilizerInfo info = PlantablesConfig.getFertilizerInfo(heldItemId);
            if (info != null) {
                bed.applyManualFertilizer(info.speedMultiplier);
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

    private InteractionResult handleHoeTill(BlockState state, Level level, BlockPos pos, Player player, RaisedBedBlockEntity bed, ItemStack heldItem, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack soilStack = bed.getItem(RaisedBedBlockEntity.SLOT_SOIL);
        if (soilStack.isEmpty() || !(soilStack.getItem() instanceof BlockItem soilBlockItem)) {
            return InteractionResult.PASS;
        }

        Block soilBlock = soilBlockItem.getBlock();
        Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> tillables = HoeItemAccessor.getTillables();
        Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> tillable = tillables.get(soilBlock);
        if (tillable == null) return InteractionResult.PASS;

        UseOnContext ctx = new UseOnContext(level, player, hand, heldItem, hitResult);
        if (!tillable.getFirst().test(ctx)) return InteractionResult.PASS;

        if (!level.isClientSide()) {
            Block resultBlock = getTillResult(soilBlock);
            if (resultBlock == null) return InteractionResult.PASS;

            bed.setItem(RaisedBedBlockEntity.SLOT_SOIL, new ItemStack(resultBlock));
            level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                heldItem.hurtAndBreak(1, player, slot);
            }
            bed.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    private Block getTillResult(Block input) {
        if (!HoeItemAccessor.getTillables().containsKey(input)) return null;
        String inputId = RegistryHelper.getBlockId(input);
        return switch (inputId) {
            case "minecraft:dirt", "minecraft:grass_block",
                 "minecraft:rooted_dirt" -> net.minecraft.world.level.block.Blocks.FARMLAND;
            case "minecraft:coarse_dirt" -> net.minecraft.world.level.block.Blocks.DIRT;
            default -> null;
        };
    }

    private void openGui(Player player, RaisedBedBlockEntity bed, BlockPos pos) {
        player.openMenu(new ExtendedMenuProvider<BlockPos>() {
            @Override
            public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory inventory, Player p) {
                return new RaisedBedMenu(containerId, inventory, bed);
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("gui.agritech.raised_bed_menu");
            }

            @Override
            public BlockPos getScreenOpeningData(ServerPlayer serverPlayer) {
                return pos;
            }
        });
    }
}