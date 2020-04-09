package com.sm9.actuallycontrolentities.util;

import com.sm9.actuallycontrolentities.common.config.MobConfig;
import com.sm9.actuallycontrolentities.common.config.SpawnerConfig;
import com.sm9.actuallycontrolentities.compat.SereneSeasonsSupport;
import com.sm9.actuallycontrolentities.entity.LivingData;
import com.sm9.actuallycontrolentities.storage.DimensionSpawnerConfigs;
import com.sm9.actuallycontrolentities.storage.LivingDataEntries;
import net.minecraft.block.Block;
import net.minecraft.entity.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import sereneseasons.api.season.Season;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static com.sm9.actuallycontrolentities.ActuallyControlEntities.dummyBlock;
import static net.minecraft.entity.EntityLiving.SpawnPlacementType.*;

@SuppressWarnings("SpellCheckingInspection")
public class EntityUtils {
    public final static EntityLiving.SpawnPlacementType IN_LAVA = net.minecraftforge.common.util.EnumHelper.addSpawnPlacementType("IN_LAVA", (t, u) -> true);
    public final static EntityLiving.SpawnPlacementType IN_FIRE = net.minecraftforge.common.util.EnumHelper.addSpawnPlacementType("IN_FIRE", (t, u) -> true);

    public static String getEntityResourceNameFromClass(Class<? extends Entity> clazz) {
        ResourceLocation key = EntityList.getKey(clazz);
        return key == null ? null : key.toString();
    }

    public static String getCreatureTypeAsString(EnumCreatureType type) {
        switch (type) {
            case AMBIENT: {
                return "AMBIENT";
            }
            case WATER_CREATURE: {
                return "WATER_CREATURE";
            }
            case MONSTER: {
                return "MONSTER";
            }
            case CREATURE: {
                return "CREATURE";
            }
        }

        return null;
    }

    public static EnumCreatureType getCreatureTypeFromString(String createType) {
        switch (createType) {
            case "AMBIENT": {
                return EnumCreatureType.AMBIENT;
            }

            case "WATER_CREATURE":
            case "WATER": {
                return EnumCreatureType.WATER_CREATURE;
            }

            case "MONSTER": {
                return EnumCreatureType.MONSTER;
            }

            case "CREATURE": {
                return EnumCreatureType.CREATURE;
            }
        }

        return null;
    }

    public static String getSpawnPlacementTypeAsString(EntityLiving.SpawnPlacementType type) {
        switch (type) {
            case IN_WATER: {
                return "WATER";
            }
            case IN_AIR: {
                return "AIR";
            }
            case ON_GROUND: {
                return "GROUND";
            }
            default: {
                if (type.equals(IN_FIRE)) {
                    return "FIRE";
                } else if (type.equals(IN_LAVA)) {
                    return "LAVA";
                }
            }
        }

        return null;
    }

    public static EntityLiving.SpawnPlacementType getSpawnPlacementTypeFromString(String placementType) {
        if (placementType == null) {
            return null;
        }

        switch (placementType) {
            case "WATER": {
                return IN_WATER;
            }

            case "AIR": {
                return IN_AIR;
            }

            case "GROUND": {
                return ON_GROUND;
            }

            case "FIRE": {
                return IN_FIRE;
            }

            case "LAVA": {
                return IN_LAVA;
            }
        }

        return null;
    }

    private static void onEntityAllowed(EntityLivingBase livingEntity, MobConfig mobConfig) {
        Actions.applyPotionEffects(livingEntity, mobConfig.getPotionEffects());
    }

    public static boolean attemptPositionFix(WorldServer worldServer, EntityLiving livingEntity, BlockPos blockPos) {
        boolean entityColliding = !livingEntity.isNotColliding();

        if (!entityColliding) {
            return true;
        }

        livingEntity.getEntityData().setBoolean("CollisionImmune", true);

        EntityUtils.CheckSpawnReturn checkSpawnReturn = null;

        for (int i = 1; i < 6; i++) {
            // Attempt to fix both axis
            livingEntity.setLocationAndAngles(blockPos.getX() + i, blockPos.getY(), blockPos.getZ() + i, worldServer.rand.nextFloat() * 360.0F, 0.0F);
            entityColliding = !livingEntity.isNotColliding();
            checkSpawnReturn = EntityUtils.Tests.checkSpawn(livingEntity, LivingDataEntries.get(livingEntity).getMobResourceName(),
                    worldServer, new BlockPos(livingEntity.posX, livingEntity.posY, livingEntity.posZ), false, true);

            if (entityColliding || checkSpawnReturn.result != EntityUtils.CheckSpawnResult.ALLOWED) {
                livingEntity.setLocationAndAngles(blockPos.getX() - i, blockPos.getY(), blockPos.getZ() - i, worldServer.rand.nextFloat() * 360.0F, 0.0F);
                entityColliding = !livingEntity.isNotColliding();
                checkSpawnReturn = EntityUtils.Tests.checkSpawn(livingEntity, LivingDataEntries.get(livingEntity).getMobResourceName(),
                        worldServer, new BlockPos(livingEntity.posX, livingEntity.posY, livingEntity.posZ), false, true);
            }

            if (entityColliding || checkSpawnReturn.result != EntityUtils.CheckSpawnResult.ALLOWED) {
                livingEntity.setLocationAndAngles(blockPos.getX() + i, blockPos.getY(), blockPos.getZ() - i, worldServer.rand.nextFloat() * 360.0F, 0.0F);
                entityColliding = !livingEntity.isNotColliding();
                checkSpawnReturn = EntityUtils.Tests.checkSpawn(livingEntity, LivingDataEntries.get(livingEntity).getMobResourceName(),
                        worldServer, new BlockPos(livingEntity.posX, livingEntity.posY, livingEntity.posZ), false, true);
            }

            if (entityColliding || checkSpawnReturn.result != EntityUtils.CheckSpawnResult.ALLOWED) {
                livingEntity.setLocationAndAngles(blockPos.getX() - i, blockPos.getY(), blockPos.getZ() + i, worldServer.rand.nextFloat() * 360.0F, 0.0F);
                entityColliding = !livingEntity.isNotColliding();
                checkSpawnReturn = EntityUtils.Tests.checkSpawn(livingEntity, LivingDataEntries.get(livingEntity).getMobResourceName(),
                        worldServer, new BlockPos(livingEntity.posX, livingEntity.posY, livingEntity.posZ), false, true);
            }

            // Attempt to fix X axis
            if (entityColliding || checkSpawnReturn.result != EntityUtils.CheckSpawnResult.ALLOWED) {
                livingEntity.setLocationAndAngles(blockPos.getX() + i, blockPos.getY(), blockPos.getZ(), worldServer.rand.nextFloat() * 360.0F, 0.0F);
                entityColliding = !livingEntity.isNotColliding();
                checkSpawnReturn = EntityUtils.Tests.checkSpawn(livingEntity, LivingDataEntries.get(livingEntity).getMobResourceName(),
                        worldServer, new BlockPos(livingEntity.posX, livingEntity.posY, livingEntity.posZ), false, true);
            }

            if (entityColliding || checkSpawnReturn.result != EntityUtils.CheckSpawnResult.ALLOWED) {
                livingEntity.setLocationAndAngles(blockPos.getX() - i, blockPos.getY(), blockPos.getZ(), worldServer.rand.nextFloat() * 360.0F, 0.0F);
                entityColliding = !livingEntity.isNotColliding();
                checkSpawnReturn = EntityUtils.Tests.checkSpawn(livingEntity, LivingDataEntries.get(livingEntity).getMobResourceName(),
                        worldServer, new BlockPos(livingEntity.posX, livingEntity.posY, livingEntity.posZ), false, true);
            }

            // Attempt to fix Z axis
            if (entityColliding || checkSpawnReturn.result != EntityUtils.CheckSpawnResult.ALLOWED) {
                livingEntity.setLocationAndAngles(blockPos.getX(), blockPos.getY(), blockPos.getZ() + i, worldServer.rand.nextFloat() * 360.0F, 0.0F);
                entityColliding = !livingEntity.isNotColliding();
                checkSpawnReturn = EntityUtils.Tests.checkSpawn(livingEntity, LivingDataEntries.get(livingEntity).getMobResourceName(),
                        worldServer, new BlockPos(livingEntity.posX, livingEntity.posY, livingEntity.posZ), false, true);
            }

            if (entityColliding || checkSpawnReturn.result != EntityUtils.CheckSpawnResult.ALLOWED) {
                livingEntity.setLocationAndAngles(blockPos.getX(), blockPos.getY(), blockPos.getZ() - i, worldServer.rand.nextFloat() * 360.0F, 0.0F);
                entityColliding = !livingEntity.isNotColliding();
                checkSpawnReturn = EntityUtils.Tests.checkSpawn(livingEntity, LivingDataEntries.get(livingEntity).getMobResourceName(),
                        worldServer, new BlockPos(livingEntity.posX, livingEntity.posY, livingEntity.posZ), false, true);
            }

            if (!entityColliding && checkSpawnReturn.result == EntityUtils.CheckSpawnResult.ALLOWED) {
                break;
            }
        }

        livingEntity.getEntityData().setBoolean("CollisionImmune", false);

        return !entityColliding && checkSpawnReturn.result == CheckSpawnResult.ALLOWED;
    }

    public enum CheckSpawnResult {
        UNKNOWN, HANDLED, INVALID, DENIED, ALLOWED
    }

    @SuppressWarnings("SameParameterValue")
    public static class CheckSpawnReturn {
        public final int posX;
        public final int posY;
        public final int posZ;
        public final String dimName;
        public int addedRarity;
        public int rngValue;
        public String biomeName;
        public CheckSpawnResult result;
        public String info;
        public String mobResourceName;
        public String creatureType;
        WorldUtils.WeatherCondition weatherAtPosition;
        boolean canPositionSeeSky;
        int height;
        boolean heightTooLow;
        boolean heightTooHigh;

        CheckSpawnReturn(CheckSpawnResult result, String info, String mobResourceName, String creatureType, int posX, int posY, int posZ, String dimName) {
            this.result = result;
            this.info = info;
            this.mobResourceName = mobResourceName;
            this.creatureType = creatureType;
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
            this.dimName = dimName;
        }
    }

    static class Actions {
        static void applyPotionEffects(EntityLivingBase livingEntity, List<PotionEffect> potionEffects) {
            for (PotionEffect potionEffect : potionEffects) {
                livingEntity.addPotionEffect(potionEffect);
            }
        }
    }

    public static class Tests {
        static boolean creatureTypeReachedWorldMaxCount(WorldServer worldServer, String mobResourceName, boolean joinWorld) {
            MobConfig mobConfig = LivingDataEntries.get(mobResourceName).getDimensionData(worldServer.provider.getDimension()).getMobConfig();

            if (mobConfig == null) {
                return true;
            }

            return creatureTypeReachedWorldMaxCount(worldServer, mobConfig.getCreatureType(), joinWorld);
        }

        static boolean creatureTypeReachedWorldMaxCount(WorldServer worldServer, EnumCreatureType creatureType, boolean joinWorld) {
            return getWorldAllowedCountForCreatureType(worldServer, creatureType, joinWorld) < 1;
        }

        public static int getWorldAllowedCountForCreatureType(WorldServer worldServer, EnumCreatureType creatureType, boolean joinWorld) {
            SpawnerConfig spawnerConfig = DimensionSpawnerConfigs.get(worldServer.provider.getDimension());

            if (creatureType == null || spawnerConfig == null) {
                return 0;
            }

            if (joinWorld && !spawnerConfig.getTypeJoinWorldEnabled(creatureType)) {
                return Integer.MAX_VALUE;
            }

            int maxCount = spawnerConfig.getTypeMaxCount(creatureType);

            if (maxCount < 0 || maxCount == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }

            if (maxCount == 0) {
                return 0;
            }

            int creatureTypeCount = worldServer.countEntities(creatureType, true);

            if (creatureTypeCount >= maxCount) {
                return 0;
            }

            return maxCount - creatureTypeCount;
        }

        static boolean creatureTypeReachedChunkMaxCount(WorldServer worldServer, BlockPos blockPos, String mobResourceName, boolean joinWorld) {
            MobConfig mobConfig = LivingDataEntries.get(mobResourceName).getDimensionData(worldServer.provider.getDimension()).getMobConfig();

            if (mobConfig == null) {
                return true;
            }

            EnumCreatureType creatureType = mobConfig.getCreatureType();

            return creatureTypeReachedChunkMaxCount(worldServer, blockPos, creatureType, joinWorld);
        }

        static boolean creatureTypeReachedChunkMaxCount(WorldServer worldServer, BlockPos blockPos, EnumCreatureType creatureType, boolean joinWorld) {
            return getChunkAllowedCountForCreatureType(worldServer, blockPos, creatureType, joinWorld) < 1;
        }

        public static int getChunkAllowedCountForCreatureType(WorldServer worldServer, BlockPos blockPos, EnumCreatureType creatureType, boolean joinWorld) {
            int dimensionId = worldServer.provider.getDimension();
            SpawnerConfig spawnerConfig = DimensionSpawnerConfigs.get(dimensionId);

            if (creatureType == null || spawnerConfig == null) {
                return 0;
            }

            if (joinWorld && !spawnerConfig.getTypeJoinWorldEnabled(creatureType)) {
                return Integer.MAX_VALUE;
            }

            int maxChunkCount = spawnerConfig.getTypeMaxChunkCount(creatureType);

            if (maxChunkCount < 0 || maxChunkCount == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }

            if (maxChunkCount == 0) {
                return 0;
            }

            Chunk chunk = worldServer.getChunk(blockPos);
            ClassInheritanceMultiMap<Entity>[] entityInheirtanceMapList = chunk.getEntityLists();
            Iterable<EntityLiving> entityList;

            int chunkCount = 0;

            LivingData livingData;
            MobConfig mobConfig;

            for (ClassInheritanceMultiMap<Entity> entityInheritanceMap : entityInheirtanceMapList) {
                entityList = entityInheritanceMap.getByClass(EntityLiving.class);

                for (EntityLivingBase entityLiving : entityList) {
                    livingData = LivingDataEntries.get(entityLiving);

                    if (livingData == null) {
                        continue;
                    }

                    mobConfig = livingData.getDimensionData(dimensionId).getMobConfig();

                    if (mobConfig == null) {
                        continue;
                    }

                    if (!mobConfig.getCreatureType().equals(creatureType)) {
                        continue;
                    }

                    chunkCount++;
                }
            }

            if (chunkCount >= maxChunkCount) {
                return 0;
            }

            return maxChunkCount - chunkCount;
        }

        static boolean entityReachedWorldMaxCount(WorldServer worldServer, String mobResourceName, boolean joinWorld) {
            return getWorldAllowedCountForEntity(worldServer, mobResourceName, joinWorld) < 1;
        }

        public static int getWorldAllowedCountForEntity(WorldServer worldServer, String mobResourceName, boolean joinWorld) {
            return getWorldAllowedCountForEntity(worldServer, LivingDataEntries.get(mobResourceName), joinWorld);
        }

        static int getWorldAllowedCountForEntity(WorldServer worldServer, LivingData livingData, boolean joinWorld) {
            MobConfig mobConfig = livingData.getDimensionData(worldServer.provider.getDimension()).getMobConfig();

            if (mobConfig == null) {
                return 0;
            }

            int maxCount = mobConfig.getMaxCount(joinWorld);

            if (maxCount < 0 || maxCount == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }

            if (maxCount == 0) {
                return 0;
            }

            int entityTypeCount = worldServer.countEntities(mobConfig.cLazz);

            if (entityTypeCount >= maxCount) {
                return 0;
            }

            return maxCount - entityTypeCount;
        }

        static boolean entityReachedChunkMaxCount(WorldServer worldServer, BlockPos blockPos, String mobResourceName, boolean joinWorld) {
            return getChunkAllowedCountForEntity(worldServer, blockPos, mobResourceName, joinWorld) < 1;
        }

        public static int getChunkAllowedCountForEntity(WorldServer worldServer, BlockPos blockPos, String mobResourceName, boolean joinWorld) {
            MobConfig mobConfig = LivingDataEntries.get(mobResourceName).getDimensionData(worldServer.provider.getDimension()).getMobConfig();

            if (mobConfig == null) {
                return 0;
            }

            int maxChunkCount = mobConfig.getMaxChunkCount(joinWorld);

            if (maxChunkCount < 0 || maxChunkCount == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }

            if (maxChunkCount == 0) {
                return 0;
            }

            Chunk chunk = worldServer.getChunk(blockPos);
            ClassInheritanceMultiMap<Entity>[] entityInheirtanceMapList = chunk.getEntityLists();
            Iterable<EntityLiving> entityList;

            int chunkCount = 0;

            for (ClassInheritanceMultiMap<Entity> entityInheritanceMap : entityInheirtanceMapList) {
                entityList = entityInheritanceMap.getByClass(EntityLiving.class);

                for (EntityLivingBase entityLiving : entityList) {
                    if (!entityLiving.getClass().equals(mobConfig.cLazz)) {
                        continue;
                    }

                    chunkCount++;
                }
            }

            if (chunkCount >= maxChunkCount) {
                return 0;
            }

            return maxChunkCount - chunkCount;
        }

        static boolean blockAllowed(WorldServer worldServer, BlockPos blockPos, String mobResourceName, boolean joinWorld) {
            MobConfig mobConfig = LivingDataEntries.get(mobResourceName).getDimensionData(worldServer.provider.getDimension()).getMobConfig();

            if (mobConfig == null) {
                return true;
            }

            if (joinWorld && !mobConfig.getJoinWorldEnabled()) {
                return true;
            }

            Block blockAtPosition = worldServer.getBlockState(blockPos).getBlock();
            List<Block> blocksAllowed = mobConfig.getBlocksAllowed();
            List<Block> blocksExcluded = mobConfig.getBlocksExcluded();

            if (blocksAllowed == null || blocksAllowed.isEmpty()) {
                return false;
            }

            if (blocksAllowed.contains(dummyBlock)) {
                return true;
            }

            return blocksAllowed.contains(blockAtPosition) && (blocksExcluded == null || !blocksExcluded.contains(blockAtPosition));
        }

        static boolean seasonAllowed(WorldServer worldServer, String mobResourceName, boolean joinWorld, CheckSpawnReturn checkSpawnReturn) {
            MobConfig mobConfig = LivingDataEntries.get(mobResourceName).getDimensionData(worldServer.provider.getDimension()).getMobConfig();

            if (mobConfig == null) {
                return true;
            }

            if (joinWorld && !mobConfig.getJoinWorldEnabled()) {
                return true;
            }

            Season currentSeason = SereneSeasonsSupport.getCurrentSeason(worldServer);
            List<Season> seasonsAllowed = mobConfig.getSeasonsAllowed();
            List<Season> seasonsExcluded = mobConfig.getSeasonsExcluded();

            if (seasonsAllowed == null || seasonsAllowed.isEmpty()) {
                return seasonsExcluded == null || !seasonsExcluded.contains(currentSeason);
            }

            return seasonsAllowed.contains(currentSeason) && (seasonsExcluded == null || !seasonsExcluded.contains(currentSeason));
        }

        static boolean isValidLightLevel(WorldServer worldServer, BlockPos blockPos, String mobResourceName, boolean joinWorld) {
            MobConfig mobConfig = LivingDataEntries.get(mobResourceName).getDimensionData(worldServer.provider.getDimension()).getMobConfig();

            if (mobConfig == null) {
                return false;
            }

            if (joinWorld && !mobConfig.getJoinWorldEnabled()) {
                return true;
            }

            int blockLighting = worldServer.getLight(blockPos, true);
            return blockLighting >= mobConfig.getMinLight() && blockLighting <= mobConfig.getMaxLight();
        }

        static boolean meetsSkyCheck(WorldServer worldServer, BlockPos blockPos, String mobResourceName, boolean joinWorld, @Nullable CheckSpawnReturn checkSpawnReturn) {
            MobConfig mobConfig = LivingDataEntries.get(mobResourceName).getDimensionData(worldServer.provider.getDimension()).getMobConfig();

            boolean canSeeSky = worldServer.canSeeSky(blockPos);

            if (checkSpawnReturn != null) {
                checkSpawnReturn.canPositionSeeSky = canSeeSky;
            }

            if (joinWorld && !mobConfig.getJoinWorldEnabled()) {
                return true;
            }

            int skyCheck = mobConfig.getSkyCheck();

            if (skyCheck == -1) {
                return true;
            }

            return (skyCheck == 0 && canSeeSky) || (skyCheck == 1 && !canSeeSky);
        }

        static boolean meetsHeightCheck(WorldServer worldServer, BlockPos blockPos, String mobResourceName, boolean joinWorld, @Nullable CheckSpawnReturn checkSpawnReturn) {
            MobConfig mobConfig = LivingDataEntries.get(mobResourceName).getDimensionData(worldServer.provider.getDimension()).getMobConfig();

            if (joinWorld && !mobConfig.getJoinWorldEnabled()) {
                return true;
            }

            int height = blockPos.getY();

            boolean tooLow = height < mobConfig.getMinHeight();
            boolean tooHigh = height > mobConfig.getMaxHeight();

            if (checkSpawnReturn != null) {
                checkSpawnReturn.height = height;
                checkSpawnReturn.heightTooLow = tooLow;
                checkSpawnReturn.heightTooHigh = tooHigh;
            }

            return !tooLow && !tooHigh;
        }

        static boolean isWeatherConditionSuitable(WorldServer worldServer, BlockPos blockPos, String mobResourceName, boolean joinWorld, @Nullable CheckSpawnReturn checkSpawnReturn) {
            MobConfig mobConfig = LivingDataEntries.get(mobResourceName).getDimensionData(worldServer.provider.getDimension()).getMobConfig();

            if (joinWorld && !mobConfig.getJoinWorldEnabled()) {
                return true;
            }

            WorldUtils.WeatherCondition weatherCondition = mobConfig.getWeatherCondition();
            WorldUtils.WeatherCondition weatherAtPosition = WorldUtils.getWeatherConditionAtPosition(blockPos, worldServer, weatherCondition);

            if (checkSpawnReturn != null) {
                checkSpawnReturn.weatherAtPosition = weatherAtPosition;
            }

            if (weatherCondition == WorldUtils.WeatherCondition.ANY || weatherAtPosition == WorldUtils.WeatherCondition.ANY) {
                return true;
            }

            return weatherAtPosition.equals(weatherCondition);
        }

        public static boolean isValidDespawnLightLevel(EntityLiving livingEntity, WorldServer worldServer, int minDespawnLightLevel, int maxDespawnLightLevel) {
            int i = MathHelper.floor(livingEntity.posX);
            int j = MathHelper.floor(livingEntity.getEntityBoundingBox().minY);
            int k = MathHelper.floor(livingEntity.posZ);

            BlockPos blockPos = new BlockPos(i, j, k);

            int blockLighting = worldServer.getLight(blockPos, true);

            if (blockLighting < minDespawnLightLevel && maxDespawnLightLevel != -1) {
                return false;
            }

            return blockLighting <= maxDespawnLightLevel || maxDespawnLightLevel == -1;
        }

        public static CheckSpawnReturn checkSpawn(@Nullable EntityLiving livingEntity, @Nullable String mobResourceName, WorldServer worldServer, BlockPos blockPos, boolean joinWorld,
                                                  boolean skipCountChecks) {
            int dimensionId = worldServer.provider.getDimension();

            CheckSpawnReturn checkSpawnReturn = new CheckSpawnReturn(CheckSpawnResult.UNKNOWN, null, null, null, blockPos.getX(),
                    blockPos.getY(), blockPos.getZ(), DimensionUtils.getDimensionNameFromId(dimensionId));

            if (mobResourceName == null) {
                if (livingEntity == null) {
                    checkSpawnReturn.result = CheckSpawnResult.INVALID;
                    checkSpawnReturn.info = "Mob ResourceName and LivingData handle are null";

                    return checkSpawnReturn;
                }

                mobResourceName = EntityUtils.getEntityResourceNameFromClass(livingEntity.getClass());
            }

            checkSpawnReturn.mobResourceName = mobResourceName;

            MobConfig mobConfig = LivingDataEntries.get(mobResourceName).getDimensionData(dimensionId).getMobConfig();
            EnumCreatureType creatureType = mobConfig.getCreatureType();

            checkSpawnReturn.creatureType = getCreatureTypeAsString(creatureType);

            SpawnerConfig spawnerConfig = DimensionSpawnerConfigs.get(dimensionId);

            if (!spawnerConfig.getTypeEnabled(creatureType)) {
                checkSpawnReturn.result = CheckSpawnResult.DENIED;
                checkSpawnReturn.info = "Creature type is disabled";

                return checkSpawnReturn;
            }

            if (livingEntity != null && livingEntity.getEntityData().getBoolean("ResultAllowed")) {
                onEntityAllowed(livingEntity, mobConfig);

                checkSpawnReturn.result = CheckSpawnResult.ALLOWED;
                checkSpawnReturn.info = "Checks already passed";

                return checkSpawnReturn;
            }

            if (!mobConfig.getCanSpawn()) {
                checkSpawnReturn.result = CheckSpawnResult.DENIED;
                checkSpawnReturn.info = "Not allowed to spawn";

                return checkSpawnReturn;
            }


            if (!mobConfig.meetsSpawnRNG(worldServer, joinWorld, checkSpawnReturn)) {
                checkSpawnReturn.result = CheckSpawnResult.DENIED;
                checkSpawnReturn.info = String.format("Not lucky enough to spawn (AddedRarity) RNG: %d / %d required 0", checkSpawnReturn.rngValue, mobConfig.getAddedRarity(joinWorld));

                return checkSpawnReturn;
            }

            if (!EntityUtils.Tests.meetsHeightCheck(worldServer, blockPos.down(), mobResourceName, joinWorld, checkSpawnReturn)) {
                checkSpawnReturn.result = CheckSpawnResult.DENIED;

                if (checkSpawnReturn.heightTooLow) {
                    checkSpawnReturn.info = String.format("Height %d is too low, required >= %d, <= %d", checkSpawnReturn.height, mobConfig.getMinHeight(), mobConfig.getMaxHeight());
                } else if (checkSpawnReturn.heightTooHigh) {
                    checkSpawnReturn.info = String.format("Height %d is too high, required >= %d, <= %d", checkSpawnReturn.height, mobConfig.getMinHeight(), mobConfig.getMaxHeight());
                }

                return checkSpawnReturn;
            }

            Biome biome = worldServer.getBiome(blockPos);
            checkSpawnReturn.biomeName = biome.getBiomeName();

            if (!mobConfig.getBiomes().contains(biome)) {
                checkSpawnReturn.result = CheckSpawnResult.DENIED;
                checkSpawnReturn.info = "Biome not allowed";

                return checkSpawnReturn;
            }

            if (!EntityUtils.Tests.isWeatherConditionSuitable(worldServer, blockPos.down(), mobResourceName, joinWorld, checkSpawnReturn)) {
                checkSpawnReturn.result = CheckSpawnResult.DENIED;
                checkSpawnReturn.info = String.format("Weather %s is not suitable, required: %s", WorldUtils.getWeatherConditionAsString(checkSpawnReturn.weatherAtPosition),
                        WorldUtils.getWeatherConditionAsString(mobConfig.getWeatherCondition()));

                return checkSpawnReturn;
            }

            if (SereneSeasonsSupport.modAvailable) {
                try {
                    if (!EntityUtils.Tests.seasonAllowed(worldServer, mobResourceName, joinWorld, checkSpawnReturn)) {
                        checkSpawnReturn.result = CheckSpawnResult.DENIED;
                        checkSpawnReturn.info = String.format("Not allowed to spawn in season %s, required: %s",
                                SereneSeasonsSupport.getSeasonAsString(SereneSeasonsSupport.getCurrentSeason(worldServer)), Arrays.toString(mobConfig.getSeasonsAllowedStringList()));

                        return checkSpawnReturn;
                    }
                } catch (NullPointerException ignored) {
                }
            }

            if (!EntityUtils.Tests.blockAllowed(worldServer, blockPos.down(), mobResourceName, joinWorld)) {
                checkSpawnReturn.result = CheckSpawnResult.DENIED;
                checkSpawnReturn.info = String.format("Not allowed to spawn on block %s", worldServer.getBlockState(blockPos.down()).getBlock().getRegistryName());

                return checkSpawnReturn;
            }

            if (!EntityUtils.Tests.meetsSkyCheck(worldServer, blockPos, mobResourceName, joinWorld, checkSpawnReturn)) {
                checkSpawnReturn.result = CheckSpawnResult.DENIED;
                if (checkSpawnReturn.canPositionSeeSky) {
                    checkSpawnReturn.info = "Position can see sky";
                } else {
                    checkSpawnReturn.info = "Position can't see sky";
                }

                return checkSpawnReturn;
            }

            if (!EntityUtils.Tests.isValidLightLevel(worldServer, blockPos, mobResourceName, joinWorld)) {
                checkSpawnReturn.result = CheckSpawnResult.DENIED;
                checkSpawnReturn.info = "Light level does not meet requirements";

                return checkSpawnReturn;
            }

            if (!skipCountChecks) {
                if (EntityUtils.Tests.creatureTypeReachedWorldMaxCount(worldServer, mobResourceName, joinWorld)) {
                    checkSpawnReturn.result = CheckSpawnResult.DENIED;
                    checkSpawnReturn.info = String.format("Creature type %s reached world global max count", checkSpawnReturn.creatureType);

                    return checkSpawnReturn;
                }

                if (EntityUtils.Tests.creatureTypeReachedChunkMaxCount(worldServer, blockPos.down(), mobResourceName, joinWorld)) {
                    checkSpawnReturn.result = CheckSpawnResult.DENIED;
                    checkSpawnReturn.info = String.format("Creature type %s reached max chunk count", checkSpawnReturn.creatureType);

                    return checkSpawnReturn;
                }

                if (EntityUtils.Tests.entityReachedWorldMaxCount(worldServer, mobResourceName, joinWorld)) {
                    checkSpawnReturn.result = CheckSpawnResult.DENIED;
                    checkSpawnReturn.info = "Mob type reached max count";

                    return checkSpawnReturn;
                }

                if (EntityUtils.Tests.entityReachedChunkMaxCount(worldServer, blockPos.down(), mobResourceName, joinWorld)) {
                    checkSpawnReturn.result = CheckSpawnResult.DENIED;
                    checkSpawnReturn.info = "Mob type reached max chunk count";

                    return checkSpawnReturn;
                }
            }

            if (worldServer.isAnyPlayerWithinRangeAt(checkSpawnReturn.posX, checkSpawnReturn.posY, checkSpawnReturn.posZ, mobConfig.getSpawnRange())) {
                checkSpawnReturn.result = CheckSpawnResult.DENIED;
                checkSpawnReturn.info = "No players within spawn range";

                return checkSpawnReturn;
            }

            checkSpawnReturn.result = CheckSpawnResult.ALLOWED;
            checkSpawnReturn.info = "Passed all spawn checks";

            if (livingEntity != null) {
                onEntityAllowed(livingEntity, mobConfig);
                livingEntity.getEntityData().setBoolean("ResultAllowed", true);
            }

            return checkSpawnReturn;
        }
    }
}
