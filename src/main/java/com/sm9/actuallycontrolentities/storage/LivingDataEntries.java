package com.sm9.actuallycontrolentities.storage;

import com.sm9.actuallycontrolentities.entity.LivingData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

@SuppressWarnings("ALL")
public class LivingDataEntries {
    private static final HashMap<String, LivingData> livingDataResourceNameMap = new HashMap<>();
    private static final HashMap<Class<? extends EntityLiving>, LivingData> livingDataClassMap = new HashMap<>();
    private static final ArrayList<LivingData> allLivingDataEntries = new ArrayList<>();

    public static Integer add(String mobResourceName, Class<? extends EntityLiving> entityClass, LivingData livingData) {
        if (allLivingDataEntries.contains(livingData) || !allLivingDataEntries.add(livingData)) {
            return -1;
        }

        livingDataResourceNameMap.putIfAbsent(mobResourceName, livingData);
        livingDataClassMap.putIfAbsent(entityClass, livingData);

        return allLivingDataEntries.indexOf(livingData);
    }

    public static LivingData get(String mobResourceName) {
        return livingDataResourceNameMap.get(mobResourceName);
    }

    public static LivingData get(Class<? extends EntityLiving> entityClass) {
        return livingDataClassMap.get(entityClass);
    }

    private static LivingData get(EntityLiving entityLiving) {
        if (entityLiving == null) {
            return null;
        }

        return livingDataClassMap.get(entityLiving.getClass());
    }

    public static LivingData get(Entity entity) {
        if (!(entity instanceof EntityLiving)) {
            return null;
        }

        return get((EntityLiving) entity);
    }

    public static LivingData get(EntityLivingBase entityLivingBase) {
        if (!(entityLivingBase instanceof EntityLiving)) {
            return null;
        }

        return get((EntityLiving) entityLivingBase);
    }

    public static LivingData get(Integer index) {
        return allLivingDataEntries.get(index);
    }

    public static LivingData getRandom() {
        int allLivingDataEntriesSize = size();

        if (allLivingDataEntriesSize < 1) {
            return null;
        }

        return allLivingDataEntries.get(new Random().nextInt(allLivingDataEntriesSize));
    }

    public static ArrayList<LivingData> getAll() {
        return allLivingDataEntries;
    }

    public static void sort() {
        allLivingDataEntries.sort(LivingData::compareTo);
    }

    public static int size() {
        return allLivingDataEntries.size();
    }
}
