package com.misterd.agritech.client.ber;

import com.misterd.agritech.blockentity.custom.PlanterBlockEntity;
import com.misterd.agritech.config.PlantablesConfig;
import com.misterd.agritech.util.RegistryHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PlanterBlockEntityRenderer implements BlockEntityRenderer<PlanterBlockEntity, PlanterBlockEntityRendererState> {

    private static final Identifier WATER_STILL = Identifier.fromNamespaceAndPath("minecraft", "block/water_still");
    private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    private final ItemModelResolver itemModelResolver;
    private final BlockModelResolver blockModelResolver;

    public PlanterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
        this.blockModelResolver = context.blockModelResolver();
    }

    @Override
    public PlanterBlockEntityRendererState createRenderState() {
        return new PlanterBlockEntityRendererState();
    }

    @Override
    public void extractRenderState(PlanterBlockEntity be, PlanterBlockEntityRendererState state, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, crumblingOverlay);

        state.soilStack = be.getItem(PlanterBlockEntity.SLOT_SOIL).copy();
        state.plantStack = be.getItem(PlanterBlockEntity.SLOT_PLANT).copy();
        state.growthProgress = be.getGrowthProgress();
        state.growthStage = be.getGrowthStage();
        state.soilIsWater = !state.soilStack.isEmpty() && RegistryHelper.getItemId(state.soilStack).equals("minecraft:water_bucket");

        if (!state.soilStack.isEmpty() && !state.soilIsWater) {
            itemModelResolver.updateForTopItem(state.soilRenderState, state.soilStack, ItemDisplayContext.FIXED, be.getLevel(), null, 0);
        } else {
            state.soilRenderState.clear();
        }

        state.plantModel.clear();
        if (!state.plantStack.isEmpty() && !state.soilStack.isEmpty() && state.plantStack.getItem() instanceof BlockItem) {
            String plantId = RegistryHelper.getItemId(state.plantStack);
            boolean isTree = PlantablesConfig.isValidSapling(plantId);
            boolean isCrop = PlantablesConfig.isValidSeed(plantId);

            if (isTree) {
                BlockState saplingState = ((BlockItem) state.plantStack.getItem()).getBlock().defaultBlockState();
                blockModelResolver.update(state.plantModel, saplingState, BLOCK_DISPLAY_CONTEXT);
            } else if (isCrop) {
                BlockState cropState = getCropBlockState(state.plantStack, state.growthStage);
                if (cropState != null) {
                    blockModelResolver.update(state.plantModel, cropState, BLOCK_DISPLAY_CONTEXT);
                }
            }
        }
    }

    @Override
    public void submit(PlanterBlockEntityRendererState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {

        int light = state.lightCoords;

        if (!state.soilStack.isEmpty()) {
            if (state.soilIsWater) {
                submitWater(poseStack, collector, light);
            } else {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.4f, 0.5);
                poseStack.scale(1.3f, 0.65f, 1.3f);
                state.soilRenderState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }

        if (!state.plantModel.isEmpty()) {
            String plantId = RegistryHelper.getItemId(state.plantStack);
            boolean isTree = PlantablesConfig.isValidSapling(plantId);

            poseStack.pushPose();
            if (isTree) {
                float scale = 0.3f + state.growthProgress * 0.4f;
                poseStack.translate(0.5, 0.58, 0.5);
                poseStack.scale(scale, scale, scale);
                poseStack.translate(-0.5, 0.0, -0.5);
            } else {
                poseStack.translate(0.1725, 0.58, 0.1725);
                poseStack.scale(0.65f, 0.65f, 0.65f);
            }
            state.plantModel.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }

    private static void submitWater(PoseStack poseStack, SubmitNodeCollector collector, int light) {
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(WATER_STILL);

        float y = 0.57f;
        float xMin = 0.175f, xMax = 0.825f;
        float zMin = 0.175f, zMax = 0.825f;
        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();

        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, consumer) -> {
                    addWaterVertex(consumer, pose, xMin, y, zMin, u0, v0, light);
                    addWaterVertex(consumer, pose, xMin, y, zMax, u0, v1, light);
                    addWaterVertex(consumer, pose, xMax, y, zMax, u1, v1, light);
                    addWaterVertex(consumer, pose, xMax, y, zMin, u1, v0, light);
                });
    }

    private static void addWaterVertex(VertexConsumer c, PoseStack.Pose pose, float x, float y, float z, float u, float v, int light) {
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
        if (def.hasProperty(BlockStateProperties.AGE_7))  return def.setValue(BlockStateProperties.AGE_7,  Math.min(age, 7));
        if (def.hasProperty(BlockStateProperties.AGE_3))  return def.setValue(BlockStateProperties.AGE_3,  Math.min(age, 3));
        if (def.hasProperty(BlockStateProperties.AGE_5))  return def.setValue(BlockStateProperties.AGE_5,  Math.min(age, 5));
        if (def.hasProperty(BlockStateProperties.AGE_15)) return def.setValue(BlockStateProperties.AGE_15, Math.min(age, 15));
        if (def.hasProperty(BlockStateProperties.AGE_25)) return def.setValue(BlockStateProperties.AGE_25, Math.min(age, 25));
        return def;
    }
}