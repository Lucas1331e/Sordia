package dasouza.telum.client.render;

import dasouza.telum.Telum;
import dasouza.telum.entity.TemporalEvokerFangs;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.effects.EvokerFangsModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EvokerFangsRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

public class TemporalEvokerFangsRenderer extends EntityRenderer<TemporalEvokerFangs, EvokerFangsRenderState> {

    private static final Identifier TEXTURE_LOCATION = Telum.id("textures/entity/evoker_temporal_fangs.png");
    private final EvokerFangsModel model;

    public TemporalEvokerFangsRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new EvokerFangsModel(context.bakeLayer(ModelLayers.EVOKER_FANGS));
    }

    @Override
    public EvokerFangsRenderState createRenderState() {
        return new EvokerFangsRenderState();
    }

    @Override
    public void extractRenderState(TemporalEvokerFangs entity, EvokerFangsRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.yRot = entity.getYRot();
        state.biteProgress = entity.getAnimationProgress(partialTick);
    }

    @Override
    public void submit(EvokerFangsRenderState state, PoseStack poseStack, SubmitNodeCollector output, CameraRenderState camera) {
        float f = state.biteProgress;
        if (f != 0.0F) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90.0F - state.yRot));
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            output.submitModel(this.model, state, poseStack, TEXTURE_LOCATION, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
            poseStack.popPose();
            super.submit(state, poseStack, output, camera);
        }
    }
}
