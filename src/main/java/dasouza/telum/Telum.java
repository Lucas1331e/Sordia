package dasouza.telum;

import dasouza.telum.block.TelumBlockEntities;
import dasouza.telum.block.TelumBlocks;
import dasouza.telum.component.TelumComponents;
import dasouza.telum.item.TelumItems;
import dasouza.telum.network.TelumNetworking;
import dasouza.telum.screen.TelumScreenHandlers;
import dasouza.telum.effect.TelumEffects;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Telum implements ModInitializer {
	public static final String MOD_ID = "telum";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Telum - Modular Weapon System");

		TelumComponents.initialize();
		TelumEffects.initialize();
		dasouza.telum.entity.TelumEntities.initialize();
		dasouza.telum.particle.TelumParticles.initialize();
		dasouza.telum.util.TemporalRewindTracker.initialize();
		dasouza.telum.util.TemporalSculkZoneManager.initialize();
		dasouza.telum.util.SculkBrushingHandler.initialize();
		TelumItems.initialize();
		TelumBlocks.initialize();
		TelumBlockEntities.initialize();
		TelumScreenHandlers.initialize();
		TelumNetworking.initialize();
		dasouza.telum.worldgen.TelumWorldGen.initialize();
		dasouza.telum.util.SulfurCubeRewindManager.initialize();
		dasouza.telum.util.VoidProtectionManager.initialize();
		dasouza.telum.command.TelumCommands.initialize();
	}


	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
