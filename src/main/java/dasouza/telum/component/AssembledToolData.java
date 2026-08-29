package dasouza.telum.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dasouza.telum.tool.ToolType;

import java.util.List;

/**
 * Data component stored on assembled tool items.
 * Contains the tool type, the list of all parts used (variable count), and computed stats.
 */
public record AssembledToolData(
        ToolType toolType,
        List<ToolPartData> parts,
        int durability,
        float attackDamage,
        float attackSpeed,
        float miningSpeed,
        int miningLevel
) {
    public static final Codec<AssembledToolData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ToolType.CODEC.fieldOf("tool_type").forGetter(AssembledToolData::toolType),
                    ToolPartData.CODEC.listOf().fieldOf("parts").forGetter(AssembledToolData::parts),
                    Codec.INT.fieldOf("durability").forGetter(AssembledToolData::durability),
                    Codec.FLOAT.fieldOf("attack_damage").forGetter(AssembledToolData::attackDamage),
                    Codec.FLOAT.fieldOf("attack_speed").forGetter(AssembledToolData::attackSpeed),
                    Codec.FLOAT.fieldOf("mining_speed").forGetter(AssembledToolData::miningSpeed),
                    Codec.INT.fieldOf("mining_level").forGetter(AssembledToolData::miningLevel)
            ).apply(instance, AssembledToolData::new)
    );

    public int getAverageMaterialLevel() {
        if (parts.isEmpty()) return 1;
        float sum = 0;
        for (ToolPartData p : parts) {
            sum += p.material().getMiningLevel();
        }
        return Math.round(sum / parts.size());
    }

    public int getMaterialLevel(dasouza.telum.tool.PartMaterial material) {
        if (parts == null || parts.isEmpty()) return 0;
        int count = 0;
        for (ToolPartData p : parts) {
            if (p.material() == material) {
                count++;
            }
        }
        return count;
    }

    public ToolPartData getPart(dasouza.telum.tool.PartType partType) {
        if (parts == null || parts.isEmpty()) return null;
        for (ToolPartData p : parts) {
            if (p.partType() == partType) {
                return p;
            }
        }
        return null;
    }
}
