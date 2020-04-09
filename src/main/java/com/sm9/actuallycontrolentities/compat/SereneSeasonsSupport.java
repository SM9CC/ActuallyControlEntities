package com.sm9.actuallycontrolentities.compat;

import net.minecraft.world.World;
import sereneseasons.api.season.ISeasonState;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

@SuppressWarnings("unused")
public class SereneSeasonsSupport {
    public static boolean modAvailable;

    public static boolean isSpring(World world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.SPRING.equals(seasonState.getSeason());
    }

    public static boolean isSummer(World world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.SUMMER.equals(seasonState.getSeason());
    }

    public static boolean isWinter(World world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.WINTER.equals(seasonState.getSeason());
    }

    public static boolean isAutumn(World world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return Season.AUTUMN.equals(seasonState.getSeason());
    }

    public static Season getCurrentSeason(World world) {
        ISeasonState seasonState = SeasonHelper.getSeasonState(world);
        return seasonState.getSeason();
    }

    public static String getSeasonAsString(Season season) {
        switch (season) {
            case SPRING: {
                return "SPRING";
            }
            case SUMMER: {
                return "SUMMER";
            }
            case WINTER: {
                return "WINTER";
            }
            case AUTUMN: {
                return "AUTUMN";
            }
        }

        return null;
    }

    public static Season getSeasonFromString(String season) {
        switch (season) {
            case "SPRING": {
                return Season.SPRING;
            }
            case "SUMMER": {
                return Season.SUMMER;
            }
            case "WINTER": {
                return Season.WINTER;
            }
            case "AUTUMN": {
                return Season.AUTUMN;
            }
        }

        return null;
    }
}

