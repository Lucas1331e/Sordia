package dasouza.telum.item;

import dasouza.telum.util.TemporalRewindTracker;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class TemporalRecallPotionItem extends Item {

    public TemporalRecallPotionItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
        super.onUseTick(level, entity, stack, count);

        // Every 4 ticks while drinking, play drinking sound and spawn ambient time sparkles around player head
        if (count % 4 == 0) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.5f, 0.9f + level.getRandom().nextFloat() * 0.2f);

            if (level instanceof ServerLevel serverLevel) {
                double px = entity.getX() + (level.getRandom().nextDouble() - 0.5) * 0.5;
                double py = entity.getEyeY() + (level.getRandom().nextDouble() - 0.5) * 0.3;
                double pz = entity.getZ() + (level.getRandom().nextDouble() - 0.5) * 0.5;

                serverLevel.sendParticles(dasouza.telum.particle.TelumParticles.CLOCK_PARTICLE, px, py, pz, 2, 0.1, 0.1, 0.1, 0.02);
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        super.finishUsingItem(stack, level, entity);

        if (entity instanceof ServerPlayer serverPlayer) {
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));

            // Execute 3-Second Time Rewind & Cure Shatter
            TemporalRewindTracker.rewind(serverPlayer);
        }

        if (entity instanceof Player player && !player.isCreative()) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
        }

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32; // Standard potion drinking speed (32 ticks = 1.6s)
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }
}
