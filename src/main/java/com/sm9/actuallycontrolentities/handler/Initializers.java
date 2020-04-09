package com.sm9.actuallycontrolentities.handler;

import com.sm9.actuallycontrolentities.common.config.MobConfig;
import com.sm9.actuallycontrolentities.common.config.SpawnerConfig;
import com.sm9.actuallycontrolentities.entity.LivingData;
import com.sm9.actuallycontrolentities.spawner.ACESpawnEntry;
import com.sm9.actuallycontrolentities.storage.*;
import com.sm9.actuallycontrolentities.util.DimensionUtils;
import com.sm9.actuallycontrolentities.util.General;
import com.sm9.actuallycontrolentities.world.DimensionData;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.logging.log4j.Level;

import java.util.*;

import static com.sm9.actuallycontrolentities.ActuallyControlEntities.mainConfig;
import static net.minecraft.entity.EntityLiving.SpawnPlacementType.IN_WATER;

@SuppressWarnings({"SpellCheckingInspection", "unchecked"})
public class Initializers {
    static boolean configReloading;

    static void cacheLivingEntities() {
        Set<Map.Entry<ResourceLocation, EntityEntry>> allEntities = ForgeRegistries.ENTITIES.getEntries();
        ProgressManager.ProgressBar progressBar = ProgressManager.push("Building mob cache", allEntities.size());
        Class<? extends Entity> entityClass;
        Class<? extends EntityLiving> entityLivingClass;

        EntityEntry entityEntry;
        ResourceLocation resourceLocation;
        String mobResourceName;
        long startTime = System.currentTimeMillis();
        int counter = 0;
        LivingData livingData;

        for (Map.Entry<ResourceLocation, EntityEntry> mapEntry : allEntities) {
            entityEntry = mapEntry.getValue();
            entityClass = entityEntry.getEntityClass();
            resourceLocation = mapEntry.getKey();
            mobResourceName = resourceLocation.toString();

            if (!EntityLiving.class.isAssignableFrom(entityClass)) {
                progressBar.step(String.format("Skipped: %s", mobResourceName));
                continue;
            }

            entityLivingClass = (Class<? extends EntityLiving>) entityClass;

            progressBar.step(String.format("Cached: %s", resourceLocation.toString()));
            livingData = new LivingData(entityLivingClass, resourceLocation, entityEntry.getName());

            if (LivingDataEntries.add(mobResourceName, entityLivingClass, livingData) != -1) {
                counter++;
            }
        }

        ProgressManager.pop(progressBar);
        General.debugToConsole(Level.INFO, "Cached %d living entities in %d ms", counter, System.currentTimeMillis() - startTime);
        LivingDataEntries.sort();
    }

    static void cacheExistingSpawnEntries() {
        int counter = 0;
        long startTime = System.currentTimeMillis();

        ACESpawnEntry aceSpawnEntry;
        LivingData livingData;

        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            for (Biome.SpawnListEntry spawnEntry : biome.getSpawnableList(EnumCreatureType.MONSTER)) {
                livingData = LivingDataEntries.get(spawnEntry.entityClass);

                if (livingData == null) {
                    continue;
                }

                aceSpawnEntry = new ACESpawnEntry(spawnEntry.entityClass, spawnEntry.minGroupCount, spawnEntry.maxGroupCount, spawnEntry.itemWeight, EnumCreatureType.MONSTER,
                        EntitySpawnPlacementRegistry.getPlacementForEntity(spawnEntry.entityClass), livingData, null);

                livingData.setDefaultSpawnEntry(biome, aceSpawnEntry);
                counter++;
            }

            for (Biome.SpawnListEntry spawnEntry : biome.getSpawnableList(EnumCreatureType.CREATURE)) {
                livingData = LivingDataEntries.get(spawnEntry.entityClass);

                if (livingData == null) {
                    continue;
                }

                aceSpawnEntry = new ACESpawnEntry(spawnEntry.entityClass, spawnEntry.minGroupCount, spawnEntry.maxGroupCount, spawnEntry.itemWeight, EnumCreatureType.CREATURE,
                        EntitySpawnPlacementRegistry.getPlacementForEntity(spawnEntry.entityClass), livingData, null);

                livingData.setDefaultSpawnEntry(biome, aceSpawnEntry);
                counter++;
            }

            for (Biome.SpawnListEntry spawnEntry : biome.getSpawnableList(EnumCreatureType.AMBIENT)) {
                livingData = LivingDataEntries.get(spawnEntry.entityClass);

                if (livingData == null) {
                    continue;
                }

                aceSpawnEntry = new ACESpawnEntry(spawnEntry.entityClass, spawnEntry.minGroupCount, spawnEntry.maxGroupCount, spawnEntry.itemWeight, EnumCreatureType.AMBIENT,
                        EntitySpawnPlacementRegistry.getPlacementForEntity(spawnEntry.entityClass), livingData, null);

                livingData.setDefaultSpawnEntry(biome, aceSpawnEntry);
                counter++;
            }

            for (Biome.SpawnListEntry spawnEntry : biome.getSpawnableList(EnumCreatureType.WATER_CREATURE)) {
                livingData = LivingDataEntries.get(spawnEntry.entityClass);

                if (livingData == null) {
                    continue;
                }

                aceSpawnEntry = new ACESpawnEntry(spawnEntry.entityClass, spawnEntry.minGroupCount, spawnEntry.maxGroupCount, spawnEntry.itemWeight, EnumCreatureType.WATER_CREATURE,
                        IN_WATER, livingData, null);

                livingData.setDefaultSpawnEntry(biome, aceSpawnEntry);
                counter++;
            }
        }

        General.debugToConsole(Level.INFO, "Cached %d existing spawn entries in %d ms", counter, System.currentTimeMillis() - startTime);
    }

    static void cacheMostCommonSpawnEntries() {
        int counter = 0;
        long startTime = System.currentTimeMillis();
        ProgressManager.ProgressBar progressBar = ProgressManager.push("Caching common spawn entries", LivingDataEntries.size());
        ArrayList<LivingData> allLivingDataEntries = LivingDataEntries.getAll();

        for (LivingData livingData : allLivingDataEntries) {
            progressBar.step(String.format("%s - %s", General.capitalizeString(livingData.getModName()), General.capitalizeString(livingData.getMobName())));
            if (!cacheMostCommonSpawnEntry(livingData)) {
                continue;
            }

            counter++;
        }

        ProgressManager.pop(progressBar);
        General.debugToConsole(Level.INFO, "Cached %d common spawn entries in %d ms", counter, System.currentTimeMillis() - startTime);
    }

    static void initDimensionConfigs() {
        int[] allDimensions = DimensionUtils.getAllDimensions();

        String dimensionName;
        ProgressManager.ProgressBar progressBar;

        int configCounter = 0;
        long startTime = System.currentTimeMillis();

        SpawnerConfig spawnerConfig;
        ArrayList<LivingData> allLivingDataEntries = LivingDataEntries.getAll();
        DimensionData dimensionData;

        for (int dimensionId : allDimensions) {
            dimensionName = DimensionUtils.getDimensionNameFromId(dimensionId);

            if (dimensionName == null) {
                throw new NullPointerException();
            }

            progressBar = ProgressManager.push(String.format("Initializing %s", dimensionName), allLivingDataEntries.size());

            for (LivingData livingData : allLivingDataEntries) {
                progressBar.step(String.format("%s - %s", General.capitalizeString(livingData.getModName()), General.capitalizeString(livingData.getMobName())));
                dimensionData = livingData.initDimensionData(dimensionId);

                if (dimensionData == null) {
                    throw new NullPointerException();
                }

                spawnerConfig = dimensionData.getSpawnerConfig();

                if (spawnerConfig == null) {
                    throw new NullPointerException();
                }

                configCounter++;
            }

            ProgressManager.pop(progressBar);
        }

        General.debugToConsole(Level.INFO, "Initialized %d mob configs in %d ms", configCounter, System.currentTimeMillis() - startTime);
    }

    public static void loadConfigs(ICommandSender commandSender) {
        if (commandSender != null) {
            commandSender.sendMessage(new TextComponentString("Reloading configs, this may take a while."));
        }

        configReloading = true;

        int[] allDimensions = DimensionUtils.getAllDimensions();
        MobConfig mobConfig;
        ACESpawnEntry spawnEntry;
        String dimensionName;
        DimensionData dimensionData;
        ProgressManager.ProgressBar progressBar;
        SpawnerConfig spawnerConfig;

        int counter = 0;
        long startTime = System.currentTimeMillis();
        boolean worldSpawner;
        boolean structureSpawner;
        EnumCreatureType creatureType;
        EntityLiving.SpawnPlacementType spawnPlacementType;

        mainConfig.reloadFromConfig();

        for (int dimensionId : allDimensions) {
            dimensionName = DimensionUtils.getDimensionNameFromId(dimensionId);

            if (dimensionName == null) {
                throw new NullPointerException();
            }

            spawnerConfig = DimensionSpawnerConfigs.get(dimensionId);

            if (spawnerConfig == null) {
                continue;
            }

            spawnerConfig.reloadFromConfig();

            progressBar = ProgressManager.push(String.format("Loading %s", dimensionName), LivingDataEntries.size());
            ArrayList<LivingData> allLivingDataEntries = LivingDataEntries.getAll();

            for (LivingData livingData : allLivingDataEntries) {
                progressBar.step(String.format("%s - %s", General.capitalizeString(livingData.getModName()), General.capitalizeString(livingData.getMobName())));

                dimensionData = livingData.getDimensionData(dimensionId);

                if (dimensionData == null) {
                    throw new NullPointerException();
                }

                mobConfig = dimensionData.getMobConfig();

                if (mobConfig == null) {
                    throw new NullPointerException();
                }

                mobConfig.reloadFromConfig();

                counter++;

                if (!mobConfig.getCanSpawn()) {
                    continue;
                }

                worldSpawner = mobConfig.getWorldSpawnerEnabled();
                structureSpawner = mobConfig.getStructureSpawnerEnabled();

                if (!worldSpawner && !structureSpawner) {
                    continue;
                }

                creatureType = mobConfig.getCreatureType();
                spawnPlacementType = mobConfig.getSpawnPlacementType();

                switch (creatureType) {
                    case MONSTER: {
                        MonsterDimensionDataMaps.put(dimensionId, spawnPlacementType, dimensionData);
                        break;
                    }

                    case CREATURE: {
                        CreatureDimensionDataMaps.put(dimensionId, spawnPlacementType, dimensionData);
                        break;
                    }

                    case AMBIENT: {
                        AmbientDimensionDataMaps.put(dimensionId, spawnPlacementType, dimensionData);
                        break;
                    }

                    case WATER_CREATURE: {
                        WaterCreatureDimensionDataMaps.put(dimensionId, spawnPlacementType, dimensionData);
                        break;
                    }
                }

                if (worldSpawner) {
                    for (Biome biome : mobConfig.getBiomes()) {
                        spawnEntry = new ACESpawnEntry(mobConfig.cLazz, mobConfig.getBiomeMinGroupCount(biome),
                                mobConfig.getBiomeMaxGroupCount(biome), mobConfig.getBiomeSpawnWeight(biome),
                                creatureType, spawnPlacementType, livingData, null);

                        dimensionData.addSpawnEntry(biome, spawnEntry);
                    }
                }

                if (structureSpawner) {
                    for (String structure : mobConfig.getStructures()) {
                        spawnEntry = new ACESpawnEntry(mobConfig.cLazz, mobConfig.getStructureMinGroupCount(structure),
                                mobConfig.getStructureMaxGroupCount(structure), mobConfig.getStructureSpawnWeight(structure),
                                creatureType, spawnPlacementType, livingData, null);

                        dimensionData.addStructureSpawnEntry(structure, spawnEntry);
                    }
                }
            }
            ProgressManager.pop(progressBar);
        }

        if (commandSender != null) {
            commandSender.sendMessage(new TextComponentString("Actually Control Entities config reloaded successfully!"));
        }

        General.debugToConsole(Level.INFO, "Loaded %d mob configs in %d ms", counter, System.currentTimeMillis() - startTime);
        configReloading = false;
    }

    // Credits to Tslat
    private static boolean cacheMostCommonSpawnEntry(LivingData livingData) {
        if (livingData == null) {
            return false;
        }

        HashMap<Biome, ACESpawnEntry> defaultSpawnEntryMap = livingData.getDefaultSpawnEntryMap();

        if (defaultSpawnEntryMap == null) {
            return false;
        }

        Collection<ACESpawnEntry> defaultSpawnEntries = defaultSpawnEntryMap.values();

        ACESpawnEntry bestResult = null;

        int bestResultTally = 0;
        int currentResultTally;

        for (ACESpawnEntry spawnEntry : defaultSpawnEntries) {
            currentResultTally = 0;

            for (ACESpawnEntry compareEntry : defaultSpawnEntries) {
                if (!spawnEntry.isIdentical(compareEntry)) {
                    continue;
                }

                currentResultTally++;
            }

            if (currentResultTally <= bestResultTally) {
                continue;
            }

            bestResultTally = currentResultTally;
            bestResult = spawnEntry;
        }

        if (bestResult == null) {
            return false;
        }

        livingData.setMostCommonDefaultSpawnEntry(bestResult);
        bestResult.setMostCommon(true);
        return true;
    }
}
