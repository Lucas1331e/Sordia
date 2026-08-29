package dasouza.telum.mixin;

import dasouza.telum.Telum;
import dasouza.telum.component.AssembledToolData;
import dasouza.telum.component.TelumComponents;
import dasouza.telum.component.ToolPartData;
import dasouza.telum.item.TelumItems;
import dasouza.telum.tool.PartMaterial;
import dasouza.telum.tool.PartType;
import dasouza.telum.tool.ToolStatsCalculator;
import dasouza.telum.tool.ToolType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ZombieVillager.class)
public class ZombieVillagerEquipmentMixin {

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void telum$equipSordiaToolInMarbleCaves(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, SpawnGroupData spawnData, CallbackInfoReturnable<SpawnGroupData> cir) {
        ZombieVillager zombieVillager = (ZombieVillager) (Object) this;
        var biomeHolder = level.getBiome(zombieVillager.blockPosition());
        ResourceKey<Biome> MARBLE_CAVES = ResourceKey.create(Registries.BIOME, Telum.id("marble_caves"));

        if (biomeHolder.is(MARBLE_CAVES)) {
            RandomSource random = level.getRandom();
            ToolType toolType = random.nextBoolean() ? ToolType.SWORD : ToolType.AXE;

            PartMaterial[] materials = new PartMaterial[]{
                    PartMaterial.WOOD,
                    PartMaterial.STONE,
                    PartMaterial.IRON,
                    PartMaterial.GOLD,
                    PartMaterial.DIAMOND,
                    PartMaterial.ZOMBIE,
                    PartMaterial.SKULK,
                    PartMaterial.SPIDER,
                    PartMaterial.SKELETON,
                    PartMaterial.CREEPER
            };

            PartMaterial headMat = materials[random.nextInt(materials.length)];
            PartMaterial handleMat = materials[random.nextInt(materials.length)];
            PartMaterial gripMat = materials[random.nextInt(materials.length)];

            List<ToolPartData> parts = List.of(
                    new ToolPartData(PartType.HEAD, headMat),
                    new ToolPartData(PartType.HANDLE, handleMat),
                    new ToolPartData(PartType.GRIP, gripMat)
            );

            AssembledToolData data = ToolStatsCalculator.calculate(toolType, parts);
            ItemStack assembledStack = new ItemStack(TelumItems.ASSEMBLED_TOOL);
            assembledStack.set(TelumComponents.ASSEMBLED_TOOL, data);

            zombieVillager.setItemSlot(EquipmentSlot.MAINHAND, assembledStack);
            zombieVillager.setDropChance(EquipmentSlot.MAINHAND, 0.085f);
        }
    }
}
