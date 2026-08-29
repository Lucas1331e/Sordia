package dasouza.telum.block;

import dasouza.telum.item.LyreItem;
import dasouza.telum.network.OpenLyreScreenPayload;
import dasouza.telum.util.PlayerSongManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EchoBarrelBlock extends Block implements EntityBlock {

    public static final int ECHO_BARREL_TUNING_MAGIC_ID = -333;

    public EchoBarrelBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EchoBarrelBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        return handleInteraction(level, pos, player, player.getMainHandItem());
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                           BlockPos pos, Player player, InteractionHand hand,
                                           BlockHitResult hitResult) {
        return handleInteraction(level, pos, player, stack);
    }

    private InteractionResult handleInteraction(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (stack.getItem() instanceof LyreItem) {
            // Require player to have learned chest_song (Canción de los Ecos)
            if (!PlayerSongManager.hasLearnedSong(player.getUUID(), "chest_song")) {
                return InteractionResult.FAIL;
            }

            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                int sourceId = Block.getId(state(pos, level));
                ServerPlayNetworking.send(serverPlayer,
                        new OpenLyreScreenPayload(
                                pos.getX(), pos.getY(), pos.getZ(),
                                sourceId, ECHO_BARREL_TUNING_MAGIC_ID
                        )
                );
            }
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MenuProvider menuProvider) {
                player.openMenu(menuProvider);
                level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private BlockState state(BlockPos pos, Level level) {
        return level.getBlockState(pos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            PlayerSongManager.removeSongByPos(pos);
            if (player instanceof ServerPlayer serverPlayer) {
                dasouza.telum.network.TelumNetworking.syncSongsToPlayer(serverPlayer);
            }
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof EchoBarrelBlockEntity barrelEntity) {
            Containers.dropContents(level, pos, barrelEntity);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
