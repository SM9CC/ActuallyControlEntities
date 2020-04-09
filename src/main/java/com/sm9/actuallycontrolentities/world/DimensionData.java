package com.sm9.actuallycontrolentities.world;

import com.sm9.actuallycontrolentities.common.config.MobConfig;
import com.sm9.actuallycontrolentities.common.config.SpawnerConfig;
import com.sm9.actuallycontrolentities.entity.LivingData;
import com.sm9.actuallycontrolentities.spawner.ACESpawnEntry;
import com.sm9.actuallycontrolentities.storage.DimensionSpawnerConfigs;
import com.sm9.actuallycontrolentities.util.DimensionUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.biome.Biome;

import java.io.File;
import java.util.HashMap;

import static com.sm9.actuallycontrolentities.ActuallyControlEntities.rootConfigDir;

public class DimensionData {
    private int dimensionId;
    private String dimensionName;
    private HashMap<Biome, ACESpawnEntry> biomeSpawnEntries;
    private HashMap<String, ACESpawnEntry> structureSpawnEntries;
    private MobConfig mobConfig;
    private SpawnerConfig spawnerConfig;
    private LivingData livingData;

    public DimensionData(int dimensionId, LivingData livingData) {
        String dimensionName = DimensionUtils.getDimensionNameFromId(dimensionId);

        if (dimensionName == null) {
            throw new NullPointerException();
        }

        File dimDir = new File(rootConfigDir, dimensionName);

        if (!dimDir.exists() && !dimDir.mkdir()) {
            throw new NullPointerException();
        }

        String modName = livingData.getModName();
        String mobName = livingData.getMobName();
        String mobResourceName = livingData.getMobResourceName();
        Class<? extends EntityLiving> entityClass = livingData.getEntityClass();

        File modMobDir = new File(dimDir, modName);

        if (!modMobDir.exists() && !modMobDir.mkdir()) {
            throw new NullPointerException();
        }

        File mobFile = new File(modMobDir, livingData.getMobName() + ".cfg");

        this.dimensionId = dimensionId;
        this.dimensionName = dimensionName;
        this.biomeSpawnEntries = new HashMap<>();
        this.structureSpawnEntries = new HashMap<>();
        this.spawnerConfig = new SpawnerConfig(new File(dimDir, "spawner.cfg"));
        this.livingData = livingData;
        this.mobConfig = new MobConfig(mobFile, mobResourceName, mobName, modName, this, entityClass);

        DimensionSpawnerConfigs.put(dimensionId, spawnerConfig);
    }

    public LivingData getLivingData() {
        return livingData;
    }

    public String getDimensionName() {
        return dimensionName;
    }

    public int getDimensionId() {
        return dimensionId;
    }

    public void addSpawnEntry(Biome biome, ACESpawnEntry spawnEntry) {
        biomeSpawnEntries.put(biome, spawnEntry);
    }

    public ACESpawnEntry getSpawnEntry(Biome biome) {
        return biomeSpawnEntries.get(biome);
    }

    public void addStructureSpawnEntry(String structure, ACESpawnEntry spawnEntry) {
        structureSpawnEntries.put(structure, spawnEntry);
    }

    public ACESpawnEntry getStructureSpawnEntry(String structure) {
        return structureSpawnEntries.get(structure);
    }

    public MobConfig getMobConfig() {
        return mobConfig;
    }

    public SpawnerConfig getSpawnerConfig() {
        return spawnerConfig;
    }

}
