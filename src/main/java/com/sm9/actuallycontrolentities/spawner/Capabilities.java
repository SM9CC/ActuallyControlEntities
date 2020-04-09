package com.sm9.actuallycontrolentities.spawner;

import com.sm9.actuallycontrolentities.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

@SuppressWarnings({"SpellCheckingInspection", "ConstantConditions", "unchecked"})
public
class Capabilities {
    @CapabilityInject(MobSpawner.class)
    static final Capability<MobSpawner> SPAWNER = null;

    private static boolean isValid() {
        return Capabilities.SPAWNER != null;
    }

    private static boolean hasCapability(ICapabilityProvider entry) {
        return entry != null && isValid() && entry.hasCapability(Capabilities.SPAWNER, null);
    }

    @Nullable
    public static <T> T getCapability(ICapabilityProvider entry) {
        return hasCapability(entry) ? entry.getCapability((Capability<T>) Capabilities.SPAWNER, null) : null;
    }

    @SubscribeEvent
    public void onAttachWorldCapabilities(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();

        if (world instanceof WorldServer) {
            WorldServer worldServer = (WorldServer) world;
            event.addCapability(new ResourceLocation(Constants.MOD_ID, "spawner"), new MobSpawner(worldServer));
        }
    }
}