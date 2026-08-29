package dasouza.telum.mixin;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Display.class)
public abstract class TextDisplayLifespanMixin {

    @Unique
    private int telum$lifespan = 20;
    @Unique
    private boolean telum$isDamageIndicator = false;
    @Unique
    private boolean telum$isRevealMarker = false;
    @Unique
    private boolean telum$checkedTag = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void telum$tickLifespan(CallbackInfo ci) {
        Display self = (Display) (Object) this;

        if (!telum$checkedTag) {
            telum$checkedTag = true;
            telum$isDamageIndicator = self.entityTags().contains("telum$damage_indicator");
            telum$isRevealMarker = self.entityTags().contains("telum$reveal_marker");
            if (telum$isRevealMarker) {
                telum$lifespan = 600; // 30 seconds of bright glowing reveal
            }
        }

        if (telum$isDamageIndicator) {
            telum$lifespan--;
            if (telum$lifespan <= 0) {
                self.discard();
            } else {
                self.setPos(self.getX(), self.getY() + 0.05, self.getZ());
            }
        } else if (telum$isRevealMarker) {
            telum$lifespan--;
            if (telum$lifespan <= 0) {
                self.discard();
            } else if (self.level() instanceof ServerLevel serverLevel && telum$lifespan % 6 == 0) {
                serverLevel.sendParticles(ParticleTypes.GLOW,
                        self.getX() + 0.5, self.getY() + 0.5, self.getZ() + 0.5,
                        2, 0.3, 0.3, 0.3, 0.01);
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        self.getX() + 0.5, self.getY() + 1.1, self.getZ() + 0.5,
                        1, 0.1, 0.1, 0.1, 0.02);
            }
        }
    }
}
