package dasouza.telum.block;

import com.mojang.serialization.MapCodec;
import dasouza.telum.item.TelumItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * The Archeology Table block. Players place Sordia on it (offhand prioritized when empty)
 * and brush it using item hold duration.
 */
public class ArcheologyTableBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 6, 16);
    private static final MapCodec<ArcheologyTableBlock> CODEC = simpleCodec(ArcheologyTableBlock::new);

    public ArcheologyTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ArcheologyTableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, TelumBlockEntities.ARCHEOLOGY_TABLE_ENTITY, ArcheologyTableBlockEntity::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                           BlockPos pos, Player player, InteractionHand hand,
                                           BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ArcheologyTableBlockEntity table)) {
            return InteractionResult.PASS;
        }

        ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offStack = player.getItemInHand(InteractionHand.OFF_HAND);

        // --- CASE 1: TABLE IS EMPTY ---
        if (!table.hasSordia()) {
            // Check Main Hand for Sordia
            if (mainStack.is(TelumItems.PIECE_OF_SORDIA) || mainStack.is(TelumItems.DRAGON_SORDIA)) {
                if (hand == InteractionHand.MAIN_HAND) {
                    table.placeSordia(mainStack, level);
                    return InteractionResult.SUCCESS;
                }
            }

            // Check Offhand for Sordia (even if Main Hand is holding Brush!)
            if (offStack.is(TelumItems.PIECE_OF_SORDIA) || offStack.is(TelumItems.DRAGON_SORDIA)) {
                table.placeSordia(offStack, level);
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        }

        // --- CASE 2: TABLE HAS SORDIA ---
        if (stack.getItem() instanceof BrushItem) {
            player.startUsingItem(hand);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ArcheologyTableBlockEntity table)) {
            return InteractionResult.PASS;
        }

        if (table.hasSordia()) {
            if (!level.isClientSide()) {
                table.removeSordia(player);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ArcheologyTableBlockEntity table) {
            table.dropContents(level);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
