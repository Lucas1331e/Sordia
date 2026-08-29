package dasouza.telum.item;

import dasouza.telum.network.OpenLyreScreenPayload;
import dasouza.telum.util.PlayerSongManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

public class LyreItem extends Item {

    public LyreItem(Properties properties) {
        super(properties);
    }

    public static final int SCULK_SONG_MAGIC_ID = -999;
    public static final int ECHO_PROJECTION_SUMMON_MAGIC_ID = -777;
    public static final int RETURN_SONG_MAGIC_ID = -888;
    public static final int DAWN_SONG_MAGIC_ID = -666;
    public static final int REVEAL_SONG_MAGIC_ID = -555;
    public static final int VOID_SONG_MAGIC_ID = -444;
    public static final int ECHO_BARREL_TUNING_MAGIC_ID = -333;

    /**
     * Checks if a block can be transformed:
     * - Turtle Egg -> Sniffer Egg
     * - Oxidized/Weathered/Exposed Copper -> Unweathered Base Copper
     * Returns null if block is not transformable.
     */
    public static Block getTransformationResult(Block block) {
        if (block == Blocks.TURTLE_EGG) {
            return Blocks.SNIFFER_EGG;
        }

        // Check WeatheringCopper system (oxidized/weathered/exposed -> unweathered base)
        var first = WeatheringCopper.getFirst(block);
        if (first != block) {
            return first;
        }

        return null;
    }

    @Override
    public InteractionResult use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        if (PlayerSongManager.getLearnedSongIds(player.getUUID()).isEmpty()) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer,
                    new OpenLyreScreenPayload(
                            player.getBlockX(), player.getBlockY(), player.getBlockZ(),
                            0, 0
                    )
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.PASS;

        // 1. Shift + Right Click on Echo Barrel with Lyre: Must have learned "chest_song"
        if (state.is(dasouza.telum.block.TelumBlocks.ECHO_BARREL)) {
            if (!PlayerSongManager.hasLearnedSong(player.getUUID(), "chest_song")) {
                return InteractionResult.FAIL;
            }

            if (player.isShiftKeyDown()) {
                if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    int sourceId = Block.getId(state);
                    ServerPlayNetworking.send(serverPlayer,
                            new OpenLyreScreenPayload(
                                    pos.getX(), pos.getY(), pos.getZ(),
                                    sourceId, dasouza.telum.block.EchoBarrelBlock.ECHO_BARREL_TUNING_MAGIC_ID
                            )
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }

        // 2. Normal Click with Lyre when player has a bound Echo Barrel: Requires "chest_song"
        if (dasouza.telum.util.EchoBarrelManager.hasBoundBarrel(player.getUUID())) {
            if (!PlayerSongManager.hasLearnedSong(player.getUUID(), "chest_song")) {
                return InteractionResult.FAIL;
            }

            BlockPos projPos = pos.relative(context.getClickedFace());
            if (level.getBlockState(projPos).isAir()) {
                if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    dasouza.telum.util.EchoBarrelManager.BoundBarrel bound = dasouza.telum.util.EchoBarrelManager.getBoundBarrel(player.getUUID());
                    ServerPlayNetworking.send(serverPlayer,
                            new OpenLyreScreenPayload(
                                    projPos.getX(), projPos.getY(), projPos.getZ(),
                                    bound.encodedSongInt(), ECHO_PROJECTION_SUMMON_MAGIC_ID
                            )
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }

        Block targetBlock = state.getBlock();
        boolean isLectern = (targetBlock == Blocks.LECTERN || targetBlock == dasouza.telum.block.TelumBlocks.MARMOL_LECTERN);

        if (isLectern) {
            if (!PlayerSongManager.hasLearnedSong(player.getUUID(), "sculk_song")) {
                return InteractionResult.FAIL;
            }

            var activeZone = dasouza.telum.util.TemporalSculkZoneManager.getZoneForPlayer(player.getUUID());
            if (activeZone != null && !activeZone.lecternPos.equals(pos)) {
                return InteractionResult.FAIL;
            }

            boolean isAncientCityOrDeepDark = (targetBlock == dasouza.telum.block.TelumBlocks.MARMOL_LECTERN);
            if (!isAncientCityOrDeepDark && level instanceof ServerLevel serverLevel) {
                var registry = serverLevel.registryAccess().lookup(Registries.STRUCTURE);
                if (registry.isPresent()) {
                    var ancientCityOpt = registry.get().get(ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath("minecraft", "ancient_city")));
                    if (ancientCityOpt.isPresent()) {
                        isAncientCityOrDeepDark = serverLevel.structureManager().getStructureAt(pos, ancientCityOpt.get().value()).isValid();
                    }
                }
            }
            if (!isAncientCityOrDeepDark) {
                isAncientCityOrDeepDark = level.getBiome(pos).is(net.minecraft.world.level.biome.Biomes.DEEP_DARK);
            }

            if (isAncientCityOrDeepDark) {
                if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    int sourceId = Block.getId(targetBlock.defaultBlockState());
                    ServerPlayNetworking.send(serverPlayer,
                            new OpenLyreScreenPayload(
                                    pos.getX(), pos.getY(), pos.getZ(),
                                    sourceId, SCULK_SONG_MAGIC_ID
                            )
                    );
                }
                return InteractionResult.SUCCESS;
            }
        }

        Block resultBlock = getTransformationResult(targetBlock);
        if (resultBlock != null) {
            if (!PlayerSongManager.hasLearnedSong(player.getUUID(), "backtime_song")) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                int sourceId = Block.getId(targetBlock.defaultBlockState());
                int resultId = Block.getId(resultBlock.defaultBlockState());
                ServerPlayNetworking.send(serverPlayer,
                        new OpenLyreScreenPayload(
                                pos.getX(), pos.getY(), pos.getZ(),
                                sourceId, resultId
                        )
                );
            }
            return InteractionResult.SUCCESS;
        }

        // Must have learned at least 1 song to open general Lyre screen
        if (PlayerSongManager.getLearnedSongIds(player.getUUID()).isEmpty()) {
            return InteractionResult.FAIL;
        }

        // Fallback: Open general Lyre screen for any block
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer,
                    new OpenLyreScreenPayload(
                            pos.getX(), pos.getY(), pos.getZ(),
                            0, 0
                    )
            );
        }
        return InteractionResult.SUCCESS;
    }
}
