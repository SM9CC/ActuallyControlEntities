package com.sm9.actuallycontrolentities.common.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.Arrays;

import static com.sm9.actuallycontrolentities.ActuallyControlEntities.rootConfigDir;

@SuppressWarnings("ALL")
public class MainConfig {
    private static boolean forceDespawns;
    private final Configuration mainConfig;
    private boolean debugEvents;
    private boolean allowMobSpawning;
    private boolean allowSpectatorSpawning;
    private long spawnTickDelay;

    public MainConfig() {
        mainConfig = new Configuration(new File(rootConfigDir, "main.cfg"));
        reloadFromConfig();
    }

    public void reloadFromConfig() {
        mainConfig.load();
        debugEvents = mainConfig.getBoolean("DebugEvents", Configuration.CATEGORY_GENERAL, false, "Extremely spammy, debugs whenever various events such as mob denial happens.");
        allowMobSpawning = mainConfig.getBoolean("DoMobSpawning", Configuration.CATEGORY_GENERAL, true, "A global toggle, set to false to completely disable mob spawns.");
        allowSpectatorSpawning = mainConfig.getBoolean("DoSpectatorSpawning", Configuration.CATEGORY_GENERAL, true, "Allow mob spawns for spectator mode.");
        spawnTickDelay = (long) mainConfig.getInt("SpawnTickDelay", Configuration.CATEGORY_GENERAL, 400, 10, Integer.MAX_VALUE, "Tick delay between spawn attempts.");

        mainConfig.setCategoryPropertyOrder(Configuration.CATEGORY_GENERAL, Arrays.asList("DebugEvents", "DoMobSpawning", "DoSpectatorSpawning", "SpawnTickDelay"));

        if (mainConfig.hasChanged()) {
            mainConfig.save();
        }
    }

    public boolean getDebugEvents() {
        return debugEvents;
    }

    public boolean getAllowMobSpawning() {
        return allowMobSpawning;
    }

    public boolean getAllowSpectatorSpawning() {
        return allowSpectatorSpawning;
    }

    public long getSpawnTickDelay() {
        return spawnTickDelay;
    }
}
