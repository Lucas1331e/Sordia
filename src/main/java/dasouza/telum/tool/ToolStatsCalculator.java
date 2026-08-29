package dasouza.telum.tool;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.component.ToolPartData;

import java.util.List;

/**
 * Calculates final tool stats with weighted part contributions and special negative multiplier mechanics:
 * - HEAD / BLADE & HANDLE / GRIP: Materials with multipliers < 1.0x (e.g. Gold durability 0.2x, Wood 0.3x)
 *   actively penalize and pull down the overall tool stats.
 * - EYE: Special exemption! The EYE never penalizes or subtracts durability. If the material's durability multiplier
 *   is < 1.0x (like Gold or Wood), the EYE uses a 1.0x positive baseline so it gives a small positive contribution
 *   instead of subtracting.
 */
public final class ToolStatsCalculator {

    private static final int BASE_DURABILITY = 200;

    private ToolStatsCalculator() {}

    public static AssembledToolData calculate(ToolType toolType, List<ToolPartData> parts) {
        if (parts.isEmpty()) {
            return new AssembledToolData(
                    toolType,
                    parts,
                    BASE_DURABILITY,
                    toolType.getBaseDamage(),
                    toolType.getBaseAttackSpeed(),
                    toolType.getBaseMiningSpeed(),
                    1
            );
        }

        float weightedDamageSum = 0;
        float totalDamageWeight = 0;

        float weightedDurabilitySum = 0;
        float totalDurabilityWeight = 0;

        float weightedSpeedSum = 0;
        float totalSpeedWeight = 0;

        int maxMiningLevel = 0;

        for (ToolPartData part : parts) {
            PartType type = part.partType();
            PartMaterial mat = part.material();

            float dmgWeight = getDamageWeight(type);
            float durWeight = getDurabilityWeight(type);
            float spdWeight = getSpeedWeight(type);

            float durMult = mat.getDurabilityMultiplier();

            // Special Exemption for EYE:
            // The EYE never penalizes or subtracts durability. If a material has a durability
            // multiplier < 1.0x (e.g. Gold 0.2x, Wood 0.3x), the EYE uses a 1.0x baseline.
            if (type == PartType.EYE && durMult < 1.0f) {
                durMult = 1.0f;
            }

            weightedDamageSum += dmgWeight * mat.getDamageMultiplier();
            totalDamageWeight += dmgWeight;

            weightedDurabilitySum += durWeight * durMult;
            totalDurabilityWeight += durWeight;

            weightedSpeedSum += spdWeight * mat.getMiningSpeedMultiplier();
            totalSpeedWeight += spdWeight;

            if (mat.getMiningLevel() > maxMiningLevel) {
                maxMiningLevel = mat.getMiningLevel();
            }
        }

        float effectiveDamageMult     = totalDamageWeight > 0 ? (weightedDamageSum / totalDamageWeight) : 1.0f;
        float effectiveDurabilityMult = totalDurabilityWeight > 0 ? (weightedDurabilitySum / totalDurabilityWeight) : 1.0f;
        float effectiveSpeedMult      = totalSpeedWeight > 0 ? (weightedSpeedSum / totalSpeedWeight) : 1.0f;

        int diamondCount = 0;
        for (ToolPartData p : parts) {
            if (p.material() == PartMaterial.DIAMOND) diamondCount++;
        }
        int diamondBonus = switch (diamondCount) {
            case 1 -> 150;
            case 2 -> 350;
            case 3 -> 600;
            default -> 0;
        };

        int durability = Math.round(BASE_DURABILITY * effectiveDurabilityMult) + diamondBonus;
        float attackDamage = toolType.getBaseDamage() * effectiveDamageMult;
        float attackSpeed = toolType.getBaseAttackSpeed();
        float miningSpeed = toolType.getBaseMiningSpeed() * effectiveSpeedMult;

        return new AssembledToolData(
                toolType,
                parts,
                durability,
                attackDamage,
                attackSpeed,
                miningSpeed,
                maxMiningLevel
        );
    }

    public static float getDamageWeight(PartType type) {
        return switch (type) {
            case HEAD, BLADE -> 0.60f; // Head/Blade dictates 60% of damage
            case EYE         -> 0.15f; // Eye gives balanced 15%
            case HANDLE, GRIP -> 0.10f; // Handles contribute minimal 10% to damage
        };
    }

    public static float getDurabilityWeight(PartType type) {
        return switch (type) {
            case HEAD, BLADE -> 0.50f; // Head/Blade dictates 50% of durability
            case HANDLE, GRIP -> 0.35f; // Handles contribute 35% to durability
            case EYE         -> 0.15f; // Eye gives balanced 15%
        };
    }

    public static float getSpeedWeight(PartType type) {
        return switch (type) {
            case HANDLE, GRIP -> 0.60f; // Handle/Grip dictates 60% of speed
            case EYE         -> 0.25f; // Eye gives balanced 25%
            case HEAD, BLADE -> 0.15f; // Head/Blade contributes 15% to speed
        };
    }
}
