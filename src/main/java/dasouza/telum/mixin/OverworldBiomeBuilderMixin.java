package dasouza.telum.mixin;

import com.mojang.datafixers.util.Pair;
import dasouza.telum.Telum;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(OverworldBiomeBuilder.class)
public class OverworldBiomeBuilderMixin {

    @Inject(method = "addUndergroundBiomes", at = @At("RETURN"))
    private void telum$addMarbleCavesBiome(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer, CallbackInfo ci) {
        ResourceKey<Biome> MARBLE_CAVES = ResourceKey.create(Registries.BIOME, Telum.id("marble_caves"));

        // Add climate target point for Marble Caves as a rare subterranean biome under Taiga/Cold regions
        consumer.accept(Pair.of(
                Climate.parameters(
                        Climate.Parameter.span(-0.6f, -0.3f), // Temperature: focused cold / taiga climate
                        Climate.Parameter.span(0.1f, 0.6f),   // Humidity
                        Climate.Parameter.span(0.3f, 0.8f),   // Continentalness: inland / mountain regions
                        Climate.Parameter.span(-0.4f, 0.4f),  // Erosion
                        Climate.Parameter.span(0.35f, 0.85f), // Depth: deep cave subterranean level
                        Climate.Parameter.span(0.25f, 0.75f), // Weirdness: specific target band for rare cave biomes
                        0.0f                                  // Offset
                ),
                MARBLE_CAVES
        ));
    }
}
