package com.sm9.actuallycontrolentities.storage;

import com.sm9.actuallycontrolentities.world.DimensionData;
import net.minecraft.entity.EntityLiving;

import java.util.ArrayList;
import java.util.HashMap;

import static com.sm9.actuallycontrolentities.util.EntityUtils.IN_FIRE;
import static com.sm9.actuallycontrolentities.util.EntityUtils.IN_LAVA;

@SuppressWarnings("ALL")
public class MonsterDimensionDataMaps {
    private static final HashMap<Integer, ArrayList<DimensionData>> groundDataMap = new HashMap<>();
    private static final HashMap<Integer, ArrayList<DimensionData>> waterDataMap = new HashMap<>();
    private static final HashMap<Integer, ArrayList<DimensionData>> fireDataMap = new HashMap<>();
    private static final HashMap<Integer, ArrayList<DimensionData>> lavaDataMap = new HashMap<>();

    public static Integer put(Integer dimensionId, EntityLiving.SpawnPlacementType spawnPlacementType, DimensionData dimensionData) {
        switch (spawnPlacementType) {
            case ON_GROUND: {
                ArrayList<DimensionData> dataList = groundDataMap.computeIfAbsent(dimensionId, k -> new ArrayList<>());

                int index = dataList.indexOf(dimensionData);
                return index != -1 ? index : dataList.add(dimensionData) ? dataList.indexOf(dimensionData) : -1;
            }
            case IN_WATER: {
                ArrayList<DimensionData> dataList = waterDataMap.computeIfAbsent(dimensionId, k -> new ArrayList<>());

                int index = dataList.indexOf(dimensionData);
                return index != -1 ? index : dataList.add(dimensionData) ? dataList.indexOf(dimensionData) : -1;
            }
            default: {
                if (spawnPlacementType.equals(IN_FIRE)) {
                    ArrayList<DimensionData> dataList = fireDataMap.computeIfAbsent(dimensionId, k -> new ArrayList<>());

                    int index = dataList.indexOf(dimensionData);
                    return index != -1 ? index : dataList.add(dimensionData) ? dataList.indexOf(dimensionData) : -1;
                } else if (spawnPlacementType.equals(IN_LAVA)) {
                    ArrayList<DimensionData> dataList = lavaDataMap.computeIfAbsent(dimensionId, k -> new ArrayList<>());

                    int index = dataList.indexOf(dimensionData);
                    return index != -1 ? index : dataList.add(dimensionData) ? dataList.indexOf(dimensionData) : -1;
                }
            }
        }

        return -1;
    }

    public static ArrayList<DimensionData> getPlacementData(Integer dimensionId, EntityLiving.SpawnPlacementType spawnPlacementType) {
        switch (spawnPlacementType) {
            case ON_GROUND: {
                return groundDataMap.get(dimensionId);
            }
            case IN_WATER: {
                return waterDataMap.get(dimensionId);
            }
            default: {
                if (spawnPlacementType.equals(IN_FIRE)) {
                    return fireDataMap.get(dimensionId);
                } else if (spawnPlacementType.equals(IN_LAVA)) {
                    return lavaDataMap.get(dimensionId);
                }
            }
        }

        return null;
    }
}
