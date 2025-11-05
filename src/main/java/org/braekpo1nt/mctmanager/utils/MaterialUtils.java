package org.braekpo1nt.mctmanager.utils;

import org.bukkit.Material;

public class MaterialUtils {
    /**
     * @param itemType the type to check if it's a banner or not
     * @return true if the given item type is a type of banner, false if not
     */
    public static boolean isBanner(Material itemType) {
        switch (itemType) {
            case WHITE_BANNER,
                 ORANGE_BANNER,
                 MAGENTA_BANNER,
                 LIGHT_BLUE_BANNER,
                 YELLOW_BANNER,
                 LIME_BANNER,
                 PINK_BANNER,
                 GRAY_BANNER,
                 LIGHT_GRAY_BANNER,
                 CYAN_BANNER,
                 PURPLE_BANNER,
                 BLUE_BANNER,
                 BROWN_BANNER,
                 GREEN_BANNER,
                 RED_BANNER,
                 BLACK_BANNER -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }
}
