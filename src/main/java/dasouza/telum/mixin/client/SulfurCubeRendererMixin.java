package dasouza.telum.mixin.client;

import dasouza.telum.util.TemporalBlockRewindManager;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.entity.SulfurCubeRenderer;
import net.minecraft.client.renderer.entity.state.SulfurCubeRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SulfurCubeRenderer.class)
public abstract class SulfurCubeRendererMixin {

    @Shadow @Final private BlockModelResolver blockModelResolver;

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/monster/cubemob/SulfurCube;Lnet/minecraft/client/renderer/entity/state/SulfurCubeRenderState;F)V", at = @At("RETURN"))
    private void telum$extractTemporalBlockState(SulfurCube sulfurCube, SulfurCubeRenderState renderState, float partialTick, CallbackInfo ci) {
        ItemStack bodyItem = sulfurCube.getItemBySlot(EquipmentSlot.BODY);
        if (bodyItem.isEmpty()) {
            bodyItem = sulfurCube.getItemBySlot(EquipmentSlot.MAINHAND);
        }

        if (!bodyItem.isEmpty() && TemporalBlockRewindManager.isTemporalItem(bodyItem)) {
            Item item = bodyItem.getItem();
            Block block = item instanceof BlockItem blockItem ? blockItem.getBlock() : Block.byItem(item);
            if (block != Blocks.AIR) {
                BlockState state = block.defaultBlockState();
                BlockItemStateProperties props = bodyItem.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
                state = props.apply(state);

                this.blockModelResolver.update(renderState.containedBlock, state, SulfurCubeRenderer.BLOCK_DISPLAY_CONTEXT);
            }
        }
    }
}
