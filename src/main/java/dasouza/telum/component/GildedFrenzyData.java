package dasouza.telum.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Stores temporary Frenzy stack data for Gold material tools.
 * @param stacks count of active stacks (1 to 3).
 * @param expiryGameTime server tick timestamp when the frenzy expires.
 */
public record GildedFrenzyData(int stacks, long expiryGameTime) {
    public static final Codec<GildedFrenzyData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("stacks").forGetter(GildedFrenzyData::stacks),
                    Codec.LONG.fieldOf("expiry_game_time").forGetter(GildedFrenzyData::expiryGameTime)
            ).apply(instance, GildedFrenzyData::new)
    );
}
