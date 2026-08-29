package dasouza.telum.block;

import dasouza.telum.Telum;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import java.util.Set;

/**
 * Central registry for all Telum mod blocks, including Suspicious End Stone and Marble blocks.
 */
public final class TelumBlocks {

    public static Block FORGE;
    public static BlockItem FORGE_ITEM;

    public static Block SUSPICIOUS_END_STONE;
    public static BlockItem SUSPICIOUS_END_STONE_ITEM;

    public static Block SUSPICIOUS_TEMPORAL_SCULK;
    public static BlockItem SUSPICIOUS_TEMPORAL_SCULK_ITEM;

    public static Block SUSPICIOUS_NETHERRACK;
    public static BlockItem SUSPICIOUS_NETHERRACK_ITEM;

    public static Block ARCHEOLOGY_TABLE;
    public static BlockItem ARCHEOLOGY_TABLE_ITEM;

    public static Block MARMOL_BLOCK;
    public static BlockItem MARMOL_BLOCK_ITEM;

    public static Block MARMOL_BRICKS;
    public static BlockItem MARMOL_BRICKS_ITEM;

    public static Block MARMOL_GILDED_BLOCK;
    public static BlockItem MARMOL_GILDED_BLOCK_ITEM;

    public static Block MARMOL_PILLAR;
    public static BlockItem MARMOL_PILLAR_ITEM;

    public static Block MARMOL_SLAB;
    public static BlockItem MARMOL_SLAB_ITEM;

    public static Block MARMOL_STAIRS;
    public static BlockItem MARMOL_STAIRS_ITEM;

    public static Block MARMOL_WALL;
    public static BlockItem MARMOL_WALL_ITEM;

    public static Block MARMOL_BRICK_SLAB;
    public static BlockItem MARMOL_BRICK_SLAB_ITEM;

    public static Block MARMOL_BRICK_STAIRS;
    public static BlockItem MARMOL_BRICK_STAIRS_ITEM;

    public static Block MARMOL_BRICK_WALL;
    public static BlockItem MARMOL_BRICK_WALL_ITEM;

    public static Block MARMOL_LECTERN;
    public static BlockItem MARMOL_LECTERN_ITEM;

    public static Block SCULK_TEMPORAL_SHRIEKER;
    public static BlockItem SCULK_TEMPORAL_SHRIEKER_ITEM;

    public static Block DEEPSLATE_TEMPORAL_POLISHED;
    public static BlockItem DEEPSLATE_TEMPORAL_POLISHED_ITEM;

    public static Block DEEPSLATE_TEMPORAL_TILES;
    public static BlockItem DEEPSLATE_TEMPORAL_TILES_ITEM;

    public static Block TEMPORAL_DEEPSLATE_BRICK;
    public static BlockItem TEMPORAL_DEEPSLATE_BRICK_ITEM;

    public static Block TEMPORAL_BARREL;
    public static BlockItem TEMPORAL_BARREL_ITEM;

    public static Block YELLOW_MARMOL_BLOCK;
    public static BlockItem YELLOW_MARMOL_BLOCK_ITEM;

    public static Block YELLOW_MARMOL_BRICKS;
    public static BlockItem YELLOW_MARMOL_BRICKS_ITEM;

    public static Block YELLOW_MARMOL_GILDED_BLOCK;
    public static BlockItem YELLOW_MARMOL_GILDED_BLOCK_ITEM;

    public static Block YELLOW_MARMOL_PILLAR;
    public static BlockItem YELLOW_MARMOL_PILLAR_ITEM;

    public static Block YELLOW_MARMOL_SLAB;
    public static BlockItem YELLOW_MARMOL_SLAB_ITEM;

    public static Block YELLOW_MARMOL_STAIRS;
    public static BlockItem YELLOW_MARMOL_STAIRS_ITEM;

    public static Block YELLOW_MARMOL_WALL;
    public static BlockItem YELLOW_MARMOL_WALL_ITEM;

    public static Block YELLOW_MARMOL_BRICK_SLAB;
    public static BlockItem YELLOW_MARMOL_BRICK_SLAB_ITEM;

    public static Block YELLOW_MARMOL_BRICK_STAIRS;
    public static BlockItem YELLOW_MARMOL_BRICK_STAIRS_ITEM;

    public static Block YELLOW_MARMOL_BRICK_WALL;
    public static BlockItem YELLOW_MARMOL_BRICK_WALL_ITEM;

    public static Block ECHO_BARREL;
    public static BlockItem ECHO_BARREL_ITEM;

    public static Block ECHO_PROJECTION_BARREL;
    public static BlockItem ECHO_PROJECTION_BARREL_ITEM;

    public static Block NOTE_COMPARATOR;
    public static BlockItem NOTE_COMPARATOR_ITEM;

    public static void initialize() {
        Telum.LOGGER.info("Registering Telum blocks");

        // Register Note Comparator
        ResourceKey<Block> noteCompKey = ResourceKey.create(Registries.BLOCK, Telum.id("note_comparator"));
        NOTE_COMPARATOR = new NoteComparatorBlock(
                BlockBehaviour.Properties.of()
                        .setId(noteCompKey)
                        .instabreak()
                        .sound(SoundType.WOOD)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, noteCompKey, NOTE_COMPARATOR);
        ResourceKey<Item> noteCompItemKey = ResourceKey.create(Registries.ITEM, Telum.id("note_comparator"));
        NOTE_COMPARATOR_ITEM = new BlockItem(NOTE_COMPARATOR, new Item.Properties().setId(noteCompItemKey));
        Registry.register(BuiltInRegistries.ITEM, noteCompItemKey, NOTE_COMPARATOR_ITEM);

        // Register Forge block
        ResourceKey<Block> forgeBlockKey = ResourceKey.create(Registries.BLOCK, Telum.id("telum_forge"));
        FORGE = new ForgeBlock(
                BlockBehaviour.Properties.of()
                        .setId(forgeBlockKey)
                        .strength(3.5f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.ANVIL)
        );
        Registry.register(BuiltInRegistries.BLOCK, forgeBlockKey, FORGE);

        ResourceKey<Item> forgeItemKey = ResourceKey.create(Registries.ITEM, Telum.id("telum_forge"));
        FORGE_ITEM = new BlockItem(FORGE, new Item.Properties().setId(forgeItemKey));
        Registry.register(BuiltInRegistries.ITEM, forgeItemKey, FORGE_ITEM);

        // Register Suspicious End Stone block
        ResourceKey<Block> endStoneKey = ResourceKey.create(Registries.BLOCK, Telum.id("suspicious_end_stone"));
        SUSPICIOUS_END_STONE = new BrushableBlock(
                Blocks.END_STONE,
                SoundEvents.BRUSH_GRAVEL,
                SoundEvents.BRUSH_GRAVEL_COMPLETED,
                BlockBehaviour.Properties.of()
                        .setId(endStoneKey)
                        .mapColor(MapColor.SAND)
                        .strength(0.5f)
                        .sound(SoundType.STONE)
        );
        Registry.register(BuiltInRegistries.BLOCK, endStoneKey, SUSPICIOUS_END_STONE);

        ResourceKey<Item> endStoneItemKey = ResourceKey.create(Registries.ITEM, Telum.id("suspicious_end_stone"));
        SUSPICIOUS_END_STONE_ITEM = new BlockItem(SUSPICIOUS_END_STONE, new Item.Properties().setId(endStoneItemKey));
        Registry.register(BuiltInRegistries.ITEM, endStoneItemKey, SUSPICIOUS_END_STONE_ITEM);

        // Register Suspicious Temporal Sculk block
        ResourceKey<Block> sculkKey = ResourceKey.create(Registries.BLOCK, Telum.id("suspicious_temporal_sculk"));
        SUSPICIOUS_TEMPORAL_SCULK = new BrushableBlock(
                Blocks.SCULK,
                SoundEvents.BRUSH_GRAVEL,
                SoundEvents.SCULK_BLOCK_CHARGE,
                BlockBehaviour.Properties.of()
                        .setId(sculkKey)
                        .mapColor(MapColor.COLOR_CYAN)
                        .strength(0.6f)
                        .sound(SoundType.SCULK)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, sculkKey, SUSPICIOUS_TEMPORAL_SCULK);

        ResourceKey<Item> sculkItemKey = ResourceKey.create(Registries.ITEM, Telum.id("suspicious_temporal_sculk"));
        SUSPICIOUS_TEMPORAL_SCULK_ITEM = new BlockItem(SUSPICIOUS_TEMPORAL_SCULK, new Item.Properties().setId(sculkItemKey));
        Registry.register(BuiltInRegistries.ITEM, sculkItemKey, SUSPICIOUS_TEMPORAL_SCULK_ITEM);

        // Register Suspicious Netherrack block
        ResourceKey<Block> netherrackKey = ResourceKey.create(Registries.BLOCK, Telum.id("suspicious_netherrack"));
        SUSPICIOUS_NETHERRACK = new BrushableBlock(
                Blocks.NETHERRACK,
                SoundEvents.BRUSH_GRAVEL,
                SoundEvents.BRUSH_GRAVEL_COMPLETED,
                BlockBehaviour.Properties.of()
                        .setId(netherrackKey)
                        .mapColor(MapColor.NETHER)
                        .strength(0.4f)
                        .sound(SoundType.NETHERRACK)
        );
        Registry.register(BuiltInRegistries.BLOCK, netherrackKey, SUSPICIOUS_NETHERRACK);

        ResourceKey<Item> netherrackItemKey = ResourceKey.create(Registries.ITEM, Telum.id("suspicious_netherrack"));
        SUSPICIOUS_NETHERRACK_ITEM = new BlockItem(SUSPICIOUS_NETHERRACK, new Item.Properties().setId(netherrackItemKey));
        Registry.register(BuiltInRegistries.ITEM, netherrackItemKey, SUSPICIOUS_NETHERRACK_ITEM);

        // Register Archeology Table block
        ResourceKey<Block> archeologyKey = ResourceKey.create(Registries.BLOCK, Telum.id("archeology_table"));
        ARCHEOLOGY_TABLE = new ArcheologyTableBlock(
                BlockBehaviour.Properties.of()
                        .setId(archeologyKey)
                        .mapColor(MapColor.WOOD)
                        .strength(2.5f)
                        .sound(SoundType.WOOD)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, archeologyKey, ARCHEOLOGY_TABLE);

        ResourceKey<Item> archeologyItemKey = ResourceKey.create(Registries.ITEM, Telum.id("archeology_table"));
        ARCHEOLOGY_TABLE_ITEM = new BlockItem(ARCHEOLOGY_TABLE, new Item.Properties().setId(archeologyItemKey));
        Registry.register(BuiltInRegistries.ITEM, archeologyItemKey, ARCHEOLOGY_TABLE_ITEM);

        // Register Marmol Block
        ResourceKey<Block> marmolBlockKey = ResourceKey.create(Registries.BLOCK, Telum.id("marmol_block"));
        MARMOL_BLOCK = new Block(
                BlockBehaviour.Properties.of()
                        .setId(marmolBlockKey)
                        .mapColor(MapColor.QUARTZ)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        );
        Registry.register(BuiltInRegistries.BLOCK, marmolBlockKey, MARMOL_BLOCK);

        ResourceKey<Item> marmolBlockItemKey = ResourceKey.create(Registries.ITEM, Telum.id("marmol_block"));
        MARMOL_BLOCK_ITEM = new BlockItem(MARMOL_BLOCK, new Item.Properties().setId(marmolBlockItemKey));
        Registry.register(BuiltInRegistries.ITEM, marmolBlockItemKey, MARMOL_BLOCK_ITEM);

        // Register Marmol Bricks
        ResourceKey<Block> marmolBricksKey = ResourceKey.create(Registries.BLOCK, Telum.id("marmol_bricks"));
        MARMOL_BRICKS = new Block(
                BlockBehaviour.Properties.of()
                        .setId(marmolBricksKey)
                        .mapColor(MapColor.QUARTZ)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        );
        Registry.register(BuiltInRegistries.BLOCK, marmolBricksKey, MARMOL_BRICKS);

        ResourceKey<Item> marmolBricksItemKey = ResourceKey.create(Registries.ITEM, Telum.id("marmol_bricks"));
        MARMOL_BRICKS_ITEM = new BlockItem(MARMOL_BRICKS, new Item.Properties().setId(marmolBricksItemKey));
        Registry.register(BuiltInRegistries.ITEM, marmolBricksItemKey, MARMOL_BRICKS_ITEM);

        // Register Marmol Gilded Block
        ResourceKey<Block> marmolGildedKey = ResourceKey.create(Registries.BLOCK, Telum.id("marmol_gilded_block"));
        MARMOL_GILDED_BLOCK = new Block(
                BlockBehaviour.Properties.of()
                        .setId(marmolGildedKey)
                        .mapColor(MapColor.QUARTZ)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        );
        Registry.register(BuiltInRegistries.BLOCK, marmolGildedKey, MARMOL_GILDED_BLOCK);

        ResourceKey<Item> marmolGildedItemKey = ResourceKey.create(Registries.ITEM, Telum.id("marmol_gilded_block"));
        MARMOL_GILDED_BLOCK_ITEM = new BlockItem(MARMOL_GILDED_BLOCK, new Item.Properties().setId(marmolGildedItemKey));
        Registry.register(BuiltInRegistries.ITEM, marmolGildedItemKey, MARMOL_GILDED_BLOCK_ITEM);

        // Register Marmol Pillar
        ResourceKey<Block> marmolPillarKey = ResourceKey.create(Registries.BLOCK, Telum.id("marmol_pillar"));
        MARMOL_PILLAR = new RotatedPillarBlock(
                BlockBehaviour.Properties.of()
                        .setId(marmolPillarKey)
                        .mapColor(MapColor.QUARTZ)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        );
        Registry.register(BuiltInRegistries.BLOCK, marmolPillarKey, MARMOL_PILLAR);

        ResourceKey<Item> marmolPillarItemKey = ResourceKey.create(Registries.ITEM, Telum.id("marmol_pillar"));
        MARMOL_PILLAR_ITEM = new BlockItem(MARMOL_PILLAR, new Item.Properties().setId(marmolPillarItemKey));
        Registry.register(BuiltInRegistries.ITEM, marmolPillarItemKey, MARMOL_PILLAR_ITEM);

        // Register Marmol Slab
        ResourceKey<Block> marmolSlabKey = ResourceKey.create(Registries.BLOCK, Telum.id("marmol_slab"));
        MARMOL_SLAB = new SlabBlock(
                BlockBehaviour.Properties.of()
                        .setId(marmolSlabKey)
                        .mapColor(MapColor.QUARTZ)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        );
        Registry.register(BuiltInRegistries.BLOCK, marmolSlabKey, MARMOL_SLAB);

        ResourceKey<Item> marmolSlabItemKey = ResourceKey.create(Registries.ITEM, Telum.id("marmol_slab"));
        MARMOL_SLAB_ITEM = new BlockItem(MARMOL_SLAB, new Item.Properties().setId(marmolSlabItemKey));
        Registry.register(BuiltInRegistries.ITEM, marmolSlabItemKey, MARMOL_SLAB_ITEM);

        // Register Marmol Stairs
        ResourceKey<Block> marmolStairsKey = ResourceKey.create(Registries.BLOCK, Telum.id("marmol_stairs"));
        MARMOL_STAIRS = new StairBlock(
                MARMOL_BLOCK.defaultBlockState(),
                BlockBehaviour.Properties.of()
                        .setId(marmolStairsKey)
                        .mapColor(MapColor.QUARTZ)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        );
        Registry.register(BuiltInRegistries.BLOCK, marmolStairsKey, MARMOL_STAIRS);

        ResourceKey<Item> marmolStairsItemKey = ResourceKey.create(Registries.ITEM, Telum.id("marmol_stairs"));
        MARMOL_STAIRS_ITEM = new BlockItem(MARMOL_STAIRS, new Item.Properties().setId(marmolStairsItemKey));
        Registry.register(BuiltInRegistries.ITEM, marmolStairsItemKey, MARMOL_STAIRS_ITEM);

        // Register Marmol Wall
        ResourceKey<Block> marmolWallKey = ResourceKey.create(Registries.BLOCK, Telum.id("marmol_wall"));
        MARMOL_WALL = new WallBlock(
                BlockBehaviour.Properties.of()
                        .setId(marmolWallKey)
                        .mapColor(MapColor.QUARTZ)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        );
        Registry.register(BuiltInRegistries.BLOCK, marmolWallKey, MARMOL_WALL);

        ResourceKey<Item> marmolWallItemKey = ResourceKey.create(Registries.ITEM, Telum.id("marmol_wall"));
        MARMOL_WALL_ITEM = new BlockItem(MARMOL_WALL, new Item.Properties().setId(marmolWallItemKey));
        Registry.register(BuiltInRegistries.ITEM, marmolWallItemKey, MARMOL_WALL_ITEM);

        // Register Marmol Brick Slab
        ResourceKey<Block> marmolBrickSlabKey = ResourceKey.create(Registries.BLOCK, Telum.id("marmol_brick_slab"));
        MARMOL_BRICK_SLAB = new SlabBlock(
                BlockBehaviour.Properties.of()
                        .setId(marmolBrickSlabKey)
                        .mapColor(MapColor.QUARTZ)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        );
        Registry.register(BuiltInRegistries.BLOCK, marmolBrickSlabKey, MARMOL_BRICK_SLAB);

        ResourceKey<Item> marmolBrickSlabItemKey = ResourceKey.create(Registries.ITEM, Telum.id("marmol_brick_slab"));
        MARMOL_BRICK_SLAB_ITEM = new BlockItem(MARMOL_BRICK_SLAB, new Item.Properties().setId(marmolBrickSlabItemKey));
        Registry.register(BuiltInRegistries.ITEM, marmolBrickSlabItemKey, MARMOL_BRICK_SLAB_ITEM);

        // Register Marmol Brick Stairs
        ResourceKey<Block> marmolBrickStairsKey = ResourceKey.create(Registries.BLOCK, Telum.id("marmol_brick_stairs"));
        MARMOL_BRICK_STAIRS = new StairBlock(
                MARMOL_BRICKS.defaultBlockState(),
                BlockBehaviour.Properties.of()
                        .setId(marmolBrickStairsKey)
                        .mapColor(MapColor.QUARTZ)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        );
        Registry.register(BuiltInRegistries.BLOCK, marmolBrickStairsKey, MARMOL_BRICK_STAIRS);

        ResourceKey<Item> marmolBrickStairsItemKey = ResourceKey.create(Registries.ITEM, Telum.id("marmol_brick_stairs"));
        MARMOL_BRICK_STAIRS_ITEM = new BlockItem(MARMOL_BRICK_STAIRS, new Item.Properties().setId(marmolBrickStairsItemKey));
        Registry.register(BuiltInRegistries.ITEM, marmolBrickStairsItemKey, MARMOL_BRICK_STAIRS_ITEM);

        // Register Marmol Brick Wall
        ResourceKey<Block> marmolBrickWallKey = ResourceKey.create(Registries.BLOCK, Telum.id("marmol_brick_wall"));
        MARMOL_BRICK_WALL = new WallBlock(
                BlockBehaviour.Properties.of()
                        .setId(marmolBrickWallKey)
                        .mapColor(MapColor.QUARTZ)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.STONE)
        );
        Registry.register(BuiltInRegistries.BLOCK, marmolBrickWallKey, MARMOL_BRICK_WALL);

        ResourceKey<Item> marmolBrickWallItemKey = ResourceKey.create(Registries.ITEM, Telum.id("marmol_brick_wall"));
        MARMOL_BRICK_WALL_ITEM = new BlockItem(MARMOL_BRICK_WALL, new Item.Properties().setId(marmolBrickWallItemKey));
        Registry.register(BuiltInRegistries.ITEM, marmolBrickWallItemKey, MARMOL_BRICK_WALL_ITEM);

        // Register Yellow Marmol Block
        ResourceKey<Block> yBlockKey = ResourceKey.create(Registries.BLOCK, Telum.id("yellow_marmol_block"));
        YELLOW_MARMOL_BLOCK = new Block(
                BlockBehaviour.Properties.of()
                        .setId(yBlockKey)
                        .mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, yBlockKey, YELLOW_MARMOL_BLOCK);
        ResourceKey<Item> yBlockItemKey = ResourceKey.create(Registries.ITEM, Telum.id("yellow_marmol_block"));
        YELLOW_MARMOL_BLOCK_ITEM = new BlockItem(YELLOW_MARMOL_BLOCK, new Item.Properties().setId(yBlockItemKey));
        Registry.register(BuiltInRegistries.ITEM, yBlockItemKey, YELLOW_MARMOL_BLOCK_ITEM);

        // Register Yellow Marmol Bricks
        ResourceKey<Block> yBricksKey = ResourceKey.create(Registries.BLOCK, Telum.id("yellow_marmol_bricks"));
        YELLOW_MARMOL_BRICKS = new Block(
                BlockBehaviour.Properties.of()
                        .setId(yBricksKey)
                        .mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, yBricksKey, YELLOW_MARMOL_BRICKS);
        ResourceKey<Item> yBricksItemKey = ResourceKey.create(Registries.ITEM, Telum.id("yellow_marmol_bricks"));
        YELLOW_MARMOL_BRICKS_ITEM = new BlockItem(YELLOW_MARMOL_BRICKS, new Item.Properties().setId(yBricksItemKey));
        Registry.register(BuiltInRegistries.ITEM, yBricksItemKey, YELLOW_MARMOL_BRICKS_ITEM);

        // Register Yellow Marmol Gilded Block
        ResourceKey<Block> yGildedKey = ResourceKey.create(Registries.BLOCK, Telum.id("yellow_marmol_gilded_block"));
        YELLOW_MARMOL_GILDED_BLOCK = new Block(
                BlockBehaviour.Properties.of()
                        .setId(yGildedKey)
                        .mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, yGildedKey, YELLOW_MARMOL_GILDED_BLOCK);
        ResourceKey<Item> yGildedItemKey = ResourceKey.create(Registries.ITEM, Telum.id("yellow_marmol_gilded_block"));
        YELLOW_MARMOL_GILDED_BLOCK_ITEM = new BlockItem(YELLOW_MARMOL_GILDED_BLOCK, new Item.Properties().setId(yGildedItemKey));
        Registry.register(BuiltInRegistries.ITEM, yGildedItemKey, YELLOW_MARMOL_GILDED_BLOCK_ITEM);

        // Register Yellow Marmol Pillar
        ResourceKey<Block> yPillarKey = ResourceKey.create(Registries.BLOCK, Telum.id("yellow_marmol_pillar"));
        YELLOW_MARMOL_PILLAR = new RotatedPillarBlock(
                BlockBehaviour.Properties.of()
                        .setId(yPillarKey)
                        .mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, yPillarKey, YELLOW_MARMOL_PILLAR);
        ResourceKey<Item> yPillarItemKey = ResourceKey.create(Registries.ITEM, Telum.id("yellow_marmol_pillar"));
        YELLOW_MARMOL_PILLAR_ITEM = new BlockItem(YELLOW_MARMOL_PILLAR, new Item.Properties().setId(yPillarItemKey));
        Registry.register(BuiltInRegistries.ITEM, yPillarItemKey, YELLOW_MARMOL_PILLAR_ITEM);

        // Register Yellow Marmol Slab
        ResourceKey<Block> ySlabKey = ResourceKey.create(Registries.BLOCK, Telum.id("yellow_marmol_slab"));
        YELLOW_MARMOL_SLAB = new SlabBlock(
                BlockBehaviour.Properties.of()
                        .setId(ySlabKey)
                        .mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, ySlabKey, YELLOW_MARMOL_SLAB);
        ResourceKey<Item> ySlabItemKey = ResourceKey.create(Registries.ITEM, Telum.id("yellow_marmol_slab"));
        YELLOW_MARMOL_SLAB_ITEM = new BlockItem(YELLOW_MARMOL_SLAB, new Item.Properties().setId(ySlabItemKey));
        Registry.register(BuiltInRegistries.ITEM, ySlabItemKey, YELLOW_MARMOL_SLAB_ITEM);

        // Register Yellow Marmol Stairs
        ResourceKey<Block> yStairsKey = ResourceKey.create(Registries.BLOCK, Telum.id("yellow_marmol_stairs"));
        YELLOW_MARMOL_STAIRS = new StairBlock(
                YELLOW_MARMOL_BLOCK.defaultBlockState(),
                BlockBehaviour.Properties.of()
                        .setId(yStairsKey)
                        .mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, yStairsKey, YELLOW_MARMOL_STAIRS);
        ResourceKey<Item> yStairsItemKey = ResourceKey.create(Registries.ITEM, Telum.id("yellow_marmol_stairs"));
        YELLOW_MARMOL_STAIRS_ITEM = new BlockItem(YELLOW_MARMOL_STAIRS, new Item.Properties().setId(yStairsItemKey));
        Registry.register(BuiltInRegistries.ITEM, yStairsItemKey, YELLOW_MARMOL_STAIRS_ITEM);

        // Register Yellow Marmol Wall
        ResourceKey<Block> yWallKey = ResourceKey.create(Registries.BLOCK, Telum.id("yellow_marmol_wall"));
        YELLOW_MARMOL_WALL = new WallBlock(
                BlockBehaviour.Properties.of()
                        .setId(yWallKey)
                        .mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, yWallKey, YELLOW_MARMOL_WALL);
        ResourceKey<Item> yWallItemKey = ResourceKey.create(Registries.ITEM, Telum.id("yellow_marmol_wall"));
        YELLOW_MARMOL_WALL_ITEM = new BlockItem(YELLOW_MARMOL_WALL, new Item.Properties().setId(yWallItemKey));
        Registry.register(BuiltInRegistries.ITEM, yWallItemKey, YELLOW_MARMOL_WALL_ITEM);

        // Register Yellow Marmol Brick Slab
        ResourceKey<Block> yBrickSlabKey = ResourceKey.create(Registries.BLOCK, Telum.id("yellow_marmol_brick_slab"));
        YELLOW_MARMOL_BRICK_SLAB = new SlabBlock(
                BlockBehaviour.Properties.of()
                        .setId(yBrickSlabKey)
                        .mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, yBrickSlabKey, YELLOW_MARMOL_BRICK_SLAB);
        ResourceKey<Item> yBrickSlabItemKey = ResourceKey.create(Registries.ITEM, Telum.id("yellow_marmol_brick_slab"));
        YELLOW_MARMOL_BRICK_SLAB_ITEM = new BlockItem(YELLOW_MARMOL_BRICK_SLAB, new Item.Properties().setId(yBrickSlabItemKey));
        Registry.register(BuiltInRegistries.ITEM, yBrickSlabItemKey, YELLOW_MARMOL_BRICK_SLAB_ITEM);

        // Register Yellow Marmol Brick Stairs
        ResourceKey<Block> yBrickStairsKey = ResourceKey.create(Registries.BLOCK, Telum.id("yellow_marmol_brick_stairs"));
        YELLOW_MARMOL_BRICK_STAIRS = new StairBlock(
                YELLOW_MARMOL_BRICKS.defaultBlockState(),
                BlockBehaviour.Properties.of()
                        .setId(yBrickStairsKey)
                        .mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, yBrickStairsKey, YELLOW_MARMOL_BRICK_STAIRS);
        ResourceKey<Item> yBrickStairsItemKey = ResourceKey.create(Registries.ITEM, Telum.id("yellow_marmol_brick_stairs"));
        YELLOW_MARMOL_BRICK_STAIRS_ITEM = new BlockItem(YELLOW_MARMOL_BRICK_STAIRS, new Item.Properties().setId(yBrickStairsItemKey));
        Registry.register(BuiltInRegistries.ITEM, yBrickStairsItemKey, YELLOW_MARMOL_BRICK_STAIRS_ITEM);

        // Register Yellow Marmol Brick Wall
        ResourceKey<Block> yBrickWallKey = ResourceKey.create(Registries.BLOCK, Telum.id("yellow_marmol_brick_wall"));
        YELLOW_MARMOL_BRICK_WALL = new WallBlock(
                BlockBehaviour.Properties.of()
                        .setId(yBrickWallKey)
                        .mapColor(MapColor.COLOR_YELLOW)
                        .strength(1.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.GLASS)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, yBrickWallKey, YELLOW_MARMOL_BRICK_WALL);
        ResourceKey<Item> yBrickWallItemKey = ResourceKey.create(Registries.ITEM, Telum.id("yellow_marmol_brick_wall"));
        YELLOW_MARMOL_BRICK_WALL_ITEM = new BlockItem(YELLOW_MARMOL_BRICK_WALL, new Item.Properties().setId(yBrickWallItemKey));
        Registry.register(BuiltInRegistries.ITEM, yBrickWallItemKey, YELLOW_MARMOL_BRICK_WALL_ITEM);

        // Register Marmol Lectern
        ResourceKey<Block> marmolLecternKey = ResourceKey.create(Registries.BLOCK, Telum.id("marmol_lectern"));
        MARMOL_LECTERN = new MarmolLecternBlock(
                BlockBehaviour.Properties.of()
                        .setId(marmolLecternKey)
                        .mapColor(MapColor.QUARTZ)
                        .strength(-1.0f, 3600000.0f)
                        .sound(SoundType.STONE)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, marmolLecternKey, MARMOL_LECTERN);

        ResourceKey<Item> marmolLecternItemKey = ResourceKey.create(Registries.ITEM, Telum.id("marmol_lectern"));
        MARMOL_LECTERN_ITEM = new BlockItem(MARMOL_LECTERN, new Item.Properties().setId(marmolLecternItemKey));
        Registry.register(BuiltInRegistries.ITEM, marmolLecternItemKey, MARMOL_LECTERN_ITEM);

        // Register Sculk Temporal Shrieker
        ResourceKey<Block> sculkShriekerKey = ResourceKey.create(Registries.BLOCK, Telum.id("sculk_temporal_shrieker"));
        SCULK_TEMPORAL_SHRIEKER = new TemporalSculkShriekerBlock(
                BlockBehaviour.Properties.of()
                        .setId(sculkShriekerKey)
                        .mapColor(MapColor.COLOR_CYAN)
                        .strength(-1.0f, 3600000.0f)
                        .sound(SoundType.SCULK_SHRIEKER)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, sculkShriekerKey, SCULK_TEMPORAL_SHRIEKER);

        ResourceKey<Item> sculkShriekerItemKey = ResourceKey.create(Registries.ITEM, Telum.id("sculk_temporal_shrieker"));
        SCULK_TEMPORAL_SHRIEKER_ITEM = new BlockItem(SCULK_TEMPORAL_SHRIEKER, new Item.Properties().setId(sculkShriekerItemKey));
        Registry.register(BuiltInRegistries.ITEM, sculkShriekerItemKey, SCULK_TEMPORAL_SHRIEKER_ITEM);

        // Register Deepslate Temporal Polished
        ResourceKey<Block> dtPolishedKey = ResourceKey.create(Registries.BLOCK, Telum.id("deepslate_temporal_polished"));
        DEEPSLATE_TEMPORAL_POLISHED = new Block(
                BlockBehaviour.Properties.of()
                        .setId(dtPolishedKey)
                        .mapColor(MapColor.DEEPSLATE)
                        .strength(3.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.DEEPSLATE)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, dtPolishedKey, DEEPSLATE_TEMPORAL_POLISHED);
        ResourceKey<Item> dtPolishedItemKey = ResourceKey.create(Registries.ITEM, Telum.id("deepslate_temporal_polished"));
        DEEPSLATE_TEMPORAL_POLISHED_ITEM = new BlockItem(DEEPSLATE_TEMPORAL_POLISHED, new Item.Properties().setId(dtPolishedItemKey));
        Registry.register(BuiltInRegistries.ITEM, dtPolishedItemKey, DEEPSLATE_TEMPORAL_POLISHED_ITEM);

        // Register Deepslate Temporal Tiles
        ResourceKey<Block> dtTilesKey = ResourceKey.create(Registries.BLOCK, Telum.id("deepslate_temporal_tiles"));
        DEEPSLATE_TEMPORAL_TILES = new Block(
                BlockBehaviour.Properties.of()
                        .setId(dtTilesKey)
                        .mapColor(MapColor.DEEPSLATE)
                        .strength(3.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.DEEPSLATE_TILES)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, dtTilesKey, DEEPSLATE_TEMPORAL_TILES);
        ResourceKey<Item> dtTilesItemKey = ResourceKey.create(Registries.ITEM, Telum.id("deepslate_temporal_tiles"));
        DEEPSLATE_TEMPORAL_TILES_ITEM = new BlockItem(DEEPSLATE_TEMPORAL_TILES, new Item.Properties().setId(dtTilesItemKey));
        Registry.register(BuiltInRegistries.ITEM, dtTilesItemKey, DEEPSLATE_TEMPORAL_TILES_ITEM);

        // Register Temporal Deepslate Brick
        ResourceKey<Block> dtBrickKey = ResourceKey.create(Registries.BLOCK, Telum.id("temporal_deepslate_brick"));
        TEMPORAL_DEEPSLATE_BRICK = new Block(
                BlockBehaviour.Properties.of()
                        .setId(dtBrickKey)
                        .mapColor(MapColor.DEEPSLATE)
                        .strength(3.5f, 6.0f)
                        .requiresCorrectToolForDrops()
                        .sound(SoundType.DEEPSLATE_BRICKS)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, dtBrickKey, TEMPORAL_DEEPSLATE_BRICK);
        ResourceKey<Item> dtBrickItemKey = ResourceKey.create(Registries.ITEM, Telum.id("temporal_deepslate_brick"));
        TEMPORAL_DEEPSLATE_BRICK_ITEM = new BlockItem(TEMPORAL_DEEPSLATE_BRICK, new Item.Properties().setId(dtBrickItemKey));
        Registry.register(BuiltInRegistries.ITEM, dtBrickItemKey, TEMPORAL_DEEPSLATE_BRICK_ITEM);

        // Register Temporal Barrel
        ResourceKey<Block> barrelKey = ResourceKey.create(Registries.BLOCK, Telum.id("temporal_barrel"));
        TEMPORAL_BARREL = new TemporalBarrelBlock(
                BlockBehaviour.Properties.of()
                        .setId(barrelKey)
                        .mapColor(MapColor.WOOD)
                        .strength(2.5f)
                        .sound(SoundType.WOOD)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, barrelKey, TEMPORAL_BARREL);
        ResourceKey<Item> barrelItemKey = ResourceKey.create(Registries.ITEM, Telum.id("temporal_barrel"));
        TEMPORAL_BARREL_ITEM = new BlockItem(TEMPORAL_BARREL, new Item.Properties().setId(barrelItemKey));
        Registry.register(BuiltInRegistries.ITEM, barrelItemKey, TEMPORAL_BARREL_ITEM);

        // Echo Barrel (Master)
        ResourceKey<Block> echoBarrelKey = ResourceKey.create(Registries.BLOCK, Telum.id("echo_barrel"));
        ECHO_BARREL = new EchoBarrelBlock(
                BlockBehaviour.Properties.of()
                        .setId(echoBarrelKey)
                        .mapColor(MapColor.WOOD)
                        .strength(2.5f)
                        .sound(SoundType.WOOD)
        );
        Registry.register(BuiltInRegistries.BLOCK, echoBarrelKey, ECHO_BARREL);
        ResourceKey<Item> echoBarrelItemKey = ResourceKey.create(Registries.ITEM, Telum.id("echo_barrel"));
        ECHO_BARREL_ITEM = new BlockItem(ECHO_BARREL, new Item.Properties().setId(echoBarrelItemKey));
        Registry.register(BuiltInRegistries.ITEM, echoBarrelItemKey, ECHO_BARREL_ITEM);

        // Echo Barrel Projection (Ethereal Copy)
        ResourceKey<Block> echoProjKey = ResourceKey.create(Registries.BLOCK, Telum.id("echo_barrel_projection"));
        ECHO_PROJECTION_BARREL = new EchoProjectionBlock(
                BlockBehaviour.Properties.of()
                        .setId(echoProjKey)
                        .mapColor(MapColor.COLOR_LIGHT_BLUE)
                        .destroyTime(0.0f)
                        .instabreak()
                        .sound(SoundType.AMETHYST)
                        .lightLevel(state -> 7)
                        .noOcclusion()
        );
        Registry.register(BuiltInRegistries.BLOCK, echoProjKey, ECHO_PROJECTION_BARREL);
        ResourceKey<Item> echoProjItemKey = ResourceKey.create(Registries.ITEM, Telum.id("echo_barrel_projection"));
        ECHO_PROJECTION_BARREL_ITEM = new BlockItem(ECHO_PROJECTION_BARREL, new Item.Properties().setId(echoProjItemKey));
        Registry.register(BuiltInRegistries.ITEM, echoProjItemKey, ECHO_PROJECTION_BARREL_ITEM);

        // Allow SculkShriekerBlockEntity and BrushableBlockEntity to validate custom block states
        try {
            java.lang.reflect.Field validBlocksField = BlockEntityType.class.getDeclaredField("validBlocks");
            validBlocksField.setAccessible(true);

            @SuppressWarnings("unchecked")
            Set<Block> shriekerBlocks = (Set<Block>) validBlocksField.get(BlockEntityTypes.SCULK_SHRIEKER);
            if (!(shriekerBlocks instanceof java.util.HashSet)) {
                shriekerBlocks = new java.util.HashSet<>(shriekerBlocks);
                validBlocksField.set(BlockEntityTypes.SCULK_SHRIEKER, shriekerBlocks);
            }
            shriekerBlocks.add(SCULK_TEMPORAL_SHRIEKER);

            @SuppressWarnings("unchecked")
            Set<Block> brushableBlocks = (Set<Block>) validBlocksField.get(BlockEntityTypes.BRUSHABLE_BLOCK);
            if (!(brushableBlocks instanceof java.util.HashSet)) {
                brushableBlocks = new java.util.HashSet<>(brushableBlocks);
                validBlocksField.set(BlockEntityTypes.BRUSHABLE_BLOCK, brushableBlocks);
            }
            brushableBlocks.add(SUSPICIOUS_END_STONE);
            brushableBlocks.add(SUSPICIOUS_TEMPORAL_SCULK);
            brushableBlocks.add(SUSPICIOUS_NETHERRACK);
        } catch (Exception e) {
            Telum.LOGGER.error("Failed to update BlockEntityTypes validBlocks", e);
        }
    }
}
