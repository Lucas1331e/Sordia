package dasouza.telum.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class EchoProjectionBlock extends Block implements EntityBlock {

    public EchoProjectionBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EchoProjectionBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : (lvl, p, s, be) -> {
            if (be instanceof EchoProjectionBlockEntity projection) {
                EchoProjectionBlockEntity.tick(lvl, p, s, projection);
            }
        };
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
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EchoProjectionBlockEntity projection) {

                // SHIFT + RIGHT CLICK: Dispel/Despawn projection instantly!
                if (player.isShiftKeyDown()) {
                    projection.dispel(serverLevel, pos);
                    return InteractionResult.SUCCESS;
                }

                // NORMAL RIGHT CLICK: Access Master Echo Barrel's shared inventory!
                BlockPos masterPos = projection.getMasterPos();
                if (masterPos != null) {
                    BlockEntity masterBE = level.getBlockEntity(masterPos);
                    if (masterBE instanceof EchoBarrelBlockEntity masterBarrel) {
                        player.openMenu(masterBarrel);
                        serverLevel.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 1.2f, 1.2f);
                        return InteractionResult.SUCCESS;
                    }
                }

                // Master barrel missing or broken: Dispel with failure chime
                serverLevel.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 1.0f, 0.5f);
                projection.dispel(serverLevel, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EchoProjectionBlockEntity projection) {
                projection.dispel(serverLevel, pos);
            }
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof EchoProjectionBlockEntity projection) {
                projection.dispel(serverLevel, pos);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
