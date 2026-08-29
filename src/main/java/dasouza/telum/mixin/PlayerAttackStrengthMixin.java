package dasouza.telum.mixin;

import dasouza.telum.util.AttackScaleTracker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerAttackStrengthMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void telum$captureAttackScale(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        AttackScaleTracker.setLastAttackScale(player.getAttackStrengthScale(0.5f));
    }
}
