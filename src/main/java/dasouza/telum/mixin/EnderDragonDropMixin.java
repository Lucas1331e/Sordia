package dasouza.telum.mixin;

import dasouza.telum.item.TelumItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin that intercepts LivingEntity death and drops 4-8 Dragon Scales if the dying entity is an Ender Dragon.
 */
@Mixin(LivingEntity.class)
public abstract class EnderDragonDropMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void telum$onEntityDeath(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof EnderDragon dragon && dragon.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 2; i++) {
                ItemStack scaleStack = new ItemStack(TelumItems.DRAGON_SORDIA);
                double offsetX = (serverLevel.getRandom().nextDouble() - 0.5) * 2.0;
                double offsetZ = (serverLevel.getRandom().nextDouble() - 0.5) * 2.0;

                ItemEntity itemEntity = new ItemEntity(
                        serverLevel,
                        dragon.getX() + offsetX,
                        dragon.getY() + 1.0,
                        dragon.getZ() + offsetZ,
                        scaleStack
                );
                itemEntity.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(itemEntity);
            }
        }
    }
}
