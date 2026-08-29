package dasouza.telum.item;

import dasouza.telum.network.TelumNetworking;
import dasouza.telum.util.PlayerSongManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Item representing a learnable song sheet/scroll for the Lyre of Time.
 * Right-clicking plays the melody notes first, followed by triumph sound effects and particles.
 * Item display names are highlighted in yellow.
 * No chat messages are displayed.
 */
public class SongSheetItem extends Item {

    private static final float[] NOTE_PITCHES = {0.5f, 0.667f, 0.75f, 0.888f, 1.0f};

    private final String songId;
    private final String songTitle;

    public SongSheetItem(Properties properties, String songId, String songTitle) {
        super(properties.stacksTo(1));
        this.songId = songId;
        this.songTitle = songTitle;
    }

    public String getSongId() {
        return songId;
    }

    public String getSongTitle() {
        return songTitle;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            boolean alreadyLearned = PlayerSongManager.hasLearnedSong(player.getUUID(), songId);

            if (alreadyLearned) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8f, 1.0f);
                return InteractionResult.FAIL;
            }

            // Learn the song!
            PlayerSongManager.learnSong(player.getUUID(), songId);
            stack.shrink(1);

            // Play the melody notes first, followed by triumph sounds and particles AFTER notes complete
            playSongMelodyAndTriumph(level, player, songId);

            if (player instanceof ServerPlayer serverPlayer) {
                TelumNetworking.syncSongsToPlayer(serverPlayer);
                if (PlayerSongManager.hasLearnedAllSongs(serverPlayer.getUUID())) {
                    dasouza.telum.advancement.TelumAdvancements.grantAdvancement(serverPlayer, dasouza.telum.Telum.id("all_songs"));
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static int[] getSongNotes(String songId) {
        return switch (songId) {
            case "backtime_song" -> new int[]{2, 1, 0, 2, 1, 0};
            case "chest_song" -> new int[]{0, 1, 2, 0, 1, 2};
            case "bed_song" -> new int[]{1, 2, 3, 1, 2, 3};
            case "sculk_song" -> new int[]{0, 4, 3, 2, 1, 0};
            case "dawn_song" -> new int[]{4, 3, 4, 3, 0, 1};
            case "reveal_song" -> new int[]{3, 1, 3, 4, 0, 2};
            case "void_song" -> new int[]{4, 1, 0, 3, 4, 2};
            default -> new int[]{0, 1, 2, 3, 4};
        };
    }

    private static void playSongMelodyAndTriumph(Level level, Player player, String songId) {
        int[] notes = getSongNotes(songId);
        new Thread(() -> {
            try {
                for (int noteIdx : notes) {
                    float pitch = NOTE_PITCHES[noteIdx % 5];
                    level.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.NOTE_BLOCK_HARP.value(), SoundSource.PLAYERS, 1.0f, pitch);
                    Thread.sleep(160);
                }

                Thread.sleep(100);

                // Triumph sound and particles AFTER notes finish!
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.5f);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.2f);

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.NOTE,
                            player.getX(), player.getY() + 1.2, player.getZ(),
                            12, 0.3, 0.3, 0.3, 0.1);
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            player.getX(), player.getY() + 1.2, player.getZ(),
                            8, 0.2, 0.2, 0.2, 0.05);
                }
            } catch (Exception ignored) {}
        }).start();
    }
}
