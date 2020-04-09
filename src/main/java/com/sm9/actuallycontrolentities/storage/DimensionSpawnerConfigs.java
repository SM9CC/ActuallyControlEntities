package com.sm9.actuallycontrolentities.storage;

import com.sm9.actuallycontrolentities.common.config.SpawnerConfig;

import java.util.HashMap;

public class DimensionSpawnerConfigs {
    private static final HashMap<Integer, SpawnerConfig> dimensionSpawnerConfigs = new HashMap<>();

    public static void put(Integer dimensionId, SpawnerConfig spawnerConfig) {
        dimensionSpawnerConfigs.putIfAbsent(dimensionId, spawnerConfig);
    }

    public static SpawnerConfig get(Integer dimensionId) {
        return dimensionSpawnerConfigs.get(dimensionId);
    }
}