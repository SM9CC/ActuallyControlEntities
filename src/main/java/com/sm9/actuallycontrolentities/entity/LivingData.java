package com.sm9.actuallycontrolentities.entity;

import com.sm9.actuallycontrolentities.spawner.ACESpawnEntry;
import com.sm9.actuallycontrolentities.util.General;
import com.sm9.actuallycontrolentities.world.DimensionData;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;

@SuppressWarnings("unused")
public class LivingData implements Comparable<LivingData> {
    private final ResourceLocation resourceLocation;
    private final String mobResourceName;
    private final String modName;
    private final String mobName;
    private final HashMap<Integer, DimensionData> dimensionDataMap;
    private final HashMap<Biome, ACESpawnEntry> defaultSpawnEntryMap;
    private final Class<? extends EntityLiving> entityClass;
    private ACESpawnEntry mostCommonDefaultSpawnEntry;

    public LivingData(Class<? extends EntityLiving> entityClass, ResourceLocation mobResourceLocation, String mobName) {
        this.entityClass = entityClass;
        this.resourceLocation = mobResourceLocation;
        this.mobResourceName = mobResourceLocation.toString();
        this.modName = General.capitalizeString(mobResourceLocation.getNamespace());

        while (mobName.contains(":")) {
            mobName = mobName.split(":")[1];
        }

        this.mobName = General.capitalizeString(mobName);

        dimensionDataMap = new HashMap<>();
        defaultSpawnEntryMap = new HashMap<>();
    }

    public void setDefaultSpawnEntry(Biome biome, ACESpawnEntry spawnEntry) {
        defaultSpawnEntryMap.put(biome, spawnEntry);
    }

    public ACESpawnEntry getMostCommonDefaultSpawnEntry() {
        return mostCommonDefaultSpawnEntry;
    }

    public void setMostCommonDefaultSpawnEntry(ACESpawnEntry spawnEntry) {
        mostCommonDefaultSpawnEntry = spawnEntry;
    }

    public ACESpawnEntry getDefaultSpawnEntry(Biome biome) {
        return defaultSpawnEntryMap.get(biome);
    }

    public HashMap<Biome, ACESpawnEntry> getDefaultSpawnEntryMap() {
        return defaultSpawnEntryMap;
    }

    public Class<? extends EntityLiving> getEntityClass() {
        return entityClass;
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public String getMobResourceName() {
        return mobResourceName;
    }

    public String getMobName() {
        return mobName;
    }

    public String getModName() {
        return modName;
    }

    public DimensionData initDimensionData(int dimensionId) {
        DimensionData dimensionData = getDimensionData(dimensionId);

        if (dimensionData == null) {
            dimensionData = new DimensionData(dimensionId, this);
            dimensionDataMap.put(dimensionId, dimensionData);
        }

        return dimensionData;
    }

    public DimensionData getDimensionData(int dimensionId) {
        return dimensionDataMap.get(dimensionId);
    }

    @Override
    public int compareTo(LivingData o) {
        int firstCompare = modName.compareTo(o.modName);
        return firstCompare == 0 ? mobName.compareTo(o.mobName) : firstCompare;
    }
}
