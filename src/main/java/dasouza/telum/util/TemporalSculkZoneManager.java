package dasouza.telum.util;

import dasouza.telum.Telum;
import dasouza.telum.block.TelumBlocks;
import dasouza.telum.particle.TelumParticles;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side manager for Temporal Sculk Zones.
 * Features NBT persistence across world save/reload,
 * clock particle rain & circular ring indicators around the lectern,
 * wide-area block backup/restoration, player death cancellation, and lectern item dispensing.
 */
public final class TemporalSculkZoneManager {

    private static final Map<BlockPos, TemporalZone> ACTIVE_ZONES = new ConcurrentHashMap<>();

    private TemporalSculkZoneManager() {}

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(TemporalSculkZoneManager::onServerTick);
        ServerLifecycleEvents.SERVER_STARTED.register(TemporalSculkZoneManager::loadZones);
        ServerLifecycleEvents.SERVER_STOPPING.register(TemporalSculkZoneManager::saveZones);
        Telum.LOGGER.info("Initialized TemporalSculkZoneManager with world persistence");
    }

    public static boolean isZoneActiveAt(BlockPos lecternPos) {
        TemporalZone zone = ACTIVE_ZONES.get(lecternPos);
        return zone != null && !zone.isDispensing;
    }

    public static boolean hasActiveZoneFor(UUID playerUuid) {
        for (TemporalZone zone : ACTIVE_ZONES.values()) {
            if (zone.playerUuid.equals(playerUuid) && !zone.isDispensing) {
                return true;
            }
        }
        return false;
    }

    public static TemporalZone getZoneForPlayer(UUID playerUuid) {
        for (TemporalZone zone : ACTIVE_ZONES.values()) {
            if (zone.playerUuid.equals(playerUuid)) {
                return zone;
            }
        }
        return null;
    }

    public static TemporalZone getZoneNear(BlockPos pos, int radius) {
        for (TemporalZone zone : ACTIVE_ZONES.values()) {
            if (!zone.isDispensing && zone.lecternPos.closerThan(pos, radius)) {
                return zone;
            }
        }
        return null;
    }

    /**
     * Attempts to buffer a Sculk Tool Part item into an active nearby zone.
     */
    public static boolean bufferItem(ServerLevel level, BlockPos pos, ItemStack stack) {
        TemporalZone zone = getZoneNear(pos, 110);
        if (zone != null && !zone.isDispensing) {
            zone.bufferedToolParts.add(stack.copy());
            
            // Visual & Sound Feedback for item buffering
            level.sendParticles(ParticleTypes.SCULK_SOUL,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    12, 0.3, 0.3, 0.3, 0.05);
            level.playSound(null, pos, SoundEvents.SCULK_BLOCK_CHARGE, SoundSource.BLOCKS, 1.0f, 1.2f);
            
            saveZones(level.getServer());
            return true;
        }
        return false;
    }

    /**
     * Starts a new Temporal Sculk Zone centered at the given lectern pos.
     */
    public static void startZone(ServerPlayer player, BlockPos lecternPos) {
        ServerLevel level = (ServerLevel) player.level();

        // If zone already exists at this lectern, end it normally
        if (ACTIVE_ZONES.containsKey(lecternPos)) {
            endZoneNormally(player, lecternPos);
            return;
        }

        TemporalZone zone = new TemporalZone(lecternPos, player.getUUID(), level);
        RandomSource rng = level.getRandom();

        int radiusXZ = 45;
        int radiusY = 15;

        BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();

        int minX = lecternPos.getX() - radiusXZ;
        int maxX = lecternPos.getX() + radiusXZ;
        int minY = Math.max(level.getMinY(), lecternPos.getY() - radiusY);
        int maxY = Math.min(level.getMaxY(), lecternPos.getY() + radiusY);
        int minZ = lecternPos.getZ() - radiusXZ;
        int maxZ = lecternPos.getZ() + radiusXZ;

        // Place saved Temporal Structures (City Center, Barracks, Ice Box, Statues)
        placeTemporalStructures(level, zone, lecternPos);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mut.set(x, y, z);
                    BlockState state = level.getBlockState(mut);

                    if (state.is(Blocks.SCULK_SHRIEKER)) {
                        BlockPos freezePos = mut.immutable();
                        if (!level.getBlockState(freezePos.below()).isAir()) {
                            if (!zone.originalBlockStates.containsKey(freezePos)) {
                                zone.originalBlockStates.put(freezePos, state);
                            }
                            level.setBlock(freezePos, TelumBlocks.SCULK_TEMPORAL_SHRIEKER.defaultBlockState(), 3);
                            trySpawnRareTemporalBarrelNearShrieker(level, zone, freezePos, rng);
                        }
                    } else if (state.is(Blocks.SCULK)) {
                        BlockPos freezePos = mut.immutable();

                        // ABSOLUTE CEILING AVOIDANCE
                        if (level.getBlockState(freezePos.below()).isAir()) {
                            continue;
                        }

                        boolean isFloor = level.getBlockState(freezePos.above()).isAir();
                        float roll = rng.nextFloat();

                        if (isFloor) {
                            if (roll < 0.025f) {
                                if (!zone.originalBlockStates.containsKey(freezePos)) {
                                    zone.originalBlockStates.put(freezePos, state);
                                }
                                level.setBlock(freezePos, TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK.defaultBlockState(), 3);
                            } else if (roll < 0.031f) {
                                BlockPos abovePos = freezePos.above();
                                BlockState aboveState = level.getBlockState(abovePos);
                                if (aboveState.isAir()) {
                                    if (!zone.originalBlockStates.containsKey(abovePos)) {
                                        zone.originalBlockStates.put(abovePos, aboveState);
                                    }
                                    level.setBlock(abovePos, TelumBlocks.SCULK_TEMPORAL_SHRIEKER.defaultBlockState(), 3);
                                    trySpawnRareTemporalBarrelNearShrieker(level, zone, abovePos, rng);
                                }
                            }
                        } else {
                            if (roll < 0.002f) {
                                if (!zone.originalBlockStates.containsKey(freezePos)) {
                                    zone.originalBlockStates.put(freezePos, state);
                                }
                                level.setBlock(freezePos, TelumBlocks.SUSPICIOUS_TEMPORAL_SCULK.defaultBlockState(), 3);
                            } else if (roll < 0.0025f) {
                                BlockPos abovePos = freezePos.above();
                                BlockState aboveState = level.getBlockState(abovePos);
                                if (aboveState.isAir()) {
                                    if (!zone.originalBlockStates.containsKey(abovePos)) {
                                        zone.originalBlockStates.put(abovePos, aboveState);
                                    }
                                    level.setBlock(abovePos, TelumBlocks.SCULK_TEMPORAL_SHRIEKER.defaultBlockState(), 3);
                                    trySpawnRareTemporalBarrelNearShrieker(level, zone, abovePos, rng);
                                }
                            }
                        }
                    } else {
                        // Check for Marble & Deepslate conversion chance (~20%)
                        float roll = rng.nextFloat();
                        if (roll < 0.20f) {
                            BlockState tempVariant = getTemporalVariantForState(state, rng);
                            if (tempVariant != null) {
                                BlockPos freezePos = mut.immutable();
                                if (!zone.originalBlockStates.containsKey(freezePos)) {
                                    zone.originalBlockStates.put(freezePos, state);
                                }
                                level.setBlock(freezePos, tempVariant, 3);
                            }
                        }
                    }
                }
            }
        }

        ACTIVE_ZONES.put(lecternPos, zone);
        saveZones(level.getServer());

        // Activation sound & particle burst
        level.playSound(null, lecternPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 2.0f, 0.8f);
        level.playSound(null, lecternPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0f, 0.6f);

        spawnActivationBurst(level, lecternPos);
    }

    private static void spawnActivationBurst(ServerLevel level, BlockPos lecternPos) {
        RandomSource rng = level.getRandom();
        for (int i = 0; i < 40; i++) {
            double ox = (rng.nextDouble() - 0.5) * 3.0;
            double oy = rng.nextDouble() * 3.0;
            double oz = (rng.nextDouble() - 0.5) * 3.0;
            level.sendParticles(TelumParticles.CLOCK_PARTICLE,
                    lecternPos.getX() + 0.5 + ox,
                    lecternPos.getY() + 1.0 + oy,
                    lecternPos.getZ() + 0.5 + oz,
                    1, 0, 0, 0, 0.05);
        }
    }

    /**
     * Safely ends the zone upon player request/re-playing song: restores blocks & enters item dispensing mode.
     */
    public static void endZoneNormally(ServerPlayer player, BlockPos lecternPos) {
        TemporalZone zone = ACTIVE_ZONES.get(lecternPos);
        if (zone == null) return;

        zone.restoreBlocks();

        if (zone.bufferedToolParts.isEmpty()) {
            ACTIVE_ZONES.remove(lecternPos);
            ServerLevel level = zone.level;
            level.playSound(null, lecternPos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.2f, 1.0f);
        } else {
            zone.isDispensing = true;
            zone.dispenseTimer = 10; // Start dispensing after short delay
        }

        saveZones(player.level().getServer());
    }

    /**
     * Cancels zone on player death: restores blocks & destroys all buffered items.
     */
    public static void endZoneOnDeath(TemporalZone zone) {
        zone.restoreBlocks();
        zone.bufferedToolParts.clear(); // Items lost!

        ServerLevel level = zone.level;
        BlockPos pos = zone.lecternPos;

        level.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.BLOCKS, 2.0f, 0.5f);
        for (int i = 0; i < 25; i++) {
            double ox = (level.getRandom().nextDouble() - 0.5) * 2.0;
            double oy = level.getRandom().nextDouble() * 2.0;
            double oz = (level.getRandom().nextDouble() - 0.5) * 2.0;
            level.sendParticles(ParticleTypes.SMOKE,
                    pos.getX() + 0.5 + ox, pos.getY() + 1.0 + oy, pos.getZ() + 0.5 + oz,
                    1, 0, 0, 0, 0.05);
        }

        ACTIVE_ZONES.remove(zone.lecternPos);
        saveZones(level.getServer());
    }

    private static void onServerTick(MinecraftServer server) {
        Iterator<Map.Entry<BlockPos, TemporalZone>> iter = ACTIVE_ZONES.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<BlockPos, TemporalZone> entry = iter.next();
            TemporalZone zone = entry.getValue();
            ServerLevel level = zone.level;

            if (!zone.isDispensing) {
                zone.ticksActive++;

                // Check player status
                ServerPlayer player = server.getPlayerList().getPlayer(zone.playerUuid);
                if (player == null || player.isDeadOrDying() || player.getHealth() <= 0.0f) {
                    endZoneOnDeath(zone);
                    continue;
                }

                // Clock particle circle & rain emission every 3 ticks
                if (zone.ticksActive % 3 == 0) {
                    BlockPos lpos = zone.lecternPos;

                    // Central Circle around Lectern (Radius 2.0)
                    int circlePoints = 20;
                    double innerRadius = 2.0;
                    for (int i = 0; i < circlePoints; i++) {
                        double angle = i * (2.0 * Math.PI / circlePoints);
                        double cx = lpos.getX() + 0.5 + Math.cos(angle) * innerRadius;
                        double cy = lpos.getY() + 0.25;
                        double cz = lpos.getZ() + 0.5 + Math.sin(angle) * innerRadius;
                        level.sendParticles(TelumParticles.CLOCK_PARTICLE, cx, cy, cz, 1, 0, 0.01, 0, 0.01);
                    }

                    // 3. Clock Particle Rain falling across the ENTIRE Temporal Zone (160x160 area)
                    RandomSource rng = level.getRandom();

                    // Rain across the full zone radius (up to 80 blocks around lectern)
                    for (int i = 0; i < 18; i++) {
                        double rx = lpos.getX() + 0.5 + (rng.nextDouble() - 0.5) * 160.0;
                        double ry = lpos.getY() + 10.0 + rng.nextDouble() * 8.0;
                        double rz = lpos.getZ() + 0.5 + (rng.nextDouble() - 0.5) * 160.0;
                        // count = 0 passes exact downward velocity dy = -0.14
                        level.sendParticles(TelumParticles.CLOCK_PARTICLE, rx, ry, rz, 0, 0.0, -0.14, 0.0, 1.0);
                    }

                    // Dense rain around the player as they explore the Ancient City
                    if (player != null && player.level() == level) {
                        for (int i = 0; i < 18; i++) {
                            double rx = player.getX() + (rng.nextDouble() - 0.5) * 35.0;
                            double ry = player.getY() + 8.0 + rng.nextDouble() * 6.0;
                            double rz = player.getZ() + (rng.nextDouble() - 0.5) * 35.0;
                            level.sendParticles(TelumParticles.CLOCK_PARTICLE, rx, ry, rz, 0, 0.0, -0.14, 0.0, 1.0);
                        }
                    }
                }
            } else {
                // Dispensing mode: drop 1 item every 20 ticks (1 second)
                zone.dispenseTimer--;
                if (zone.dispenseTimer <= 0) {
                    if (!zone.bufferedToolParts.isEmpty()) {
                        ItemStack itemToDispense = zone.bufferedToolParts.remove(0);
                        BlockPos pos = zone.lecternPos;

                        ItemEntity entity = new ItemEntity(level,
                                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                                itemToDispense);
                        entity.setDeltaMovement(
                                (level.getRandom().nextDouble() - 0.5) * 0.1,
                                0.25,
                                (level.getRandom().nextDouble() - 0.5) * 0.1
                        );
                        level.addFreshEntity(entity);

                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.2f);
                        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8f, 1.4f);
                        level.sendParticles(ParticleTypes.END_ROD,
                                pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                                8, 0.2, 0.2, 0.2, 0.05);

                        zone.dispenseTimer = 20; // 1 second interval
                        saveZones(server);
                    } else {
                        // Finished dispensing!
                        level.playSound(null, zone.lecternPos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.5f);
                        iter.remove();
                        saveZones(server);
                    }
                }
            }
        }
    }

    // World Save & Persistence Methods

    private static Path getSaveFilePath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("telum_temporal_sculk_zones.dat");
    }

    public static void saveZones(MinecraftServer server) {
        if (server == null) return;
        try {
            Path file = getSaveFilePath(server);
            CompoundTag root = new CompoundTag();
            ListTag zonesList = new ListTag();

            for (TemporalZone zone : ACTIVE_ZONES.values()) {
                CompoundTag ztag = new CompoundTag();
                ztag.putLong("LecternPos", zone.lecternPos.asLong());
                ztag.putString("PlayerUuid", zone.playerUuid.toString());
                ztag.putBoolean("IsDispensing", zone.isDispensing);
                ztag.putInt("DispenseTimer", zone.dispenseTimer);
                ztag.putInt("TicksActive", zone.ticksActive);

                // Save Original Block States
                ListTag blocksList = new ListTag();
                for (Map.Entry<BlockPos, BlockState> bentry : zone.originalBlockStates.entrySet()) {
                    CompoundTag btag = new CompoundTag();
                    btag.putLong("Pos", bentry.getKey().asLong());
                    btag.putInt("StateId", Block.getId(bentry.getValue()));
                    blocksList.add(btag);
                }
                ztag.put("OriginalBlocks", blocksList);

                // Save Buffered Items
                ListTag itemsList = new ListTag();
                for (ItemStack stack : zone.bufferedToolParts) {
                    Tag itemTag = ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, stack).getOrThrow();
                    itemsList.add(itemTag);
                }
                ztag.put("BufferedItems", itemsList);

                zonesList.add(ztag);
            }

            root.put("Zones", zonesList);
            NbtIo.writeCompressed(root, file);
        } catch (Exception e) {
            Telum.LOGGER.error("Failed to save Temporal Sculk Zones to file", e);
        }
    }

    public static void loadZones(MinecraftServer server) {
        if (server == null) return;
        ACTIVE_ZONES.clear();
        try {
            Path file = getSaveFilePath(server);
            if (!Files.exists(file)) return;

            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            ListTag zonesList = root.getList("Zones").orElseGet(ListTag::new);

            ServerLevel overworld = server.overworld();

            for (int i = 0; i < zonesList.size(); i++) {
                CompoundTag ztag = zonesList.getCompound(i).orElseGet(CompoundTag::new);
                BlockPos lecternPos = BlockPos.of(ztag.getLongOr("LecternPos", 0L));
                String uuidStr = ztag.getStringOr("PlayerUuid", "");
                if (uuidStr.isEmpty()) continue;

                UUID playerUuid = UUID.fromString(uuidStr);

                ServerLevel level = overworld;
                ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
                if (player != null && player.level() instanceof ServerLevel sl) {
                    level = sl;
                }

                TemporalZone zone = new TemporalZone(lecternPos, playerUuid, level);
                zone.isDispensing = ztag.getBooleanOr("IsDispensing", false);
                zone.dispenseTimer = ztag.getIntOr("DispenseTimer", 0);
                zone.ticksActive = ztag.getIntOr("TicksActive", 0);

                // Restore Original Block States map
                ListTag blocksList = ztag.getList("OriginalBlocks").orElseGet(ListTag::new);
                for (int j = 0; j < blocksList.size(); j++) {
                    CompoundTag btag = blocksList.getCompound(j).orElseGet(CompoundTag::new);
                    BlockPos bpos = BlockPos.of(btag.getLongOr("Pos", 0L));
                    int stateId = btag.getIntOr("StateId", 0);
                    BlockState origState = Block.stateById(stateId);
                    zone.originalBlockStates.put(bpos, origState);
                }

                // Restore Buffered Items
                ListTag itemsList = ztag.getList("BufferedItems").orElseGet(ListTag::new);
                for (int k = 0; k < itemsList.size(); k++) {
                    Tag itemTag = itemsList.get(k);
                    ItemStack stack = ItemStack.CODEC.parse(NbtOps.INSTANCE, itemTag).result().orElse(ItemStack.EMPTY);
                    if (!stack.isEmpty()) {
                        zone.bufferedToolParts.add(stack);
                    }
                }

                ACTIVE_ZONES.put(lecternPos, zone);
            }

            Telum.LOGGER.info("Successfully restored " + ACTIVE_ZONES.size() + " active Temporal Sculk Zones from save file.");
        } catch (Exception e) {
            Telum.LOGGER.error("Failed to load Temporal Sculk Zones from file", e);
        }
    }

    public static class TemporalZone {
        public final BlockPos lecternPos;
        public final UUID playerUuid;
        public ServerLevel level;
        public final Map<BlockPos, BlockState> originalBlockStates = new HashMap<>();
        public final List<ItemStack> bufferedToolParts = new ArrayList<>();
        public boolean isDispensing = false;
        public int dispenseTimer = 0;
        public int ticksActive = 0;

        public TemporalZone(BlockPos lecternPos, UUID playerUuid, ServerLevel level) {
            this.lecternPos = lecternPos;
            this.playerUuid = playerUuid;
            this.level = level;
        }

        public void restoreBlocks() {
            for (Map.Entry<BlockPos, BlockState> entry : originalBlockStates.entrySet()) {
                BlockPos pos = entry.getKey();
                BlockState origState = entry.getValue();
                level.setBlock(pos, origState, 3);
            }
            originalBlockStates.clear();
        }
    }

    private static String getTemporalPathFor(String origPath) {
        if (origPath.contains("city_center_1")) return "ancient_city/city_center/city_center_1_temporal";
        if (origPath.contains("city_center_2")) return "ancient_city/city_center/city_center_2_temporal";
        if (origPath.contains("barracks")) return "ancient_city/structures/barracks_temporal";
        if (origPath.contains("ice_box")) return "ancient_city/structures/ice_box_1_temporal";
        if (origPath.contains("small_statue")) return "ancient_city/structures/small_statue_temporal";
        return null;
    }

    private static void placeTemporalStructures(ServerLevel level, TemporalZone zone, BlockPos lecternPos) {
        StructureTemplateManager manager = level.getStructureManager();

        StructureStart start = findAncientCityStructureStart(level, lecternPos);
        if (start != null && start.isValid()) {
            List<StructurePiece> pieces = start.getPieces();
            Telum.LOGGER.info("Ancient City structure start found with " + pieces.size() + " pieces around " + lecternPos);

            for (StructurePiece piece : pieces) {
                String templatePath = null;

                if (piece instanceof PoolElementStructurePiece poolPiece) {
                    if (poolPiece.getElement() instanceof SinglePoolElement singlePoolElement) {
                        Identifier loc = singlePoolElement.getTemplateLocation();
                        if (loc != null) {
                            templatePath = loc.getPath();
                        }
                    }
                }

                if (templatePath == null) {
                    templatePath = piece.toString().toLowerCase();
                }

                String temporalPath = getTemporalPathFor(templatePath);
                if (temporalPath == null) continue;

                List<Identifier> candidateIds = List.of(
                        Identifier.fromNamespaceAndPath("telum", temporalPath),
                        Identifier.fromNamespaceAndPath("sordia", temporalPath)
                );

                for (Identifier id : candidateIds) {
                    Optional<StructureTemplate> opt = manager.get(id);
                    if (opt.isPresent()) {
                        StructureTemplate template = opt.get();
                        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true);

                        if (piece.getRotation() != null) {
                            settings.setRotation(piece.getRotation());
                        }
                        if (piece.getMirror() != null) {
                            settings.setMirror(piece.getMirror());
                        }

                        BlockPos placePos;
                        if (piece instanceof PoolElementStructurePiece poolPiece) {
                            placePos = poolPiece.getPosition();
                        } else {
                            BoundingBox pieceBox = piece.getBoundingBox();
                            BlockPos minBoxPos = new BlockPos(pieceBox.minX(), pieceBox.minY(), pieceBox.minZ());
                            placePos = template.getZeroPositionWithTransform(minBoxPos, settings.getMirror(), settings.getRotation());
                        }

                        // Centroid alignment for City Center
                        if (temporalPath.contains("city_center")) {
                            BoundingBox pieceBox = piece.getBoundingBox();
                            long sumWorldX = 0, sumWorldY = 0, sumWorldZ = 0;
                            int worldCount = 0;
                            for (int x = pieceBox.minX(); x <= pieceBox.maxX(); x++) {
                                for (int y = pieceBox.minY(); y <= pieceBox.maxY(); y++) {
                                    for (int z = pieceBox.minZ(); z <= pieceBox.maxZ(); z++) {
                                        BlockPos p = new BlockPos(x, y, z);
                                        if (level.getBlockState(p).is(Blocks.REINFORCED_DEEPSLATE)) {
                                            sumWorldX += x;
                                            sumWorldY += y;
                                            sumWorldZ += z;
                                            worldCount++;
                                        }
                                    }
                                }
                            }

                            List<StructureTemplate.StructureBlockInfo> templateReinforced = new ArrayList<>(template.filterBlocks(placePos, settings, Blocks.REINFORCED_DEEPSLATE));
                            long sumTempX = 0, sumTempY = 0, sumTempZ = 0;
                            int tempCount = 0;
                            for (StructureTemplate.StructureBlockInfo info : templateReinforced) {
                                sumTempX += info.pos().getX();
                                sumTempY += info.pos().getY();
                                sumTempZ += info.pos().getZ();
                                tempCount++;
                            }

                            if (worldCount > 0 && tempCount > 0) {
                                BlockPos worldCentroid = new BlockPos((int)(sumWorldX / worldCount), (int)(sumWorldY / worldCount), (int)(sumWorldZ / worldCount));
                                BlockPos tempCentroid = new BlockPos((int)(sumTempX / tempCount), (int)(sumTempY / tempCount), (int)(sumTempZ / tempCount));
                                BlockPos alignmentShift = worldCentroid.subtract(tempCentroid);
                                placePos = placePos.offset(alignmentShift);
                                Telum.LOGGER.info("Applied portal centroid alignment shift: " + alignmentShift);
                            }
                        }

                        applyStructureTemplate(level, zone, template, placePos, settings);
                        Telum.LOGGER.info("Matched & placed temporal structure: " + id + " over piece " + templatePath + " at " + placePos + " with rotation " + settings.getRotation());
                        break;
                    }
                }
            }
        } else {
            Telum.LOGGER.warn("Ancient City structure start not found near " + lecternPos + ", falling back to lectern relative placement.");
            List<Identifier> cityCenterIds = List.of(
                    Identifier.fromNamespaceAndPath("telum", "ancient_city/city_center/city_center_1_temporal"),
                    Identifier.fromNamespaceAndPath("sordia", "ancient_city/city_center/city_center_1_temporal")
            );
            for (Identifier id : cityCenterIds) {
                Optional<StructureTemplate> opt = manager.get(id);
                if (opt.isPresent()) {
                    StructureTemplate template = opt.get();
                    Vec3i size = template.getSize();
                    BlockPos origin = lecternPos.offset(-size.getX() / 2, -1, -size.getZ() / 2);
                    applyStructureTemplate(level, zone, template, origin, new StructurePlaceSettings().setIgnoreEntities(true));
                    break;
                }
            }
        }
    }

    private static StructureStart findAncientCityStructureStart(ServerLevel level, BlockPos originPos) {
        try {
            var structureRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            var ancientCityHolder = structureRegistry.get(ResourceKey.create(
                    Registries.STRUCTURE,
                    Identifier.fromNamespaceAndPath("minecraft", "ancient_city")
            ));

            if (ancientCityHolder.isPresent()) {
                var structure = ancientCityHolder.get().value();
                StructureStart start = level.structureManager().getStructureAt(originPos, structure);

                if (start != null && start.isValid()) return start;

                int chunkX = SectionPos.blockToSectionCoord(originPos.getX());
                int chunkZ = SectionPos.blockToSectionCoord(originPos.getZ());

                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        int cx = chunkX + dx;
                        int cz = chunkZ + dz;
                        var chunk = level.getChunkSource().getChunk(cx, cz, net.minecraft.world.level.chunk.status.ChunkStatus.FULL, false);
                        if (chunk != null) {
                            start = level.structureManager().getStartForStructure(
                                    SectionPos.of(new ChunkPos(cx, cz), 0),
                                    structure,
                                    chunk
                            );
                            if (start != null && start.isValid()) return start;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Telum.LOGGER.warn("Failed to find Ancient City structure start: " + e.getMessage());
        }
        return null;
    }

    private static void applyStructureTemplate(ServerLevel level, TemporalZone zone, StructureTemplate template, BlockPos origin, StructurePlaceSettings settings) {
        BoundingBox box = template.getBoundingBox(settings, origin);
        Set<Block> PROTECTED = Set.of(
                Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.BARREL,
                Blocks.SHULKER_BOX, Blocks.ENDER_CHEST,
                Blocks.SPAWNER, Blocks.TRIAL_SPAWNER
        );

        Map<BlockPos, BlockState> protectedBlocks = new HashMap<>();

        BlockPos.betweenClosedStream(box).forEach(pos -> {
            BlockPos immutablePos = pos.immutable();
            BlockState state = level.getBlockState(immutablePos);
            if (!zone.originalBlockStates.containsKey(immutablePos)) {
                zone.originalBlockStates.put(immutablePos, state);
            }
            if (PROTECTED.contains(state.getBlock())) {
                protectedBlocks.put(immutablePos, state);
            }
        });

        // Place structure in world using exact settings (rotation/mirror)
        template.placeInWorld(level, origin, origin, settings, level.getRandom(), 2);

        // Restore protected chests and spawners so player loot is not destroyed
        protectedBlocks.forEach((pos, state) -> level.setBlock(pos, state, 3));

        // Replace leftover Structure Blocks / Jigsaws / Structure Voids with Temporal Deepslate Bricks
        BlockPos.betweenClosedStream(box).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.STRUCTURE_BLOCK) || state.is(Blocks.JIGSAW) || state.is(Blocks.STRUCTURE_VOID)) {
                level.setBlock(pos, TelumBlocks.TEMPORAL_DEEPSLATE_BRICK.defaultBlockState(), 3);
            }
        });
    }

    private static void trySpawnRareTemporalBarrelNearShrieker(ServerLevel level, TemporalZone zone, BlockPos shriekerPos, RandomSource rng) {
        if (rng.nextFloat() < 0.05f) {
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos barrelPos = shriekerPos.relative(dir);
                if (level.getBlockState(barrelPos).isAir() && !level.getBlockState(barrelPos.below()).isAir()) {
                    if (!zone.originalBlockStates.containsKey(barrelPos)) {
                        zone.originalBlockStates.put(barrelPos, level.getBlockState(barrelPos));
                    }
                    level.setBlock(barrelPos, TelumBlocks.TEMPORAL_BARREL.defaultBlockState(), 3);
                    Telum.LOGGER.info("Rare temporal barrel spawned near temporal shrieker at " + barrelPos);
                    break;
                }
            }
        }
    }

    private static BlockState getTemporalVariantForState(BlockState state, RandomSource rng) {
        if (state.is(TelumBlocks.MARMOL_BLOCK)) return TelumBlocks.YELLOW_MARMOL_BLOCK.defaultBlockState();
        if (state.is(TelumBlocks.MARMOL_BRICKS)) return TelumBlocks.YELLOW_MARMOL_BRICKS.defaultBlockState();
        if (state.is(TelumBlocks.MARMOL_GILDED_BLOCK)) return TelumBlocks.YELLOW_MARMOL_GILDED_BLOCK.defaultBlockState();
        if (state.is(TelumBlocks.MARMOL_PILLAR)) return TelumBlocks.YELLOW_MARMOL_PILLAR.defaultBlockState();
        if (state.is(TelumBlocks.MARMOL_SLAB)) return TelumBlocks.YELLOW_MARMOL_SLAB.defaultBlockState();
        if (state.is(TelumBlocks.MARMOL_STAIRS)) return TelumBlocks.YELLOW_MARMOL_STAIRS.defaultBlockState();
        if (state.is(TelumBlocks.MARMOL_WALL)) return TelumBlocks.YELLOW_MARMOL_WALL.defaultBlockState();
        if (state.is(TelumBlocks.MARMOL_BRICK_SLAB)) return TelumBlocks.YELLOW_MARMOL_BRICK_SLAB.defaultBlockState();
        if (state.is(TelumBlocks.MARMOL_BRICK_STAIRS)) return TelumBlocks.YELLOW_MARMOL_BRICK_STAIRS.defaultBlockState();
        if (state.is(TelumBlocks.MARMOL_BRICK_WALL)) return TelumBlocks.YELLOW_MARMOL_BRICK_WALL.defaultBlockState();

        if (state.is(Blocks.DEEPSLATE) || state.is(Blocks.COBBLED_DEEPSLATE) ||
            state.is(Blocks.POLISHED_DEEPSLATE) || state.is(Blocks.DEEPSLATE_BRICKS) ||
            state.is(Blocks.DEEPSLATE_TILES)) {
            float r = rng.nextFloat();
            if (r < 0.35f) return TelumBlocks.DEEPSLATE_TEMPORAL_POLISHED.defaultBlockState();
            if (r < 0.70f) return TelumBlocks.DEEPSLATE_TEMPORAL_TILES.defaultBlockState();
            return TelumBlocks.TEMPORAL_DEEPSLATE_BRICK.defaultBlockState();
        }
        return null;
    }
}
