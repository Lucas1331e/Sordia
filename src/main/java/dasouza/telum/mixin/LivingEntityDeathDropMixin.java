package dasouza.telum.mixin;

import dasouza.telum.component.AssembledToolData;
import dasouza.telum.item.AssembledToolItem;
import dasouza.telum.tool.PartMaterial;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDeathDropMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void telum$handleCustomMobDeathDrops(DamageSource damageSource, CallbackInfo ci) {
        LivingEntity victim = (LivingEntity) (Object) this;
        if (victim.level().isClientSide()) return;

        Entity killerEntity = damageSource.getEntity();
        Player player = null;
        if (killerEntity instanceof Player p) {
            player = p;
        } else {
            player = victim.getLastHurtByPlayer();
        }
        if (player == null) return;

        ItemStack mainHand = player.getMainHandItem();
        if (!(mainHand.getItem() instanceof AssembledToolItem)) return;

        AssembledToolData data = AssembledToolItem.getToolData(mainHand);
        if (data == null) return;

        ServerLevel level = (ServerLevel) victim.level();

        // 1. Greed Ability: Extra chance to drop mob's equipped armor & weapons
        int greedLvl = data.getMaterialLevel(PartMaterial.GREED);
        if (greedLvl >= 1) {
            float equipDropChance = 0.25f * greedLvl;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack equipped = victim.getItemBySlot(slot);
                if (!equipped.isEmpty() && level.getRandom().nextFloat() < equipDropChance) {
                    ItemEntity entity = new ItemEntity(level, victim.getX(), victim.getY() + 0.5, victim.getZ(), equipped.copy());
                    level.addFreshEntity(entity);
                    victim.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }

        // 2. Emerald Ability: Chance to drop Emeralds on mob kill
        int emeraldLvl = data.getMaterialLevel(PartMaterial.EMERALD);
        if (emeraldLvl >= 1) {
            float dropChance = 0.25f + (0.10f * emeraldLvl);
            if (level.getRandom().nextFloat() < dropChance) {
                int count = 1 + level.getRandom().nextInt(emeraldLvl);
                ItemEntity entity = new ItemEntity(level, victim.getX(), victim.getY() + 0.5, victim.getZ(), new ItemStack(Items.EMERALD, count));
                level.addFreshEntity(entity);
            }
        }

        // 3. Amethyst Ability: Chance to drop Amethyst Shards on mob kill
        int amethystLvl = data.getMaterialLevel(PartMaterial.AMETHYST);
        if (amethystLvl >= 1) {
            float dropChance = 0.25f + (0.10f * amethystLvl);
            if (level.getRandom().nextFloat() < dropChance) {
                int count = 1 + level.getRandom().nextInt(amethystLvl);
                ItemEntity entity = new ItemEntity(level, victim.getX(), victim.getY() + 0.5, victim.getZ(), new ItemStack(Items.AMETHYST_SHARD, count));
                level.addFreshEntity(entity);
            }
        }

    }
}
