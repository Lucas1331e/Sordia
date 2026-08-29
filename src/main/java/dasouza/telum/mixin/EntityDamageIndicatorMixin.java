package dasouza.telum.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArmorStand.class)
public abstract class EntityDamageIndicatorMixin {

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void telum$showDamageIndicator(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof Player && amount > 0.0F) {
            ArmorStand target = (ArmorStand) (Object) this;
            telum$spawnIndicator(level, target, amount);
        }
    }

    @Unique
    private void telum$spawnIndicator(ServerLevel level, ArmorStand target, float amount) {
        if (level == null) return;

        Display.TextDisplay indicator = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, level);
        indicator.setText(Component.literal(String.format("-%.1f", amount)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

        indicator.setPos(target.getX(), target.getY() + target.getBbHeight() + 0.2, target.getZ());
        indicator.setBillboardConstraints(Display.BillboardConstraints.CENTER);
        indicator.addTag("telum$damage_indicator");

        level.addFreshEntity(indicator);
    }
}


