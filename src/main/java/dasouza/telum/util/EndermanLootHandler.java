package dasouza.telum.util;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.tool.PartMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class EndermanLootHandler {

    private EndermanLootHandler() {}

    public static boolean hasEndermanTool(Player player) {
        if (player == null) return false;
        ItemStack mainStack = player.getMainHandItem();
        if (mainStack.getItem() instanceof AssembledToolItem) {
            AssembledToolData data = AssembledToolItem.getToolData(mainStack);
            return data != null && data.getMaterialLevel(PartMaterial.ENDERMAN) >= 1;
        }
        return false;
    }

    /**
     * Deposits the item into:
     * 1. Placed Containers in the world near player (Chests, Barrels, Shulker Boxes within 10 blocks)
     * 2. Shulker Boxes & Bundles in player inventory (Full 27-slot precision)
     * 3. Player Main Inventory
     * 4. Safe Fallback: Drops remaining items at player's feet (Never loses items!)
     */
    public static boolean depositLoot(Player player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) return true;

        Level level = player.level();
        BlockPos playerPos = player.blockPosition();

        // 1. Check Placed Containers in World near Player (Radius of 10 blocks)
        if (!level.isClientSide()) {
            int radius = 10;
            for (BlockPos targetPos : BlockPos.betweenClosed(playerPos.offset(-radius, -5, -radius), playerPos.offset(radius, 5, radius))) {
                BlockEntity be = level.getBlockEntity(targetPos);
                if (be instanceof Container container) {
                    insertIntoContainer(container, stack);
                    if (stack.isEmpty()) {
                        spawnEnderTeleportEffects(player, targetPos.immutable());
                        return true;
                    }
                }
            }
        }

        // 2. Check Shulker Boxes in Player Inventory (27 fixed slots per Shulker)
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack containerStack = player.getInventory().getItem(i);
            if (!containerStack.isEmpty() && containerStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
                ItemContainerContents contents = containerStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
                NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
                contents.copyInto(items);

                boolean modified = false;

                // Stack into existing items in shulker box
                for (int slot = 0; slot < 27; slot++) {
                    ItemStack inShulker = items.get(slot);
                    if (!inShulker.isEmpty() && ItemStack.isSameItemSameComponents(inShulker, stack)) {
                        int maxStack = Math.min(inShulker.getMaxStackSize(), stack.getMaxStackSize());
                        int space = maxStack - inShulker.getCount();
                        if (space > 0) {
                            int toAdd = Math.min(stack.getCount(), space);
                            inShulker.grow(toAdd);
                            stack.shrink(toAdd);
                            modified = true;
                            if (stack.isEmpty()) break;
                        }
                    }
                }

                // Insert into empty slots in shulker box
                if (!stack.isEmpty()) {
                    for (int slot = 0; slot < 27; slot++) {
                        ItemStack inShulker = items.get(slot);
                        if (inShulker.isEmpty()) {
                            items.set(slot, stack.copy());
                            stack.setCount(0);
                            modified = true;
                            break;
                        }
                    }
                }

                if (modified) {
                    containerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
                    if (stack.isEmpty()) {
                        spawnEnderTeleportEffects(player, playerPos);
                        return true;
                    }
                }
            }
        }

        // 3. Check Bundles in Player Inventory
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack bundleStack = player.getInventory().getItem(i);
            if (!bundleStack.isEmpty() && bundleStack.getItem() instanceof BundleItem) {
                BundleContents contents = bundleStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
                BundleContents.Mutable mutable = new BundleContents.Mutable(contents);
                int inserted = mutable.tryInsert(stack);
                if (inserted > 0) {
                    bundleStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
                    if (stack.isEmpty()) {
                        spawnEnderTeleportEffects(player, playerPos);
                        return true;
                    }
                }
            }
        }

        // 4. Deposit into Player Main Inventory
        if (!stack.isEmpty()) {
            boolean added = player.getInventory().add(stack);
            if (added || stack.isEmpty()) {
                spawnEnderTeleportEffects(player, playerPos);
                return true;
            }
        }

        // 5. Fallback Safeguard: Drop remaining items at player's feet if everything is 100% full
        if (!stack.isEmpty()) {
            player.drop(stack.copy(), false);
            stack.setCount(0);
            spawnEnderTeleportEffects(player, playerPos);
        }

        return true;
    }

    private static boolean insertIntoContainer(Container container, ItemStack stack) {
        if (container == null || stack.isEmpty()) return true;

        // 1. Stack into existing slots
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack inSlot = container.getItem(i);
            if (!inSlot.isEmpty() && ItemStack.isSameItemSameComponents(inSlot, stack)) {
                int maxStack = Math.min(container.getMaxStackSize(), inSlot.getMaxStackSize());
                int space = maxStack - inSlot.getCount();
                if (space > 0) {
                    int toAdd = Math.min(stack.getCount(), space);
                    inSlot.grow(toAdd);
                    stack.shrink(toAdd);
                    container.setChanged();
                    if (stack.isEmpty()) return true;
                }
            }
        }

        // 2. Insert into empty slots
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack inSlot = container.getItem(i);
            if (inSlot.isEmpty() && container.canPlaceItem(i, stack)) {
                int maxStack = Math.min(container.getMaxStackSize(), stack.getMaxStackSize());
                int toAdd = Math.min(stack.getCount(), maxStack);
                ItemStack copy = stack.copy();
                copy.setCount(toAdd);
                container.setItem(i, copy);
                stack.shrink(toAdd);
                container.setChanged();
                if (stack.isEmpty()) return true;
            }
        }

        return stack.isEmpty();
    }

    private static void spawnEnderTeleportEffects(Player player, BlockPos targetPos) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.PORTAL,
                    targetPos.getX() + 0.5, targetPos.getY() + 0.8, targetPos.getZ() + 0.5,
                    6, 0.2, 0.4, 0.2, 0.05);
        }
    }
}
