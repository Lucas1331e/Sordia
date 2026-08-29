package dasouza.telum.mixin.client;

import dasouza.telum.Telum;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.component.AssembledToolData;
import dasouza.telum.tool.PartMaterial;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpinAttackEffectLayer.class)
public class SpinAttackEffectLayerMixin {

    @Unique
    private static final Identifier SKULK_RIPTIDE_TEXTURE = Telum.id("textures/entity/sculk_riptide.png");

    @Unique
    private AvatarRenderState capturedState;

    @Inject(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
        at = @At("HEAD")
    )
    private void captureState(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, AvatarRenderState state, float yRot, float xRot, CallbackInfo ci) {
        this.capturedState = state;
    }

    @ModifyArg(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/resources/Identifier;IIILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
        ),
        index = 3
    )
    private Identifier modifyRiptideTexture(Identifier originalTexture) {
        if (this.capturedState != null) {
            ItemStack mainStack = this.capturedState.rightHandItemStack;
            ItemStack offStack  = this.capturedState.leftHandItemStack;

            if (isSkulkTool(mainStack) || isSkulkTool(offStack)) {
                return SKULK_RIPTIDE_TEXTURE;
            }
        }
        return originalTexture;
    }

    @Unique
    private static boolean isSkulkTool(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof AssembledToolItem) {
            AssembledToolData data = AssembledToolItem.getToolData(stack);
            return data != null && data.getMaterialLevel(PartMaterial.SKULK) >= 1;
        }
        return false;
    }
}
