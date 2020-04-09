package com.sm9.actuallycontrolentities;

import com.sm9.actuallycontrolentities.block.DummyBlock;
import com.sm9.actuallycontrolentities.common.config.MainConfig;
import com.sm9.actuallycontrolentities.handler.EventHandler;
import net.minecraft.profiler.Profiler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;

@SuppressWarnings("SpellCheckingInspection")
@Mod(modid = Constants.MOD_ID, name = Constants.MOD_NAME, version = Constants.MOD_VERSION, dependencies = Constants.DEPENDENCIES, acceptedMinecraftVersions = Constants.MINECRAFT_VERSION, acceptableRemoteVersions = "*")

public final class ActuallyControlEntities {
    public static File rootConfigDir;
    public static Logger ACELogger;
    public static Field allBiomeTags;
    public static int totalBiomeCount;
    public static MainConfig mainConfig;
    public static Profiler aceProfiler;

    // Dummy Block
    public static DummyBlock dummyBlock;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        EventHandler.preInit(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        EventHandler.postInit();
    }

    @Mod.EventHandler
    public void onWorldLoad(FMLServerStartingEvent event) {
        EventHandler.onWorldLoad(event);
    }
}