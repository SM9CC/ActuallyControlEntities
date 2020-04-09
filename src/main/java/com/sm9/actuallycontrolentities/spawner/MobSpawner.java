package com.sm9.actuallycontrolentities.spawner;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nonnull;

@SuppressWarnings({"SpellCheckingInspection", "ConstantConditions"})
class MobSpawner implements ICapabilityProvider {
    private final MobSpawner spawner;

    MobSpawner(WorldServer worldServer) {
        this.spawner = new MobSpawner(worldServer);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability.equals(Capabilities.SPAWNER);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability.equals(Capabilities.SPAWNER)) {
            return Capabilities.SPAWNER.cast(spawner);
        }

        return null;
    }
}