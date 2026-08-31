package dasouza.telum.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dasouza.telum.Telum;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class TelumCommands {

    private TelumCommands() {}

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            register(dispatcher);
        });
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("telum")
                .then(Commands.literal("unlockrecipes")
                    .executes(TelumCommands::unlockAllTelumRecipes))
                .then(Commands.literal("unlock_recipes")
                    .executes(TelumCommands::unlockAllTelumRecipes))
        );
    }

    private static int unlockAllTelumRecipes(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            ServerPlayer player = source.getPlayerOrException();

            Collection<RecipeHolder<?>> recipes = source.getServer().getRecipeManager().getRecipes();
            List<RecipeHolder<?>> telumRecipes = new ArrayList<>();

            for (RecipeHolder<?> recipe : recipes) {
                if (recipe != null && recipe.id() != null) {
                    String idStr = recipe.id().toString();
                    if (idStr.contains("telum:") || idStr.contains(":telum/") || 
                        (recipe.id().identifier() != null && "telum".equalsIgnoreCase(recipe.id().identifier().getNamespace()))) {
                        telumRecipes.add(recipe);
                    }
                }
            }



            if (!telumRecipes.isEmpty()) {
                player.awardRecipes(telumRecipes);
                source.sendSuccess(() -> Component.literal("§a[Telum] Se han desbloqueado " + telumRecipes.size() + " recetas de Telum en tu libro de recetas."), true);
            } else {
                source.sendSuccess(() -> Component.literal("§c[Telum] No se encontraron recetas registradas de Telum."), false);
            }
            return telumRecipes.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("§c[Telum] Error al desbloquear recetas: " + e.getMessage()));
            return 0;
        }
    }
}
