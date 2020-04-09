package com.sm9.actuallycontrolentities.handler;

import com.sm9.actuallycontrolentities.block.DummyBlock;
import com.sm9.actuallycontrolentities.command.Reload;
import com.sm9.actuallycontrolentities.common.config.MainConfig;
import com.sm9.actuallycontrolentities.common.config.MobConfig;
import com.sm9.actuallycontrolentities.common.config.SpawnerConfig;
import com.sm9.actuallycontrolentities.compat.SereneSeasonsSupport;
import com.sm9.actuallycontrolentities.entity.LivingData;
import com.sm9.actuallycontrolentities.storage.LivingDataEntries;
import com.sm9.actuallycontrolentities.util.BiomeUtils;
import com.sm9.actuallycontrolentities.util.EntityUtils;
import com.sm9.actuallycontrolentities.util.General;
import com.sm9.actuallycontrolentities.util.WorldUtils;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import java.io.File;

import static com.sm9.actuallycontrolentities.ActuallyControlEntities.*;

@SuppressWarnings("ALL")
public class EventHandler {

    public static void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new EventHandler());

        DummyBlock.preInit();

        ACELogger = LogManager.getLogger("ActuallyControlEntities");
        rootConfigDir = new File(event.getModConfigurationDirectory(), "sm9/ActuallyControlEntities");

        try {
            allBiomeTags = BiomeDictionary.Type.class.getDeclaredField("byName");
            allBiomeTags.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }

        mainConfig = new MainConfig();
        aceProfiler = new Profiler();
    }

    public static void postInit() {
        SereneSeasonsSupport.modAvailable = Loader.isModLoaded("sereneseasons");

        if (SereneSeasonsSupport.modAvailable) {
            General.debugToConsole(Level.INFO, "Serene Seasons support enabled.");
        }

        initACE();
    }

    private static void initACE() {
        totalBiomeCount = ForgeRegistries.BIOMES.getValuesCollection().size();
        Initializers.cacheLivingEntities();
        Initializers.cacheExistingSpawnEntries();
        Initializers.cacheMostCommonSpawnEntries();
        Initializers.initDimensionConfigs();
        Initializers.loadConfigs(null);
        BiomeUtils.removeAllLegacySpawns();
    }

    public static void onWorldLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new Reload());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntitySpawnEvent(LivingSpawnEvent.CheckSpawn event) {
        EntityLivingBase livingEntityBase = event.getEntityLiving();

        if (!(livingEntityBase instanceof EntityLiving)) {
            return;
        }

        EntityLiving livingEntity = (EntityLiving) livingEntityBase;

        World worldAny = livingEntity.getEntityWorld();

        if (!(worldAny instanceof WorldServer)) {
            return;
        }

        livingEntity.setDropItemsWhenDead(false);

        WorldServer worldServer = (WorldServer) worldAny;
        EntityUtils.CheckSpawnReturn checkSpawnReturn = EntityUtils.Tests.checkSpawn(livingEntity, LivingDataEntries.get(livingEntity).getMobResourceName(), worldServer, livingEntity.getPosition(),
                false, false);

        switch (checkSpawnReturn.result) {
            case INVALID:
            case DENIED: {
                livingEntity.setDropItemsWhenDead(false);
                livingEntity.setDead();
                event.setResult(Event.Result.DENY);

                if (mainConfig.getDebugEvents()) {
                    String creatureTypeName = checkSpawnReturn.creatureType;

                    General.debugToConsole(Level.WARN, "[CheckSpawnEvent][%s] - Skipping entity: [%s] %s at position: X: %d, Y: %d, Z: %d) Biome: %s (%s)",
                            checkSpawnReturn.dimName, creatureTypeName == null ? "Null" : creatureTypeName,
                            checkSpawnReturn.mobResourceName == null ? "Null" : checkSpawnReturn.mobResourceName,
                            checkSpawnReturn.posX, checkSpawnReturn.posY, checkSpawnReturn.posZ, checkSpawnReturn.biomeName, checkSpawnReturn.info);
                }

                return;
            }
            case HANDLED: {
                return;
            }
        }

        EntityUtils.attemptPositionFix(worldServer, livingEntity, livingEntity.getPosition());
        event.setResult(Event.Result.ALLOW);
        livingEntity.setDropItemsWhenDead(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        LivingData livingData = LivingDataEntries.get(entity);

        if (livingData == null) {
            return;
        }

        EntityLiving livingEntity = (EntityLiving) entity;

        World worldAny = livingEntity.getEntityWorld();

        if (!(worldAny instanceof WorldServer)) {
            return;
        }

        livingEntity.setDropItemsWhenDead(false);

        WorldServer worldServer = (WorldServer) worldAny;
        EntityUtils.CheckSpawnReturn checkSpawnReturn = EntityUtils.Tests.checkSpawn(livingEntity, LivingDataEntries.get(livingEntity).getMobResourceName(), worldServer, livingEntity.getPosition(),
                true, false);

        switch (checkSpawnReturn.result) {
            case INVALID:
            case DENIED: {
                livingEntity.setDead();
                event.setResult(Event.Result.DENY);
                event.setCanceled(true);

                if (mainConfig.getDebugEvents()) {
                    String creatureTypeName = checkSpawnReturn.creatureType;

                    General.debugToConsole(Level.WARN, "[JoinWorldEvent][%s] - Skipping entity: [%s] %s at position: X: %d, Y: %d, Z: %d) Biome: %s (%s)",
                            checkSpawnReturn.dimName, creatureTypeName == null ? "Null" : creatureTypeName,
                            checkSpawnReturn.mobResourceName == null ? "Null" : checkSpawnReturn.mobResourceName,
                            checkSpawnReturn.posX, checkSpawnReturn.posY, checkSpawnReturn.posZ, checkSpawnReturn.biomeName, checkSpawnReturn.info);
                }

                return;
            }
            case HANDLED: {
                return;
            }
        }

        EntityUtils.attemptPositionFix(worldServer, livingEntity, livingEntity.getPosition());
        event.setResult(Event.Result.ALLOW);
        livingEntity.setDropItemsWhenDead(true);
        event.setCanceled(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityAttacked(LivingAttackEvent event) {
        EntityLivingBase livingEntity = event.getEntityLiving();
        LivingData livingData = LivingDataEntries.get(livingEntity);

        if (livingData == null) {
            return;
        }

        String mobResourceName = livingData.getMobResourceName();

        World worldAny = livingEntity.getEntityWorld();

        if (!(worldAny instanceof WorldServer)) {
            return;
        }

        WorldServer worldServer = (WorldServer) worldAny;

        int dimensionId = worldServer.provider.getDimension();

        MobConfig mobConfig = livingData.getDimensionData(dimensionId).getMobConfig();

        if (mobConfig == null) {
            return;
        }

        DamageSource damageSource = event.getSource();

        if (damageSource == null) {
            event.setCanceled(true);
            return;
        }

        boolean immune = livingEntity.getEntityData().getBoolean("CollisionImmune") || mobConfig.getNoSuffocationEnabled();

        if (damageSource != DamageSource.IN_WALL || !immune) {
            return;
        }

        if (mainConfig.getDebugEvents()) {
            General.debugToConsole(Level.INFO, "[LivingAttackEvent] Preventing entity: %s suffocation, (X: %.1f, Y: %.1f, Z: %.1f) - NoSuffocation: true", mobResourceName, livingEntity.posX, livingEntity.posY, livingEntity.posZ);
        }

        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityTakeDamage(LivingDamageEvent event) {
        EntityLivingBase livingEntity = event.getEntityLiving();
        LivingData livingData = LivingDataEntries.get(livingEntity);

        if (livingData == null) {
            return;
        }

        String mobResourceName = livingData.getMobResourceName();

        World worldAny = livingEntity.getEntityWorld();

        if (!(worldAny instanceof WorldServer)) {
            return;
        }

        WorldServer worldServer = (WorldServer) worldAny;

        int dimensionId = worldServer.provider.getDimension();

        MobConfig mobConfig = livingData.getDimensionData(dimensionId).getMobConfig();

        if (mobConfig == null) {
            return;
        }

        DamageSource damageSource = event.getSource();

        if (damageSource == null) {
            event.setAmount(0.0f);
            return;
        }

        boolean immune = livingEntity.getEntityData().getBoolean("CollisionImmune") || mobConfig.getNoSuffocationEnabled();

        if (damageSource != DamageSource.IN_WALL || !immune) {
            return;
        }

        if (mainConfig.getDebugEvents()) {
            General.debugToConsole(Level.INFO, "[LivingDamageEvent] Preventing entity: %s suffocation, (X: %.1f, Y: %.1f, Z: %.1f) - NoSuffocation: true", mobResourceName,
                    livingEntity.posX, livingEntity.posY, livingEntity.posZ);
        }

        event.setAmount(0.0f);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        World worldAny = event.world;

        GameRules gameRules = worldAny.getGameRules();

        if (gameRules.getBoolean("doMobSpawning")) {
            gameRules.setOrCreateGameRule("doMobSpawning", "false");
        }

        if (event.side != Side.SERVER || !(event.world instanceof WorldServer) || event.phase != TickEvent.Phase.END ||
                    Initializers.configReloading || !mainConfig.getAllowMobSpawning()) {
                return;
        }

        WorldServer worldServer = (WorldServer) worldAny;
        WorldInfo worldInfo = worldServer.getWorldInfo();

        if (worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            return;
        }

        WorldSpawner worldSpawner = WorldSpawner.get(worldServer);
        MinecraftServer server = worldServer.getMinecraftServer();

        if (server == null) {
            return;
        }

        boolean spawnHostileMobs;
        boolean spawnPeacefulMobs;

        if (server.isSinglePlayer()) {
            spawnHostileMobs = server.getDifficulty() != EnumDifficulty.PEACEFUL;
            spawnPeacefulMobs = true;
        } else {
            spawnHostileMobs = server.allowSpawnMonsters();
            spawnPeacefulMobs = server.getCanSpawnAnimals();
        }

        aceProfiler.startSection("aceMobSpawner");
        worldSpawner.performRandomSpawning(worldServer.provider.getDimension(), spawnHostileMobs, spawnPeacefulMobs,
                worldInfo.getWorldTotalTime() % mainConfig.getSpawnTickDelay() == 0L, WorldUtils.getShuffledPositionsForAllPlayers(worldServer), false);
        aceProfiler.endStartSection("aceMobSpawner");
    }

    @SubscribeEvent()
    public void peformCustomWorldGenSpawning(PopulateChunkEvent.Pre event) {
        World worldAny = event.getWorld();

        GameRules gameRules = worldAny.getGameRules();

        if (gameRules.getBoolean("doMobSpawning")) {
            gameRules.setOrCreateGameRule("doMobSpawning", "false");
        }

        if (Initializers.configReloading || !mainConfig.getAllowMobSpawning()) {
            return;
        }

        WorldServer worldServer = (WorldServer) worldAny;
        WorldInfo worldInfo = worldServer.getWorldInfo();

        if (worldInfo.getTerrainType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            return;
        }

        WorldSpawner worldSpawner = WorldSpawner.get(worldServer);
        MinecraftServer server = worldServer.getMinecraftServer();

        if (server == null) {
            return;
        }

        boolean spawnHostileMobs;
        boolean spawnPeacefulMobs;

        if (server.isSinglePlayer()) {
            spawnHostileMobs = server.getDifficulty() != EnumDifficulty.PEACEFUL;
            spawnPeacefulMobs = true;
        } else {
            spawnHostileMobs = server.allowSpawnMonsters();
            spawnPeacefulMobs = server.getCanSpawnAnimals();
        }

        int x = event.getChunkX();
        int z = event.getChunkX();
        int i = x * 16;
        int j = z * 16;

        Biome biome = worldServer.getBiome(new BlockPos(x, 0, z));

        aceProfiler.startSection("aceChunkGenSpawner");
        worldSpawner.performRandomSpawning(worldServer.provider.getDimension(), spawnHostileMobs, spawnPeacefulMobs, true,
                WorldUtils.getShuffledPositionsForChunk(worldServer, i + 8, j + 8), true);
        aceProfiler.endStartSection("aceChunkGenSpawner");

    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDespawn(LivingSpawnEvent.AllowDespawn event) {
        EntityLivingBase livingEntity = event.getEntityLiving();
        LivingData livingData = LivingDataEntries.get(livingEntity);

        if (livingData == null) {
            return;
        }

        World worldAny = livingEntity.getEntityWorld();

        if (!(worldAny instanceof WorldServer)) {
            return;
        }

        WorldServer worldServer = (WorldServer) worldAny;

        int dimensionId = worldServer.provider.getDimension();

        MobConfig mobConfig = livingData.getDimensionData(dimensionId).getMobConfig();

        if (mobConfig == null) {
            return;
        }

        SpawnerConfig spawnerConfig = livingData.getDimensionData(dimensionId).getSpawnerConfig();

        if (spawnerConfig == null) {
            return;
        }

        if (!spawnerConfig.shouldForceDespawns()) {
            return;
        }

        if (IMob.class.isAssignableFrom(livingEntity.getClass()) || IRangedAttackMob.class.isAssignableFrom(livingEntity.getClass()) ||
                mobConfig.getCreatureType().equals(EnumCreatureType.MONSTER)) {
            return;
        }

        if ((livingEntity instanceof EntityTameable) && ((EntityTameable) livingEntity).isTamed()) {
            return;
        }

        if ((livingEntity instanceof EntitySheep) || (livingEntity instanceof EntityPig) || (livingEntity instanceof EntityCow) || (livingEntity instanceof EntityChicken)) {
            if (EntityUtils.Tests.isValidDespawnLightLevel((EntityLiving) livingEntity, worldServer, spawnerConfig.getMinDespawnLightLevel(), spawnerConfig.getMaxDespawnLightLevel())) {
                return;
            }
        }

        NBTTagCompound nbt = new NBTTagCompound();
        livingEntity.writeToNBT(nbt);

        if (nbt.hasKey("Owner") && !nbt.getString("Owner").equals("")) {
            return;
        }
        if (nbt.hasKey("Tamed") && nbt.getBoolean("Tamed")) {
            return;
        }

        EntityPlayer entityplayer = worldServer.getClosestPlayerToEntity(livingEntity, -1.0D);

        if (entityplayer != null) {
            double d = entityplayer.posX - livingEntity.posX;
            double d1 = entityplayer.posY - livingEntity.posY;
            double d2 = entityplayer.posZ - livingEntity.posZ;
            double distance = d * d + d1 * d1 + d2 * d2;

            if (distance > 16384.0D) {
                livingEntity.setDropItemsWhenDead(false);
                event.setResult(Event.Result.ALLOW);
            } else if (livingEntity.getIdleTime() > 600) {
                if (distance < 1024.0D) {
                    return;
                }
                livingEntity.setDropItemsWhenDead(false);
                event.setResult(Event.Result.ALLOW);
            }
        }
    }
}