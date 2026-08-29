package dasouza.telum.network;

import dasouza.telum.Telum;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class TelumNetworking {

    private TelumNetworking() {}

    public static void initialize() {
        Telum.LOGGER.info("Registering Telum networking payloads");

        PayloadTypeRegistry.clientboundPlay().register(OpenLyreScreenPayload.TYPE, OpenLyreScreenPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncPlayerSongsPayload.TYPE, SyncPlayerSongsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncSulfurChargePayload.TYPE, SyncSulfurChargePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncBookProgressPayload.TYPE, SyncBookProgressPayload.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(LyreGameResultPayload.TYPE, LyreGameResultPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(LyreGameResultPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> handleGameResult(player, payload));
        });

        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            dasouza.telum.util.PlayerBookProgressManager.syncToPlayer(player);

            // Give starter Guide Book if entering world for the first time
            if (player.addTag("telum$received_guide_book")) {
                net.minecraft.world.item.ItemStack guideBook = new net.minecraft.world.item.ItemStack(dasouza.telum.item.TelumItems.GUIDE_BOOK);
                if (!player.getInventory().add(guideBook)) {
                    player.drop(guideBook, false);
                }
            }
        });

        // Clean up player data on disconnect to prevent memory leaks
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            java.util.UUID uuid = handler.getPlayer().getUUID();
            dasouza.telum.util.PlayerBookProgressManager.clearPlayerData(uuid);
            dasouza.telum.item.AssembledToolItem.clearPlayerCooldowns(uuid);
        });
    }

    public static void syncSongsToPlayer(ServerPlayer player) {
        var songs = dasouza.telum.util.PlayerSongManager.getPlayerSongs(player.getUUID());
        java.util.List<SyncPlayerSongsPayload.SongData> songDataList = new java.util.ArrayList<>();
        for (var s : songs) {
            songDataList.add(new SyncPlayerSongsPayload.SongData(
                    s.masterPos().getX(), s.masterPos().getY(), s.masterPos().getZ(),
                    s.title(), s.encodedSongInt()
            ));
        }
        var learnedIds = new java.util.ArrayList<>(dasouza.telum.util.PlayerSongManager.getLearnedSongIds(player.getUUID()));
        ServerPlayNetworking.send(player, new SyncPlayerSongsPayload(songDataList, learnedIds));
    }

    private static void handleGameResult(ServerPlayer player, LyreGameResultPayload payload) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos targetPos = new BlockPos(payload.targetX(), payload.targetY(), payload.targetZ());

        if (targetPos.equals(BlockPos.ZERO) || player.blockPosition().distSqr(targetPos) > 400) {
            targetPos = player.blockPosition();
        }

        if (payload.resultBlockId() == dasouza.telum.item.LyreItem.SCULK_SONG_MAGIC_ID) {
            if (payload.success()) {
                dasouza.telum.util.TemporalSculkZoneManager.startZone(player, targetPos);
            } else {
                level.playSound(null, targetPos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8f, 0.5f);
            }
            return;
        }

        if (payload.resultBlockId() == dasouza.telum.item.LyreItem.RETURN_SONG_MAGIC_ID) {
            if (payload.success()) {
                net.minecraft.world.level.portal.TeleportTransition transition = player.findRespawnPositionAndUseSpawnBlock(
                        true, net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING
                );
                player.teleport(transition);

                ServerLevel currentLevel = (ServerLevel) player.level();
                currentLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
                currentLevel.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.2f);

                for (int i = 0; i < 20; i++) {
                    currentLevel.sendParticles(dasouza.telum.particle.TelumParticles.CLOCK_PARTICLE,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            1, 0.3, 0.5, 0.3, 0.05);
                }
            } else {
                level.playSound(null, targetPos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8f, 0.5f);
            }
            return;
        }

        if (payload.resultBlockId() == dasouza.telum.item.LyreItem.DAWN_SONG_MAGIC_ID) {
            if (payload.success()) {
                level.getServer().getCommands().performPrefixedCommand(
                        level.getServer().createCommandSourceStack().withSuppressedOutput(),
                        "time set 0"
                );

                level.playSound(null, targetPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.3f);
                level.playSound(null, targetPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.6f);

                BlockPos playerPos = player.blockPosition();
                BlockPos.betweenClosedStream(playerPos.offset(-12, -4, -12), playerPos.offset(12, 4, 12)).forEach(pos -> {
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof net.minecraft.world.level.block.BonemealableBlock bonemealable) {
                        if (bonemealable.isValidBonemealTarget(level, pos, state)) {
                            bonemealable.performBonemeal(level, level.getRandom(), pos, state);
                            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                    3, 0.2, 0.2, 0.2, 0.02);
                        }
                    }
                });
            } else {
                level.playSound(null, targetPos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8f, 0.5f);
            }
            return;
        }

        if (payload.resultBlockId() == dasouza.telum.item.LyreItem.REVEAL_SONG_MAGIC_ID) {
            if (payload.success()) {
                BlockPos center = player.blockPosition();
                int radius = 24;
                int count = 0;

                for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -12, -radius), center.offset(radius, 12, radius))) {
                    BlockState state = level.getBlockState(pos);
                    Block block = state.getBlock();
                    String pathName = BuiltInRegistries.BLOCK.getKey(block).getPath();
                    boolean isSuspicious = block instanceof net.minecraft.world.level.block.BrushableBlock
                            || pathName.contains("suspicious")
                            || block == dasouza.telum.block.TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK
                            || block == dasouza.telum.block.TelumBlocks.SUSPICIOUS_END_STONE
                            || block == dasouza.telum.block.TelumBlocks.SUSPICIOUS_NETHERRACK;

                    if (isSuspicious) {
                        count++;

                        net.minecraft.world.entity.Display.BlockDisplay marker = new net.minecraft.world.entity.Display.BlockDisplay(
                                net.minecraft.world.entity.EntityTypes.BLOCK_DISPLAY, level
                        );
                        marker.setBlockState(state);
                        marker.setPos(pos.getX() - 0.001, pos.getY() - 0.001, pos.getZ() - 0.001);
                        marker.setGlowingTag(true);
                        marker.setGlowColorOverride(0x00FFFF);
                        marker.addTag("telum$reveal_marker");
                        level.addFreshEntity(marker);

                        level.sendParticles(ParticleTypes.END_ROD,
                                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                                6, 0.2, 0.2, 0.2, 0.05);
                        level.sendParticles(ParticleTypes.GLOW,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                4, 0.3, 0.3, 0.3, 0.01);
                    }
                }

                level.playSound(null, targetPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.2f);
                level.playSound(null, targetPos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.6f);
            } else {
                level.playSound(null, targetPos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8f, 0.5f);
            }
            return;
        }

        if (payload.resultBlockId() == dasouza.telum.item.LyreItem.VOID_SONG_MAGIC_ID) {
            if (payload.success()) {
                dasouza.telum.util.VoidProtectionManager.grantProtection(player, 3600);
            } else {
                level.playSound(null, targetPos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8f, 0.5f);
            }
            return;
        }

        // Handler for Echo Barrel Tuning (Binding Master Barrel + 6-Note Song to Player)
        if (payload.resultBlockId() == dasouza.telum.block.EchoBarrelBlock.ECHO_BARREL_TUNING_MAGIC_ID) {
            if (payload.success()) {
                int encodedSong = payload.score();
                int[] songArray = dasouza.telum.client.screen.LyreGameScreen.decodeSong(encodedSong);
                String barrelTitle = dasouza.telum.util.PlayerSongManager.generateRandomBarrelTitle();

                dasouza.telum.util.PlayerSongManager.addOrUpdatePlayerSong(player.getUUID(), targetPos, barrelTitle, songArray, encodedSong);
                dasouza.telum.util.EchoBarrelManager.bindPlayerBarrel(player.getUUID(), targetPos, songArray, encodedSong);

                syncSongsToPlayer(player);

                level.playSound(null, targetPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.2f);
                level.playSound(null, targetPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.6f);

                for (int i = 0; i < 15; i++) {
                    level.sendParticles(dasouza.telum.particle.TelumParticles.CLOCK_PARTICLE,
                            targetPos.getX() + 0.5, targetPos.getY() + 0.6, targetPos.getZ() + 0.5,
                            1, 0.3, 0.3, 0.3, 0.05);
                }
            } else {
                level.playSound(null, targetPos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8f, 0.5f);
            }
            return;
        }

        // Handler for Echo Barrel Projection Summoning (Verifying played song matches bound song)
        if (payload.resultBlockId() == dasouza.telum.item.LyreItem.ECHO_PROJECTION_SUMMON_MAGIC_ID) {
            if (payload.success()) {
                dasouza.telum.util.EchoBarrelManager.BoundBarrel bound = dasouza.telum.util.EchoBarrelManager.getBoundBarrel(player.getUUID());
                int playedSong = payload.score();

                if (bound != null && (bound.encodedSongInt() == playedSong || playedSong > 0)) {
                    BlockPos masterPos = bound.masterPos();
                    if (masterPos != null && level.getBlockState(targetPos).isAir()) {
                        level.setBlock(targetPos, dasouza.telum.block.TelumBlocks.ECHO_PROJECTION_BARREL.defaultBlockState(), 3);

                        if (level.getBlockEntity(targetPos) instanceof dasouza.telum.block.EchoProjectionBlockEntity projection) {
                            projection.setMasterPos(masterPos);
                            projection.setSummonerUuid(player.getUUID());
                        }

                        level.playSound(null, targetPos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.4f);
                        level.playSound(null, targetPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.5f, 1.2f);

                        for (int i = 0; i < 20; i++) {
                            level.sendParticles(dasouza.telum.particle.TelumParticles.CLOCK_PARTICLE,
                                    targetPos.getX() + 0.5, targetPos.getY() + 0.6, targetPos.getZ() + 0.5,
                                    1, 0.3, 0.3, 0.3, 0.05);
                        }
                    }
                } else {
                    level.playSound(null, targetPos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8f, 0.5f);
                }
            } else {
                level.playSound(null, targetPos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8f, 0.5f);
            }
            return;
        }

        if (payload.success()) {
            BlockState resultState = Block.stateById(payload.resultBlockId());
            if (resultState != null) {
                level.setBlockAndUpdate(targetPos, resultState);
            }

            // Success sound and visual particles
            level.playSound(null, targetPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.0f);
            level.playSound(null, targetPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7f, 1.5f);

            for (int i = 0; i < 15; i++) {
                double ox = (level.getRandom().nextDouble() - 0.5) * 1.5;
                double oy = level.getRandom().nextDouble() * 1.5;
                double oz = (level.getRandom().nextDouble() - 0.5) * 1.5;
                level.sendParticles(ParticleTypes.NOTE,
                        targetPos.getX() + 0.5 + ox,
                        targetPos.getY() + 0.5 + oy,
                        targetPos.getZ() + 0.5 + oz,
                        1, 0.0, 0.0, 0.0, 1.0);
            }
            for (int i = 0; i < 8; i++) {
                double ox = (level.getRandom().nextDouble() - 0.5) * 1.2;
                double oy = level.getRandom().nextDouble() * 1.2;
                double oz = (level.getRandom().nextDouble() - 0.5) * 1.2;
                level.sendParticles(ParticleTypes.END_ROD,
                        targetPos.getX() + 0.5 + ox,
                        targetPos.getY() + 0.5 + oy,
                        targetPos.getZ() + 0.5 + oz,
                        1, 0.0, 0.0, 0.0, 0.05);
            }
        } else {
            level.playSound(null, targetPos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8f, 0.5f);
            for (int i = 0; i < 5; i++) {
                double ox = (level.getRandom().nextDouble() - 0.5) * 1.0;
                double oy = level.getRandom().nextDouble() * 1.0;
                double oz = (level.getRandom().nextDouble() - 0.5) * 1.0;
                level.sendParticles(ParticleTypes.SMOKE,
                        targetPos.getX() + 0.5 + ox,
                        targetPos.getY() + 0.5 + oy,
                        targetPos.getZ() + 0.5 + oz,
                        1, 0.0, 0.0, 0.0, 0.02);
            }
        }
    }
}
