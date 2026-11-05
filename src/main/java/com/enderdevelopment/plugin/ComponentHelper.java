package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Small helpers for building chat components used across the combat system.
 */
public final class ComponentHelper {

    private ComponentHelper() {
    }

    public static Component secondsLeft(long untilMillis) {
        long seconds = Math.max(0, (untilMillis - System.currentTimeMillis()) / 1000L + 1);
        return Component.text("Not ready. " + seconds + "s remaining.").color(NamedTextColor.RED);
    }
}
