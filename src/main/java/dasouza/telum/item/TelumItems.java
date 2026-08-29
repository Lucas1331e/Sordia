package dasouza.telum.item;

import dasouza.telum.Telum;
import dasouza.telum.block.TelumBlocks;
import dasouza.telum.component.TelumComponents;
import dasouza.telum.component.ToolPartData;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.tool.PartType;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TelumItems {

    public static final Map<String, ToolPartItem> PART_ITEMS = new LinkedHashMap<>();
    public static AssembledToolItem ASSEMBLED_TOOL;
    public static PieceOfSordiaItem PIECE_OF_SORDIA;
    public static DragonSordiaItem DRAGON_SORDIA;
    public static LyreItem LYRE;
    public static Item LYRE_PART_LEFT;
    public static Item LYRE_PART_RIGHT;
    public static TemporalRecallPotionItem TEMPORAL_RECALL_POTION;
    public static SongSheetItem BACKTIME_SONG;
    public static SongSheetItem BED_SONG;
    public static SongSheetItem CHEST_SONG;
    public static SongSheetItem SCULK_SONG;
    public static SongSheetItem DAWN_SONG;
    public static SongSheetItem REVEAL_SONG;
    public static SongSheetItem VOID_SONG;

    public static final ResourceKey<CreativeModeTab> TELUM_TAB_KEY = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, Telum.id("telum_tab"));

    public static void initialize() {
        Telum.LOGGER.info("Registering Telum items");

        for (PartType partType : PartType.values()) {
            for (PartMaterial material : PartMaterial.values()) {
                if (!isPartTypeAllowed(material, partType)) continue;

                String name = partType.getPartName() + "_" + material.getMaterialName();
                ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Telum.id(name));
                ToolPartData defaultData = new ToolPartData(partType, material);

                ToolPartItem item = new ToolPartItem(
                        new Item.Properties()
                                .setId(key)
                                .stacksTo(16)
                                .component(TelumComponents.TOOL_PART, defaultData),
                        partType,
                        material
                );

                Registry.register(BuiltInRegistries.ITEM, key, item);
                PART_ITEMS.put(name, item);
            }
        }

        ResourceKey<Item> toolKey = ResourceKey.create(Registries.ITEM, Telum.id("assembled_tool"));
        ASSEMBLED_TOOL = new AssembledToolItem(
                new Item.Properties().setId(toolKey).durability(100)
        );
        Registry.register(BuiltInRegistries.ITEM, toolKey, ASSEMBLED_TOOL);



        ResourceKey<Item> sordiaKey = ResourceKey.create(Registries.ITEM, Telum.id("piece_of_sordia"));
        PIECE_OF_SORDIA = new PieceOfSordiaItem(
                new Item.Properties().setId(sordiaKey)
        );
        Registry.register(BuiltInRegistries.ITEM, sordiaKey, PIECE_OF_SORDIA);

        ResourceKey<Item> dragonSordiaKey = ResourceKey.create(Registries.ITEM, Telum.id("dragon_sordia"));
        DRAGON_SORDIA = new DragonSordiaItem(
                new Item.Properties().setId(dragonSordiaKey)
        );
        Registry.register(BuiltInRegistries.ITEM, dragonSordiaKey, DRAGON_SORDIA);

        ResourceKey<Item> lyreKey = ResourceKey.create(Registries.ITEM, Telum.id("lyre"));
        LYRE = new LyreItem(
                new Item.Properties().setId(lyreKey).stacksTo(1)
        );
        Registry.register(BuiltInRegistries.ITEM, lyreKey, LYRE);

        ResourceKey<Item> lyreLeftKey = ResourceKey.create(Registries.ITEM, Telum.id("lyre_part_left"));
        LYRE_PART_LEFT = new Item(new Item.Properties().setId(lyreLeftKey));
        Registry.register(BuiltInRegistries.ITEM, lyreLeftKey, LYRE_PART_LEFT);

        ResourceKey<Item> lyreRightKey = ResourceKey.create(Registries.ITEM, Telum.id("lyre_part_right"));
        LYRE_PART_RIGHT = new Item(new Item.Properties().setId(lyreRightKey));
        Registry.register(BuiltInRegistries.ITEM, lyreRightKey, LYRE_PART_RIGHT);

        ResourceKey<Item> potionKey = ResourceKey.create(Registries.ITEM, Telum.id("temporal_recall_potion"));
        TEMPORAL_RECALL_POTION = new TemporalRecallPotionItem(
                new Item.Properties().setId(potionKey).stacksTo(1)
        );
        Registry.register(BuiltInRegistries.ITEM, potionKey, TEMPORAL_RECALL_POTION);

        ResourceKey<Item> backtimeKey = ResourceKey.create(Registries.ITEM, Telum.id("backtime_song"));
        BACKTIME_SONG = new SongSheetItem(new Item.Properties().setId(backtimeKey), "backtime_song", "Canción del Tiempo");
        Registry.register(BuiltInRegistries.ITEM, backtimeKey, BACKTIME_SONG);

        ResourceKey<Item> bedKey = ResourceKey.create(Registries.ITEM, Telum.id("bed_song"));
        BED_SONG = new SongSheetItem(new Item.Properties().setId(bedKey), "bed_song", "Canción de Retorno");
        Registry.register(BuiltInRegistries.ITEM, bedKey, BED_SONG);

        ResourceKey<Item> chestKey = ResourceKey.create(Registries.ITEM, Telum.id("chest_song"));
        CHEST_SONG = new SongSheetItem(new Item.Properties().setId(chestKey), "chest_song", "Canción de los Ecos");
        Registry.register(BuiltInRegistries.ITEM, chestKey, CHEST_SONG);

        ResourceKey<Item> sculkSongKey = ResourceKey.create(Registries.ITEM, Telum.id("sculk_song"));
        SCULK_SONG = new SongSheetItem(new Item.Properties().setId(sculkSongKey), "sculk_song", "Canción del Sculk");
        Registry.register(BuiltInRegistries.ITEM, sculkSongKey, SCULK_SONG);

        ResourceKey<Item> dawnKey = ResourceKey.create(Registries.ITEM, Telum.id("dawn_song"));
        DAWN_SONG = new SongSheetItem(new Item.Properties().setId(dawnKey), "dawn_song", "Canción del Amanecer");
        Registry.register(BuiltInRegistries.ITEM, dawnKey, DAWN_SONG);

        ResourceKey<Item> revealKey = ResourceKey.create(Registries.ITEM, Telum.id("reveal_song"));
        REVEAL_SONG = new SongSheetItem(new Item.Properties().setId(revealKey), "reveal_song", "Sonata de la Revelación");
        Registry.register(BuiltInRegistries.ITEM, revealKey, REVEAL_SONG);

        ResourceKey<Item> voidKey = ResourceKey.create(Registries.ITEM, Telum.id("void_song"));
        VOID_SONG = new SongSheetItem(new Item.Properties().setId(voidKey), "void_song", "Balada del Vacío");
        Registry.register(BuiltInRegistries.ITEM, voidKey, VOID_SONG);

        // Custom Telum Creative Tab
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TELUM_TAB_KEY,
                FabricCreativeModeTab.builder()
                        .title(Component.translatable("itemGroup.telum"))
                        .icon(() -> new ItemStack(TelumBlocks.FORGE_ITEM))
                        .displayItems((displayContext, output) -> {
                            output.accept(TelumBlocks.FORGE_ITEM);
                            output.accept(TelumBlocks.ARCHEOLOGY_TABLE_ITEM);
                            output.accept(TelumBlocks.SUSPICIOUS_END_STONE_ITEM);
                            output.accept(TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK_ITEM);
                            output.accept(TelumBlocks.SUSPICIOUS_NETHERRACK_ITEM);
                            output.accept(TelumBlocks.MARMOL_BLOCK_ITEM);
                            output.accept(TelumBlocks.MARMOL_BRICKS_ITEM);
                            output.accept(TelumBlocks.MARMOL_GILDED_BLOCK_ITEM);
                            output.accept(TelumBlocks.MARMOL_PILLAR_ITEM);
                            output.accept(TelumBlocks.MARMOL_SLAB_ITEM);
                            output.accept(TelumBlocks.MARMOL_STAIRS_ITEM);
                            output.accept(TelumBlocks.MARMOL_WALL_ITEM);
                            output.accept(TelumBlocks.MARMOL_BRICK_SLAB_ITEM);
                            output.accept(TelumBlocks.MARMOL_BRICK_STAIRS_ITEM);
                            output.accept(TelumBlocks.MARMOL_BRICK_WALL_ITEM);
                            output.accept(TelumBlocks.MARMOL_LECTERN_ITEM);
                            output.accept(TelumBlocks.SCULK_TEMPORAL_SHRIEKER_ITEM);
                            output.accept(TelumBlocks.DEEPSLATE_TEMPORAL_POLISHED_ITEM);
                            output.accept(TelumBlocks.DEEPSLATE_TEMPORAL_TILES_ITEM);
                            output.accept(TelumBlocks.TEMPORAL_DEEPSLATE_BRICK_ITEM);
                            output.accept(TelumBlocks.TEMPORAL_BARREL_ITEM);
                            output.accept(TelumBlocks.ECHO_BARREL_ITEM);
                            output.accept(TelumBlocks.ECHO_PROJECTION_BARREL_ITEM);
                            output.accept(TelumBlocks.NOTE_COMPARATOR_ITEM);
                            output.accept(PIECE_OF_SORDIA);
                            output.accept(DRAGON_SORDIA);
                            output.accept(LYRE_PART_LEFT);
                            output.accept(LYRE_PART_RIGHT);
                            output.accept(LYRE);
                            output.accept(TEMPORAL_RECALL_POTION);
                            output.accept(BACKTIME_SONG);
                            output.accept(BED_SONG);
                            output.accept(CHEST_SONG);
                            output.accept(SCULK_SONG);
                            output.accept(DAWN_SONG);
                            output.accept(REVEAL_SONG);
                            output.accept(VOID_SONG);
                            for (PartMaterial material : PartMaterial.values()) {
                                for (PartType partType : PartType.values()) {
                                    String name = partType.getPartName() + "_" + material.getMaterialName();
                                    ToolPartItem item = PART_ITEMS.get(name);
                                    if (item != null) {
                                        output.accept(item);
                                    }
                                }
                            }
                            output.accept(ASSEMBLED_TOOL);
                        })
                        .build()
        );

        // Register items into Vanilla Creative Tabs
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
            output.accept(TelumBlocks.MARMOL_BLOCK_ITEM);
            output.accept(TelumBlocks.MARMOL_BRICKS_ITEM);
            output.accept(TelumBlocks.MARMOL_GILDED_BLOCK_ITEM);
            output.accept(TelumBlocks.MARMOL_PILLAR_ITEM);
            output.accept(TelumBlocks.MARMOL_SLAB_ITEM);
            output.accept(TelumBlocks.MARMOL_STAIRS_ITEM);
            output.accept(TelumBlocks.MARMOL_WALL_ITEM);
            output.accept(TelumBlocks.MARMOL_BRICK_SLAB_ITEM);
            output.accept(TelumBlocks.MARMOL_BRICK_STAIRS_ITEM);
            output.accept(TelumBlocks.MARMOL_BRICK_WALL_ITEM);
            output.accept(TelumBlocks.MARMOL_LECTERN_ITEM);
            output.accept(TelumBlocks.DEEPSLATE_TEMPORAL_POLISHED_ITEM);
            output.accept(TelumBlocks.DEEPSLATE_TEMPORAL_TILES_ITEM);
            output.accept(TelumBlocks.TEMPORAL_DEEPSLATE_BRICK_ITEM);
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(output -> {
            output.accept(TelumBlocks.FORGE_ITEM);
            output.accept(TelumBlocks.ARCHEOLOGY_TABLE_ITEM);
            output.accept(TelumBlocks.SUSPICIOUS_END_STONE_ITEM);
            output.accept(TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK_ITEM);
            output.accept(TelumBlocks.MARMOL_LECTERN_ITEM);
            output.accept(TelumBlocks.SCULK_TEMPORAL_SHRIEKER_ITEM);
            output.accept(TelumBlocks.TEMPORAL_BARREL_ITEM);
            output.accept(TelumBlocks.ECHO_BARREL_ITEM);
            output.accept(TelumBlocks.ECHO_PROJECTION_BARREL_ITEM);
            output.accept(TelumBlocks.NOTE_COMPARATOR_ITEM);
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(output -> {
            output.accept(TelumBlocks.NOTE_COMPARATOR_ITEM);
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            output.accept(PIECE_OF_SORDIA);
            output.accept(DRAGON_SORDIA);
            output.accept(LYRE_PART_LEFT);
            output.accept(LYRE_PART_RIGHT);
            PART_ITEMS.values().forEach(output::accept);
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
            output.accept(ASSEMBLED_TOOL);
            output.accept(LYRE);
            output.accept(BACKTIME_SONG);
            output.accept(BED_SONG);
            output.accept(CHEST_SONG);
            output.accept(SCULK_SONG);
            output.accept(DAWN_SONG);
            output.accept(REVEAL_SONG);
            output.accept(VOID_SONG);
        });

        // Register Loot Table modifier to inject Piece of Sordia and Prismarine parts into archaeology/entity loot tables
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            String path = key.identifier().getPath();
            if (path.contains("archaeology")) {
                tableBuilder.withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(PIECE_OF_SORDIA).setWeight(2))
                );
            }
            if (path.contains("ocean_ruin")) {
                // High probability for Prismarine tool parts in ocean ruin suspicious gravel!
                for (PartType partType : PartType.values()) {
                    ToolPartItem partItem = getPartItem(partType, PartMaterial.PRISMARINE);
                    if (partItem != null) {
                        tableBuilder.withPool(LootPool.lootPool()
                                .add(LootItem.lootTableItem(partItem).setWeight(20))
                        );
                    }
                }
            }
            if (path.contains("elder_guardian")) {
                ToolPartItem prismarineHead = getPartItem(PartType.HEAD, PartMaterial.PRISMARINE);
                if (prismarineHead != null) {
                    tableBuilder.withPool(LootPool.lootPool()
                            .add(LootItem.lootTableItem(prismarineHead).setWeight(1))
                    );
                }
            }
            if (path.contains("ruined_portal") || path.contains("nether_bridge") || path.contains("bastion") || path.contains("suspicious_netherrack")) {
                for (PartType partType : PartType.values()) {
                    ToolPartItem blazePart = getPartItem(partType, PartMaterial.BLAZE);
                    if (blazePart != null) {
                        tableBuilder.withPool(LootPool.lootPool()
                                .add(LootItem.lootTableItem(blazePart).setWeight(15))
                        );
                    }
                }
            }
            if (path.contains("end_city")) {
                tableBuilder.withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(net.minecraft.world.item.Items.DRAGON_BREATH).setWeight(30))
                );
            }
            if (path.contains("trial_chambers") || path.contains("trail_ruins") || path.contains("trail_ruin")) {
                int weight = (path.contains("trial_chambers") && path.contains("chest")) ? 2 : 20;
                for (PartType partType : PartType.values()) {
                    ToolPartItem windPart = getPartItem(partType, PartMaterial.WIND);
                    if (windPart != null) {
                        tableBuilder.withPool(LootPool.lootPool()
                                .add(LootItem.lootTableItem(windPart).setWeight(weight))
                        );
                    }
                }
            }
        });

        // Rare Mob Tool Part Drops (Spider Eye, Skeleton Handle, Zombie Grip, Creeper Head, Enderman Handle)
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (!(entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

            // Elder Guardian guaranteed drop
            if (entity instanceof net.minecraft.world.entity.monster.ElderGuardian) {
                ToolPartItem head = getPartItem(PartType.HEAD, PartMaterial.PRISMARINE);
                if (head != null) {
                    entity.spawnAtLocation(serverLevel, new ItemStack(head));
                }
            }

            // Check if Ender Dragon has been defeated previously (increases drop rate from 1.5% to 6.0%)
            boolean dragonDefeated = false;
            var dragonFight = serverLevel.getServer().overworld().getDragonFight();
            if (dragonFight != null && dragonFight.hasPreviouslyKilledDragon()) {
                dragonDefeated = true;
            }

            float dropRate = dragonDefeated ? 0.06f : 0.015f;

            if (serverLevel.getRandom().nextFloat() < dropRate) {
                ToolPartItem partToDrop = null;

                if (entity instanceof Spider || entity instanceof CaveSpider) {
                    partToDrop = getPartItem(PartType.EYE, PartMaterial.SPIDER);
                } else if (entity instanceof AbstractSkeleton) {
                    partToDrop = getPartItem(PartType.HANDLE, PartMaterial.SKELETON);
                } else if (entity instanceof Zombie) {
                    partToDrop = getPartItem(PartType.GRIP, PartMaterial.ZOMBIE);
                } else if (entity instanceof Creeper) {
                    partToDrop = getPartItem(PartType.HEAD, PartMaterial.CREEPER);
                } else if (entity instanceof EnderMan) {
                    partToDrop = getPartItem(PartType.HANDLE, PartMaterial.ENDERMAN);
                }

                if (partToDrop != null) {
                    entity.spawnAtLocation(serverLevel, new ItemStack(partToDrop));
                    serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                            net.minecraft.sounds.SoundSource.HOSTILE, 1.0f, 1.4f);
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,
                            entity.getX(), entity.getY() + 0.5, entity.getZ(),
                            10, 0.2, 0.2, 0.2, 0.05);
                }
            }
        });
    }

    public static boolean isPartTypeAllowed(PartMaterial material, PartType partType) {
        return switch (material) {
            case SPIDER -> partType == PartType.EYE;
            case SKELETON -> partType == PartType.HANDLE;
            case ZOMBIE -> partType == PartType.GRIP;
            case CREEPER -> partType == PartType.HEAD;
            case ENDERMAN -> partType == PartType.HANDLE;
            default -> true;
        };
    }

    public static ToolPartItem getPartItem(PartType partType, PartMaterial material) {
        String name = partType.getPartName() + "_" + material.getMaterialName();
        return PART_ITEMS.get(name);
    }
}
