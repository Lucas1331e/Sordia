package dasouza.telum.block;

import dasouza.telum.item.TelumItems;
import dasouza.telum.item.ToolPartItem;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.tool.PartType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Map;

/**
 * Block entity for the Archeology Table.
 * Stores the placed Sordia item, cleaning progress, and pre-determined tool part.
 * Material selection follows a weighted rarity distribution (Wood = common, Netherite = rarest).
 * Particle sequence (3 particles, 1 per second) gives progressive clues about the material quality.
 */
public class ArcheologyTableBlockEntity extends BlockEntity {

    private static final int TICKS_PER_LEVEL = 20; // 1.0 second per level (20 ticks)
    private static final int TOTAL_LEVELS = 3;     // 3 levels = 3 seconds total (60 ticks)
    private static final int SOUND_INTERVAL = 5;    // Soft brush sound every 5 ticks

    // Particle Colors (Hex)
    private static final int COLOR_BLACK  = 0x222222; // Negra
    private static final int COLOR_GRAY   = 0x888888; // Gris
    private static final int COLOR_YELLOW = 0xFFD700; // Amarilla
    private static final int COLOR_CYAN   = 0x33CCFF; // Celeste
    private static final int COLOR_GREEN  = 0x33FF33; // Verde

    // Material Rarity Weights (Wood = most common, Skulk & Prismarine = ultra rarest)
    private static final Map<PartMaterial, Integer> MATERIAL_WEIGHTS = Map.ofEntries(
            Map.entry(PartMaterial.WOOD,       40),
            Map.entry(PartMaterial.STONE,      30),
            Map.entry(PartMaterial.COPPER,     25),
            Map.entry(PartMaterial.IRON,       20),
            Map.entry(PartMaterial.GOLD,       10),
            Map.entry(PartMaterial.DIAMOND,    5),
            Map.entry(PartMaterial.NETHERITE,  3),
            Map.entry(PartMaterial.PRISMARINE, 1),
            Map.entry(PartMaterial.SKULK,      1),
            Map.entry(PartMaterial.SULFUR,     8),
            Map.entry(PartMaterial.WIND,       0),
            Map.entry(PartMaterial.BLAZE,      0),
            Map.entry(PartMaterial.SPIDER,     0),
            Map.entry(PartMaterial.SKELETON,   0),
            Map.entry(PartMaterial.ZOMBIE,     0),
            Map.entry(PartMaterial.CREEPER,    0),
            Map.entry(PartMaterial.ENDERMAN,   0)
    );

    private ItemStack heldItem = ItemStack.EMPTY;
    private int brushStrokes = 0;
    private int activeBrushTicks = 0;

    // Pre-determined at placement time so the reveal & particles are consistent
    private PartType revealedPartType = null;
    private PartMaterial revealedMaterial = null;

    // The 3 clue particle colors for levels 1, 2, and 3
    private int[] particleColors = new int[]{COLOR_BLACK, COLOR_BLACK, COLOR_BLACK};

    public ArcheologyTableBlockEntity(BlockPos pos, BlockState state) {
        super(TelumBlockEntities.ARCHEOLOGY_TABLE_ENTITY, pos, state);
    }

    public boolean hasSordia() {
        return !heldItem.isEmpty();
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public int getBrushStrokes() {
        return brushStrokes;
    }

    public boolean placeSordia(ItemStack sordiaStack, Level level) {
        if (hasSordia()) return false;

        heldItem = sordiaStack.split(1);
        brushStrokes = 0;
        activeBrushTicks = 0;

        if (!level.isClientSide()) {
            RandomSource rng = level.getRandom();

            // 1. Pick random PartType (uniform)
            PartType[] allTypes = PartType.values();
            revealedPartType = allTypes[rng.nextInt(allTypes.length)];

            // 2. Pick random PartMaterial (weighted rarity)
            if (heldItem.is(TelumItems.DRAGON_SORDIA)) {
                revealedMaterial = dasouza.telum.item.DragonSordiaItem.selectWeightedDragonMaterial(rng);
            } else {
                revealedMaterial = selectWeightedMaterial(rng);
            }

            // 3. Generate 3 clue particle colors based on selected material
            particleColors = generateParticleClues(revealedMaterial, rng);

            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        return true;
    }

    private static PartMaterial selectWeightedMaterial(RandomSource rng) {
        int totalWeight = 0;
        for (int w : MATERIAL_WEIGHTS.values()) {
            totalWeight += w;
        }

        int roll = rng.nextInt(totalWeight);
        int current = 0;
        for (Map.Entry<PartMaterial, Integer> entry : MATERIAL_WEIGHTS.entrySet()) {
            current += entry.getValue();
            if (roll < current) {
                return entry.getKey();
            }
        }
        return PartMaterial.WOOD;
    }

    private static int[] generateParticleClues(PartMaterial material, RandomSource rng) {
        if (material == null) return new int[]{COLOR_BLACK, COLOR_BLACK, COLOR_BLACK};

        return switch (material) {
            case WOOD -> {
                // 3 negras (60%) OR 2 negras y 1 gris (40%)
                if (rng.nextFloat() < 0.60f) {
                    yield new int[]{COLOR_BLACK, COLOR_BLACK, COLOR_BLACK};
                } else {
                    yield new int[]{COLOR_BLACK, COLOR_BLACK, COLOR_GRAY};
                }
            }
            case STONE -> {
                // 2 negras y 1 gris (50%) OR 2 grises y 1 negra (50%)
                if (rng.nextFloat() < 0.50f) {
                    yield new int[]{COLOR_BLACK, COLOR_BLACK, COLOR_GRAY};
                } else {
                    yield new int[]{COLOR_GRAY, COLOR_GRAY, COLOR_BLACK};
                }
            }
            case COPPER -> {
                // 2 grises y 1 negra
                yield new int[]{COLOR_GRAY, COLOR_GRAY, COLOR_BLACK};
            }
            case PRISMARINE -> {
                // 2 celestes y 1 gris
                yield new int[]{COLOR_CYAN, COLOR_CYAN, COLOR_GRAY};
            }
            case SKULK -> {
                // 2 celestes y 1 negra
                yield new int[]{COLOR_CYAN, COLOR_CYAN, COLOR_BLACK};
            }
            case IRON -> {
                // 3 grises (60%) OR 2 grises y 1 amarilla (40%)
                if (rng.nextFloat() < 0.60f) {
                    yield new int[]{COLOR_GRAY, COLOR_GRAY, COLOR_GRAY};
                } else {
                    yield new int[]{COLOR_GRAY, COLOR_GRAY, COLOR_YELLOW};
                }
            }
            case GOLD -> {
                // 2 grises y 1 amarilla (20% - low chance)
                // 2 amarillas y 1 gris (50%)
                // 3 amarillas (30% - good chance)
                float roll = rng.nextFloat();
                if (roll < 0.20f) {
                    yield new int[]{COLOR_GRAY, COLOR_GRAY, COLOR_YELLOW};
                } else if (roll < 0.70f) {
                    yield new int[]{COLOR_YELLOW, COLOR_YELLOW, COLOR_GRAY};
                } else {
                    yield new int[]{COLOR_YELLOW, COLOR_YELLOW, COLOR_YELLOW};
                }
            }
            case DIAMOND -> {
                // 2 amarillas y 1 gris (15% - low chance)
                // 3 amarillas (25% - good chance hint)
                // 2 celestes o mas (35%)
                // 1 verde (25% - guaranteed diamond hint)
                float roll = rng.nextFloat();
                if (roll < 0.15f) {
                    yield new int[]{COLOR_YELLOW, COLOR_YELLOW, COLOR_GRAY};
                } else if (roll < 0.40f) {
                    yield new int[]{COLOR_YELLOW, COLOR_YELLOW, COLOR_YELLOW};
                } else if (roll < 0.75f) {
                    yield new int[]{COLOR_CYAN, COLOR_CYAN, COLOR_CYAN};
                } else {
                    yield new int[]{COLOR_GREEN, COLOR_GRAY, COLOR_GRAY};
                }
            }
            case NETHERITE -> {
                // 3 verdes (guaranteed netherite)
                yield new int[]{COLOR_GREEN, COLOR_GREEN, COLOR_GREEN};
            }
            case WIND -> {
                // 2 celestes y 1 amarilla
                yield new int[]{COLOR_CYAN, COLOR_CYAN, COLOR_YELLOW};
            }
            case BLAZE -> {
                // 2 amarillas y 1 roja
                yield new int[]{COLOR_YELLOW, COLOR_YELLOW, 0xFF4500};
            }
            case SPIDER -> new int[]{COLOR_BLACK, COLOR_BLACK, 0x6B1A24};
            case SKELETON -> new int[]{COLOR_GRAY, COLOR_GRAY, COLOR_GRAY};
            case ZOMBIE -> new int[]{COLOR_BLACK, COLOR_BLACK, 0x486B38};
            case CREEPER -> new int[]{COLOR_GREEN, COLOR_GREEN, COLOR_BLACK};
            case ENDERMAN -> new int[]{COLOR_CYAN, COLOR_CYAN, 0x160C26};
            case SULFUR -> new int[]{COLOR_YELLOW, COLOR_YELLOW, COLOR_YELLOW};
            case AMETHYST -> new int[]{0xC067F8, 0xC067F8, COLOR_GRAY};
            case GREED -> new int[]{COLOR_YELLOW, COLOR_YELLOW, 0xFFD700};
            case EMERALD -> new int[]{COLOR_GREEN, COLOR_GREEN, 0x55FF55};
            default -> new int[]{COLOR_GRAY, COLOR_GRAY, COLOR_GRAY};
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ArcheologyTableBlockEntity table) {
        if (!table.hasSordia()) {
            table.activeBrushTicks = 0;
            return;
        }

        // Find a nearby player actively holding/using a Brush AND looking at THIS table
        Player brushingPlayer = null;
        for (Player player : level.players()) {
            if (player.isUsingItem() && player.getUseItem().getItem() instanceof BrushItem) {
                if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 25.0) {
                    HitResult hit = player.pick(6.0D, 0.0F, false);
                    if (hit.getType() == HitResult.Type.BLOCK && ((BlockHitResult) hit).getBlockPos().equals(pos)) {
                        brushingPlayer = player;
                        break;
                    }
                }
            }
        }

        if (brushingPlayer != null) {
            table.activeBrushTicks++;

            // Soft brush sound & durability wear every 5 ticks (4 times per second)
            if (table.activeBrushTicks % SOUND_INTERVAL == 0) {
                level.playSound(null, pos, SoundEvents.BRUSH_GENERIC, SoundSource.BLOCKS, 0.8f, 0.9f + ((table.activeBrushTicks / (float) TICKS_PER_LEVEL) * 0.15f));
                if (!level.isClientSide()) {
                    ItemStack brushStack = brushingPlayer.getUseItem();
                    brushStack.hurtAndBreak(1, brushingPlayer, brushingPlayer.getEquipmentSlotForItem(brushStack));
                }
            }

            // Advance level every 20 ticks (1 second per level)
            if (table.activeBrushTicks % TICKS_PER_LEVEL == 0) {
                table.brushStrokes++;

                if (!level.isClientSide()) {
                    table.spawnProgressParticle(level, table.brushStrokes);
                }

                if (table.brushStrokes >= TOTAL_LEVELS) {
                    table.completeCleaning(level, brushingPlayer);
                    table.activeBrushTicks = 0;
                } else {
                    table.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        } else {
            table.activeBrushTicks = 0;
        }
    }

    public void removeSordia(Player player) {
        if (!hasSordia()) return;

        if (!player.getInventory().add(heldItem.copy())) {
            Level level = player.level();
            ItemEntity entity = new ItemEntity(level,
                    player.getX(), player.getY() + 0.5, player.getZ(), heldItem.copy());
            level.addFreshEntity(entity);
        }

        resetState();
        Level level = player.level();
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    private void completeCleaning(Level level, Player player) {
        if (revealedPartType != null && revealedMaterial != null) {
            ToolPartItem partItem = TelumItems.getPartItem(revealedPartType, revealedMaterial);
            if (partItem != null) {
                ItemStack revealedStack = new ItemStack(partItem);
                if (!player.getInventory().add(revealedStack)) {
                    ItemEntity entity = new ItemEntity(level,
                            player.getX(), player.getY() + 0.5, player.getZ(), revealedStack);
                    level.addFreshEntity(entity);
                }
            }
        }

        if (revealedMaterial == PartMaterial.SKULK && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_CHARGE_POP,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5,
                    30, 0.3, 0.3, 0.3, 0.05);
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_SOUL,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5,
                    15, 0.2, 0.2, 0.2, 0.02);
            level.playSound(null, worldPosition, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, 1.2f, 1.0f);
        } else {
            level.playSound(null, worldPosition, SoundEvents.BRUSH_SAND_COMPLETED, SoundSource.BLOCKS, 1.0f, 1.2f);
        }

        resetState();
        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);

        // Seamless continuous loop: cut old brush session, place new Sordia, restart brush session!
        if (player != null) {
            player.stopUsingItem();

            ItemStack offStack = player.getItemInHand(InteractionHand.OFF_HAND);
            ItemStack mainStack = player.getItemInHand(InteractionHand.MAIN_HAND);

            boolean placedNew = false;
            if (offStack.is(TelumItems.PIECE_OF_SORDIA) || offStack.is(TelumItems.DRAGON_SORDIA)) {
                placedNew = placeSordia(offStack, level);
            } else if (mainStack.is(TelumItems.PIECE_OF_SORDIA) || mainStack.is(TelumItems.DRAGON_SORDIA)) {
                placedNew = placeSordia(mainStack, level);
            }

            if (placedNew) {
                if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BrushItem) {
                    player.startUsingItem(InteractionHand.MAIN_HAND);
                } else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof BrushItem) {
                    player.startUsingItem(InteractionHand.OFF_HAND);
                }
            }
        }
    }

    private void spawnProgressParticle(Level level, int stage) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        double bx = worldPosition.getX();
        double by = worldPosition.getY();
        double bz = worldPosition.getZ();

        double px, py, pz;
        switch (stage) {
            case 1 -> { // Left edge
                px = bx - 0.1;
                py = by + 1.1;
                pz = bz + 0.5;
            }
            case 2 -> { // Front edge
                px = bx + 0.5;
                py = by + 1.1;
                pz = bz - 0.1;
            }
            case 3 -> { // Right edge
                px = bx + 1.1;
                py = by + 1.1;
                pz = bz + 0.5;
            }
            default -> { return; }
        }

        int stageIdx = Math.min(Math.max(stage - 1, 0), 2);
        int colorHex = particleColors[stageIdx];

        DustParticleOptions dustOptions = new DustParticleOptions(colorHex, 1.5f);

        for (int i = 0; i < 10; i++) {
            double ox = (serverLevel.getRandom().nextDouble() - 0.5) * 0.3;
            double oy = (serverLevel.getRandom().nextDouble()) * 0.3;
            double oz = (serverLevel.getRandom().nextDouble() - 0.5) * 0.3;
            serverLevel.sendParticles(dustOptions, px + ox, py + oy, pz + oz, 1, 0.0, 0.02, 0.0, 0.01);
        }

        if (revealedMaterial == PartMaterial.SKULK) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_CHARGE_POP, px, py, pz, 4, 0.1, 0.1, 0.1, 0.01);
        }
    }

    private void resetState() {
        heldItem = ItemStack.EMPTY;
        brushStrokes = 0;
        activeBrushTicks = 0;
        revealedPartType = null;
        revealedMaterial = null;
        particleColors = new int[]{COLOR_BLACK, COLOR_BLACK, COLOR_BLACK};
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("HeldItem", ItemStack.OPTIONAL_CODEC, heldItem);
        output.putInt("BrushStrokes", brushStrokes);
        output.putIntArray("ParticleColors", particleColors);
        if (revealedPartType != null) {
            output.putString("RevealedPartType", revealedPartType.getSerializedName());
        }
        if (revealedMaterial != null) {
            output.putString("RevealedMaterial", revealedMaterial.getSerializedName());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        heldItem = input.read("HeldItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        brushStrokes = input.getIntOr("BrushStrokes", 0);
        particleColors = input.getIntArray("ParticleColors").orElse(new int[]{COLOR_BLACK, COLOR_BLACK, COLOR_BLACK});

        String ptName = input.getStringOr("RevealedPartType", "");
        for (PartType pt : PartType.values()) {
            if (pt.getSerializedName().equals(ptName)) {
                revealedPartType = pt;
                break;
            }
        }

        String matName = input.getStringOr("RevealedMaterial", "");
        for (PartMaterial pm : PartMaterial.values()) {
            if (pm.getSerializedName().equals(matName)) {
                revealedMaterial = pm;
                break;
            }
        }
    }

    public void dropContents(Level level) {
        if (!heldItem.isEmpty()) {
            ItemEntity entity = new ItemEntity(level,
                    worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                    heldItem.copy());
            level.addFreshEntity(entity);
            resetState();
        }
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }
}
