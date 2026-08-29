package dasouza.telum.block;

import dasouza.telum.particle.TelumParticles;
import dasouza.telum.util.SculkBrushingHandler;
import dasouza.telum.util.TemporalSculkZoneManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Custom Temporal Barrel block for containing loot in temporal structures.
 * Upon player interaction, spawns clock & end rod particles with sounds,
 * buffers loot to the active Sculk Temporal Zone lectern (or drops it),
 * and disappears.
 */
public class TemporalBarrelBlock extends Block {

    public TemporalBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        return handleInteraction(level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                           BlockPos pos, Player player, InteractionHand hand,
                                           BlockHitResult hitResult) {
        return handleInteraction(level, pos, player);
    }

    private InteractionResult handleInteraction(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            ItemStack loot = SculkBrushingHandler.getRandomSculkLoot(serverLevel, serverLevel.getRandom());

            // Buffer item to lectern if inside active Temporal Sculk Zone
            if (!TemporalSculkZoneManager.bufferItem(serverLevel, pos, loot)) {
                // Outside zone: drop item entity at block location
                ItemEntity itemEntity = new ItemEntity(serverLevel,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, loot);
                serverLevel.addFreshEntity(itemEntity);
            }

            // Spawn Clock & End Rod particles
            serverLevel.sendParticles(TelumParticles.CLOCK_PARTICLE,
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    25, 0.3, 0.3, 0.3, 0.05);

            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                    15, 0.3, 0.3, 0.3, 0.05);

            // Play chimes & opening sounds
            serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5f, 1.2f);
            serverLevel.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.2f, 1.0f);
            serverLevel.playSound(null, pos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, 1.0f, 1.4f);

            // Emit container open noise vibration to trigger nearby Sculk Sensors / Temporal Shriekers!
            serverLevel.gameEvent(player, net.minecraft.world.level.gameevent.GameEvent.CONTAINER_OPEN, pos);

            // Block disappears!
            serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
        return InteractionResult.SUCCESS;
    }
}
