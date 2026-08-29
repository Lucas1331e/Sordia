package dasouza.telum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dasouza.telum.block.ArcheologyTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ArcheologyTableBlockEntityRenderer implements BlockEntityRenderer<ArcheologyTableBlockEntity, ArcheologyTableBlockEntityRenderer.ArcheologyTableRenderState> {

    private final ItemModelResolver itemModelResolver;

    public static class ArcheologyTableRenderState extends BlockEntityRenderState {
        public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
        public boolean hasItem = false;
    }

    public ArcheologyTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = Minecraft.getInstance().getItemModelResolver();
    }

    @Override
    public ArcheologyTableRenderState createRenderState() {
        return new ArcheologyTableRenderState();
    }

    @Override
    public void extractRenderState(ArcheologyTableBlockEntity blockEntity, ArcheologyTableRenderState state, float partialTick,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        // CRITICAL: Call base extraction to populate blockPos, blockState, blockEntityType, lightCoords, breakProgress
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);

        ItemStack heldItem = blockEntity.getHeldItem();
        if (!heldItem.isEmpty()) {
            state.hasItem = true;
            ClientLevel level = blockEntity.getLevel() instanceof ClientLevel cl ? cl : Minecraft.getInstance().level;
            this.itemModelResolver.updateForTopItem(
                    state.itemRenderState,
                    heldItem,
                    ItemDisplayContext.FIXED,
                    level,
                    null,
                    0
            );
        } else {
            state.hasItem = false;
            state.itemRenderState.clear();
        }
    }

    @Override
    public void submit(ArcheologyTableRenderState state, PoseStack poseStack, SubmitNodeCollector output, CameraRenderState camera) {
        if (!state.hasItem || state.itemRenderState.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        // 1. Translate to block center, slightly above table surface
        poseStack.translate(0.5F, 0.34F, 0.5F);

        // 2. Lay item flat on surface
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

        // 3. Scale up to fill the table frame
        poseStack.scale(0.75F, 0.75F, 0.75F);

        // 4. Center the item model using its actual bounding box
        AABB aabb = state.itemRenderState.getModelBoundingBox();
        poseStack.translate(
                -(aabb.minX + aabb.maxX) / 2.0,
                -(aabb.minY + aabb.maxY) / 2.0,
                0.0
        );

        state.itemRenderState.submit(poseStack, output, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
