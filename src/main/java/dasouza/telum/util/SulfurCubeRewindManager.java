package dasouza.telum.util;

import dasouza.telum.Telum;
import dasouza.telum.particle.TelumParticles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Manages 4-second (~80 ticks) temporal rewinds for SulfurCube entities containing Temporal Blocks.
 */
public final class SulfurCubeRewindManager {

    public record SulfurCubeRewindEntry(
            ServerLevel level,
            SulfurCube sulfurCube,
            Vec3 originPos,
            long returnTick
    ) {}

    private static final List<SulfurCubeRewindEntry> ACTIVE_REWINDS = new ArrayList<>();

    private SulfurCubeRewindManager() {}

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<SulfurCubeRewindEntry> iter = ACTIVE_REWINDS.iterator();
            while (iter.hasNext()) {
                SulfurCubeRewindEntry entry = iter.next();
                SulfurCube cube = entry.sulfurCube();
                ServerLevel level = entry.level();

                if (!cube.isAlive() || cube.isRemoved()) {
                    iter.remove();
                    continue;
                }

                long gameTime = level.getGameTime();
                if (gameTime >= entry.returnTick()) {
                    Vec3 origin = entry.originPos();

                    // Teleport entity back to origin pos
                    cube.teleportTo(level, origin.x, origin.y, origin.z, Set.of(), cube.getYRot(), cube.getXRot(), true);

                    // Clock particles ONLY at the origin position
                    level.sendParticles(TelumParticles.CLOCK_PARTICLE,
                            origin.x, origin.y + 0.5, origin.z,
                            18, 0.2, 0.2, 0.2, 0.04);

                    // Sound effects
                    level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.NEUTRAL, 1.5f, 1.2f);
                    level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.NEUTRAL, 1.0f, 1.0f);

                    iter.remove();
                } else if (gameTime % 5 == 0) {
                    // Periodic clock particles strictly at the origin location it will return to
                    Vec3 origin = entry.originPos();
                    level.sendParticles(TelumParticles.CLOCK_PARTICLE,
                            origin.x, origin.y + 0.5, origin.z,
                            5, 0.25, 0.25, 0.25, 0.02);
                }
            }
        });

        Telum.LOGGER.info("Initialized SulfurCubeRewindManager");
    }

    public static void triggerRewind(ServerLevel level, SulfurCube cube) {
        // Prevent duplicate rewind entries for the same entity
        for (SulfurCubeRewindEntry entry : ACTIVE_REWINDS) {
            if (entry.sulfurCube() == cube) {
                return;
            }
        }

        Vec3 originPos = cube.position();
        long returnTick = level.getGameTime() + 80L; // 4 seconds = 80 ticks

        ACTIVE_REWINDS.add(new SulfurCubeRewindEntry(level, cube, originPos, returnTick));

        // Initial feedback particles
        level.sendParticles(TelumParticles.CLOCK_PARTICLE,
                originPos.x, originPos.y + 0.5, originPos.z,
                15, 0.3, 0.3, 0.3, 0.05);
    }
}
