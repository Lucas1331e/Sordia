package dasouza.telum.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClientBookProgress {

    private static final Set<String> CRAFTED_MATERIALS = new HashSet<>(); // Parts acquired (cleans dirt)
    private static final Set<String> CRAFTED_TOOLS = new HashSet<>();     // Tools acquired (marks page background done)

    private ClientBookProgress() {}

    public static void setProgress(List<String> crafted, List<String> tools) {
        CRAFTED_MATERIALS.clear();
        if (crafted != null) {
            for (String s : crafted) {
                if (s != null) CRAFTED_MATERIALS.add(s.toLowerCase());
            }
        }

        CRAFTED_TOOLS.clear();
        if (tools != null) {
            for (String s : tools) {
                if (s != null) CRAFTED_TOOLS.add(s.toLowerCase());
            }
        }
    }

    public static boolean isMaterialCrafted(String materialName) {
        if ("skulk".equalsIgnoreCase(materialName)) return true; // Skulk never has dirt!
        return materialName != null && CRAFTED_MATERIALS.contains(materialName.toLowerCase());
    }

    public static boolean isToolCrafted(String materialName) {
        return materialName != null && CRAFTED_TOOLS.contains(materialName.toLowerCase());
    }
}
