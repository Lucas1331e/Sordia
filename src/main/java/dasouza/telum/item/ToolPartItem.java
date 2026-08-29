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
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        ToolPartData data = getPartData(stack);

        tooltip.accept(Component.translatable(data.partType().getTranslationKey())
                .withStyle(ChatFormatting.GRAY));

        ChatFormatting matColor = switch (data.material()) {
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
        };

        tooltip.accept(Component.translatable(data.material().getTranslationKey())
                .withStyle(matColor));

        float dmgWeight = dasouza.telum.tool.ToolStatsCalculator.getDamageWeight(data.partType());
        float durWeight = dasouza.telum.tool.ToolStatsCalculator.getDurabilityWeight(data.partType());
        float spdWeight = dasouza.telum.tool.ToolStatsCalculator.getSpeedWeight(data.partType());

        float durMult = data.material().getDurabilityMultiplier();
        if (data.partType() == PartType.EYE && durMult < 1.0f) {
            durMult = 1.0f; // Eye durability exemption!
        }

        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("tooltip.telum.durability_mult",
                        String.format("%.2fx (Weight: %.0f%%)", durMult, durWeight * 100))
                .withStyle(ChatFormatting.DARK_GREEN));
        tooltip.accept(Component.translatable("tooltip.telum.damage_mult",
                        String.format("%.2fx (Weight: %.0f%%)", data.material().getDamageMultiplier(), dmgWeight * 100))
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.accept(Component.translatable("tooltip.telum.mining_speed_mult",
                        String.format("%.2fx (Weight: %.0f%%)", data.material().getMiningSpeedMultiplier(), spdWeight * 100))
                .withStyle(ChatFormatting.BLUE));
    }
}
