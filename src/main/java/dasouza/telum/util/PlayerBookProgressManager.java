package dasouza.telum.util;

import dasouza.telum.network.SyncBookProgressPayload;
import dasouza.telum.tool.PartMaterial;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerBookProgressManager {

    private static final Map<UUID, Set<String>> CRAFTED_MATERIALS = new ConcurrentHashMap<>(); // Parts acquired (cleans dirt)
    private static final Map<UUID, Set<String>> CRAFTED_TOOLS = new ConcurrentHashMap<>();     // Tools acquired (marks page background done)

    private PlayerBookProgressManager() {}

    public static Set<String> getCraftedMaterials(UUID uuid) {
        return CRAFTED_MATERIALS.getOrDefault(uuid, Collections.emptySet());
    }

    public static Set<String> getCraftedTools(UUID uuid) {
        return CRAFTED_TOOLS.getOrDefault(uuid, Collections.emptySet());
    }

    public static boolean markMaterialCrafted(ServerPlayer player, PartMaterial material) {
        if (material == null) return false;
        return markMaterialCrafted(player, material.getMaterialName());
    }

    public static boolean markMaterialCrafted(ServerPlayer player, String materialName) {
        if (materialName == null || materialName.isEmpty()) return false;
        UUID uuid = player.getUUID();
        Set<String> mats = CRAFTED_MATERIALS.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
        if (mats.add(materialName.toLowerCase(Locale.ROOT))) {
            syncToPlayer(player);
            return true;
        }
        return false;
    }

    public static boolean markToolCrafted(ServerPlayer player, PartMaterial material) {
        if (material == null) return false;
        return markToolCrafted(player, material.getMaterialName());
    }

    private static final List<String> ALL_MATERIAL_NAMES = List.of(
            "wood", "stone", "copper", "iron", "gold", "diamond", "netherite", "emerald", "amethyst",
            "prismarine", "blaze", "spider", "skeleton", "zombie", "creeper", "enderman", "wind", "sulfur", "greed", "skulk"
    );

    public static boolean isAllMaterialsDone(UUID uuid) {
        Set<String> tools = CRAFTED_TOOLS.get(uuid);
        if (tools == null || tools.size() < ALL_MATERIAL_NAMES.size()) return false;
        for (String mat : ALL_MATERIAL_NAMES) {
            if (!tools.contains(mat)) return false;
        }
        return true;
    }

    public static boolean markToolCrafted(ServerPlayer player, String materialName) {
        if (materialName == null || materialName.isEmpty()) return false;
        UUID uuid = player.getUUID();
        Set<String> tools = CRAFTED_TOOLS.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
        boolean addedTool = tools.add(materialName.toLowerCase(Locale.ROOT));
        // Add material directly to avoid double sync from markMaterialCrafted()
        Set<String> mats = CRAFTED_MATERIALS.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet());
        boolean addedMat = mats.add(materialName.toLowerCase(Locale.ROOT));
        if (addedTool || addedMat) {
            syncToPlayer(player);
            if (isAllMaterialsDone(uuid)) {
                dasouza.telum.advancement.TelumAdvancements.grantAdvancement(player, dasouza.telum.Telum.id("complete_book"));
            }
            return true;
        }
        return false;
    }

    public static void syncToPlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        List<String> craftedList = new ArrayList<>(getCraftedMaterials(uuid));
        List<String> toolsList = new ArrayList<>(getCraftedTools(uuid));
        ServerPlayNetworking.send(player, new SyncBookProgressPayload(craftedList, toolsList));
    }

    /** Called on player disconnect to prevent memory leaks */
    public static void clearPlayerData(UUID uuid) {
        CRAFTED_MATERIALS.remove(uuid);
        CRAFTED_TOOLS.remove(uuid);
    }
}
