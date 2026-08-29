package dasouza.telum.block;

import dasouza.telum.particle.TelumParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public class EchoProjectionBlockEntity extends BlockEntity {

    private BlockPos masterPos;
    private UUID summonerUuid;

    public EchoProjectionBlockEntity(BlockPos pos, BlockState state) {
        super(TelumBlockEntities.ECHO_PROJECTION_ENTITY, pos, state);
    }

    public void setMasterPos(BlockPos masterPos) {
        this.masterPos = masterPos;
        setChanged();
    }

    public BlockPos getMasterPos() {
        return masterPos;
    }

    public void setSummonerUuid(UUID summonerUuid) {
        this.summonerUuid = summonerUuid;
        setChanged();
    }

    public UUID getSummonerUuid() {
        return summonerUuid;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (masterPos != null) {
            output.putInt("MasterX", masterPos.getX());
            output.putInt("MasterY", masterPos.getY());
            output.putInt("MasterZ", masterPos.getZ());
        }
        if (summonerUuid != null) {
            output.putString("SummonerUuid", summonerUuid.toString());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        if (input.getInt("MasterX").isPresent() && input.getInt("MasterY").isPresent() && input.getInt("MasterZ").isPresent()) {
            this.masterPos = new BlockPos(
                    input.getIntOr("MasterX", 0),
                    input.getIntOr("MasterY", 0),
                    input.getIntOr("MasterZ", 0)
            );
        }
        input.getString("SummonerUuid").ifPresent(s -> {
            try {
                this.summonerUuid = UUID.fromString(s);
            } catch (Exception ignored) {}
        });
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EchoProjectionBlockEntity be) {
        if (level.isClientSide()) return;

        if (level.getGameTime() % 10 == 0 && be.summonerUuid != null && level instanceof ServerLevel serverLevel) {
            Player player = serverLevel.getPlayerByUUID(be.summonerUuid);

            if (player == null || player.level() != level || player.blockPosition().distSqr(pos) > 100) {
                be.dispel(serverLevel, pos);
            }
        }
    }

    public void dispel(ServerLevel serverLevel, BlockPos pos) {
        serverLevel.sendParticles(TelumParticles.CLOCK_PARTICLE,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                20, 0.3, 0.3, 0.3, 0.05);

        serverLevel.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                12, 0.3, 0.3, 0.3, 0.05);

        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2f, 1.4f);
        serverLevel.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.8f, 1.2f);

        serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
}
