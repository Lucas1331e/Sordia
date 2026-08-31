package dasouza.telum.tool;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

import java.util.List;

/**
 * Defines each type of tool/weapon and the 3 part types required to assemble it.
 */
public enum ToolType implements StringRepresentable {
    PICKAXE("pickaxe", PartType.HANDLE, PartType.EYE, PartType.HEAD,
            1.5f, 1.2f, 6.0f),
    SWORD("sword", PartType.GRIP, PartType.EYE, PartType.BLADE,
            3.0f, 1.6f, 1.0f),
    AXE("axe", PartType.HANDLE, PartType.GRIP, PartType.HEAD,
            5.0f, 0.8f, 5.0f),
    SHOVEL("shovel", PartType.HANDLE, PartType.GRIP, PartType.HEAD,
            1.5f, 1.0f, 5.0f),
    HOE("hoe", PartType.HANDLE, PartType.EYE, PartType.HEAD,
            0.5f, 3.0f, 4.0f),
    TRIDENT("trident", PartType.HANDLE, PartType.EYE, PartType.HEAD,
            5.0f, 1.1f, 1.0f);


    public static final Codec<ToolType> CODEC = StringRepresentable.fromEnum(ToolType::values);

    private final String name;
    private final PartType structuralPart;
    private final PartType corePart;
    private final PartType headPart;
    private final float baseDamage;
    private final float baseAttackSpeed;
    private final float baseMiningSpeed;

    ToolType(String name, PartType structural, PartType core, PartType head,
             float baseDamage, float baseAttackSpeed, float baseMiningSpeed) {
        this.name = name;
        this.structuralPart = structural;
        this.corePart = core;
        this.headPart = head;
        this.baseDamage = baseDamage;
        this.baseAttackSpeed = baseAttackSpeed;
        this.baseMiningSpeed = baseMiningSpeed;
    }

    public String getToolName() {
        return name;
    }

    public PartType getStructuralPart() {
        return structuralPart;
    }

    public PartType getCorePart() {
        return corePart;
    }

    public PartType getHeadPart() {
        return headPart;
    }

    public List<PartType> getRequiredParts() {
        return List.of(structuralPart, corePart, headPart);
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public float getBaseAttackSpeed() {
        return baseAttackSpeed;
    }

    public float getBaseMiningSpeed() {
        return baseMiningSpeed;
    }

    public boolean matchesParts(PartType part1, PartType part2, PartType part3) {
        List<PartType> required = getRequiredParts();
        List<PartType> provided = List.of(part1, part2, part3);

        for (PartType req : required) {
            if (!provided.contains(req)) {
                return false;
            }
        }
        return true;
    }

    public String getTranslationKey() {
        return "tool.telum." + name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
