package dasouza.telum.tool;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * Defines tool part materials (Wood, Stone, Copper, Iron, Gold, Diamond, Netherite).
 * Stat multipliers strictly enforce material characteristics:
 * - Wood: Medium speed (1.3x), very low durability (0.3x) & damage (0.7x).
 * - Stone: Moderate (1.0x speed), better damage (1.0x) & durability (0.7x) than wood.
 * - Copper: Faster than iron (1.4x), slightly superior to stone (0.9x dur, 1.1x dmg), worse than iron.
 * - Iron: Superior to copper/stone in durability (1.4x) & damage (1.3x), moderate speed (1.2x).
 * - Gold: Fastest of ALL (2.5x speed), damage like stone (1.0x), lowest durability of ALL (0.2x).
 * - Diamond: Highest durability of ALL (3.0x), faster than netherite (1.8x speed), high damage (1.8x).
 * - Netherite: Highest damage of ALL (2.2x), high durability (2.5x - lower than diamond), faster than usual (1.6x speed).
 */
public enum PartMaterial implements StringRepresentable {
    WOOD("wood",           0, 0.3f, 1.0f, 1.3f, 1.3f, 0xA0795B),
    STONE("stone",         1, 0.7f, 1.33f, 1.0f, 1.0f, 0x737373),
    COPPER("copper",       1, 0.9f, 1.4f, 1.4f, 1.4f, 0xE77C56),
    PRISMARINE("prismarine", 2, 0.85f, 1.4f, 1.2f, 1.2f, 0x368C86),
    SKULK("skulk",         3, 0.18f, 1.5f, 1.6f, 1.6f, 0x0D6272),
    WIND("wind",           2, 0.25f, 1.3f, 2.2f, 2.2f, 0x9CE5F2),
    IRON("iron",           2, 1.4f, 1.67f, 1.2f, 1.2f, 0xD8D8D8),
    GOLD("gold",           0, 0.2f, 1.0f, 2.5f, 2.5f, 0xFDF55F),
    DIAMOND("diamond",     3, 3.0f, 2.0f, 1.8f, 1.8f, 0x4AEDD9),
    NETHERITE("netherite", 4, 2.5f, 2.33f, 1.6f, 1.6f, 0x4D4345),
    BLAZE("blaze",         3, 0.7f, 1.7f, 1.5f, 1.5f, 0xFF7A00),
    SPIDER("spider",       2, 0.8f, 1.5f, 1.4f, 1.4f, 0x6B1A24),
    SKELETON("skeleton",   2, 0.6f, 1.4f, 1.6f, 1.6f, 0xC4C4C4),
    ZOMBIE("zombie",       2, 1.2f, 1.4f, 0.9f, 0.9f, 0x486B38),
    CREEPER("creeper",     2, 0.5f, 1.6f, 1.1f, 1.1f, 0x36B044),
    ENDERMAN("enderman",   3, 0.9f, 1.7f, 1.5f, 1.5f, 0x160C26),
    SULFUR("sulfur",       2, 0.9f, 1.5f, 1.4f, 1.4f, 0xE5C158),
    AMETHYST("amethyst",   2, 1.3f, 1.4f, 1.2f, 1.2f, 0xC067F8),
    GREED("greed",         3, 1.8f, 2.6f, 0.45f, 0.45f, 0xFFD700),
    EMERALD("emerald",     3, 1.5f, 1.8f, 1.4f, 1.4f, 0x55FF55);

    public static final Codec<PartMaterial> CODEC = StringRepresentable.fromEnum(PartMaterial::values);

    private final String name;
    private final int miningLevel;
    private final float durabilityMultiplier;
    private final float damageMultiplier;
    private final float speedMultiplier;
    private final float miningSpeedMultiplier;
    private final int color;

    PartMaterial(String name, int miningLevel, float durabilityMult, float damageMult,
                 float speedMult, float miningSpeedMult, int color) {
        this.name = name;
        this.miningLevel = miningLevel;
        this.durabilityMultiplier = durabilityMult;
        this.damageMultiplier = damageMult;
        this.speedMultiplier = speedMult;
        this.miningSpeedMultiplier = miningSpeedMult;
        this.color = color;
    }

    public String getMaterialName() {
        return name;
    }

    public int getIndex() {
        return ordinal();
    }

    public int getMiningLevel() {
        return miningLevel;
    }

    public float getDurabilityMultiplier() {
        return durabilityMultiplier;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public float getMiningSpeedMultiplier() {
        return miningSpeedMultiplier;
    }

    public int getColor() {
        return color;
    }

    public String getTranslationKey() {
        return "material.telum." + name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
