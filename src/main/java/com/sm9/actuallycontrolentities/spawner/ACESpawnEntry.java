package com.sm9.actuallycontrolentities.spawner;

import com.sm9.actuallycontrolentities.common.config.MobConfig;
import com.sm9.actuallycontrolentities.common.config.SpawnerConfig;
import com.sm9.actuallycontrolentities.entity.LivingData;
import com.sm9.actuallycontrolentities.util.EntityUtils;
import com.sm9.actuallycontrolentities.world.DimensionData;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;

import java.util.List;

@SuppressWarnings("ALL")
public class ACESpawnEntry extends WeightedRandom.Item {
    private final Class<? extends EntityLiving> cLazz;
    private final int minGroupCount;
    private final int maxGroupCount;
    private final int weight;
    private final EnumCreatureType creatureType;
    private final SpawnPlacementType spawnPlacementType;
    private final String mobResourceName;
    private final List<ACESpawnEntry> validReplacements;
    private final LivingData livingData;
    private boolean mostCommon;

    public ACESpawnEntry(Class<? extends EntityLiving> entityclassIn, int minGroupCount, int maxGroupCount, int weight, EnumCreatureType creatureType,
                         SpawnPlacementType spawnPlacementType, LivingData livingData, List<ACESpawnEntry> validReplacements) {
        super(weight);
        this.minGroupCount = minGroupCount;
        this.maxGroupCount = maxGroupCount;
        this.weight = weight;
        this.creatureType = creatureType;
        this.spawnPlacementType = spawnPlacementType;
        this.cLazz = entityclassIn;
        this.mobResourceName = EntityUtils.getEntityResourceNameFromClass(entityclassIn);
        this.livingData = livingData;
        this.validReplacements = validReplacements;
    }

    public int getMinGroupCount() {
        return minGroupCount;
    }

    public int getMaxGroupCount() {
        return maxGroupCount;
    }

    public EnumCreatureType getCreatureType() {
        return creatureType;
    }

    public SpawnPlacementType getSpawnPlacementType() {
        return spawnPlacementType;
    }

    public int getWeight() {
        return weight;
    }

    public Class<? extends EntityLiving> getEntryClass() {
        return cLazz;
    }

    public String getmobResourceName() {
        return mobResourceName;
    }

    public EntityLiving newInstance(World world) throws Exception {
        net.minecraftforge.fml.common.registry.EntityEntry entry = net.minecraftforge.fml.common.registry.EntityRegistry.getEntry(this.cLazz);
        if (entry != null) return (EntityLiving) entry.newInstance(world);
        return this.cLazz.getConstructor(World.class).newInstance(world);
    }

    public boolean isIdentical(ACESpawnEntry otherSpawnEntry) {
        return otherSpawnEntry.getMinGroupCount() == this.minGroupCount && otherSpawnEntry.getMaxGroupCount() == this.maxGroupCount &&
                otherSpawnEntry.getWeight() == this.weight;
    }

    public boolean getMostCommon() {
        return this.mostCommon;
    }

    public void setMostCommon(boolean mostCommon) {
        this.mostCommon = mostCommon;
    }

    public MobConfig getMobConfig(int dimensionId) {
        if (livingData == null) {
            return null;
        }

        DimensionData dimensionData = livingData.getDimensionData(dimensionId);

        if (dimensionData == null) {
            return null;
        }

        return dimensionData.getMobConfig();
    }

    public SpawnerConfig getSpawnerConfig(int dimensionId) {
        if (livingData == null) {
            return null;
        }

        DimensionData dimensionData = livingData.getDimensionData(dimensionId);

        if (dimensionData == null) {
            return null;
        }

        return dimensionData.getSpawnerConfig();
    }
}
