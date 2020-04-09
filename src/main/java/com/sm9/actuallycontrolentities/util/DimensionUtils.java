package com.sm9.actuallycontrolentities.util;

import com.sm9.actuallycontrolentities.common.config.SpawnerConfig;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraftforge.common.DimensionManager;

import java.util.Collection;
import java.util.List;

import static net.minecraftforge.common.BiomeDictionary.Type.END;
import static net.minecraftforge.common.BiomeDictionary.Type.NETHER;

@SuppressWarnings("SpellCheckingInspection")
public class DimensionUtils {
    public static int[] getAllDimensions() {
        return DimensionManager.getRegisteredDimensions().values().stream().flatMap(Collection::stream).mapToInt(Integer::intValue).toArray();
    }

    public static String getDimensionNameFromId(int dimensionId) {
        DimensionType dimType;
        WorldServer worldServer = DimensionManager.getWorld(dimensionId);

        if (worldServer == null) {
            dimType = DimensionManager.getProviderType(dimensionId);
        } else {
            dimType = worldServer.provider.getDimensionType();
        }

        if (dimType == null) {
            return null;
        }

        return dimType.getName();
    }

    public static boolean dimensionHasBiome(int dimensionId, Biome biome) {
        boolean result = false;

        switch (dimensionId) {
            case -1: {
                result = BiomeUtils.hasTag(biome, NETHER);
                break;
            }

            case 0: {
                result = !BiomeUtils.hasTag(biome, NETHER) && !BiomeUtils.hasTag(biome, END);
                break;
            }

            case 1: {
                result = BiomeUtils.hasTag(biome, END);
                break;
            }
        }

        if (result && dimensionId != 0) {
            return true;
        }

        WorldProvider worldProvider = null;

        try {
            worldProvider = DimensionManager.getProvider(dimensionId);
        } catch (NullPointerException ex) {
            try {
                worldProvider = DimensionManager.createProviderFor(dimensionId);
            } catch (NullPointerException ignored) {
            }
        }

        if (worldProvider == null) {
            return result;
        }

        BiomeProvider biomeProvider = null;

        try {
            biomeProvider = worldProvider.getBiomeProvider();
        } catch (NullPointerException ignored) {
        }

        if (biomeProvider == null) {
            return result;
        }

        Biome fixedBiome = null;

        try {
            fixedBiome = biomeProvider.getFixedBiome();
        } catch (NullPointerException ignored) {
        }

        if (fixedBiome != null && fixedBiome.equals(biome)) {
            return true;
        }

        List<Biome> potentialBiomes = null;

        try {
            potentialBiomes = biomeProvider.getBiomesToSpawnIn();
        } catch (NullPointerException ignored) {
        }

        return result || (potentialBiomes != null && potentialBiomes.contains(biome));
    }

    public static boolean canSpawnCreature(boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnOnSetTickRate, SpawnerConfig spawnerConfig, EnumCreatureType creatureType) {
        return (spawnerConfig.getTypeEnabled(creatureType) && (!creatureType.getPeacefulCreature() || spawnPeacefulMobs) && (creatureType.getPeacefulCreature()
                || spawnHostileMobs) && (!creatureType.getAnimal() || spawnOnSetTickRate));
    }
}