package dasouza.telum.entity;

import dasouza.telum.effect.TelumEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Custom Evoker Fangs entity that spawns under players when Temporal Sculk Shrieker is activated.
 * Strikes the player, dealing damage and applying infinite Shatter status effect (stacking levels).
 * Stops applying/stacking if the player cannot lose any more max health (min 1 heart / 2.0 HP).
 */
public class TemporalEvokerFangs extends EvokerFangs {

    private boolean dealtShatter = false;

    public TemporalEvokerFangs(EntityType<? extends EvokerFangs> type, Level level) {
        super(type, level);
    }

    public TemporalEvokerFangs(Level level, double x, double y, double z, float yRot, int warmup, LivingEntity owner) {
        super(TelumEntities.TEMPORAL_EVOKER_FANGS, level);
        this.setPos(x, y, z);
        this.setYRot(yRot * (180F / (float)Math.PI));
        this.setOwner(owner);
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide() && !dealtShatter) {
            List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.2D, 0.0D, 0.2D));
            for (LivingEntity target : targets) {
                if (target.isAlive() && !target.isInvulnerable()) {
                    applyShatterStack(target);
                    dealtShatter = true;
                }
            }
        }
    }

    private void applyShatterStack(LivingEntity target) {
        if (!(target instanceof Player player)) {
            return;
        }

        // Do not apply or stack Shatter if max health is already 2.0 HP (1 heart) or less
        if (player.getMaxHealth() <= 2.0D) {
            return;
        }

        MobEffectInstance current = target.getEffect(TelumEffects.SHATTER);
        int newAmplifier = 0;
        if (current != null) {
            newAmplifier = current.getAmplifier() + 1;
        }

        // Prevent reducing max health below 2.0 HP (1 heart)
        if (player.getMaxHealth() - 2.0D < 2.0D && current != null) {
            return;
        }

        // Apply infinite duration Shatter effect
        target.addEffect(new MobEffectInstance(
                TelumEffects.SHATTER,
                MobEffectInstance.INFINITE_DURATION,
                newAmplifier,
                false, // ambient
                true,  // visible
                true   // showIcon
        ));
    }
}
