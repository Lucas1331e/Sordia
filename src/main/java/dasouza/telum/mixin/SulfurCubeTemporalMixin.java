package dasouza.telum.mixin;

import dasouza.telum.util.SulfurCubeRewindManager;
import dasouza.telum.util.TemporalBlockRewindManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SulfurCube.class)
public abstract class SulfurCubeTemporalMixin extends LivingEntity {

    protected SulfurCubeTemporalMixin(net.minecraft.world.entity.EntityType<? extends LivingEntity> type, net.minecraft.world.level.Level level) {
        super(type, level);
    }

    @Inject(method = "canHoldItem", at = @At("HEAD"), cancellable = true)
    private void telum$canHoldTemporalItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (TemporalBlockRewindManager.isTemporalItem(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void telum$interactToEquipTemporalItem(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        SulfurCube sulfurCube = (SulfurCube) (Object) this;
        ItemStack held = player.getItemInHand(hand);

        if (TemporalBlockRewindManager.isTemporalItem(held)) {
            if (!this.level().isClientSide()) {
                ItemStack singleCopy = held.copyWithCount(1);
                sulfurCube.setItemSlot(EquipmentSlot.BODY, singleCopy);
                sulfurCube.setDropChance(EquipmentSlot.BODY, 1.0f);

                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                this.level().playSound(null, sulfurCube.getX(), sulfurCube.getY(), sulfurCube.getZ(),
                        net.minecraft.sounds.SoundEvents.SLIME_SQUISH, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
            cir.setReturnValue(net.minecraft.world.InteractionResult.SUCCESS);
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void telum$onHurtServer(ServerLevel level, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        SulfurCube sulfurCube = (SulfurCube) (Object) this;

        boolean holdsTemporalBlock = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = sulfurCube.getItemBySlot(slot);
            if (TemporalBlockRewindManager.isTemporalItem(stack)) {
                holdsTemporalBlock = true;
                break;
            }
        }

        if (holdsTemporalBlock) {
            SulfurCubeRewindManager.triggerRewind(level, sulfurCube);
        }
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void telum$boostKnockbackOnHurt(ServerLevel level, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        SulfurCube sulfurCube = (SulfurCube) (Object) this;

        boolean holdsTemporalBlock = false;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = sulfurCube.getItemBySlot(slot);
            if (TemporalBlockRewindManager.isTemporalItem(stack)) {
                holdsTemporalBlock = true;
                break;
            }
        }

        if (holdsTemporalBlock) {
            var attacker = damageSource.getEntity();
            if (attacker != null) {
                var look = attacker.getLookAngle();
                sulfurCube.setDeltaMovement(look.x * 1.8, 0.25, look.z * 1.8);
            } else {
                var delta = sulfurCube.getDeltaMovement();
                sulfurCube.setDeltaMovement(delta.x * 2.8, delta.y * 1.5 + 0.1, delta.z * 2.8);
            }
            sulfurCube.hurtMarked = true;
        }
    }
}
