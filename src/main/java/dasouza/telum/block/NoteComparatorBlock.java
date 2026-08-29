package dasouza.telum.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class NoteComparatorBlock extends DiodeBlock {

    public static final MapCodec<NoteComparatorBlock> CODEC = simpleCodec(NoteComparatorBlock::new);
    public static final EnumProperty<NoteComparatorMode> MODE = EnumProperty.create("mode", NoteComparatorMode.class);

    public NoteComparatorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(MODE, NoteComparatorMode.STRICT));
    }

    @Override
    protected MapCodec<? extends DiodeBlock> codec() {
        return CODEC;
    }

    @Override
    protected int getDelay(BlockState state) {
        return 2;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        }

        state = state.cycle(MODE);
        float pitch = state.getValue(MODE) == NoteComparatorMode.STRICT ? 1.0f : 1.5f;
        level.playSound(player, pos, SoundEvents.COMPARATOR_CLICK, SoundSource.BLOCKS, 0.4f, pitch);
        level.setBlock(pos, state, 3);
        this.refreshOutputState(level, pos, state);
        return InteractionResult.SUCCESS;
    }

    private void refreshOutputState(Level level, BlockPos pos, BlockState state) {
        int calcSignal = this.calculateOutputSignal(level, pos, state);
        int currentSignal = state.getValue(POWERED) ? 15 : 0;
        if (calcSignal != currentSignal && !level.getBlockTicks().willTickThisTick(pos, this)) {
            level.scheduleTick(pos, this, 2);
        }
    }

    @Override
    protected boolean shouldTurnOn(Level level, BlockPos pos, BlockState state) {
        return this.calculateOutputSignal(level, pos, state) > 0;
    }

    @Override
    protected int getOutputSignal(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        if (!state.getValue(POWERED)) {
            return 0;
        }
        Direction facing = state.getValue(FACING);
        if (side == facing) {
            return this.getOutputSignal(level, pos, state);
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
        return this.getSignal(state, level, pos, side);
    }

    private int calculateOutputSignal(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);

        BlockPos frontPos = pos.relative(facing);
        BlockState frontState = level.getBlockState(frontPos);

        if (frontState.isAir()) {
            return 0;
        }

        NoteComparatorMode mode = state.getValue(MODE);

        Direction clockWise = facing.getClockWise();
        Direction counterClockWise = facing.getCounterClockWise();

        BlockPos sidePos1 = pos.relative(clockWise);
        BlockPos sidePos2 = pos.relative(counterClockWise);

        BlockState sideState1 = level.getBlockState(sidePos1);
        BlockState sideState2 = level.getBlockState(sidePos2);

        boolean match1 = compareBlocks(level, frontPos, frontState, sidePos1, sideState1, mode);
        boolean match2 = compareBlocks(level, frontPos, frontState, sidePos2, sideState2, mode);

        if (match1 || match2) {
            return 15;
        }

        return 0;
    }

    private boolean compareBlocks(Level level, BlockPos pos1, BlockState state1, BlockPos pos2, BlockState state2, NoteComparatorMode mode) {
        if (state2.isAir()) {
            return false;
        }

        if (state1.is(Blocks.NOTE_BLOCK) && state2.is(Blocks.NOTE_BLOCK)) {
            int note1 = state1.getValue(NoteBlock.NOTE);
            int note2 = state2.getValue(NoteBlock.NOTE);

            if (note1 != note2) {
                return false;
            }

            if (mode == NoteComparatorMode.STRICT) {
                NoteBlockInstrument inst1 = state1.getValue(NoteBlock.INSTRUMENT);
                NoteBlockInstrument inst2 = state2.getValue(NoteBlock.INSTRUMENT);

                if (inst1 != inst2) {
                    return false;
                }

                Block blockBelow1 = level.getBlockState(pos1.below()).getBlock();
                Block blockBelow2 = level.getBlockState(pos2.below()).getBlock();

                return blockBelow1 == blockBelow2;
            }

            return true;
        }

        return state1.getBlock() == state2.getBlock();
    }

    @Override
    protected void checkTickOnNeighbor(Level level, BlockPos pos, BlockState state) {
        if (!level.getBlockTicks().willTickThisTick(pos, this)) {
            int calcSignal = this.calculateOutputSignal(level, pos, state);
            int currentSignal = state.getValue(POWERED) ? 15 : 0;
            if (calcSignal != currentSignal) {
                level.scheduleTick(pos, this, 2);
            }
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int calcSignal = this.calculateOutputSignal(level, pos, state);
        boolean isPowered = state.getValue(POWERED);
        boolean shouldBePowered = calcSignal > 0;

        if (isPowered != shouldBePowered) {
            state = state.setValue(POWERED, shouldBePowered);
            level.setBlock(pos, state, 2);
            this.updateAllNeighbors(level, pos, state);
        }
    }

    private void updateAllNeighbors(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);

        // Back (Output recipient)
        BlockPos backPos = pos.relative(facing.getOpposite());
        Orientation backOrientation = ExperimentalRedstoneUtils.initialOrientation(level, facing.getOpposite(), Direction.UP);
        level.neighborChanged(backPos, this, backOrientation);
        level.updateNeighborsAtExceptFromFacing(backPos, this, facing, backOrientation);

        // Front (Input source)
        BlockPos frontPos = pos.relative(facing);
        Orientation frontOrientation = ExperimentalRedstoneUtils.initialOrientation(level, facing, Direction.UP);
        level.neighborChanged(frontPos, this, frontOrientation);
        level.updateNeighborsAtExceptFromFacing(frontPos, this, facing.getOpposite(), frontOrientation);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, MODE);
    }
}
