package com.sm9.actuallycontrolentities.common.config;

import com.google.common.collect.Sets;
import com.sm9.actuallycontrolentities.compat.SereneSeasonsSupport;
import com.sm9.actuallycontrolentities.entity.LivingData;
import com.sm9.actuallycontrolentities.spawner.ACESpawnEntry;
import com.sm9.actuallycontrolentities.util.*;
import com.sm9.actuallycontrolentities.world.DimensionData;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import sereneseasons.api.season.Season;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static com.sm9.actuallycontrolentities.ActuallyControlEntities.dummyBlock;
import static com.sm9.actuallycontrolentities.ActuallyControlEntities.totalBiomeCount;

@SuppressWarnings({"BooleanMethodIsAlwaysInverted", "SpellCheckingInspection", "unused"})
public class MobConfig {
    public final Class<? extends EntityLiving> cLazz;
    private final List<String> configOrder = Arrays.asList("CanSpawn", "WorldSpawner", "StructureSpawner", "CreatureType", "PlacementType", "Weight",
            "MinGroupCount", "MaxGroupCount", "Biomes", "ExludedBiomes", "Structures", "Blocks", "ExcludedBlocks", "IgnoreVanillaChecks", "GroupOverflow", "MinHeight", "MaxHeight",
            "MinLight", "MaxLight", "SkyCheck", "WeatherCondition", "Seasons", "ExcludedSeasons", "NoSuffocation", "SpawnRange",
            "MaxCount", "MaxChunkCount", "AddedRarity", "PotionEffects", "JoinWorld");

    private final Configuration mobConfig;
    private final String mobResourceName;
    private final String mobName;
    private final String modName;
    private final int dimensionId;
    private final String dimensionName;
    private final HashMap<Biome, Integer> biomeWeights = new HashMap<>();
    private final HashMap<Biome, Integer> biomeMinGroupCounts = new HashMap<>();
    private final HashMap<Biome, Integer> biomeMaxGroupCounts = new HashMap<>();
    private final HashMap<String, Integer> structureWeights = new HashMap<>();
    private final HashMap<String, Integer> structureMinGroupCounts = new HashMap<>();
    private final HashMap<String, Integer> structureMaxGroupCounts = new HashMap<>();
    private final int defaultMaxHeight;
    private final LivingData livingData;
    private final DimensionData dimensionData;
    private final String[] defaultSpawnSeasons = new String[]{"*"};
    private final String[] defaultSpawnBlocks = new String[]{"*"};
    String[] seasonsAllowedStringList;
    private boolean canSpawn;
    private boolean worldSpawnerEnabled;
    private boolean structureSpawnerEnabled;
    private boolean spawnerDefault = true;
    private boolean ignoreVanillaChecks = false;
    private EnumCreatureType creatureType;
    private EntityLiving.SpawnPlacementType spawnPlacementType;
    private int spawnWeight;
    private int minGroupCount;
    private int maxGroupCount;
    private List<Biome> spawnBiomes;
    private List<String> spawnStructures;
    private List<Block> blocksAllowed;
    private List<Block> blocksExcluded;
    private List<Season> seasonsAllowed;
    private List<Season> seasonsExcluded;
    private List<PotionEffect> potionEffects;
    private String[] worldReplacementsStringList;
    private String[] structureReplacementsStringList;
    private boolean joinWorldEvent;
    private boolean noSuffocation;
    private float spawnRange;
    private int maxCount;
    private int maxChunkCount;
    private int addedRarity;
    private int defaultMinHeight;
    private int defaultMinLight;
    private int defaultMaxLight;
    private int minHeight;
    private int maxHeight;
    private int minLight;
    private int maxLight;
    private int skyCheck;
    private WorldUtils.WeatherCondition weatherCondition;
    private String defaultCreatureTypeString = "CREATURE";
    private String defaultSpawnPlacementTypeString = "GROUND";
    private int defaultSpawnWeight = 100;
    private int defaultMinGroupCount = 1;
    private int defaultMaxGroupCount = 4;
    private int defaultMaxChunkCount = 4;
    private int defaulySkyCheck = -1;
    private float defaultSpawnRange = 24.0f;
    private boolean groupOverflowAllowed;
    private String[] defaultSpawnBiomesString = new String[]{"*"};
    private ACESpawnEntry mostCommonSpawnEntry;

    public MobConfig(File mobFile, String mobResourceName, String mobName, String modName, DimensionData dimensionData, Class<? extends EntityLiving> cLazzIn) {
        cLazz = cLazzIn;
        mobConfig = new Configuration(mobFile);
        this.mobResourceName = mobResourceName;
        this.mobName = mobName;
        this.modName = modName;
        this.dimensionData = dimensionData;
        this.livingData = dimensionData.getLivingData();
        this.dimensionId = dimensionData.getDimensionId();
        this.dimensionName = dimensionData.getDimensionName();
        this.mostCommonSpawnEntry = livingData.getMostCommonDefaultSpawnEntry();

        defaultMinHeight = 0;
        defaultMaxHeight = 256;

        if (mostCommonSpawnEntry != null) {
            defaultCreatureTypeString = EntityUtils.getCreatureTypeAsString(mostCommonSpawnEntry.getCreatureType());
            defaultSpawnPlacementTypeString = EntityUtils.getSpawnPlacementTypeAsString(mostCommonSpawnEntry.getSpawnPlacementType());
            defaultSpawnWeight = mostCommonSpawnEntry.getWeight();
            defaultMinGroupCount = mostCommonSpawnEntry.getMinGroupCount();
            defaultMaxGroupCount = mostCommonSpawnEntry.getMaxGroupCount();

            Set<String> processedBiomes = getProcessedBiomeList();

            if (processedBiomes != null) {
                defaultSpawnBiomesString = processedBiomes.toArray(new String[0]);
            }

            if (defaultMaxGroupCount > defaultMaxChunkCount) {
                defaultMaxChunkCount = defaultMaxGroupCount;
            }
        } else {
            if (EnumCreatureType.CREATURE.getCreatureClass().isAssignableFrom(cLazz)) {
                defaultCreatureTypeString = "CREATURE";
            } else if (EnumCreatureType.MONSTER.getCreatureClass().isAssignableFrom(cLazz)) {
                defaultCreatureTypeString = "MONSTER";
            } else if (EnumCreatureType.AMBIENT.getCreatureClass().isAssignableFrom(cLazz)) {
                defaultCreatureTypeString = "AMBIENT";
            } else if (EnumCreatureType.WATER_CREATURE.getCreatureClass().isAssignableFrom(cLazz)) {
                defaultCreatureTypeString = "WATER_CREATURE";
                defaultSpawnPlacementTypeString = "WATER";
            }

            if (cLazz.isAssignableFrom(EntityGhast.class)) {
                defaultMaxChunkCount = 1;
                defaultMaxGroupCount = 1;
            } else if (cLazz.isAssignableFrom(EntityWolf.class)) {
                defaultMaxGroupCount = 8;
                defaultMaxChunkCount = 8;
            } else if (cLazz.isAssignableFrom(AbstractHorse.class)) {
                defaultMaxGroupCount = 6;
                defaultMaxChunkCount = 6;
            }
        }

        switch (defaultCreatureTypeString) {
            case "MONSTER": {
                defaultMinLight = 0;
                defaultMaxLight = 7;
                break;
            }

            case "CREATURE": {
                defaultMinLight = 9;
                defaultMaxLight = 15;
            }

            case "AMBIENT":
            case "WATER_CREATURE": {
                defaultMinLight = 0;
                defaultMaxLight = 15;
                break;
            }
        }

        if (cLazz.isAssignableFrom(EntityBat.class)) {
            defaulySkyCheck = 1;
            defaultMinHeight = 0;
        } else if (cLazz.isAssignableFrom(EntitySquid.class)) {
            defaultMinHeight = 46;
        }

        if (defaultSpawnBiomesString == null || defaultSpawnBiomesString.length < 1 || Arrays.equals(defaultSpawnBiomesString, new String[]{"*"})) {
            defaultSpawnBiomesString = new String[]{"*"};
            spawnerDefault = false;
        }
    }

    public void reloadFromConfig() {
        mobConfig.load();

        mobConfig.setCategoryComment("config", String.format("%s (%s) Config for %s (%d)", mobName, mobResourceName, dimensionName, dimensionId));

        canSpawn = mobConfig.getBoolean("CanSpawn", "config", true, "Allow the mob to spawn, setting this to false will restrict all spawns including ones handled by other mods.");
        worldSpawnerEnabled = mobConfig.getBoolean("WorldSpawner", "config", spawnerDefault, "Allow World spawning for this mob.");
        //worldReplacementsStringList = mobConfig.getStringList("WorldReplacements", "config", new String[]{""}, "Mobs which can randomly spawn in place of this mob (<mobid>-<chance>)");
        structureSpawnerEnabled = mobConfig.getBoolean("StructureSpawner", "config", false, "Allow structure spawning for this mob.");
        //structureReplacementsStringList = mobConfig.getStringList("StructureReplacements", "config", new String[]{""}, "Mobs which can randomly spawn in place of this mob (<mobid>-<chance>)");
        creatureType = EntityUtils.getCreatureTypeFromString(mobConfig.getString("CreatureType", "config", defaultCreatureTypeString, "Valid values: AMBIENT, CREATURE, MONSTER, WATER_CREATURE."));
        spawnPlacementType = EntityUtils.getSpawnPlacementTypeFromString(mobConfig.getString("PlacementType", "config", defaultSpawnPlacementTypeString == null ? "GROUND" : defaultSpawnPlacementTypeString, "Valid values: GROUND, WATER, FIRE, LAVA."));
        spawnWeight = mobConfig.getInt("Weight", "config", defaultSpawnWeight, 0, Integer.MAX_VALUE, "How often the mob is chosen to spawn, weighted against others (higher number is higher chance).");
        minGroupCount = mobConfig.getInt("MinGroupCount", "config", defaultMinGroupCount, 1, Integer.MAX_VALUE, "Minimum number that should spawn together.");
        maxGroupCount = mobConfig.getInt("MaxGroupCount", "config", defaultMaxGroupCount, 1, Integer.MAX_VALUE, "Maximum number that should spawn together.");
        String[] spawnBiomesStringList = mobConfig.getStringList("Biomes", "config", defaultSpawnBiomesString, "Biomes this mob is allowed to spawn in (Accepts biome types and ids), * = everything.");
        String[] excludedBiomesStringList = mobConfig.getStringList("ExludedBiomes", "config", new String[]{""}, "Biomes this mob is not allowed to spawn in (Accepts biome types and ids), * = everything.");
        String[] structureSpawns = mobConfig.getStringList("Structures", "config", new String[]{""}, "Structures this mob can spawn at (<structure>-<weight>-<mingroupcount>-<maxroupcount>).");
        String[] blocksAllowedStringList = mobConfig.getStringList("Blocks", "config", defaultSpawnBlocks, "Blocks this mob is allowed to spawn on * = everything.");
        String[] blocksExcludedStringList = mobConfig.getStringList("ExcludedBlocks", "config", new String[]{""}, "Blocks this mob is not allowed to spawn on.");

        ignoreVanillaChecks = mobConfig.getBoolean("IgnoreVanillaChecks", "config", false, "Needed in specific scenarios such as spawning hostiles during the day, in fire / lava etc (only works with WorldSpawner / StructureSpawner handled mobs).");
        groupOverflowAllowed = mobConfig.getBoolean("GroupOverflow", "config", false, "Always spawn this mob in determined group count instead of limiting when it would cause creature count to go over limit.");
        minHeight = mobConfig.getInt("MinHeight", "config", defaultMinHeight, 0, 256, "Minimum height for this mob to spawn.");
        maxHeight = mobConfig.getInt("MaxHeight", "config", defaultMaxHeight, 0, 256, "Maximum height for this mob to spawn.");
        minLight = mobConfig.getInt("MinLight", "config", defaultMinLight, 0, 15, "Minimum light for this mob to spawn.");
        maxLight = mobConfig.getInt("MaxLight", "config", defaultMaxLight, 0, 15, "Maximum light for this mob to spawn.");
        skyCheck = mobConfig.getInt("SkyCheck", "config", defaulySkyCheck, -1, 1, "-1 = Either, 0 = Must see sky, 1 = Must not see sky.");
        weatherCondition = WorldUtils.getWeatherConditionFromString(mobConfig.getString("WeatherCondition", "config", "ANY", "Valid values: ANY, CLEAR, THUNDER, RAIN, SNOW, DOWNFALL."));

        if (SereneSeasonsSupport.modAvailable) {
            seasonsAllowedStringList = mobConfig.getStringList("Seasons", "config", defaultSpawnSeasons, "Seasons this mob is allowed to spawn during - Valid values: SPRING, SUMMER, WINTER, AUTUMN, * = all.");
            String[] seasonsExcludedStringList = mobConfig.getStringList("ExcludedSeasons", "config", new String[]{""}, "Seasons this mob is not allowed to spawn during - Valid values: SPRING, SUMMER, WINTER, AUTUMN.");
            seasonsAllowed = getSeasonsAllowedFromConfig(seasonsAllowedStringList);
            seasonsExcluded = getSeasonsExcludedFromConfig(seasonsExcludedStringList);
        } else {
            mobConfig.getCategory("general").remove("Seasons");
            mobConfig.getCategory("general").remove("ExcludedSeasons");
        }

        noSuffocation = mobConfig.getBoolean("NoSuffocation", "config", false, "Prevents mob from taking suffocation damage.");
        spawnRange = mobConfig.getFloat("SpawnRange", "config", defaultSpawnRange, 1.0f, (float) Integer.MAX_VALUE, "Use this to increase or decrease mobs default spawn range.");
        maxCount = mobConfig.getInt("MaxCount", "config", -1, -1, Integer.MAX_VALUE, "Global world limit for this mob, -1 = No limit.");
        maxChunkCount = mobConfig.getInt("MaxChunkCount", "config", defaultMaxChunkCount, -1, Integer.MAX_VALUE, "Chunk limit for this mob, -1 = No limit.");
        addedRarity = mobConfig.getInt("AddedRarity", "config", 0, 0, Integer.MAX_VALUE, "Additional added rarity.");
        String[] potionEffectsStringList = mobConfig.getStringList("PotionEffects", "config", new String[]{""}, "Potion effects which get applied on spawn (<potion>-<duration>-<amplifier>).");
        joinWorldEvent = mobConfig.getBoolean("JoinWorld", "config", false, "Makes restrictions & limits more aggressive by checking JoinWorld event (WARNING: Affects MobSpawners, SpawnEggs, Natural structure spawns).");
        spawnBiomes = getBiomeListFromConfig(spawnBiomesStringList, excludedBiomesStringList);
        spawnStructures = getSpawnStructureFromConfig(structureSpawns);
        blocksAllowed = getBlocksAllowedFromConfig(blocksAllowedStringList);
        blocksExcluded = getBlocksExcludedFromConfig(blocksExcludedStringList);
        potionEffects = getPotionEffectsFromConfig(potionEffectsStringList);

        mobConfig.setCategoryPropertyOrder("config", configOrder);

        if (mobConfig.hasChanged()) {
            mobConfig.save();
        }
    }

    private Set<String> getProcessedBiomeList() {
        HashMap<Biome, ACESpawnEntry> defaultSpawnEntryMap = livingData.getDefaultSpawnEntryMap();
        StringBuilder stringBuilder = new StringBuilder();

        Set<String> processedBiomes = Sets.newHashSet();
        Set<String> processedBiomesSpecific = Sets.newHashSet();

        if (defaultSpawnEntryMap == null) {
            return null;
        }

        ResourceLocation registryName;
        ACESpawnEntry currentEntry;
        boolean dimensionHasSpecificBiomes = false;

        for (Biome biome : defaultSpawnEntryMap.keySet()) {
            registryName = biome.getRegistryName();

            if (registryName == null) {
                continue;
            }

            currentEntry = livingData.getDefaultSpawnEntry(biome);

            if (currentEntry == null) {
                continue;
            }

            if (mostCommonSpawnEntry == null) {
                mostCommonSpawnEntry = currentEntry;
            }

            if (!currentEntry.isIdentical(mostCommonSpawnEntry)) {
                stringBuilder.
                        append(registryName.toString()).append("-").
                        append(currentEntry.getWeight()).append("-").
                        append(currentEntry.getMinGroupCount()).append("-").
                        append(currentEntry.getMaxGroupCount());
            } else {
                stringBuilder.append(registryName.toString());
            }

            processedBiomes.add(stringBuilder.toString());
            stringBuilder.delete(0, stringBuilder.length());

            if (!DimensionUtils.dimensionHasBiome(dimensionId, biome)) {
                continue;
            }

            dimensionHasSpecificBiomes = true;

            if (!currentEntry.isIdentical(mostCommonSpawnEntry)) {
                stringBuilder.
                        append(registryName.toString()).append("-").
                        append(currentEntry.getWeight()).append("-").
                        append(currentEntry.getMinGroupCount()).append("-").
                        append(currentEntry.getMaxGroupCount());
            } else {
                stringBuilder.append(registryName.toString());
            }

            processedBiomesSpecific.add(stringBuilder.toString());
            stringBuilder.delete(0, stringBuilder.length());
        }

        return dimensionHasSpecificBiomes ? processedBiomesSpecific : processedBiomes;
    }

    public boolean getCanSpawn() {
        return canSpawn;
    }

    public boolean getIgnoreVanillaChecks() {
        return ignoreVanillaChecks;
    }

    public boolean getWorldSpawnerEnabled() {
        return worldSpawnerEnabled;
    }

    public boolean getStructureSpawnerEnabled() {
        return structureSpawnerEnabled;
    }

    public EnumCreatureType getCreatureType() {
        return creatureType;
    }

    public EntityLiving.SpawnPlacementType getSpawnPlacementType() {
        return spawnPlacementType;
    }

    public int getBiomeSpawnWeight(Biome biome) {
        int finalWeight = spawnWeight;

        if (biomeWeights.containsKey(biome)) {
            finalWeight = biomeWeights.get(biome);

            if (finalWeight < 1) {
                finalWeight = spawnWeight;
            }
        }

        return finalWeight;
    }

    public int getBiomeMinGroupCount(Biome biome) {
        int finalMinGroupCount = minGroupCount;

        if (biomeMinGroupCounts.containsKey(biome)) {
            finalMinGroupCount = biomeMinGroupCounts.get(biome);

            if (finalMinGroupCount < 1) {
                finalMinGroupCount = minGroupCount;
            }
        }

        return finalMinGroupCount;
    }

    public int getBiomeMaxGroupCount(Biome biome) {
        int finalMaxGroupCount = maxGroupCount;

        if (biomeMaxGroupCounts.containsKey(biome)) {
            finalMaxGroupCount = biomeMaxGroupCounts.get(biome);

            if (finalMaxGroupCount < 1) {
                finalMaxGroupCount = minGroupCount;
            }
        }

        return finalMaxGroupCount;
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public List<Biome> getBiomes() {
        return spawnBiomes;
    }

    public int getStructureSpawnWeight(String structure) {
        int finalWeight = -1;

        if (structureWeights.containsKey(structure)) {
            finalWeight = structureWeights.get(structure);

            if (finalWeight < 1) {
                finalWeight = -1;
            }
        }

        return finalWeight;
    }

    public int getStructureMinGroupCount(String structure) {
        int finalMinGroupCount = -1;

        if (structureMinGroupCounts.containsKey(structure)) {
            finalMinGroupCount = structureMinGroupCounts.get(structure);

            if (finalMinGroupCount < 1) {
                finalMinGroupCount = -1;
            }
        }

        return finalMinGroupCount;
    }

    public int getStructureMaxGroupCount(String structure) {
        int finalMaxGroupCount = -1;

        if (structureMaxGroupCounts.containsKey(structure)) {
            finalMaxGroupCount = structureMaxGroupCounts.get(structure);

            if (finalMaxGroupCount < 1) {
                finalMaxGroupCount = -1;
            }
        }

        return finalMaxGroupCount;
    }

    public List<String> getStructures() {
        return spawnStructures;
    }

    public boolean getJoinWorldEnabled() {
        return joinWorldEvent;
    }

    public boolean getNoSuffocationEnabled() {
        return noSuffocation;
    }

    public float getSpawnRange() {
        if (spawnRange == defaultSpawnRange) {
            return dimensionData.getSpawnerConfig().getTypeSpawnRange(creatureType);
        }

        return spawnRange;
    }

    public int getMaxCount(boolean joinWorld) {
        if (joinWorld && !getJoinWorldEnabled()) {
            return -1;
        }

        return maxCount;
    }

    public int getMaxChunkCount(boolean joinWorld) {
        if (joinWorld && !getJoinWorldEnabled()) {
            return -1;
        }

        return maxChunkCount;
    }

    public int getAddedRarity(boolean joinWorld) {
        if (joinWorld && !getJoinWorldEnabled()) {
            return 0;
        }

        return addedRarity;
    }

    public boolean meetsSpawnRNG(World world, boolean joinWorld, @Nullable EntityUtils.CheckSpawnReturn checkSpawnReturn) {
        if (joinWorld && !getJoinWorldEnabled()) {
            return true;
        }

        int aRarity = getAddedRarity(joinWorld);
        int rngValue = aRarity == 0 ? 0 : world.rand.nextInt(aRarity);

        if (checkSpawnReturn != null) {
            checkSpawnReturn.addedRarity = aRarity;
            checkSpawnReturn.rngValue = rngValue;
        }

        return rngValue == 0;
    }

    public String getMobResourceName() {
        return mobResourceName;
    }

    public String getMobName() {
        return mobName;
    }

    public String getModName() {
        return modName;
    }

    public List<Block> getBlocksAllowed() {
        return blocksAllowed;
    }

    public List<Block> getBlocksExcluded() {
        return blocksExcluded == null || blocksExcluded.isEmpty() ? null : blocksExcluded;
    }

    public List<Season> getSeasonsAllowed() {
        return seasonsAllowed == null || seasonsAllowed.isEmpty() ? null : seasonsAllowed;
    }

    public List<Season> getSeasonsExcluded() {
        return seasonsExcluded == null || seasonsExcluded.isEmpty() ? null : seasonsExcluded;
    }

    public String[] getSeasonsAllowedStringList() {
        return seasonsAllowedStringList;
    }

    public int getMinLight() {
        return minLight;
    }

    public int getMaxLight() {
        return maxLight;
    }

    public int getSkyCheck() {
        return skyCheck;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public WorldUtils.WeatherCondition getWeatherCondition() {
        if (weatherCondition == null) {
            return WorldUtils.WeatherCondition.ANY;
        }

        return weatherCondition;
    }

    private List<Biome> getBiomeListFromConfig(String[] biomesAllowed, String[] biomesExluded) {
        BiomeDictionary.Type[] biomeTags;
        Biome biomeTemp;
        String[] sBiomeAdditions = new String[totalBiomeCount + 1];
        String[] sBiomeRemovals = new String[totalBiomeCount + 1];
        String[] overrideSplits;
        int weightOverride = -1;
        int groupCountMinOverride = -1;
        int groupCountMaxOverride = -1;

        int iBiomeCounter = 0;

        boolean bAddAll = false;

        for (String sBiome : biomesAllowed) {
            if (sBiome.equals("*")) {
                bAddAll = true;
                break;
            }

            if (!sBiome.contains(":")) {
                biomeTags = BiomeUtils.getTags(sBiome);

                if (biomeTags == null) {
                    continue;
                }

                for (Biome extraBiome : BiomeUtils.getBiomesWithTags(biomeTags)) {
                    if (BiomeUtils.biomeListHasBiome(sBiomeAdditions, extraBiome)) {
                        continue;
                    }

                    sBiomeAdditions[iBiomeCounter] = Objects.requireNonNull(extraBiome.getRegistryName()).toString();
                    iBiomeCounter++;
                }

                continue;
            }

            if (sBiome.contains("-")) {
                overrideSplits = sBiome.split("-");

                for (int i = 0; i < overrideSplits.length; i++) {
                    if (i == 0) {
                        sBiome = overrideSplits[i];
                    } else if (i == 1) {
                        weightOverride = Integer.parseInt(overrideSplits[i]);
                    } else if (i == 2) {
                        groupCountMinOverride = Integer.parseInt(overrideSplits[i]);
                    } else if (i == 3) {
                        groupCountMaxOverride = Integer.parseInt(overrideSplits[i]);
                    }
                }
            }

            biomeTemp = Biome.REGISTRY.getObject(new ResourceLocation(sBiome));

            if (biomeTemp != null && !BiomeUtils.biomeListHasBiome(sBiomeAdditions, biomeTemp)) {
                if (weightOverride != -1) {
                    biomeWeights.put(biomeTemp, weightOverride);
                }

                if (groupCountMinOverride != -1) {
                    biomeMinGroupCounts.put(biomeTemp, groupCountMinOverride);
                }

                if (groupCountMaxOverride != -1) {
                    biomeMaxGroupCounts.put(biomeTemp, groupCountMaxOverride);
                }

                sBiomeAdditions[iBiomeCounter] = Objects.requireNonNull(biomeTemp.getRegistryName()).toString();
                iBiomeCounter++;
            }
        }

        if (bAddAll) {
            for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
                if (biome == null) {
                    continue;
                }

                if (BiomeUtils.biomeListHasBiome(sBiomeAdditions, biome)) {
                    continue;
                }

                sBiomeAdditions[iBiomeCounter++] = Objects.requireNonNull(biome.getRegistryName()).toString();
            }
        }

        iBiomeCounter = 0;

        for (String sBiome : biomesExluded) {
            if (!sBiome.contains(":")) {
                biomeTags = BiomeUtils.getTags(sBiome);

                if (biomeTags == null) {
                    continue;
                }

                for (Biome removedBiome : BiomeUtils.getBiomesWithTags(biomeTags)) {
                    if (BiomeUtils.biomeListHasBiome(sBiomeRemovals, removedBiome)) {
                        continue;
                    }

                    sBiomeRemovals[iBiomeCounter] = Objects.requireNonNull(removedBiome.getRegistryName()).toString();
                    iBiomeCounter++;
                }

                continue;
            }

            biomeTemp = Biome.REGISTRY.getObject(new ResourceLocation(sBiome));

            if (biomeTemp != null && !BiomeUtils.biomeListHasBiome(sBiomeRemovals, biomeTemp)) {
                sBiomeRemovals[iBiomeCounter] = Objects.requireNonNull(biomeTemp.getRegistryName()).toString();
                iBiomeCounter++;
            }
        }

        ArrayList<String> biomesFinal = new ArrayList<>(Arrays.asList(Arrays.stream(sBiomeAdditions).filter(value -> value != null && value.length() > 0).sorted().toArray(String[]::new)));
        biomesFinal.removeAll(Arrays.asList(Arrays.stream(sBiomeRemovals).filter(value -> value != null && value.length() > 0).sorted().toArray(String[]::new)));

        return Arrays.asList(BiomeUtils.getBiomeList(biomesFinal));
    }

    private List<String> getSpawnStructureFromConfig(String[] structureSpawns) {
        String[] splits;
        List<String> structures = new ArrayList<>();

        for (String structure : structureSpawns) {
            if (!structure.contains("-")) {
                continue;
            }

            splits = structure.split("-");

            if (splits.length < 4) {
                continue;
            }

            structures.add(splits[0]);
            structureWeights.put(splits[0], Integer.valueOf(splits[1]));
            structureMinGroupCounts.put(splits[0], Integer.valueOf(splits[2]));
            structureMaxGroupCounts.put(splits[0], Integer.valueOf(splits[3]));
        }

        return structures;
    }

    private List<Block> getBlocksAllowedFromConfig(String[] blocksAllowedStringList) {
        List<Block> allowedBlocks = new ArrayList<>();
        Block block;

        for (String blockString : blocksAllowedStringList) {
            if (blockString.contains("*")) {
                allowedBlocks.add(dummyBlock);
                return allowedBlocks;
            }

            block = Block.getBlockFromName(blockString);

            if (block == null) {
                continue;
            }

            allowedBlocks.add(block);
        }

        return allowedBlocks;
    }

    private List<Block> getBlocksExcludedFromConfig(String[] blocksExcludedStringList) {
        List<Block> excludedBlocks = new ArrayList<>();
        Block block;

        for (String blockString : blocksExcludedStringList) {
            block = Block.getBlockFromName(blockString);

            if (block == null) {
                continue;
            }

            excludedBlocks.add(block);
        }

        return excludedBlocks.isEmpty() ? null : excludedBlocks;
    }

    private List<Season> getSeasonsExcludedFromConfig(String[] seasonsExcludedStringList) {
        List<Season> excludedSeasons = new ArrayList<>();
        Season season;

        for (String seasonString : seasonsExcludedStringList) {
            season = SereneSeasonsSupport.getSeasonFromString(seasonString);

            if (season == null) {
                continue;
            }

            excludedSeasons.add(season);
        }

        return excludedSeasons.isEmpty() ? null : excludedSeasons;
    }

    private List<Season> getSeasonsAllowedFromConfig(String[] seasonsAllowedStringList) {
        List<Season> allowedSeasons = new ArrayList<>();
        Season season;

        for (String seasonString : seasonsAllowedStringList) {
            if (seasonString.contains("*")) {
                return null;
            }

            season = SereneSeasonsSupport.getSeasonFromString(seasonString);

            if (season == null) {
                continue;
            }

            allowedSeasons.add(season);
        }

        return allowedSeasons.isEmpty() ? null : allowedSeasons;
    }

    private List<PotionEffect> getPotionEffectsFromConfig(String[] potionEffectsStringList) {
        Potion potion;

        int potionDuration;
        int potionAmplifier;

        String[] potionSplit;
        List<PotionEffect> potionList = new ArrayList<>();
        PotionEffect potionEffect;

        for (String potionString : potionEffectsStringList) {
            if (potionString == null || potionString.length() < 1 || !potionString.contains("-")) {
                continue;
            }

            potionSplit = StringUtils.split(potionString, '-');

            if (potionSplit == null || potionSplit.length != 3) {
                General.debugToConsole(Level.ERROR, "Bad potion specifier: '%s' for mob: %s Use <potion>-<duration>-<amplifier>", potionString, mobResourceName);
                continue;
            }

            potion = ForgeRegistries.POTIONS.getValue(new ResourceLocation(potionSplit[0]));

            if (potion == null) {
                General.debugToConsole(Level.ERROR, "Can't find potion: %s for mob: %s", potionString);
                continue;
            }

            try {
                potionDuration = Integer.parseInt(potionSplit[1]);
                potionAmplifier = Integer.parseInt(potionSplit[2]);
            } catch (NumberFormatException e) {
                General.debugToConsole(Level.ERROR, "Bad duration %s or amplifier %s for mob: %s", potionSplit[1], potionSplit[2]);
                continue;
            }

            potionEffect = new PotionEffect(potion, potionDuration, potionAmplifier);

            if (potionList.contains(potionEffect)) {
                continue;
            }

            potionList.add(potionEffect);
        }

        return potionList;
    }

    public String[] getWorldReplacementsStringList() {
        return worldReplacementsStringList;
    }

    public String[] getStrutureReplacementsStringList() {
        return structureReplacementsStringList;
    }

    public boolean getGroupOverflowAllowed() {
        return groupOverflowAllowed;
    }

    public EntityLiving newInstance(World world) throws Exception {
        EntityEntry entry = EntityRegistry.getEntry(this.cLazz);
        if (entry != null) return (EntityLiving) entry.newInstance(world);
        return this.cLazz.getConstructor(World.class).newInstance(world);
    }
}
