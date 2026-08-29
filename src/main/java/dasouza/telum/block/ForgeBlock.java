package dasouza.telum.block;

import dasouza.telum.screen.ForgeScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The Telum Forge block where players combine 3 tool parts to assemble modular tools.
 * Opens a custom GUI with 3 input slots and 1 output slot.
 */
public class ForgeBlock extends Block {

    private static final Component TITLE = Component.translatable("container.telum.forge");

    public ForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            player.openMenu(createMenuProvider(level, pos));
        }
        return InteractionResult.SUCCESS;
    }

    private MenuProvider createMenuProvider(Level level, BlockPos pos) {
        return new SimpleMenuProvider(
                (syncId, playerInv, player) ->
                        new ForgeScreenHandler(syncId, playerInv, ContainerLevelAccess.create(level, pos)),
                TITLE
        );
    }
}
