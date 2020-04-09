package com.sm9.actuallycontrolentities.handler;

import com.sm9.actuallycontrolentities.common.config.MobConfig;
import com.sm9.actuallycontrolentities.common.config.SpawnerConfig;
import com.sm9.actuallycontrolentities.entity.LivingData;
import com.sm9.actuallycontrolentities.spawner.ACESpawnEntry;
import com.sm9.actuallycontrolentities.spawner.Capabilities;
import com.sm9.actuallycontrolentities.storage.DimensionSpawnerConfigs;
import com.sm9.actuallycontrolentities.storage.LivingDataEntries;
import com.sm9.actuallycontrolentities.util.DimensionUtils;
import com.sm9.actuallycontrolentities.util.EntityUtils;
import com.sm9.actuallycontrolentities.util.General;
import com.sm9.actuallycontrolentities.util.WorldUtils;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.Level;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import static com.sm9.actuallycontrolentities.ActuallyControlEntities.mainConfig;

@SuppressWarnings("ALL")
public class WorldSpawner {
    private WorldServer worldServer;

    private WorldSpawner(WorldServer worldServer) {
        this.worldServer = worldServer;
    }

    public static WorldSpawner get(WorldServer worldServer) {
        return ObjectUtils.defaultIfNull(Capabilities.getCapability(worldServer), new WorldSpawner(worldServer));
    }

    void performRandomSpawning(int dimensionId, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate, HashSet<BlockPos> shuffledPositions,
                               boolean chunkGenSpawner) {
        if (!spawnHostileMobs && !spawnPeacefulMobs) {
            return;
        }

        if (shuffledPositions == null || shuffledPositions.isEmpty()) {
            return;
        }

        creatureTypeLoop:
        for (EnumCreatureType creatureType : EnumCreatureType.values()) {
            if (!DimensionUtils.canSpawnCreature(spawnHostileMobs, spawnPeacefulMobs, spawnOnSetTickRate, DimensionSpawnerConfigs.get(dimensionId), creatureType)) {
                continue;
            }

            performRandomSpawningForCreatureType(dimensionId, shuffledPositions, creatureType, chunkGenSpawner);
        }
    }

    int performRandomSpawningForCreatureType(int dimensionId, HashSet<BlockPos> shuffledPositions, EnumCreatureType creatureType, boolean chunkGenSpawner) {
        SpawnerConfig spawnerConfig = DimensionSpawnerConfigs.get(dimensionId);

        if (spawnerConfig == null) {
            return 0;
        }

        String creatureTypeName = EntityUtils.getCreatureTypeAsString(creatureType);
        boolean losChecking = spawnerConfig.getTypeLosCheckEnabled(creatureType);
        int creatureWorldMax = EntityUtils.Tests.getWorldAllowedCountForCreatureType(worldServer, creatureType, false);

        if (creatureWorldMax < 1) {
            return 0;
        }

        ACESpawnEntry spawnEntry;
        String mobResourceName;
        MobConfig mobConfig;
        EntityLiving previousEntity;

        int creatureChunkMax, minGroupCount, maxGroupCount, mobWorldMax, mobChunkMax, randomGroupAmt;
        int[] capCounts;

        String dimensionName = DimensionUtils.getDimensionNameFromId(dimensionId);

        int spawnedCount = 0;

        blockPosLoop:
        for (BlockPos validPos : shuffledPositions) {
            creatureChunkMax = EntityUtils.Tests.getChunkAllowedCountForCreatureType(worldServer, validPos, creatureType, false);

            if (creatureChunkMax < 1) {
                continue;
            }

            spawnEntry = WorldUtils.getSpawnEntryForPos(worldServer, creatureType, validPos, chunkGenSpawner);

            if (spawnEntry == null) {
                continue;
            }

            mobResourceName = spawnEntry.getmobResourceName();
            mobConfig = spawnEntry.getMobConfig(dimensionId);

            minGroupCount = spawnEntry.getMinGroupCount();
            maxGroupCount = spawnEntry.getMaxGroupCount();
            mobWorldMax = EntityUtils.Tests.getWorldAllowedCountForEntity(worldServer, mobResourceName, false);
            mobChunkMax = EntityUtils.Tests.getChunkAllowedCountForEntity(worldServer, validPos, mobResourceName, false);
            randomGroupAmt = worldServer.rand.nextInt((maxGroupCount - minGroupCount) + 1) + minGroupCount;

            capCounts = new int[]{creatureWorldMax, creatureChunkMax, mobWorldMax, mobChunkMax};
            Arrays.sort(capCounts);

            if (!mobConfig.getGroupOverflowAllowed() && capCounts[0] < randomGroupAmt) {
                randomGroupAmt = capCounts[0]; // Ensure group spawn amount does not exceed other limits.
            }

            previousEntity = null;

            if (randomGroupAmt < 1) {
                continue;
            }

            for (int x = 0; x < randomGroupAmt; x++) {
                previousEntity = handleSpawn(spawnEntry, mobConfig, previousEntity != null ? previousEntity.getPosition() : validPos,
                        previousEntity != null, losChecking);

                if (previousEntity == null) {
                    break;
                }

                if (mainConfig.getDebugEvents()) {
                    General.debugToConsole(Level.INFO, "[%s][%s] - Successfully spawned entity: [%s] %s (%d/%d) at position: X: %d, Y: %d, Z: %d",
                            chunkGenSpawner ? "ChunkGenSpawner" : "WorldSpawner", dimensionName, creatureTypeName,
                            mobResourceName, x + 1, randomGroupAmt, (int) previousEntity.posX, (int) previousEntity.posY, (int) previousEntity.posZ);

                    previousEntity.getEntityData().setBoolean("ResultAllowed", true);
                    previousEntity.setDropItemsWhenDead(true);
                }

                spawnedCount++;
            }
        }

        return spawnedCount;
    }

    private EntityLiving handleSpawn(ACESpawnEntry spawnEntry, MobConfig mobConfig, BlockPos blockPos, Boolean validLastSpawn, Boolean checkLos) {
        EntityLiving newEntity;

        try {
            newEntity = spawnEntry.newInstance(worldServer);
            newEntity.getEntityData().setBoolean("ACESpawn", true);
            newEntity.setDropItemsWhenDead(false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        newEntity.setLocationAndAngles(blockPos.getX(), blockPos.getY(), blockPos.getZ(), worldServer.rand.nextFloat() * 360.0F, 0.0F);

        boolean positionMeetsRequirements;

        LivingData livingData = LivingDataEntries.get(newEntity);

        if (validLastSpawn) {
            switch (new Random().nextInt(7)) {
                case 0: { // Variate X axis (Increase).
                    newEntity.setLocationAndAngles(blockPos.getX() + new Random().nextInt(2), blockPos.getY(),
                            blockPos.getZ(), worldServer.rand.nextFloat() * 360.0F, 0.0F);
                    break;
                }

                case 1: { // Variate X axis (Decrease).
                    newEntity.setLocationAndAngles(blockPos.getX() - new Random().nextInt(2), blockPos.getY(),
                            blockPos.getZ(), worldServer.rand.nextFloat() * 360.0F, 0.0F);
                    break;
                }

                case 2: { // Variate Z axis (Increase).
                    newEntity.setLocationAndAngles(blockPos.getX(), blockPos.getY(),
                            blockPos.getZ() + new Random().nextInt(2), worldServer.rand.nextFloat() * 360.0F, 0.0F);
                    break;
                }

                case 3: { // Variate Z axis (Decrease).
                    newEntity.setLocationAndAngles(blockPos.getX(), blockPos.getY(),
                            blockPos.getZ() - new Random().nextInt(2), worldServer.rand.nextFloat() * 360.0F, 0.0F);
                    break;
                }

                case 4: { // Variate both axis (Increase).
                    newEntity.setLocationAndAngles(blockPos.getX() + new Random().nextInt(2), blockPos.getY(),
                            blockPos.getZ() + new Random().nextInt(2), worldServer.rand.nextFloat() * 360.0F, 0.0F);
                    break;
                }

                case 5: { // Variate both axis (Decrease).
                    newEntity.setLocationAndAngles(blockPos.getX() - new Random().nextInt(2), blockPos.getY(),
                            blockPos.getZ() - new Random().nextInt(2), worldServer.rand.nextFloat() * 360.0F, 0.0F);
                    break;
                }

                case 6: { // Variate increase axis X, decrease axis Z.
                    newEntity.setLocationAndAngles(blockPos.getX() + new Random().nextInt(2), blockPos.getY() - new Random().nextInt(2),
                            blockPos.getZ() - new Random().nextInt(2), worldServer.rand.nextFloat() * 360.0F, 0.0F);
                    break;
                }

                case 7: { // Variate decrease axis X, increase axis Z.
                    newEntity.setLocationAndAngles(blockPos.getX() - new Random().nextInt(2), blockPos.getY() - new Random().nextInt(2),
                            blockPos.getZ() + new Random().nextInt(2), worldServer.rand.nextFloat() * 360.0F, 0.0F);
                    break;
                }
            }
        }

        if (!EntityUtils.attemptPositionFix(worldServer, newEntity, blockPos) && !validLastSpawn) {
            newEntity.setDead();
            return null;
        }

        blockPos = newEntity.getPosition();

        if (!ForgeEventFactory.doSpecialSpawn(newEntity, worldServer, blockPos.getX(), blockPos.getY(), blockPos.getZ(), null)) {
            newEntity.onInitialSpawn(worldServer.getDifficultyForLocation(blockPos), null);
        }

        if (!mobConfig.getIgnoreVanillaChecks() && !newEntity.getCanSpawnHere()) {
            newEntity.setDead();
            return null;
        }

        if (checkLos) {
            for (EntityPlayer player : worldServer.playerEntities) {
                if (player.canEntityBeSeen(newEntity) || newEntity.canEntityBeSeen(player)) {
                    newEntity.setDead();
                    return null;
                }
            }
        }

        if (!worldServer.spawnEntity(newEntity)) {
            newEntity.setDead();
            return null;
        }

        return newEntity;
    }
}