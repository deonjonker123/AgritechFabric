package com.misterd.agritech.client.ber;

import com.misterd.agritech.blockentity.custom.RaisedBedBlockEntity;
import com.misterd.agritech.config.PlantablesConfig;
import com.misterd.agritech.util.RegistryHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

public class RaisedBedBlockEntityRenderer implements BlockEntityRenderer<RaisedBedBlockEntity, RaisedBedBlockEntityRendererState> {

    private static final Identifier WATER_STILL = Identifier.fromNamespaceAndPath("minecraft", "block/water_still");

    public RaisedBedBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public RaisedBedBlockEntityRendererState createRenderState() {
        return new RaisedBedBlockEntityRendererState();
    }

    @Override
    public void extractRenderState(RaisedBedBlockEntity be, RaisedBedBlockEntityRendererState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);

        state.soilStack = be.getItem(RaisedBedBlockEntity.SLOT_SOIL).copy();
        state.plantStack = be.getItem(RaisedBedBlockEntity.SLOT_PLANT).copy();
        state.growthProgress = be.getGrowthProgress();
        state.growthStage = be.getGrowthStage();
        state.posSeed = be.getBlockPos().asLong();
        state.soilIsWater = !state.soilStack.isEmpty()
                && RegistryHelper.getItemId(state.soilStack).equals("minecraft:water_bucket");

        BlockAndTintGetter level = (BlockAndTintGetter) be.getLevel();
        BlockPos pos = be.getBlockPos();
        state.soilTints = sampleTints(state.soilStack, level, pos);
        state.plantTints = sampleTints(state.plantStack, level, pos);
    }

    private static int[] sampleTints(ItemStack stack, BlockAndTintGetter level, BlockPos pos) {
        if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem bi)) return new int[0];
        BlockState blockState = bi.getBlock().defaultBlockState();
        var blockColors = Minecraft.getInstance().getBlockColors();
        var sources = blockColors.getTintSources(blockState);
        if (sources.isEmpty()) return new int[0];
        int[] tints = new int[sources.size()];
        for (int i = 0; i < sources.size(); i++) {
            BlockTintSource src = sources.get(i);
            tints[i] = src != null ? src.colorInWorld(blockState, level, pos) : -1;
        }
        return tints;
    }

    @Override
    public void submit(RaisedBedBlockEntityRendererState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {

        int light = state.lightCoords;

        if (!state.soilStack.isEmpty()) {
            if (state.soilIsWater) {
                submitWater(poseStack, collector, light);
            } else if (state.soilStack.getItem() instanceof BlockItem soilBlockItem) {
                BlockState soilState = soilBlockItem.getBlock().defaultBlockState();
                poseStack.pushPose();
                poseStack.translate(0.064, 0.1, 0.065);
                poseStack.scale(0.88f, 0.18f, 0.88f);
                submitBlockQuads(soilState, state.posSeed, state.soilTints, poseStack, collector, light);
                poseStack.popPose();
            }
        }

        if (!state.plantStack.isEmpty() && !state.soilStack.isEmpty()
                && state.plantStack.getItem() instanceof BlockItem plantBlockItem) {

            String plantId = RegistryHelper.getItemId(state.plantStack);
            boolean isTree = PlantablesConfig.isValidSapling(plantId);
            boolean isCrop = PlantablesConfig.isValidSeed(plantId);

            if (isTree || isCrop) {
                BlockState plantState = isTree
                        ? plantBlockItem.getBlock().defaultBlockState()
                        : getCropBlockState(state.plantStack, state.growthStage);

                if (plantState != null) {
                    poseStack.pushPose();
                    if (isTree) {
                        float scale = 0.3f + state.growthProgress * 0.4f;
                        poseStack.translate(0.5, 0.30, 0.5);
                        poseStack.scale(scale, scale, scale);
                        poseStack.translate(-0.5, 0.0, -0.5);
                    } else {
                        float gs = 0.2f + Math.min(1f, state.growthProgress) * 0.5f;
                        poseStack.translate(0.1725, 0.3, 0.1725);
                        poseStack.scale(0.65f, gs, 0.65f);
                    }
                    submitBlockQuads(plantState, state.posSeed ^ 1L, state.plantTints, poseStack, collector, light);
                    poseStack.popPose();
                }
            }
        }
    }

    private static void submitBlockQuads(BlockState blockState, long seed, int[] tints, PoseStack poseStack, SubmitNodeCollector collector, int light) {
        var modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        var model = modelSet.get(blockState);
        if (model == null) return;

        var parts = new ArrayList<BlockStateModelPart>();
        model.collectParts(RandomSource.create(seed), parts);
        if (parts.isEmpty()) return;

        var renderType = blockState.canOcclude()
                ? RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS)
                : RenderTypes.entityCutout(TextureAtlas.LOCATION_BLOCKS);

        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            QuadInstance qi = new QuadInstance();
            qi.setLightCoords(light);
            qi.setOverlayCoords(OverlayTexture.NO_OVERLAY);
            for (var part : parts) {
                emitQuads(part.getQuads(null), qi, tints, pose, consumer);
                for (Direction dir : Direction.values()) {
                    emitQuads(part.getQuads(dir), qi, tints, pose, consumer);
                }
            }
        });
    }

    private static void emitQuads(Iterable<BakedQuad> quads, QuadInstance qi, int[] tints, PoseStack.Pose pose, VertexConsumer consumer) {
        for (BakedQuad quad : quads) {
            qi.setColor(-1);
            if (quad.materialInfo().isTinted()) {
                int layer = quad.materialInfo().tintIndex();
                if (layer >= 0 && layer < tints.length) {
                    qi.multiplyColor(tints[layer]);
                }
            }
            consumer.putBakedQuad(pose, quad, qi);
        }
    }

    private static void submitWater(PoseStack poseStack, SubmitNodeCollector collector, int light) {
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(WATER_STILL);

        float y = 0.28f;
        float xMin = 0f, xMax = 0.99f;
        float zMin = 0f, zMax = 0.99f;
        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();

        collector.submitCustomGeometry(poseStack,
                RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS),
                (pose, consumer) -> {
                    addWaterVertex(consumer, pose, xMin, y, zMin, u0, v0, light);
                    addWaterVertex(consumer, pose, xMin, y, zMax, u0, v1, light);
                    addWaterVertex(consumer, pose, xMax, y, zMax, u1, v1, light);
                    addWaterVertex(consumer, pose, xMax, y, zMin, u1, v0, light);
                });
    }

    private static void addWaterVertex(VertexConsumer c, PoseStack.Pose pose,
                                       float x, float y, float z, float u, float v, int light) {
        c.addVertex(pose, x, y, z)
                .setColor(0x3F, 0x76, 0xE4, 0xA0)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, 0f, 1f, 0f);
    }

    private static @Nullable BlockState getCropBlockState(ItemStack stack, int age) {
        if (!(stack.getItem() instanceof BlockItem bi)) return null;
        BlockState def = bi.getBlock().defaultBlockState();
        for (Property<?> prop : def.getProperties()) {
            if (prop instanceof IntegerProperty ip && prop.getName().equals("age")) {
                int max = ip.getPossibleValues().stream().mapToInt(Integer::intValue).max().orElse(7);
                return def.setValue(ip, Math.min(age, max));
            }
        }
        if (def.hasProperty(BlockStateProperties.AGE_7)) return def.setValue(BlockStateProperties.AGE_7, Math.min(age, 7));
        if (def.hasProperty(BlockStateProperties.AGE_3)) return def.setValue(BlockStateProperties.AGE_3, Math.min(age, 3));
        if (def.hasProperty(BlockStateProperties.AGE_5)) return def.setValue(BlockStateProperties.AGE_5, Math.min(age, 5));
        if (def.hasProperty(BlockStateProperties.AGE_15)) return def.setValue(BlockStateProperties.AGE_15, Math.min(age, 15));
        if (def.hasProperty(BlockStateProperties.AGE_25)) return def.setValue(BlockStateProperties.AGE_25, Math.min(age, 25));
        return def;
    }
}