package com.sm9.actuallycontrolentities.command;

import com.sm9.actuallycontrolentities.handler.Initializers;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings("SpellCheckingInspection")
public class Reload extends CommandBase {
    @Nonnull
    @Override
    public String getName() {
        return "acereload";
    }

    @Nonnull
    @Override
    public String getUsage(@Nonnull ICommandSender commandSender) {
        return "acereload";
    }

    @Override
    public void execute(@Nonnull MinecraftServer localServer, ICommandSender commandSender, @Nonnull String[] sArgs) {
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> Initializers.loadConfigs(commandSender));
    }
}