package dasouza.telum.util;

import dasouza.telum.Telum;
import dasouza.telum.block.TelumBlocks;
import dasouza.telum.item.TelumItems;
import dasouza.telum.item.ToolPartItem;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.tool.PartType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles brushing and breaking interactions on Suspicious Temporal Sculk blocks.
 * Intercepts produced Sculk Tool Parts, Sordia, Echo Shards, Disc Fragments, and Mending Books,
 * routing them to the active Temporal Zone buffer.
 */
public final class SculkBrushingHandler {

    private static final Map<BlockPos, Integer> BRUSH_PROGRESS = new ConcurrentHashMap<>();

    private SculkBrushingHandler() {}

    public static void initialize() {
        // Handle block break
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) -> {
            if (!level.isClientSide() && state.is(TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK)) {
                if (level instanceof ServerLevel serverLevel) {
                    ItemStack lootItem = getRandomSculkLoot(serverLevel, serverLevel.getRandom());
                    if (TemporalSculkZoneManager.bufferItem(serverLevel, pos, lootItem)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        return false;
                    }
                }
            }
            return true;
        });

        Telum.LOGGER.info("Initialized SculkBrushingHandler");
    }

    public static ItemStack getRandomSculkLoot(ServerLevel level, RandomSource rng) {
        float roll = rng.nextFloat();

        if (roll < 0.18f) {
            // 18% Sculk Tool Parts (Rare!)
            return getRandomSculkPart(rng);
        } else if (roll < 0.45f) {
            // 27% Piece of Sordia
            return new ItemStack(TelumItems.PIECE_OF_SORDIA);
        } else if (roll < 0.70f) {
            // 25% Echo Shard
            return new ItemStack(Items.ECHO_SHARD);
        } else if (roll < 0.88f) {
            // 18% Disc Fragment 5
            return new ItemStack(Items.DISC_FRAGMENT_5);
        } else {
            // 12% Mending Enchanted Book
            return createMendingBook(level);
        }
    }

    private static ItemStack createMendingBook(ServerLevel level) {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        var registryOpt = level.registryAccess().lookup(Registries.ENCHANTMENT);
        if (registryOpt.isPresent()) {
            var mendingOpt = registryOpt.get().get(Enchantments.MENDING);
            if (mendingOpt.isPresent()) {
                ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                enchantments.set(mendingOpt.get(), 1);
                book.set(DataComponents.STORED_ENCHANTMENTS, enchantments.toImmutable());
                return book;
            }
        }
        return new ItemStack(Items.ECHO_SHARD);
    }

    public static ItemStack getRandomSculkPart(RandomSource rng) {
        List<ToolPartItem> sculkParts = new ArrayList<>();

        for (PartType type : PartType.values()) {
            ToolPartItem item = TelumItems.getPartItem(type, PartMaterial.SKULK);
            if (item != null) {
                sculkParts.add(item);
            }
        }

        if (sculkParts.isEmpty()) {
            return new ItemStack(TelumItems.PIECE_OF_SORDIA);
        }

        ToolPartItem selected = sculkParts.get(rng.nextInt(sculkParts.size()));
        return new ItemStack(selected);
    }
}
