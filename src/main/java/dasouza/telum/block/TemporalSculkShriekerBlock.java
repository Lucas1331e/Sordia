package dasouza.telum.block;

import com.mojang.serialization.MapCodec;
import dasouza.telum.entity.TemporalEvokerFangs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Custom unbreakable Temporal Sculk Shrieker block.
 * Retains full vibration listening, shrieking sound/particles (vanilla), and Warden summoning,
 * while being bedrock-tier unbreakable.
 * Spawns 1 TemporalEvokerFangs under triggering player (even from a distance via vibrations)
 * with a 5-second cooldown per block.
 */
public class TemporalSculkShriekerBlock extends SculkShriekerBlock {

    public static final MapCodec<TemporalSculkShriekerBlock> CODEC = simpleCodec(TemporalSculkShriekerBlock::new);
    private static final Map<BlockPos, Long> COOLDOWN_MAP = new ConcurrentHashMap<>();

    public TemporalSculkShriekerBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MapCodec<SculkShriekerBlock> codec() {
        return (MapCodec) CODEC;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide() && entity instanceof Player player) {
            spawnTemporalFang(level, pos, player);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        checkShriekActivation(state, level, pos, oldState);
    }

    private void checkShriekActivation(BlockState state, Level level, BlockPos pos, BlockState oldState) {
        if (level.isClientSide()) return;

        boolean isShriekingNow = state.hasProperty(SHRIEKING) && state.getValue(SHRIEKING);
        boolean wasShriekingBefore = oldState.hasProperty(SHRIEKING) && oldState.getValue(SHRIEKING);

        if (isShriekingNow && !wasShriekingBefore) {
            if (level instanceof ServerLevel serverLevel) {
                // Find nearest player within 24 blocks (vibration range)
                Player targetPlayer = level.getNearestPlayer(
                        pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                        24.0D, EntitySelector.NO_SPECTATORS
                );

                if (targetPlayer != null) {
                    spawnTemporalFang(serverLevel, pos, targetPlayer);
                }
            }
        }
    }

    private void spawnTemporalFang(Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            long currentTime = serverLevel.getGameTime();
            Long lastTrigger = COOLDOWN_MAP.get(pos);

            // 100 ticks = 5 seconds cooldown per block
            if (lastTrigger != null && (currentTime - lastTrigger) < 100L) {
                return;
            }

            COOLDOWN_MAP.put(pos, currentTime);

            double pX = player.getX();
            double pY = player.getY();
            double pZ = player.getZ();
            TemporalEvokerFangs fangs = new TemporalEvokerFangs(
                    serverLevel, pX, pY, pZ, player.getYRot(), 5, player
            );
            serverLevel.addFreshEntity(fangs);
        }
    }
}
