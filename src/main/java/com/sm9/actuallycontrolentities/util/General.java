package com.sm9.actuallycontrolentities.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.Level;

import java.util.Formatter;

import static com.sm9.actuallycontrolentities.ActuallyControlEntities.ACELogger;

@SuppressWarnings("unused")
public class General {
    public static void printToPlayer(EntityPlayer entityPlayer, String sFormat, Object... oArgs) {
        String sMessage = new Formatter().format(sFormat, oArgs).toString();
        entityPlayer.sendMessage(new TextComponentString("[ACE] " + sMessage));
    }

    public static void debugToConsole(Level logLevel, String message, Object... oArgs) {
        ACELogger.log(logLevel, new Formatter().format(message, oArgs).toString());
    }

    public static String capitalizeString(String str) {
        if (str == null || str.length() == 0) {
            return "";
        }

        if (str.length() == 1) {
            return str.toUpperCase();
        }

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}