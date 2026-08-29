package dasouza.telum.mixin;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.util.EndermanLootHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockAutoSmeltMixin {

    @Inject(
            method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void telum$autoSmeltAndEndermanTeleport(Level level, BlockPos pos, ItemStack stack, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel && stack != null && !stack.isEmpty()) {
            Player player = serverLevel.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6.0, false);
            if (player != null) {
                ItemStack mainStack = player.getMainHandItem();
                if (mainStack.getItem() instanceof AssembledToolItem) {
                    AssembledToolData data = AssembledToolItem.getToolData(mainStack);
                    if (data != null) {
                        boolean modifiedStack = false;
                        ItemStack targetStack = stack;

                        // 1. Blaze Material Ability: Auto-Smelt Mined Blocks
                        if (data.getMaterialLevel(PartMaterial.BLAZE) >= 1) {
                            ItemStack smelted = AssembledToolItem.getSmeltedResult(stack, serverLevel);
                            if (!smelted.isEmpty()) {
                                serverLevel.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.2, 0.2, 0.2, 0.03);
                                targetStack = smelted;
                                modifiedStack = true;
                            }
                        }

                        // 2. Enderman Material Ability: Teleport Mined Drops to Container / Inventory
                        if (data.getMaterialLevel(PartMaterial.ENDERMAN) >= 1) {
                            EndermanLootHandler.depositLoot(player, targetStack);
                            ci.cancel();
                            return;
                        }

                        // If stack was auto-smelted but NO Enderman part is present:
                        if (modifiedStack) {
                            Block.popResource(serverLevel, pos, targetStack);
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }
}
