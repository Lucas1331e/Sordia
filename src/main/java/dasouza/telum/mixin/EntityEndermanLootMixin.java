package dasouza.telum.mixin;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.util.EndermanLootHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityEndermanLootMixin {

    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void telum$endermanMobLootToContainers(ServerLevel level, ItemStack stack, CallbackInfoReturnable<ItemEntity> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity living && !stack.isEmpty()) {
            Player player = living.getLastHurtByPlayer();
            if (player != null) {
                ItemStack mainStack = player.getMainHandItem();
                if (mainStack.getItem() instanceof AssembledToolItem) {
                    AssembledToolData data = AssembledToolItem.getToolData(mainStack);
                    if (data != null) {
                        int greedLvl = data.getMaterialLevel(PartMaterial.GREED);
                        int emeraldLvl = data.getMaterialLevel(PartMaterial.EMERALD);
                        int lootingBonus = 0;
                        if (greedLvl >= 1 || emeraldLvl >= 2) {
                            lootingBonus = 3;
                        } else if (emeraldLvl >= 1) {
                            lootingBonus = 2;
                        }

                        if (lootingBonus > 0 && level.getRandom().nextFloat() < 0.75f) {
                            int extra = level.getRandom().nextInt(lootingBonus) + 1;
                            stack.grow(extra);
                        }
                    }
                }

                if (EndermanLootHandler.hasEndermanTool(player)) {
                    EndermanLootHandler.depositLoot(player, stack);
                    cir.setReturnValue(null);
                }
            }
        }
    }

    @Inject(
            method = "spawnAtLocation(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/entity/item/ItemEntity;",
            at = @At("RETURN"),
            require = 0
    )
    private void telum$blazeProtectItemEntity(ServerLevel level, ItemStack stack, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity entity = cir.getReturnValue();
        if (entity != null && (Entity)(Object)this instanceof LivingEntity living) {
            Player player = living.getLastHurtByPlayer();
            if (player != null) {
                ItemStack mainStack = player.getMainHandItem();
                if (mainStack.getItem() instanceof AssembledToolItem) {
                    AssembledToolData data = AssembledToolItem.getToolData(mainStack);
                    if (data != null && data.getMaterialLevel(PartMaterial.BLAZE) >= 1) {
                        entity.addTag("telum$blaze_protected_drop");
                        entity.clearFire();
                    }
                }
            }
        }
    }
}
