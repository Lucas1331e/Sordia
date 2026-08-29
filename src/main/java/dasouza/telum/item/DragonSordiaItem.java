package dasouza.telum.item;

import dasouza.telum.tool.PartMaterial;
import dasouza.telum.tool.PartType;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

/**
 * Dragon Sordia item obtained from Suspicious End Stone or crafted with Dragon's Breath.
 * When cleaned in a water cauldron, it grants a random non-wood, non-stone tool part
 * with higher chances for Diamond and Netherite.
 */
public class DragonSordiaItem extends Item {

    public static final Map<PartMaterial, Integer> DRAGON_MATERIAL_WEIGHTS = Map.ofEntries(
            Map.entry(PartMaterial.COPPER,     25),
            Map.entry(PartMaterial.IRON,       24),
            Map.entry(PartMaterial.GOLD,       20),
            Map.entry(PartMaterial.DIAMOND,    16),
            Map.entry(PartMaterial.NETHERITE,   9),
            Map.entry(PartMaterial.PRISMARINE,  4),
            Map.entry(PartMaterial.SKULK,       2),
            Map.entry(PartMaterial.WIND,        0),
            Map.entry(PartMaterial.BLAZE,       0)
    );

    public DragonSordiaItem(Properties properties) {
        super(properties.stacksTo(64));
    }

    public static PartMaterial selectWeightedDragonMaterial(RandomSource rng) {
        int totalWeight = 0;
        for (int w : DRAGON_MATERIAL_WEIGHTS.values()) {
            totalWeight += w;
        }

        int roll = rng.nextInt(totalWeight);
        int current = 0;
        for (Map.Entry<PartMaterial, Integer> entry : DRAGON_MATERIAL_WEIGHTS.entrySet()) {
            current += entry.getValue();
            if (roll < current) {
                return entry.getKey();
            }
        }
        return PartMaterial.DIAMOND;
    }
}
