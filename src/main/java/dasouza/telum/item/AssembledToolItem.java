package dasouza.telum.item;

import com.mojang.datafixers.util.Pair;
import dasouza.telum.Telum;
import dasouza.telum.component.AssembledToolData;
import dasouza.telum.component.GildedFrenzyData;
import dasouza.telum.component.TelumComponents;
import dasouza.telum.component.ToolPartData;
import dasouza.telum.mixin.AxeItemAccessor;
import dasouza.telum.mixin.HoeItemAccessor;
import dasouza.telum.mixin.ShovelItemAccessor;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.tool.ToolType;
import dasouza.telum.util.ClientTooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AssembledToolItem extends Item {

    private static final Map<UUID, Long> WIND_JUMP_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Integer> SULFUR_HIT_COUNTS = new HashMap<>();

    public static final Identifier GILDED_FRENZY_ATTACK_SPEED_ID = Telum.id("gilded_frenzy_attack_speed");
    public static final Identifier VENOMOUS_FURY_ATTACK_DAMAGE_ID = Telum.id("venomous_fury_attack_damage");

    public AssembledToolItem(Properties properties) {
        super(properties);
    }

    /** Called on player disconnect to prevent memory leaks in static maps */
    public static void clearPlayerCooldowns(java.util.UUID uuid) {
        WIND_JUMP_COOLDOWNS.remove(uuid);
        SULFUR_HIT_COUNTS.remove(uuid);
    }

    public static AssembledToolData getToolData(ItemStack stack) {
        return stack.get(TelumComponents.ASSEMBLED_TOOL);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        AssembledToolData data = getToolData(stack);
        if (data == null) return 1.0f;

        if (isEffectiveOn(data.toolType(), state)) {
            ToolPartData headPart = data.getPart(dasouza.telum.tool.PartType.HEAD);
            if (headPart != null && headPart.material() == PartMaterial.ZOMBIE) {
                return 9999.0f; // Instant mining with Zombie head!
            }

            float speed = data.miningSpeed();
            int gLvl = data.getMaterialLevel(PartMaterial.GOLD);
            if (gLvl >= 1) {
                GildedFrenzyData frenzy = stack.get(TelumComponents.GILDED_FRENZY);
                if (frenzy != null && frenzy.stacks() > 0) {
                    float maxBoost = Math.min(gLvl, 3) * 0.10f;
                    float stackRatio = Math.min(3, frenzy.stacks()) / 3.0f;
                    speed *= (1.0f + maxBoost * stackRatio);
                }
            }
            int pLvl = data.getMaterialLevel(PartMaterial.PRISMARINE);
            if (pLvl >= 1) {
                speed *= (1.0f + pLvl * 0.30f);
            }
            return speed;
        }
        return 1.0f;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        AssembledToolData data = getToolData(stack);
        if (data == null) return false;
        return isEffectiveOn(data.toolType(), state);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        AssembledToolData data = getToolData(stack);
        if (data != null) {
            // Blaze Ability: Volcanic Ignition (Set target on fire & extra damage to burning enemies at Lv2+)
            int bLvl = data.getMaterialLevel(PartMaterial.BLAZE);
            if (bLvl >= 1) {
                boolean targetWasBurning = target.isOnFire();
                int fireDuration = switch (bLvl) {
                    case 1 -> 4;
                    case 2 -> 8;
                    default -> 11; // Level 3: +3 seconds of fire duration (11s total) instead of scaling damage
                };
                target.igniteForSeconds(fireDuration);

                if (bLvl >= 2 && targetWasBurning) {
                    float extraDamage = 3.0f; // Level 2 and Level 3 deal 3.0 extra damage
                    target.hurt(attacker.damageSources().mobAttack(attacker), extraDamage);
                    if (attacker.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
                    }
                }
            }

            // Prismarine Ability: Extra damage underwater
            int pLvl = data.getMaterialLevel(PartMaterial.PRISMARINE);
            if (pLvl >= 1 && attacker.isInWater()) {
                target.hurt(attacker.damageSources().mobAttack(attacker), pLvl * 1.5f);
            }

            // Gold Ability: Gilded Frenzy (Hitting builds up Frenzy stacks up to 3 times)
            int gLvl = data.getMaterialLevel(PartMaterial.GOLD);
            if (gLvl >= 1) {
                applyGoldFrenzyStack(stack, attacker, gLvl);
            }

            // Wind Ability Level 2 & 3: Critical Hit Air Jump & Enhanced Critical Damage
            int wLvl = data.getMaterialLevel(PartMaterial.WIND);
            if (wLvl >= 2 && attacker instanceof Player player) {
                boolean isCrit = player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger();
                if (isCrit) {
                    Vec3 current = player.getDeltaMovement();
                    player.setDeltaMovement(current.x, 0.65, current.z);
                    player.hurtMarked = true;
                    player.fallDistance = 0.0f; // Reset fall damage!

                    if (wLvl >= 3) {
                        target.hurt(attacker.damageSources().playerAttack(player), 4.0f);
                    }

                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.GUST, target.getX(), target.getY() + 1.0, target.getZ(), 2, 0.1, 0.1, 0.1, 0.02);
                        player.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.6f, 1.4f);
                    }
                }
            }

            // Stone Ability: Solid Core (Chance to save durability)
            int sLvl = data.getMaterialLevel(PartMaterial.STONE);
            if (sLvl >= 1) {
                float saveChance = switch (sLvl) {
                    case 1 -> 0.15f;
                    case 2 -> 0.30f;
                    default -> 0.50f;
                };
                if (attacker.level().getRandom().nextFloat() < saveChance) {
                    return; // Skip durability damage!
                }
            }

            // Spider Ability: Venomous Fury (Deals +4.0 bonus damage per level if target is poisoned)
            int spiderLvl = data.getMaterialLevel(PartMaterial.SPIDER);
            if (spiderLvl >= 1 && target.hasEffect(MobEffects.POISON)) {
                float bonusDamage = 4.0f * spiderLvl;
                target.hurt(attacker.damageSources().mobAttack(attacker), bonusDamage);
                if (attacker.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1.0, target.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                }
            }

            // Sulfur Ability: Volatile Reaction (Reacción Volátil)
            int sulfurLvl = data.getMaterialLevel(PartMaterial.SULFUR);
            if (sulfurLvl >= 1) {
                float attackScale = dasouza.telum.util.AttackScaleTracker.getLastAttackScale();
                if (attackScale >= 0.9f) {
                    UUID playerUUID = attacker.getUUID();
                    int currentHits = SULFUR_HIT_COUNTS.getOrDefault(playerUUID, 0) + 1;

                    if (attacker instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                        net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(
                                serverPlayer,
                                new dasouza.telum.network.SyncSulfurChargePayload(currentHits)
                        );
                    }

                    if (attacker.level() instanceof ServerLevel serverLevel) {
                        boolean isCrit = false;
                        if (attacker instanceof Player player) {
                            isCrit = player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger();
                        }

                        if (currentHits >= 4) {
                            SULFUR_HIT_COUNTS.put(playerUUID, 0);

                            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, 100, 0));
                            if (sulfurLvl >= 2) {
                                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                            }

                            // Level 3 Critical Hit Chemical Geyser Explosion
                            if (sulfurLvl >= 3 && isCrit) {
                                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, target.getX(), target.getY() + 0.5, target.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                                serverLevel.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 0.5, target.getZ(), 12, 0.4, 0.4, 0.4, 0.05);
                                serverLevel.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 0.5, target.getZ(), 15, 0.4, 0.4, 0.4, 0.05);
                                serverLevel.sendParticles(ParticleTypes.POOF, target.getX(), target.getY() + 0.5, target.getZ(), 10, 0.3, 0.3, 0.3, 0.08);

                                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9f, 1.3f);
                                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0f, 0.8f);

                                AABB area = target.getBoundingBox().inflate(3.5);
                                for (LivingEntity nearby : attacker.level().getEntitiesOfClass(LivingEntity.class, area)) {
                                    if (nearby != attacker && nearby.isAlive()) {
                                        nearby.hurt(attacker.damageSources().mobAttack(attacker), 4.0f);
                                        nearby.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, 100, 0));
                                        nearby.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                                    }
                                }
                            } else {
                                serverLevel.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                                serverLevel.sendParticles(ParticleTypes.LAVA, target.getX(), target.getY() + 0.8, target.getZ(), 4, 0.1, 0.1, 0.1, 0.02);
                                serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.7f, 1.4f);
                            }
                        } else {
                            SULFUR_HIT_COUNTS.put(playerUUID, currentHits);
                        }

                        // Sweeping attack: poison/weakness to all surrounding entities
                        if (data.toolType() == ToolType.SWORD && attacker instanceof Player player) {
                            float sweepThreshold = dasouza.telum.util.AttackScaleTracker.getLastAttackScale();
                            if (sweepThreshold > 0.9f) {
                                AABB sweepArea = target.getBoundingBox().inflate(1.5, 0.5, 1.5);
                                for (LivingEntity nearby : attacker.level().getEntitiesOfClass(LivingEntity.class, sweepArea)) {
                                    if (nearby != attacker && nearby != target && nearby.isAlive()) {
                                        nearby.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON, 100, 0));
                                        if (sulfurLvl >= 2) {
                                            nearby.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Zombie Ability: Voracious Strike (Consumes 1 hunger thigh / 2 food points, deals +3.5 bonus damage)
            int zombieLvl = data.getMaterialLevel(PartMaterial.ZOMBIE);
            if (zombieLvl >= 1 && attacker instanceof Player player) {
                if (player.getFoodData().getFoodLevel() > 0) {
                    player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - 2));
                    target.hurt(attacker.damageSources().playerAttack(player), 3.5f);
                    if (attacker.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1.0, target.getZ(), 4, 0.1, 0.1, 0.1, 0.02);
                    }
                }
            }

            // Creeper Ability: Explosive Shockwave (Deals +3.0 AOE damage to surrounding mobs, 1.5 recoil damage to player)
            int creeperLvl = data.getMaterialLevel(PartMaterial.CREEPER);
            if (creeperLvl >= 1) {
                Level lvl = attacker.level();
                if (lvl instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.0, target.getZ(), 1, 0, 0, 0, 0);
                    lvl.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.8f, 1.4f);

                    AABB area = target.getBoundingBox().inflate(3.5);
                    for (LivingEntity nearby : lvl.getEntitiesOfClass(LivingEntity.class, area)) {
                        if (nearby != attacker && nearby != target && nearby.isAlive()) {
                            nearby.hurt(attacker.damageSources().mobAttack(attacker), 3.0f);
                        }
                    }
                    attacker.hurt(attacker.damageSources().explosion(null, attacker), 1.5f);
                }
            }

            // Skeleton Ability: Fragile Precision (+3.0 crit damage, extra durability loss)
            int skeletonLvl = data.getMaterialLevel(PartMaterial.SKELETON);
            if (skeletonLvl >= 1 && attacker instanceof Player player) {
                boolean isCrit = player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger();
                if (isCrit) {
                    target.hurt(attacker.damageSources().playerAttack(player), 3.0f);
                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1.0, target.getZ(), 8, 0.2, 0.2, 0.2, 0.1);
                    }
                }
                stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
            }
        }

        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide()) {
            AssembledToolData data = getToolData(stack);
            if (data != null) {
                // Zombie Head Ability: Instamine block breaking consumes 1 food shank / 2 food points
                ToolPartData headPart = data.getPart(dasouza.telum.tool.PartType.HEAD);
                if (headPart != null && headPart.material() == PartMaterial.ZOMBIE && miner instanceof Player player) {
                    if (!player.isCreative() && player.getFoodData().getFoodLevel() > 0) {
                        player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - 2));
                    }
                }

                // Gold Ability: Gilded Frenzy (Mining builds up Frenzy stacks up to 3 times)
                int gLvl = data.getMaterialLevel(PartMaterial.GOLD);
                if (gLvl >= 1) {
                    applyGoldFrenzyStack(stack, miner, gLvl);
                }

                // Copper Ability: Lucky Vein (Drop diamonds on mining Copper or Coal ore)
                int cLvl = data.getMaterialLevel(PartMaterial.COPPER);
                if (cLvl >= 1 && isCopperOrCoalOre(state)) {
                    float dropChance = switch (cLvl) {
                        case 1 -> 0.05f;
                        case 2 -> 0.12f;
                        default -> 0.25f;
                    };
                    if (level.getRandom().nextFloat() < dropChance) {
                        ItemEntity entity = new ItemEntity(level,
                                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                new ItemStack(Items.DIAMOND));
                        level.addFreshEntity(entity);
                        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.2f);
                    }
                }

                // Stone Ability: Solid Core (Chance to save durability)
                int sLvl = data.getMaterialLevel(PartMaterial.STONE);
                if (sLvl >= 1) {
                    float saveChance = switch (sLvl) {
                        case 1 -> 0.15f;
                        case 2 -> 0.30f;
                        default -> 0.50f;
                    };
                    if (level.getRandom().nextFloat() < saveChance) {
                        return true; // Skip durability damage!
                    }
                }
            }

            stack.hurtAndBreak(1, miner, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    private void applyGoldFrenzyStack(ItemStack stack, LivingEntity entity, int gLvl) {
        if (gLvl < 1 || entity == null || entity.level().isClientSide()) return;

        GildedFrenzyData currentData = stack.get(TelumComponents.GILDED_FRENZY);
        long currentTime = entity.level().getGameTime();

        int currentStacks = 0;
        if (currentData != null && currentData.expiryGameTime() > currentTime) {
            currentStacks = currentData.stacks();
        }

        int nextStacks = Math.min(3, currentStacks + 1);
        long expiryTime = currentTime + 100L; // 5 seconds (100 ticks)

        stack.set(TelumComponents.GILDED_FRENZY, new GildedFrenzyData(nextStacks, expiryTime));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        AssembledToolData data = getToolData(stack);

        if (data != null) {
            Player player = context.getPlayer();

            // 1. Log Stripping (Axe OR any tool with a Wood part)
            int wLvl = data.getMaterialLevel(PartMaterial.WOOD);
            if (data.toolType() == ToolType.AXE || wLvl >= 1) {
                Map<Block, Block> strippables = AxeItemAccessor.getStrippables();
                Block strippedBlock = strippables.get(state.getBlock());

                if (strippedBlock != null) {
                    level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (!level.isClientSide()) {
                        level.setBlock(pos, strippedBlock.defaultBlockState(), 11);
                        if (wLvl >= 1) {
                            // Wood Ability: repair durability on log strip!
                            int repairAmount = switch (wLvl) {
                                case 1 -> 8;
                                case 2 -> 16;
                                default -> 25;
                            };
                            stack.setDamageValue(Math.max(0, stack.getDamageValue() - repairAmount));
                        } else {
                            if (player != null) {
                                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                            }
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            // 2. SHOVEL: Dirt Path Flattening
            if (data.toolType() == ToolType.SHOVEL) {
                Map<Block, BlockState> flattenables = ShovelItemAccessor.getFlattenables();
                BlockState flattenedState = flattenables.get(state.getBlock());

                if (flattenedState != null) {
                    level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0f, 1.0f);
                    if (!level.isClientSide()) {
                        level.setBlock(pos, flattenedState, 11);
                        if (player != null) {
                            stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }

            // 3. HOE: Farmland Tilling
            if (data.toolType() == ToolType.HOE) {
                Map<Block, Pair<Predicate<UseOnContext>, Consumer<UseOnContext>>> tillables = HoeItemAccessor.getTillables();
                Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = tillables.get(state.getBlock());

                if (pair != null) {
                    Predicate<UseOnContext> predicate = pair.getFirst();
                    Consumer<UseOnContext> consumer = pair.getSecond();

                    if (predicate.test(context)) {
                        level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                        if (!level.isClientSide()) {
                            consumer.accept(context);
                            if (player != null) {
                                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                            }
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        return super.useOn(context);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof Player player)) return;

        AssembledToolData data = getToolData(stack);

        // Book progress tracking: register immediately upon holding item in inventory
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            if (data != null) {
                for (ToolPartData part : data.parts()) {
                    dasouza.telum.util.PlayerBookProgressManager.markToolCrafted(serverPlayer, part.material());
                }
            }
        }

        boolean isHeld = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;

        if (isHeld && data != null) {
            // Wind Ability Level 1: Shift + Jump Wind Charge Propulsion
            if (data.getMaterialLevel(PartMaterial.WIND) >= 1) {
                if (player.isCrouching() && player.getDeltaMovement().y > 0.0) {
                    long currentTime = level.getGameTime();
                    Long lastJump = WIND_JUMP_COOLDOWNS.get(player.getUUID());
                    if (lastJump == null || currentTime - lastJump > 12) {
                        int chargeSlot = findWindCharge(player);
                        if (chargeSlot != -1 || player.isCreative()) {
                            if (!player.isCreative() && chargeSlot != -1) {
                                player.getInventory().getItem(chargeSlot).shrink(1);
                            }
                            WIND_JUMP_COOLDOWNS.put(player.getUUID(), currentTime);
                            Vec3 current = player.getDeltaMovement();
                            player.setDeltaMovement(current.x, 1.25, current.z);
                            player.hurtMarked = true;
                            player.fallDistance = 0.0f; // Reset fall damage!
                            level.sendParticles(ParticleTypes.GUST, player.getX(), player.getY(), player.getZ(), 3, 0.2, 0.1, 0.2, 0.02);
                            level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY(), player.getZ(), 2, 0.1, 0.1, 0.1, 0.01);
                            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.8f, 1.2f);
                        }
                    }
                }
            }

            // Iron Ability: Magnetic Pull (Attract nearby dropped items when held in hand)
            int iLvl = data.getMaterialLevel(PartMaterial.IRON);
            if (iLvl >= 1) {
                double radius = switch (iLvl) {
                    case 1 -> 3.0;
                    case 2 -> 5.0;
                    default -> 8.0;
                };

                AABB box = player.getBoundingBox().inflate(radius);
                for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, box)) {
                    if (!item.isAlive() || item.hasPickUpDelay()) continue;

                    Vec3 targetDir = player.position().add(0, 0.8, 0).subtract(item.position()).normalize().scale(0.35);
                    item.setDeltaMovement(targetDir);
                }
            }
        }

        // Gold Ability: Update / clean up Gilded Frenzy Attack Speed modifier
        ItemStack mainHandStack = player.getMainHandItem();
        AssembledToolData mainHandData = getToolData(mainHandStack);

        int gLvl = mainHandData != null ? mainHandData.getMaterialLevel(PartMaterial.GOLD) : 0;
        GildedFrenzyData frenzy = mainHandStack != null ? mainHandStack.get(TelumComponents.GILDED_FRENZY) : null;
        long currentTime = level.getGameTime();
        boolean frenzyActive = gLvl >= 1 && frenzy != null && frenzy.expiryGameTime() > currentTime && frenzy.stacks() > 0;

        var speedAttrInst = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttrInst != null) {
            if (frenzyActive) {
                if (mainHandStack == stack) {
                    float maxBoost = Math.min(gLvl, 3) * 0.10f;
                    float stackRatio = Math.min(3, frenzy.stacks()) / 3.0f;
                    double boostAmount = maxBoost * stackRatio;

                    speedAttrInst.removeModifier(GILDED_FRENZY_ATTACK_SPEED_ID);
                    speedAttrInst.addTransientModifier(new AttributeModifier(
                            GILDED_FRENZY_ATTACK_SPEED_ID,
                            boostAmount,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    ));
                }
            } else {
                if (frenzy != null && frenzy.expiryGameTime() <= currentTime && mainHandStack == stack) {
                    stack.remove(TelumComponents.GILDED_FRENZY);
                }
                if (speedAttrInst.getModifier(GILDED_FRENZY_ATTACK_SPEED_ID) != null) {
                    speedAttrInst.removeModifier(GILDED_FRENZY_ATTACK_SPEED_ID);
                }
            }
        }



        // Riptide Particle Trail (Only for item currently held in mainhand/offhand, spawned behind camera to avoid blocking 1st-person view)
        if ((slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) && player.isAutoSpinAttack() && level.getGameTime() % 3 == 0 && data != null) {
            Vec3 backPos = player.position().add(0, 0.4, 0).subtract(player.getLookAngle().scale(0.95));
            double px = backPos.x + (level.getRandom().nextDouble() - 0.5) * 0.3;
            double py = backPos.y + (level.getRandom().nextDouble() - 0.5) * 0.3;
            double pz = backPos.z + (level.getRandom().nextDouble() - 0.5) * 0.3;

            ToolPartData headPart = data.getPart(dasouza.telum.tool.PartType.HEAD);
            PartMaterial headMat = headPart != null ? headPart.material() : null;

            if (headMat == PartMaterial.SKULK) {
                level.sendParticles(ParticleTypes.SCULK_SOUL, px, py, pz, 1, 0.01, 0.01, 0.01, 0.01);
            } else if (headMat == PartMaterial.WIND) {
                level.sendParticles(ParticleTypes.CLOUD, px, py, pz, 1, 0.01, 0.01, 0.01, 0.01);
            } else if (headMat == PartMaterial.BLAZE) {
                level.sendParticles(ParticleTypes.FLAME, px, py, pz, 1, 0.01, 0.01, 0.01, 0.01);
            }
        }
    }

    public static ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed, AssembledToolData toolData) {
        float knockbackRes = 0.0f;
        ToolType toolType = toolData != null ? toolData.toolType() : null;

        if (toolData != null) {
            // Netherite Ability: Knockback Resistance (nv2 = +0.2, nv3 = +0.4)
            int nLvl = toolData.getMaterialLevel(PartMaterial.NETHERITE);
            if (nLvl == 2) knockbackRes = 0.2f;
            else if (nLvl >= 3) knockbackRes = 0.4f;
        }

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(
                                BASE_ATTACK_DAMAGE_ID,
                                attackDamage,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED,
                        new AttributeModifier(
                                BASE_ATTACK_SPEED_ID,
                                attackSpeed,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND);

        // Swords get sweeping damage ratio (like vanilla SwordItem)
        if (toolType == ToolType.SWORD) {
            builder.add(Attributes.SWEEPING_DAMAGE_RATIO,
                    new AttributeModifier(
                            Telum.id("sweeping_damage_ratio"),
                            0.5,
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }

        if (knockbackRes > 0.0f) {
            builder.add(Attributes.KNOCKBACK_RESISTANCE,
                    new AttributeModifier(
                            BASE_ATTACK_DAMAGE_ID,
                            knockbackRes,
                            AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.HAND);
        }

        return builder.build();
    }

    private static int findWindCharge(Player player) {
        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(Items.WIND_CHARGE)) {
                return i;
            }
        }
        return -1;
    }

    private static int countWindCharges(Player player) {
        int total = 0;
        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(Items.WIND_CHARGE)) {
                total += s.getCount();
            }
        }
        return total;
    }

    private static void consumeWindCharges(Player player, int amount) {
        if (player.isCreative()) return;
        int needed = amount;
        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size && needed > 0; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (s.is(Items.WIND_CHARGE)) {
                int shrink = Math.min(needed, s.getCount());
                s.shrink(shrink);
                needed -= shrink;
            }
        }
    }

    public static ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed) {
        return createAttributes(attackDamage, attackSpeed, null);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        AssembledToolData data = getToolData(stack);
        if (data == null) {
            tooltip.accept(Component.literal("No data").withStyle(ChatFormatting.RED));
            return;
        }

        // 1. Tool Type Header
        tooltip.accept(Component.translatable(data.toolType().getTranslationKey())
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        // If SHIFT is NOT held, show prompt to hold shift
        if (!ClientTooltipHelper.isShiftDown()) {
            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("tooltip.telum.hold_shift"));
            return;
        }

        tooltip.accept(Component.literal(" §8────────────────────────"));

        // 2. Material Abilities Section
        boolean hasAbilities = false;
        for (PartMaterial mat : PartMaterial.values()) {
            int lvl = data.getMaterialLevel(mat);
            if (lvl >= 1) {
                if (!hasAbilities) {
                    tooltip.accept(Component.translatable("tooltip.telum.abilities").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD));
                    hasAbilities = true;
                }
                boolean isMultiLevel = (mat == PartMaterial.SULFUR || mat == PartMaterial.BLAZE || mat == PartMaterial.DIAMOND || mat == PartMaterial.GOLD || mat == PartMaterial.NETHERITE);
                String romanLvl = isMultiLevel ? " " + getRomanLevel(lvl) : "";
                tooltip.accept(Component.translatable("ability.telum." + mat.getMaterialName(), romanLvl)
                        .withStyle(getMaterialColor(mat)));
            }
        }

        // 3. Tool Parts List
        tooltip.accept(Component.empty());
        tooltip.accept(Component.translatable("tooltip.telum.parts").withStyle(ChatFormatting.GRAY));
        for (ToolPartData part : data.parts()) {
            addPartTooltip(tooltip, part);
        }

        tooltip.accept(Component.literal(" §8────────────────────────"));

        // 4. Compact 2-Column Tool Stats Summary with Icons & Material Mining Level Name
        String miningLevelName = getMiningLevelName(data.miningLevel());
        float attackSpeedVal = 4.0f + data.attackSpeed();

        // Row 1: ⚔ Daño | ⚡ Vel. Ataque
        Component row1 = Component.literal("  ⚔ Daño: ")
                .append(Component.literal(String.format("%.1f", data.attackDamage())).withStyle(ChatFormatting.RED))
                .append(Component.literal("    ⚡ Vel. Ataque: "))
                .append(Component.literal(String.format("%.1f", attackSpeedVal)).withStyle(ChatFormatting.YELLOW));
        tooltip.accept(row1);

        // Row 2: ⛏ Minado | ◆ Nivel
        Component row2 = Component.literal("  ⛏ Minado: ")
                .append(Component.literal(String.format("%.1fx", data.miningSpeed())).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("   ◆ Nivel: "))
                .append(Component.literal(miningLevelName).withStyle(ChatFormatting.GOLD));
        tooltip.accept(row2);

        // Row 3: ✚ Durabilidad
        Component row3 = Component.literal("  ✚ Durabilidad: ")
                .append(Component.literal(String.valueOf(data.durability())).withStyle(ChatFormatting.GREEN));
        tooltip.accept(row3);
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

    private String getRomanLevel(int level) {
        return switch (level) {
            case 1 -> "I/III";
            case 2 -> "II/III";
            default -> "III/III";
        };
    }

    private void addPartTooltip(Consumer<Component> tooltip, ToolPartData part) {
        ChatFormatting matColor = getMaterialColor(part.material());
        tooltip.accept(Component.literal("  ")
                .append(Component.translatable(part.partType().getTranslationKey()))
                .append(Component.literal(" - "))
                .append(Component.translatable(part.material().getTranslationKey()))
                .withStyle(matColor));
    }

    @Override
    public InteractionResult use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        AssembledToolData data = getToolData(stack);
        if (data != null && data.toolType() == ToolType.TRIDENT) {
            ToolPartData headPart = data.getPart(dasouza.telum.tool.PartType.HEAD);
            PartMaterial headMat = headPart != null ? headPart.material() : null;
            boolean inWater = player.isInWaterOrRain();

            boolean canCreeperRiptide = headMat == PartMaterial.CREEPER;
            boolean canWindRiptide = headMat == PartMaterial.WIND && (player.isCreative() || countWindCharges(player) >= 2);
            boolean canBlazeRiptide = headMat == PartMaterial.BLAZE && (player.isCreative() || player.isOnFire() || player.isInLava());
            boolean canSkulkRiptide = headMat == PartMaterial.SKULK && (player.isCreative() || player.totalExperience >= 5 || player.experienceLevel > 0);
            boolean canStandardRiptide = (headMat == null || (headMat != PartMaterial.WIND && headMat != PartMaterial.BLAZE && headMat != PartMaterial.SKULK && headMat != PartMaterial.CREEPER)) && inWater;

            if (canCreeperRiptide || canWindRiptide || canBlazeRiptide || canSkulkRiptide || canStandardRiptide) {
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        AssembledToolData data = getToolData(stack);
        if (data != null && data.toolType() == ToolType.TRIDENT) {
            return 72000;
        }
        return super.getUseDuration(stack, entity);
    }

    @Override
    public net.minecraft.world.item.ItemUseAnimation getUseAnimation(ItemStack stack) {
        AssembledToolData data = getToolData(stack);
        if (data != null && data.toolType() == ToolType.TRIDENT) {
            return net.minecraft.world.item.ItemUseAnimation.SPEAR;
        }
        return super.getUseAnimation(stack);
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            AssembledToolData data = getToolData(stack);
            if (data != null && data.toolType() == ToolType.TRIDENT) {
                ToolPartData headPart = data.getPart(dasouza.telum.tool.PartType.HEAD);
                PartMaterial headMat = headPart != null ? headPart.material() : null;
                int chargeDuration = this.getUseDuration(stack, entity) - timeLeft;
                boolean inWater = player.isInWaterOrRain();

                boolean canCreeperRiptide = headMat == PartMaterial.CREEPER;
                boolean canWindRiptide = headMat == PartMaterial.WIND && (player.isCreative() || countWindCharges(player) >= 2);
                boolean canBlazeRiptide = headMat == PartMaterial.BLAZE && (player.isCreative() || player.isOnFire() || player.isInLava());
                boolean canSkulkRiptide = headMat == PartMaterial.SKULK && (player.isCreative() || player.totalExperience >= 5 || player.experienceLevel > 0);
                boolean canStandardRiptide = (headMat == null || (headMat != PartMaterial.WIND && headMat != PartMaterial.BLAZE && headMat != PartMaterial.SKULK && headMat != PartMaterial.CREEPER)) && inWater;

                if (chargeDuration >= 10 && (canCreeperRiptide || canWindRiptide || canBlazeRiptide || canSkulkRiptide || canStandardRiptide)) {
                    if (!level.isClientSide()) {
                        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                        if (headMat == PartMaterial.WIND && !player.isCreative()) {
                            consumeWindCharges(player, 2);
                        } else if (headMat == PartMaterial.SKULK && !player.isCreative()) {
                            player.giveExperiencePoints(-5);
                        }
                    }
                    float f = player.getYRot();
                    float g = player.getXRot();
                    float h = -net.minecraft.util.Mth.sin(f * (float) (Math.PI / 180.0)) * net.minecraft.util.Mth.cos(g * (float) (Math.PI / 180.0));
                    float k = -net.minecraft.util.Mth.sin(g * (float) (Math.PI / 180.0));
                    float l = net.minecraft.util.Mth.cos(f * (float) (Math.PI / 180.0)) * net.minecraft.util.Mth.cos(g * (float) (Math.PI / 180.0));

                    int activeLvl = headMat == PartMaterial.CREEPER ? 3 : (headMat != null ? data.getMaterialLevel(headMat) : 1);
                    float speedMultiplier = 0.75f + (float) activeLvl * 0.75f;
                    player.push((double)(h * speedMultiplier), (double)(k * speedMultiplier), (double)(l * speedMultiplier));
                    player.startAutoSpinAttack(20, 8.0f, stack);
                    if (player.onGround()) {
                        player.move(net.minecraft.world.entity.MoverType.SELF, new Vec3(0.0, 1.1999999, 0.0));
                    }

                    if (headMat == PartMaterial.CREEPER && level instanceof ServerLevel serverLevel) {
                        Vec3 feetPos = player.position();

                        // 1. Denser & richer particle burst (Emitter, Explosion, Large Smoke, Flames, Crit sparks)
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, feetPos.x, feetPos.y + 0.5, feetPos.z, 2, 0.2, 0.2, 0.2, 0.0);
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION, feetPos.x, feetPos.y + 0.5, feetPos.z, 6, 0.5, 0.5, 0.5, 0.1);
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, feetPos.x, feetPos.y + 0.2, feetPos.z, 25, 0.6, 0.4, 0.6, 0.08);
                        serverLevel.sendParticles(ParticleTypes.FLAME, feetPos.x, feetPos.y + 0.3, feetPos.z, 20, 0.5, 0.3, 0.5, 0.1);
                        serverLevel.sendParticles(ParticleTypes.CRIT, feetPos.x, feetPos.y + 0.5, feetPos.z, 15, 0.4, 0.4, 0.4, 0.15);

                        level.playSound(null, feetPos.x, feetPos.y, feetPos.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2f, 0.9f);

                        // 2. Recoil Damage to Player
                        player.hurt(player.damageSources().explosion(null, player), 2.0f);

                        // 3. AOE Damage & Blast Knockback to Nearby Enemies
                        AABB area = new AABB(feetPos.x - 4.0, feetPos.y - 1.0, feetPos.z - 4.0, feetPos.x + 4.0, feetPos.y + 3.0, feetPos.z + 4.0);
                        for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, area)) {
                            if (nearby != player && nearby.isAlive()) {
                                nearby.hurt(player.damageSources().explosion(player, player), 6.0f);
                                Vec3 push = nearby.position().subtract(feetPos).normalize().scale(0.8).add(0, 0.4, 0);
                                nearby.setDeltaMovement(nearby.getDeltaMovement().add(push));
                            }
                        }
                    } else if (headMat == PartMaterial.WIND && level instanceof ServerLevel serverLevel) {
                        Vec3 spawnPos = player.position().add(0, 0.4, 0).subtract(player.getLookAngle().scale(0.95));
                        serverLevel.sendParticles(ParticleTypes.GUST, spawnPos.x, spawnPos.y, spawnPos.z, 2, 0.1, 0.1, 0.1, 0.02);
                        serverLevel.sendParticles(ParticleTypes.CLOUD, spawnPos.x, spawnPos.y, spawnPos.z, 2, 0.1, 0.1, 0.1, 0.01);
                    } else if (headMat == PartMaterial.BLAZE && level instanceof ServerLevel serverLevel) {
                        Vec3 spawnPos = player.position().add(0, 0.4, 0).subtract(player.getLookAngle().scale(0.95));
                        serverLevel.sendParticles(ParticleTypes.FLAME, spawnPos.x, spawnPos.y, spawnPos.z, 5, 0.2, 0.2, 0.2, 0.05);
                        serverLevel.sendParticles(ParticleTypes.LAVA, spawnPos.x, spawnPos.y, spawnPos.z, 2, 0.1, 0.1, 0.1, 0.02);
                    } else if (headMat == PartMaterial.SKULK && level instanceof ServerLevel serverLevel) {
                        Vec3 spawnPos = player.position().add(0, 0.4, 0).subtract(player.getLookAngle().scale(0.95));
                        serverLevel.sendParticles(ParticleTypes.SCULK_SOUL, spawnPos.x, spawnPos.y, spawnPos.z, 3, 0.1, 0.1, 0.1, 0.01);
                    }

                    net.minecraft.sounds.SoundEvent soundEvent = switch (activeLvl) {
                        case 1 -> SoundEvents.TRIDENT_RIPTIDE_1.value();
                        case 2 -> SoundEvents.TRIDENT_RIPTIDE_2.value();
                        default -> SoundEvents.TRIDENT_RIPTIDE_3.value();
                    };
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundSource.PLAYERS, 1.0f, 1.0f);
                    return true;
                }
            }
        }
        return super.releaseUsing(stack, level, entity, timeLeft);
    }

    private ChatFormatting getMaterialColor(PartMaterial material) {
        return switch (material) {
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

    public static ItemStack getSmeltedResult(ItemStack rawStack, ServerLevel level) {
        if (rawStack.isEmpty()) return ItemStack.EMPTY;
        try {
            var input = new net.minecraft.world.item.crafting.SingleRecipeInput(rawStack);
            var recipeHolder = level.getServer().getRecipeManager().getRecipeFor(
                    net.minecraft.world.item.crafting.RecipeType.SMELTING,
                    input,
                    level
            ).orElse(null);

            if (recipeHolder != null) {
                ItemStack result = recipeHolder.value().assemble(input).copy();
                if (!result.isEmpty()) {
                    result.setCount(rawStack.getCount() * result.getCount());
                    return result;
                }
            }
        } catch (Exception ignored) {}

        return getSmeltedResult(rawStack);
    }

    private static ItemStack getSmeltedResult(ItemStack rawStack) {
        if (rawStack.isEmpty()) return ItemStack.EMPTY;
        Item rawItem = rawStack.getItem();
        if (rawItem == Items.RAW_IRON || rawItem == Items.IRON_ORE || rawItem == Items.DEEPSLATE_IRON_ORE) {
            return new ItemStack(Items.IRON_INGOT, rawStack.getCount());
        }
        if (rawItem == Items.RAW_GOLD || rawItem == Items.GOLD_ORE || rawItem == Items.DEEPSLATE_GOLD_ORE || rawItem == Items.NETHER_GOLD_ORE) {
            return new ItemStack(Items.GOLD_INGOT, rawStack.getCount());
        }
        if (rawItem == Items.RAW_COPPER || rawItem == Items.COPPER_ORE || rawItem == Items.DEEPSLATE_COPPER_ORE) {
            return new ItemStack(Items.COPPER_INGOT, rawStack.getCount());
        }
        if (rawItem == Items.ANCIENT_DEBRIS) {
            return new ItemStack(Items.NETHERITE_SCRAP, rawStack.getCount());
        }
        if (rawItem == Items.COBBLESTONE || rawItem == Items.COBBLED_DEEPSLATE) {
            return new ItemStack(rawItem == Items.COBBLESTONE ? Items.STONE : Items.DEEPSLATE, rawStack.getCount());
        }
        if (rawItem == Items.SAND || rawItem == Items.RED_SAND) {
            return new ItemStack(Items.GLASS, rawStack.getCount());
        }
        if (rawItem == Items.WET_SPONGE) {
            return new ItemStack(Items.SPONGE, rawStack.getCount());
        }
        if (rawItem == Items.CLAY) {
            return new ItemStack(Items.TERRACOTTA, rawStack.getCount());
        }
        if (rawItem == Items.CLAY_BALL) {
            return new ItemStack(Items.BRICK, rawStack.getCount());
        }
        if (rawItem == Items.OAK_LOG || rawItem == Items.SPRUCE_LOG || rawItem == Items.BIRCH_LOG || rawItem == Items.JUNGLE_LOG || rawItem == Items.ACACIA_LOG || rawItem == Items.DARK_OAK_LOG || rawItem == Items.MANGROVE_LOG || rawItem == Items.CHERRY_LOG) {
            return new ItemStack(Items.CHARCOAL, rawStack.getCount());
        }
        return ItemStack.EMPTY;
    }

    private boolean isCopperOrCoalOre(BlockState state) {
        return state.is(BlockTags.COPPER_ORES) || state.is(Blocks.COAL_ORE) || state.is(Blocks.DEEPSLATE_COAL_ORE);
    }

    private boolean isEffectiveOn(ToolType toolType, BlockState state) {
        return switch (toolType) {
            case PICKAXE -> state.is(BlockTags.MINEABLE_WITH_PICKAXE);
            case AXE -> state.is(BlockTags.MINEABLE_WITH_AXE);
            case SHOVEL -> state.is(BlockTags.MINEABLE_WITH_SHOVEL);
            case HOE -> state.is(BlockTags.MINEABLE_WITH_HOE);
            case SWORD, TRIDENT -> true;
        };
    }
}
