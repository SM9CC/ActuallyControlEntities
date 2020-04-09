package com.sm9.actuallycontrolentities.common.config;

import net.minecraft.entity.EnumCreatureType;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Arrays;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "SpellCheckingInspection", "unused"})
public class SpawnerConfig {
    private final Configuration spawnerConfig;

    private boolean monsterEnabled;
    private boolean creatureEnabled;
    private boolean ambientEnabled;
    private boolean waterCreatureEnabled;

    private int monsterMaxCount;
    private int creatureMaxCount;
    private int ambientMaxCount;
    private int waterCreatureMaxCount;

    private int monsterMaxChunkCount;
    private int creatureMaxChunkCount;
    private int ambientMaxChunkCount;
    private int waterCreatureMaxChunkCount;

    private boolean monsterLosChecking;
    private boolean creatureLosChecking;
    private boolean ambientLosChecking;
    private boolean waterCreatureLosChecking;

    private boolean monsterJoinWorldEvent;
    private boolean creatureJoinWorldEvent;
    private boolean ambientJoinWorldEvent;
    private boolean waterCreatureJoinWorldEvent;

    private float monsterSpawnRange;
    private float creatureSpawnRange;
    private float ambientSpawnRange;
    private float waterCreatureSpawnRange;

    private int minDespawnLightLevel;
    private int maxDespawnLightLevel;
    private boolean forceDespawns;

    public SpawnerConfig(File spawnerFile) {
        spawnerConfig = new Configuration(spawnerFile);
    }

    public final void reloadFromConfig() {
        spawnerConfig.load();

        forceDespawns = spawnerConfig.getBoolean("ForceDespawns", " General", true,
                "If true, ACE will attempt to despawn all creatures including vanilla. It will attempt to prevent despawning of villagers, tamed creatures, and farm animals. " +
                        "The purpose of this setting is to provide a more dynamic experience.");

        minDespawnLightLevel = spawnerConfig.getInt("MinDespawnLightLevel", " General", 2, 0, 15,
                "The minimum light level threshold used to determine whether or not to despawn a farm animal.");

        maxDespawnLightLevel = spawnerConfig.getInt("MaxDespawnLightLevel", " General", 7, 0, 15,
                "The maximum light level threshold used to determine whether or not to despawn a farm animal.");

        monsterEnabled = spawnerConfig.getBoolean("Enabled", "Monster", true, "Whether this mob type is allowed to spawn.");
        creatureEnabled = spawnerConfig.getBoolean("Enabled", "Creature", true, "Whether this mob type is allowed to spawn.");
        ambientEnabled = spawnerConfig.getBoolean("Enabled", "Ambient", true, "Whether this mob type is allowed to spawn.");
        waterCreatureEnabled = spawnerConfig.getBoolean("Enabled", "WaterCreature", true, "Whether this mob type is allowed to spawn.");

        monsterMaxCount = spawnerConfig.getInt("MaxCount", "Monster", EnumCreatureType.MONSTER.getMaxNumberOfCreature(), -1, Integer.MAX_VALUE, "Global world limit for this mob type, -1 = No limit.");
        creatureMaxCount = spawnerConfig.getInt("MaxCount", "Creature", EnumCreatureType.CREATURE.getMaxNumberOfCreature(), -1, Integer.MAX_VALUE, "Global world limit for this mob type, -1 = No limit.");
        ambientMaxCount = spawnerConfig.getInt("MaxCount", "Ambient", EnumCreatureType.AMBIENT.getMaxNumberOfCreature(), -1, Integer.MAX_VALUE, "Global world limit for this mob type, -1 = No limit.");
        waterCreatureMaxCount = spawnerConfig.getInt("MaxCount", "WaterCreature", EnumCreatureType.WATER_CREATURE.getMaxNumberOfCreature(), -1, Integer.MAX_VALUE, "Global world limit for this mob type, -1 = No limit.");

        monsterMaxChunkCount = spawnerConfig.getInt("MaxChunkCount", "Monster", -1, -1, Integer.MAX_VALUE, "Chunk limit for this mob type, -1 = No limit.");
        creatureMaxChunkCount = spawnerConfig.getInt("MaxChunkCount", "Creature", -1, -1, Integer.MAX_VALUE, "Chunk limit for this mob type, -1 = No limit.");
        ambientMaxChunkCount = spawnerConfig.getInt("MaxChunkCount", "Ambient", -1, -1, Integer.MAX_VALUE, "Chunk limit for this mob type, -1 = No limit.");
        waterCreatureMaxChunkCount = spawnerConfig.getInt("MaxChunkCount", "WaterCreature", -1, -1, Integer.MAX_VALUE, "Chunk limit for this mob type, -1 = No limit.");

        monsterLosChecking = spawnerConfig.getBoolean("LOSCheck", "Monster", false, "Prevents this mob spawning in positions where it would be seen by a player or where it would see a player.");
        creatureLosChecking = spawnerConfig.getBoolean("LOSCheck", "Creature", false, "Prevents this mob spawning in positions where it would be seen by a player or where it would see a player.");
        ambientLosChecking = spawnerConfig.getBoolean("LOSCheck", "Ambient", false, "Prevents this mob spawning in positions where it would be seen by a player or where it would see a player.");
        waterCreatureLosChecking = spawnerConfig.getBoolean("LOSCheck", "WaterCreature", false, "Prevents this mob spawning in positions where it would be seen by a player or where it would see a player.");

        monsterJoinWorldEvent = spawnerConfig.getBoolean("JoinWorld", "Monster", false, "Makes checks more aggressive by checking JoinWorld event (WARNING: Affects MobSpawners, SpawnEggs, Natural structure spawns).");
        creatureJoinWorldEvent = spawnerConfig.getBoolean("JoinWorld", "Creature", false, "Makes checks more aggressive by checking JoinWorld event (WARNING: Affects MobSpawners, SpawnEggs, Natural structure spawns).");
        ambientJoinWorldEvent = spawnerConfig.getBoolean("JoinWorld", "Ambient", false, "Makes checks more aggressive by checking JoinWorld event (WARNING: Affects MobSpawners, SpawnEggs, Natural structure spawns).");
        waterCreatureJoinWorldEvent = spawnerConfig.getBoolean("JoinWorld", "WaterCreature", false, "Makes checks more aggressive by checking JoinWorld event (WARNING: Affects MobSpawners, SpawnEggs, Natural structure spawns).");

        monsterSpawnRange = spawnerConfig.getFloat("SpawnRange", "Monster", 24.0f, 1.0f, (float) Integer.MAX_VALUE, "How close to player this mob type can spawn.");
        creatureSpawnRange = spawnerConfig.getFloat("SpawnRange", "Creature", 24.0f, 1.0f, (float) Integer.MAX_VALUE, "How close to player this mob type can spawn.");
        ambientSpawnRange = spawnerConfig.getFloat("SpawnRange", "Ambient", 24.0f, 1.0f, (float) Integer.MAX_VALUE, "How close to player this mob type can spawn.");
        waterCreatureSpawnRange = spawnerConfig.getFloat("SpawnRange", "WaterCreature", 24.0f, 1.0f, (float) Integer.MAX_VALUE, "How close to player this mob type can spawn.");

        spawnerConfig.setCategoryPropertyOrder("Monster", Arrays.asList("Enabled", "MaxCount", "MaxChunkCount", "LOSCheck", "JoinWorld", "SpawnRange"));
        spawnerConfig.setCategoryPropertyOrder("Creature", Arrays.asList("Enabled", "MaxCount", "MaxChunkCount", "LOSCheck", "JoinWorld", "SpawnRange"));
        spawnerConfig.setCategoryPropertyOrder("Ambient", Arrays.asList("Enabled", "MaxCount", "MaxChunkCount", "LOSCheck", "JoinWorld", "SpawnRange"));
        spawnerConfig.setCategoryPropertyOrder("WaterCreature", Arrays.asList("Enabled", "MaxCount", "MaxChunkCount", "LOSCheck", "JoinWorld", "SpawnRange"));
        spawnerConfig.setCategoryPropertyOrder(" General", Arrays.asList("ForceDespawns", "MinDespawnLightLevel", "MaxDespawnLightLevel"));

        if (spawnerConfig.hasChanged()) {
            spawnerConfig.save();
        }
    }

    public final boolean getTypeEnabled(EnumCreatureType creatureType) {
        switch (creatureType) {
            case MONSTER: {
                return monsterEnabled;
            }
            case CREATURE: {
                return creatureEnabled;
            }
            case AMBIENT: {
                return ambientEnabled;
            }
            case WATER_CREATURE: {
                return waterCreatureEnabled;
            }
        }
        return false;
    }

    public final int getTypeMaxCount(EnumCreatureType creatureType) {
        switch (creatureType) {
            case MONSTER: {
                return monsterMaxCount;
            }
            case CREATURE: {
                return creatureMaxCount;
            }
            case AMBIENT: {
                return ambientMaxCount;
            }
            case WATER_CREATURE: {
                return waterCreatureMaxCount;
            }
        }
        return 0;
    }

    public final int getTypeMaxChunkCount(EnumCreatureType creatureType) {
        switch (creatureType) {
            case MONSTER: {
                return monsterMaxChunkCount;
            }
            case CREATURE: {
                return creatureMaxChunkCount;
            }
            case AMBIENT: {
                return ambientMaxChunkCount;
            }
            case WATER_CREATURE: {
                return waterCreatureMaxChunkCount;
            }
        }
        return 0;
    }

    public final boolean getTypeLosCheckEnabled(EnumCreatureType creatureType) {
        switch (creatureType) {
            case MONSTER: {
                return monsterLosChecking;
            }
            case CREATURE: {
                return creatureLosChecking;
            }
            case AMBIENT: {
                return ambientLosChecking;
            }
            case WATER_CREATURE: {
                return waterCreatureLosChecking;
            }
        }
        return false;
    }

    public final boolean getTypeJoinWorldEnabled(EnumCreatureType creatureType) {
        switch (creatureType) {
            case MONSTER: {
                return monsterJoinWorldEvent;
            }
            case CREATURE: {
                return creatureJoinWorldEvent;
            }
            case AMBIENT: {
                return ambientJoinWorldEvent;
            }
            case WATER_CREATURE: {
                return waterCreatureJoinWorldEvent;
            }
        }
        return false;
    }

    final float getTypeSpawnRange(EnumCreatureType creatureType) {
        if (creatureType == null) {
            return 24.0f;
        }

        switch (creatureType) {
            case MONSTER: {
                return monsterSpawnRange;
            }
            case CREATURE: {
                return creatureSpawnRange;
            }
            case AMBIENT: {
                return ambientSpawnRange;
            }
            case WATER_CREATURE: {
                return waterCreatureSpawnRange;
            }
        }
        return 24.0f;
    }

    public final boolean shouldForceDespawns() {
        return forceDespawns;
    }

    public final int getMinDespawnLightLevel() {
        return minDespawnLightLevel;
    }

    public final int getMaxDespawnLightLevel() {
        return maxDespawnLightLevel;
    }
}