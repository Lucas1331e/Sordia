package dasouza.telum;

import dasouza.telum.client.screen.LyreGameScreen;
import dasouza.telum.network.OpenLyreScreenPayload;
import dasouza.telum.screen.ForgeScreen;
import dasouza.telum.screen.TelumScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;

public class TelumClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Telum.LOGGER.info("Initializing Telum client");

        MenuScreens.register(TelumScreenHandlers.FORGE_SCREEN_HANDLER, ForgeScreen::new);

        net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
                dasouza.telum.entity.TelumEntities.TEMPORAL_EVOKER_FANGS,
                dasouza.telum.client.render.TemporalEvokerFangsRenderer::new
        );

        net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry.register(
                dasouza.telum.block.TelumBlockEntities.ARCHEOLOGY_TABLE_ENTITY,
                dasouza.telum.client.render.ArcheologyTableBlockEntityRenderer::new
        );

        net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register(
                dasouza.telum.particle.TelumParticles.CLOCK_PARTICLE,
                dasouza.telum.particle.ClockParticle.Provider::new
        );

        net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.getInstance().register(
                dasouza.telum.particle.TelumParticles.SHRIEK_TEMPORAL_PARTICLE,
                dasouza.telum.particle.ShriekTemporalParticle.Provider::new
        );

        ClientPlayNetworking.registerGlobalReceiver(OpenLyreScreenPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                Minecraft mc = Minecraft.getInstance();
                mc.setScreenAndShow(new LyreGameScreen(
                        payload.targetX(), payload.targetY(), payload.targetZ(),
                        payload.sourceBlockId(), payload.resultBlockId()
                ));
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(dasouza.telum.network.SyncPlayerSongsPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                dasouza.telum.client.ClientSongRepertoire.setPayload(payload);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(dasouza.telum.network.SyncSulfurChargePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                dasouza.telum.client.SulfurClientHudTracker.onFullHit(payload.charge());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(dasouza.telum.network.SyncBookProgressPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                dasouza.telum.client.ClientBookProgress.setProgress(payload.craftedMaterials(), payload.craftedTools());
            });
        });

        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            dasouza.telum.client.SulfurClientHudTracker.clientTick();
        });
    }
}
