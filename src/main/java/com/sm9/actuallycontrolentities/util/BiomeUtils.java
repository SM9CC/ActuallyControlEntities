package com.sm9.actuallycontrolentities.util;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.sm9.actuallycontrolentities.ActuallyControlEntities.allBiomeTags;

@SuppressWarnings({"SpellCheckingInspection", "unchecked"})
public class BiomeUtils {
    public static Biome[] getBiomeList(List<String> names) {
        Biome[] biomes = new Biome[names.size()];

        for (int i = 0; i < biomes.length; i++) {
            try {
                biomes[i] = Biome.REGISTRY.getObject(new ResourceLocation(names.get(i)));
            } catch (NullPointerException ignored) {
            }
        }

        return biomes;
    }

    public static BiomeDictionary.Type[] getTags(String... names) {
        BiomeDictionary.Type[] types = new BiomeDictionary.Type[names.length];
        Map<String, BiomeDictionary.Type> byName;

        try {
            byName = (Map<String, BiomeDictionary.Type>) allBiomeTags.get(BiomeDictionary.Type.class);

            for (int i = 0; i < names.length; i++) {
                types[i] = byName.get(names[i]);
                if (types[i] == null) {
                    return null;
                }
            }

            return types;
        } catch (IllegalArgumentException | IllegalAccessException ignored) {
        }
        return null;
    }

    public static Biome[] getBiomesWithTags(BiomeDictionary.Type[] tags) {
        LinkedList<Biome> biomeList = new LinkedList<>();
        ForgeRegistries.BIOMES.forEach(b -> {
            if (hasAnyOfTags(b, tags)) {
                biomeList.add(b);
            }
        });
        return biomeList.toArray(new Biome[0]);
    }

    public static boolean biomeListHasBiome(String[] biomeList, Biome biome) {
        if (biomeList == null || biomeList.length < 1 || biome == null) {
            return false;
        }

        for (String biomeName : biomeList) {
            if (biomeName == null || !biomeName.equals(Objects.requireNonNull(biome.getRegistryName()).toString())) {
                continue;
            }

            return true;
        }

        return false;
    }

    private static boolean hasAnyOfTags(Biome biome, BiomeDictionary.Type[] tags) {
        for (BiomeDictionary.Type tag : tags) {
            if (!BiomeDictionary.hasType(biome, tag)) {
                continue;
            }

            return true;
        }
        return false;
    }

    static boolean hasTag(Biome biome, BiomeDictionary.Type tag) {
        return BiomeDictionary.hasType(biome, tag);
    }

    public static void removeAllLegacySpawns() {
        for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
            biome.getSpawnableList(EnumCreatureType.MONSTER).clear();
            biome.getSpawnableList(EnumCreatureType.AMBIENT).clear();
            biome.getSpawnableList(EnumCreatureType.CREATURE).clear();
            biome.getSpawnableList(EnumCreatureType.WATER_CREATURE).clear();
        }
    }
}
