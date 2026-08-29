package dasouza.telum.block;

import com.mojang.serialization.MapCodec;
import dasouza.telum.util.PlayerSongManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Custom unbreakable Marble Lectern block.
 * Has the exact shape of a lectern, can face 4 directions, cannot take books,
 * and requires the player to have learned sculk_song to be used.
 */
public class MarmolLecternBlock extends HorizontalDirectionalBlock {

    public static final MapCodec<MarmolLecternBlock> CODEC = simpleCodec(MarmolLecternBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public static final VoxelShape SHAPE_BASE = box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    public static final VoxelShape SHAPE_POST = box(4.0D, 2.0D, 4.0D, 12.0D, 14.0D, 12.0D);
    public static final VoxelShape SHAPE_TOP = box(0.0D, 10.0D, 0.0D, 16.0D, 15.0D, 16.0D);
    public static final VoxelShape SHAPE = Shapes.or(SHAPE_BASE, SHAPE_POST, SHAPE_TOP);

    public MarmolLecternBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(dasouza.telum.item.TelumItems.LYRE)) {
            return InteractionResult.PASS;
        }
        if (!PlayerSongManager.hasLearnedSong(player.getUUID(), "sculk_song")) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!PlayerSongManager.hasLearnedSong(player.getUUID(), "sculk_song")) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }
}
