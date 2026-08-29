package dasouza.telum.item;

import dasouza.telum.component.TelumComponents;
import dasouza.telum.component.ToolPartData;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.tool.PartType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class ToolPartItem extends Item {

    private final PartType defaultPartType;
    private final PartMaterial defaultMaterial;

    public ToolPartItem(Properties properties, PartType partType, PartMaterial material) {
        super(properties);
        this.defaultPartType = partType;
        this.defaultMaterial = material;
    }

    public PartType getPartType() {
        return defaultPartType;
    }

    public PartMaterial getMaterial() {
        return defaultMaterial;
    }

    public ToolPartData getPartData(ItemStack stack) {
        ToolPartData data = stack.get(TelumComponents.TOOL_PART);
        if (data != null) {
            return data;
        }
        return new ToolPartData(defaultPartType, defaultMaterial);
    }

    @Override
    public Component getName(ItemStack stack) {
        ToolPartData data = getPartData(stack);
        String key = "item.telum." + data.partType().getPartName() + "_" + data.material().getMaterialName();
        return Component.translatable(key);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        ToolPartData data = getPartData(stack);
        PartMaterial mat = data.material();
        ChatFormatting matColor = getMaterialColor(mat);

        // Header: Tipo: <PartType>  |  Material: <Material>
        Component header = Component.literal(" §8Tipo: ")
                .append(Component.translatable(data.partType().getTranslationKey()).withStyle(ChatFormatting.GRAY))
                .append(Component.literal("  |  Material: "))
                .append(Component.translatable(mat.getTranslationKey()).withStyle(matColor));
        tooltip.accept(header);

        // Ability section
        tooltip.accept(Component.literal(" §8────────────────────────"));
        boolean isMultiLevel = (mat == PartMaterial.SULFUR || mat == PartMaterial.BLAZE || mat == PartMaterial.DIAMOND || mat == PartMaterial.GOLD || mat == PartMaterial.NETHERITE || mat == PartMaterial.EMERALD);
        String romanLvl = isMultiLevel ? " I" : "";
        tooltip.accept(Component.literal("  ✦ ")
                .append(Component.translatable("ability.telum." + mat.getMaterialName(), romanLvl).withStyle(matColor)));

        tooltip.accept(Component.literal(" §8────────────────────────"));

        // Calculation Weights & Multipliers
        float dmgWeight = dasouza.telum.tool.ToolStatsCalculator.getDamageWeight(data.partType());
        float durWeight = dasouza.telum.tool.ToolStatsCalculator.getDurabilityWeight(data.partType());
        float spdWeight = dasouza.telum.tool.ToolStatsCalculator.getSpeedWeight(data.partType());

        float durMult = mat.getDurabilityMultiplier();
        if (data.partType() == PartType.EYE && durMult < 1.0f) {
            durMult = 1.0f; // Eye durability exemption!
        }

        // Row 1: ✚ Durabilidad
        Component durRow = Component.literal("  ✚ Durabilidad: ")
                .append(Component.literal(String.format("%.2fx", durMult)).withStyle(ChatFormatting.GREEN))
                .append(Component.literal(String.format(" §8(Aporte: %.0f%%)", durWeight * 100)));
        tooltip.accept(durRow);

        // Row 2: ⚔ Daño
        Component dmgRow = Component.literal("  ⚔ Daño:         ")
                .append(Component.literal(String.format("%.2fx", mat.getDamageMultiplier())).withStyle(ChatFormatting.RED))
                .append(Component.literal(String.format(" §8(Aporte: %.0f%%)", dmgWeight * 100)));
        tooltip.accept(dmgRow);

        // Row 3: ⚡ Velocidad
        Component spdRow = Component.literal("  ⚡ Velocidad:    ")
                .append(Component.literal(String.format("%.2fx", mat.getMiningSpeedMultiplier())).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(String.format(" §8(Aporte: %.0f%%)", spdWeight * 100)));
        tooltip.accept(spdRow);

        // Row 4: ◆ Nivel
        String miningLevelName = getMiningLevelName(mat.getMiningLevel());
        Component levelRow = Component.literal("  ◆ Nivel:        ")
                .append(Component.literal(miningLevelName).withStyle(ChatFormatting.GOLD));
        tooltip.accept(levelRow);
    }

    private static String getMiningLevelName(int level) {
        return switch (level) {
            case 0 -> "Madera / Oro (0)";
            case 1 -> "Piedra / Cobre (1)";
            case 2 -> "Hierro (2)";
            case 3 -> "Diamante (3)";
            case 4 -> "Netherita (4)";
            default -> "Nivel " + level;
        };
    }

    private ChatFormatting getMaterialColor(PartMaterial mat) {
        return switch (mat) {
            case WOOD -> ChatFormatting.WHITE;
            case STONE -> ChatFormatting.DARK_GRAY;
            case COPPER -> ChatFormatting.GOLD;
            case PRISMARINE -> ChatFormatting.DARK_AQUA;
            case SKULK -> ChatFormatting.DARK_AQUA;
            case WIND -> ChatFormatting.AQUA;
            case IRON -> ChatFormatting.GRAY;
            case GOLD -> ChatFormatting.YELLOW;
            case DIAMOND -> ChatFormatting.AQUA;
            case NETHERITE -> ChatFormatting.DARK_GRAY;
            case BLAZE -> ChatFormatting.GOLD;
            case SPIDER -> ChatFormatting.DARK_RED;
            case SKELETON -> ChatFormatting.WHITE;
            case ZOMBIE -> ChatFormatting.DARK_GREEN;
            case CREEPER -> ChatFormatting.GREEN;
            case ENDERMAN -> ChatFormatting.DARK_PURPLE;
            case SULFUR -> ChatFormatting.YELLOW;
            case AMETHYST -> ChatFormatting.LIGHT_PURPLE;
            case GREED -> ChatFormatting.GOLD;
            case EMERALD -> ChatFormatting.GREEN;
        };
    }
}
