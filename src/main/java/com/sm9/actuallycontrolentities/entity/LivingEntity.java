package com.sm9.actuallycontrolentities.entity;

import com.sm9.actuallycontrolentities.util.General;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

@SuppressWarnings("unused")
public class LivingEntity implements Comparable<LivingEntity> {
    private final Class<? extends EntityLiving> entityClass;
    private final ResourceLocation resourceLocation;
    private final String resourceName;
    private final String modName;
    private final String name;

    public LivingEntity(Class<? extends EntityLiving> entityClass, ResourceLocation resourceLocation, String name) {
        this.entityClass = entityClass;
        this.resourceLocation = resourceLocation;
        this.resourceName = resourceLocation.toString();
        this.modName = General.capitalizeString(resourceLocation.getNamespace());

        while (name.contains(":")) {
            name = name.split(":")[1];
        }

        this.name = General.capitalizeString(name);
    }

    public Class<? extends EntityLiving> getEntityClass() {
        return entityClass;
    }

    public ResourceLocation getResourceLocation() {
        return resourceLocation;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getName() {
        return name;
    }

    public String getModName() {
        return modName;
    }

    @Override
    public int compareTo(LivingEntity o) {
        int firstCompare = modName.compareTo(o.modName);
        return firstCompare == 0 ? name.compareTo(o.name) : firstCompare;
    }
}
