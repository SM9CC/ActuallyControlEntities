package com.sm9.actuallycontrolentities.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sm9.actuallycontrolentities.common.config.MobConfig;
import com.sm9.actuallycontrolentities.entity.LivingData;
import com.sm9.actuallycontrolentities.spawner.ACESpawnEntry;
import com.sm9.actuallycontrolentities.storage.*;
import com.sm9.actuallycontrolentities.world.DimensionData;
import com.sm9.actuallycontrolentities.world.WorldEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.WeightedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Level;

import java.util.*;

import static com.sm9.actuallycontrolentities.ActuallyControlEntities.mainConfig;
import static net.minecraft.world.WorldEntitySpawner.isValidEmptySpawnBlock;

public class WorldUtils {
    private static String getStructureAtPosition(WorldServer worldIn, BlockPos pos) {
        String structureFound = null;

        if (worldIn.getChunkProvider().isInsideStructure(worldIn, "Stronghold", pos)) {
            structureFound = "Stronghold";
        } else if (worldIn.getChunkProvider().isInsideStructure(worldIn, "Mansion", pos)) {
            structureFound = "Mansion";
        } else if (worldIn.getChunkProvider().isInsideStructure(worldIn, "Monument", pos)) {
            structureFound = "Monument";
        } else if (worldIn.getChunkProvider().isInsideStructure(worldIn, "Village", pos)) {
            structureFound = "Village";
        } else if (worldIn.getChunkProvider().isInsideStructure(worldIn, "Mineshaft", pos)) {
            structureFound = "Mineshaft";
        } else if (worldIn.getChunkProvider().isInsideStructure(worldIn, "Temple", pos)) {
            structureFound = "Temple";
        } else if (worldIn.getChunkProvider().isInsideStructure(worldIn, "Fortress", pos)) {
            structureFound = "Fortress";
        } else if (worldIn.getChunkProvider().isInsideStructure(worldIn, "EndCity", pos)) {
            structureFound = "EndCity";
        }

        return structureFound;
    }

    public static WeatherCondition getWeatherConditionFromString(String weatherCondition) {
        switch (weatherCondition) {
            case "ANY": {
                return WeatherCondition.ANY;
            }
            case "CLEAR": {
                return WeatherCondition.CLEAR;
            }
            case "THUNDER": {
                return WeatherCondition.THUNDERSTORM;
            }
            case "RAIN": {
                return WeatherCondition.RAIN;
            }
            case "SNOW": {
                return WeatherCondition.SNOW;
            }
            case "DOWNFALL": {
                return WeatherCondition.GENERAL_DOWNFALL;
            }
        }

        return WeatherCondition.ANY;
    }

    static String getWeatherConditionAsString(WeatherCondition weatherCondition) {
        if (weatherCondition == WeatherCondition.ANY) {
            return "ANY";
        } else if (weatherCondition == WeatherCondition.CLEAR) {
            return "CLEAR";
        } else if (weatherCondition == WeatherCondition.THUNDERSTORM) {
            return "THUNDER";
        } else if (weatherCondition == WeatherCondition.RAIN) {
            return "RAIN";
        } else if (weatherCondition == WeatherCondition.SNOW) {
            return "SNOW";
        } else if (weatherCondition == WeatherCondition.GENERAL_DOWNFALL) {
            return "DOWNFALL";
        }

        return "ANY";
    }

    public static WeatherCondition getWeatherConditionAtEntitiesPosition(EntityLiving livingEntity) {
        World worldAny = livingEntity.getEntityWorld();

        if (!(worldAny instanceof WorldServer)) {
            return WeatherCondition.ANY;
        }

        WorldServer worldServer = (WorldServer) worldAny;
        MobConfig mobConfig = LivingDataEntries.get(livingEntity).getDimensionData(worldServer.provider.getDimension()).getMobConfig();

        return getWeatherConditionAtPosition(livingEntity.getPosition(), worldServer, mobConfig.getWeatherCondition());
    }

    static WeatherCondition getWeatherConditionAtPosition(BlockPos blockPos, WorldServer worldServer, WeatherCondition weatherCondition) {
        if (isDownfallAtPosition(worldServer, blockPos, weatherCondition)) {
            return worldServer.getBiome(blockPos).isSnowyBiome() ? WeatherCondition.SNOW : WeatherCondition.RAIN;
        } else if (worldServer.isThundering()) {
            return WeatherCondition.THUNDERSTORM;
        } else if (worldServer.isRaining()) {
            return WeatherCondition.GENERAL_DOWNFALL;
        }

        return WeatherCondition.ANY;
    }

    private static boolean isDownfallAtPosition(WorldServer worldServer, BlockPos position, WeatherCondition weatherCondition) {
        if (!worldServer.isRaining()) {
            return false;
        }

        if (!worldServer.canSeeSky(position)) {
            return false;
        }

        if (worldServer.getPrecipitationHeight(position).getY() > position.getY()) {
            return false;
        }

        Biome biome = worldServer.getBiome(position);

        if (weatherCondition == WeatherCondition.RAIN) {
            return biome.canRain();
        }

        if (weatherCondition == WeatherCondition.SNOW) {
            return biome.getEnableSnow() && worldServer.canSnowAt(position, false);
        }

        return true;
    }

    private static HashSet<BlockPos> getShuffledPositionsForPlayer(EntityPlayer player) {
        if (player.isSpectator() && !mainConfig.getAllowSpectatorSpawning()) {
            return null;
        }

        World worldAny = player.getEntityWorld();

        if (!(worldAny instanceof WorldServer)) {
            return null;
        }

        WorldServer worldServer = (WorldServer) worldAny;

        return getShuffledPositionsForChunk(worldServer, player.posX, player.posZ);
    }

    public static HashSet<BlockPos> getShuffledPositionsForChunk(WorldServer worldServer, double posX, double posZ) {
        boolean flag;

        ChunkPos chunkPos;
        PlayerChunkMapEntry chunkMapEntry;

        BlockPos worldSpawnPos = worldServer.getSpawnPoint();
        Set<ChunkPos> eligibleChunksForSpawning = Sets.newHashSet();

        int j = MathHelper.floor(posX / 16.0D);
        int k = MathHelper.floor(posZ / 16.0D);

        for (int i1 = -8; i1 <= 8; ++i1) {
            for (int j1 = -8; j1 <= 8; ++j1) {
                flag = i1 == -8 || i1 == 8 || j1 == -8 || j1 == 8;
                chunkPos = new ChunkPos(i1 + j, j1 + k);

                if (eligibleChunksForSpawning.contains(chunkPos)) {
                    continue;
                }

                if (flag || !worldServer.getWorldBorder().contains(chunkPos)) {
                    continue;
                }

                chunkMapEntry = worldServer.getPlayerChunkMap().getEntry(chunkPos.x, chunkPos.z);

                if (chunkMapEntry == null || !chunkMapEntry.isSentToPlayers()) {
                    continue;
                }

                eligibleChunksForSpawning.add(chunkPos);
            }
        }

        List<ChunkPos> shuffledPositions = Lists.newArrayList(eligibleChunksForSpawning);
        Collections.shuffle(shuffledPositions);

        WorldBorder worldBorder = worldServer.getWorldBorder();
        HashSet<BlockPos> validPositions = new HashSet<>();

        int originX, originY, originZ, x, y, z, f;

        BlockPos tempPos, finalPos;

        IBlockState blockState;

        for (ChunkPos shuffledPos : shuffledPositions) {
            tempPos = getRandomPosition(worldServer, shuffledPos.x, shuffledPos.z);
            blockState = worldServer.getBlockState(tempPos);

            if (blockState.isNormalCube()) {
                continue;
            }

            originX = tempPos.getX();
            originY = tempPos.getY();
            originZ = tempPos.getZ();

            for (int l = 0; l < 3; ++l) {
                x = originX;
                y = originY;
                z = originZ;

                f = MathHelper.ceil(Math.random() * 4.0D);

                for (int m = 0; m < f; ++m) {
                    x += worldServer.rand.nextInt(6) - worldServer.rand.nextInt(6) + 0.5f;
                    y += worldServer.rand.nextInt(1) - worldServer.rand.nextInt(1);
                    z += worldServer.rand.nextInt(6) - worldServer.rand.nextInt(6) + 0.5f;
                    finalPos = new BlockPos(x, y, z);

                    if (validPositions.contains(finalPos) || !worldBorder.contains(finalPos) || worldSpawnPos.distanceSq(x, y, z) < 576.0D) {
                        continue;
                    }

                    validPositions.add(finalPos);
                }
            }
        }

        return validPositions;
    }

    public static HashSet<BlockPos> getShuffledPositionsForAllPlayers(WorldServer worldServer) {
        HashSet<BlockPos> shuffledPlayerPositions;
        HashSet<BlockPos> allShuffledPlayerPositions = new HashSet<>();

        for (EntityPlayer playerEntity : worldServer.playerEntities) {
            shuffledPlayerPositions = getShuffledPositionsForPlayer(playerEntity);

            if (shuffledPlayerPositions == null || shuffledPlayerPositions.isEmpty()) {
                continue;
            }

            allShuffledPlayerPositions.addAll(shuffledPlayerPositions);
        }

        return allShuffledPlayerPositions;
    }

    private static BlockPos getRandomPosition(WorldServer worldServer, int x, int z) {
        Chunk chunk = worldServer.getChunk(x, z);
        int i = x * 16 + worldServer.rand.nextInt(16);
        int j = z * 16 + worldServer.rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.getHeight(new BlockPos(i, 0, j)) + 1, 16);
        int l = worldServer.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        return new BlockPos(i, l, j);
    }

    public static ACESpawnEntry getSpawnEntryForPos(WorldServer worldServer, EnumCreatureType creatureType, BlockPos pos, boolean chunkGenSpawner) {
        List<ACESpawnEntry> potentialSpawns = processPotentialSpawns(getSpawnListForPos(creatureType, worldServer, pos, chunkGenSpawner));
        return potentialSpawns == null || potentialSpawns.isEmpty() ? null : WeightedRandom.getRandomItem(worldServer.rand, potentialSpawns);
    }

    private static List<ACESpawnEntry> processPotentialSpawns(List<ACESpawnEntry> oldList) {
        WorldEvent.PotentialSpawns event = new WorldEvent.PotentialSpawns(oldList);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return null;
        }

        return event.getList();
    }

    public static List<ACESpawnEntry> getSpawnListForPos(EnumCreatureType creatureType, WorldServer worldServer, BlockPos pos, boolean chunkGenSpawner) {
        Biome biomeAtPos;

        try {
            biomeAtPos = worldServer.getBiome(pos);
        } catch (NullPointerException ignored) {
            return null;
        }

        EntityLiving.SpawnPlacementType positionPlacementType = getSpawnPlacementTypeForPos(worldServer, pos);

        if (positionPlacementType == null) {
            return null;
        }

        int dimension = worldServer.provider.getDimension();

        String structureAtPosition = WorldUtils.getStructureAtPosition(worldServer, pos);

        ArrayList<DimensionData> dimensionDataList;

        switch (creatureType) {
            case MONSTER: {
                dimensionDataList = MonsterDimensionDataMaps.getPlacementData(dimension, positionPlacementType);
                break;
            }

            case CREATURE: {
                dimensionDataList = CreatureDimensionDataMaps.getPlacementData(dimension, positionPlacementType);
                break;
            }

            case AMBIENT: {
                dimensionDataList = AmbientDimensionDataMaps.getPlacementData(dimension, positionPlacementType);
                break;
            }

            case WATER_CREATURE: {
                dimensionDataList = WaterCreatureDimensionDataMaps.getPlacementData(dimension, positionPlacementType);
                break;
            }

            default: {
                dimensionDataList = null;
                break;
            }
        }

        List<ACESpawnEntry> applicableSpawnEntries = new ArrayList<>();
        List<Biome.SpawnListEntry> vanillaPlacementList = worldServer.getChunkProvider().getPossibleCreatures(creatureType, pos);

        LivingData livingData;
        ACESpawnEntry spawnEntry;

        EntityLiving.SpawnPlacementType entityPlacementType;

        for (Biome.SpawnListEntry vanillaSpawn : vanillaPlacementList) {
            entityPlacementType = EntitySpawnPlacementRegistry.getPlacementForEntity(vanillaSpawn.entityClass);

            if (!positionPlacementType.equals(entityPlacementType)) {
                continue;
            }

            livingData = LivingDataEntries.get(vanillaSpawn.entityClass);

            spawnEntry = new ACESpawnEntry(vanillaSpawn.entityClass, vanillaSpawn.minGroupCount, vanillaSpawn.maxGroupCount, vanillaSpawn.itemWeight, creatureType,
                    entityPlacementType, livingData, null);

            EntityUtils.CheckSpawnReturn checkSpawnReturn = EntityUtils.Tests.checkSpawn(null, spawnEntry.getmobResourceName(),
                    worldServer, pos, false, true);

            if (checkSpawnReturn.result != EntityUtils.CheckSpawnResult.ALLOWED) {
                if (mainConfig.getDebugEvents()) {
                    String creatureTypeName = checkSpawnReturn.creatureType;

                    General.debugToConsole(Level.WARN, "[Vanilla%s PotentialSpawn][%s] - Skipping entity: [%s] %s at position: X: %d, Y: %d, Z: %d) Biome: %s (%s)",
                            chunkGenSpawner ? " ChunkGen" : "", checkSpawnReturn.dimName, creatureTypeName == null ? "Null" : creatureTypeName,
                            spawnEntry.getmobResourceName(), checkSpawnReturn.posX, checkSpawnReturn.posY, checkSpawnReturn.posZ, checkSpawnReturn.biomeName, checkSpawnReturn.info);
                }

                continue;
            }

            applicableSpawnEntries.add(spawnEntry);
        }

        if (dimensionDataList != null) {
            for (DimensionData dimensionData : dimensionDataList) {
                spawnEntry = dimensionData.getSpawnEntry(biomeAtPos);

                if (customSpawnEntryMeetsRequirements(spawnEntry, dimension, biomeAtPos, worldServer, pos, false, chunkGenSpawner)) {
                    applicableSpawnEntries.add(spawnEntry);
                }

                if (structureAtPosition == null) {
                    continue;
                }

                spawnEntry = dimensionData.getStructureSpawnEntry(structureAtPosition);

                if (customSpawnEntryMeetsRequirements(spawnEntry, dimension, biomeAtPos, worldServer, pos, true, chunkGenSpawner)) {
                    applicableSpawnEntries.add(spawnEntry);
                }
            }
        }

        return applicableSpawnEntries;
    }

    private static boolean customSpawnEntryMeetsRequirements(ACESpawnEntry spawnEntry, int dimension, Biome biomeAtPos, WorldServer worldServer, BlockPos pos,
                                                             boolean structureSpawn, boolean chunkGenSpawner) {
        if (spawnEntry == null) {
            return false;
        }

        EntityUtils.CheckSpawnReturn checkSpawnReturn = EntityUtils.Tests.checkSpawn(null, spawnEntry.getmobResourceName(),
                worldServer, pos, false, true);

        if (checkSpawnReturn.result != EntityUtils.CheckSpawnResult.ALLOWED) {
            if (mainConfig.getDebugEvents()) {
                String creatureTypeName = checkSpawnReturn.creatureType;

                General.debugToConsole(Level.WARN, "[%s%s PotentialSpawn][%s] - Skipping entity: [%s] %s at position: X: %d, Y: %d, Z: %d) Biome: %s (%s)",
                        chunkGenSpawner ? " ChunkGen" : "", structureSpawn ? "Structure" : "World", checkSpawnReturn.dimName,
                        creatureTypeName == null ? "Null" : creatureTypeName, spawnEntry.getmobResourceName(), checkSpawnReturn.posX,
                        checkSpawnReturn.posY, checkSpawnReturn.posZ, checkSpawnReturn.biomeName, checkSpawnReturn.info);
            }

            return false;
        }

        return true;
    }

    private static EntityLiving.SpawnPlacementType getSpawnPlacementTypeForPos(WorldServer worldServer, BlockPos blockPos) {
        if (!worldServer.getWorldBorder().contains(blockPos)) {
            return null;
        }

        IBlockState blockState = worldServer.getBlockState(blockPos);
        Material material = blockState.getMaterial();

        BlockPos blockPosDown = blockPos.down();

        IBlockState blockStateDown = worldServer.getBlockState(blockPosDown);
        Material materialDown = blockStateDown.getMaterial();
        Block blockDown = blockStateDown.getBlock();

        IBlockState blockStateUp = worldServer.getBlockState(blockPos.up());
        boolean isNormalCube = blockStateUp.isNormalCube();

        if (materialDown.equals(Material.FIRE) || material.equals(Material.FIRE)) {
            return EntityUtils.IN_FIRE;
        } else if (material == Material.WATER && materialDown == Material.WATER && !isNormalCube) {
            return EntityLiving.SpawnPlacementType.IN_WATER;
        } else if (material == Material.LAVA && materialDown == Material.LAVA && !isNormalCube) {
            return EntityUtils.IN_LAVA;
        }

        if (!blockDown.canCreatureSpawn(blockStateDown, worldServer, blockPosDown, null)) {
            return null;
        }

        boolean flag = blockState != Blocks.BEDROCK && !blockDown.equals(Blocks.BARRIER);

        if (flag && isValidEmptySpawnBlock(blockState) && isValidEmptySpawnBlock(blockStateUp)) {
            return EntityLiving.SpawnPlacementType.ON_GROUND;
        }

        return null;
    }

    public List<ACESpawnEntry> getAllSpawnEntriesForPos(WorldServer worldServer, EnumCreatureType creatureType, BlockPos pos, boolean chunkGenSpawner) {
        return getSpawnListForPos(creatureType, worldServer, pos, chunkGenSpawner);
    }

    public enum WeatherCondition {
        ANY,
        CLEAR,
        GENERAL_DOWNFALL,
        THUNDERSTORM,
        RAIN,
        SNOW
    }
}
