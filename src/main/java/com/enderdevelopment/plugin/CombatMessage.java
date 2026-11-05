package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Centralises all combat-related chat messages. Roblox style: thrilling,
 * arcade-y phrasing with red/numeric emphasis.
 */
public final class CombatMessage {

    private CombatMessage() {
    }

    public static Component prefix() {
        return Component.text("[Combat] ").color(NamedTextColor.DARK_RED);
    }

    public static Component inCombat() {
        return Component.text("You are now in combat!").color(NamedTextColor.RED);
    }

    public static Component noLongerInCombat() {
        return Component.text("You are no longer in combat.").color(NamedTextColor.GREEN);
    }

    public static Component combo(int count) {
        return Component.text("COMBO x" + count + "!").color(NamedTextColor.GOLD);
    }

    public static Component kill(int streak) {
        return Component.text("KILL STREAK x" + streak + "!").color(NamedTextColor.RED);
    }

    public static Component playerKilledBy(String killer, String victim) {
        return Component.text(killer).color(NamedTextColor.RED)
                .append(Component.text(" eliminated ").color(NamedTextColor.GRAY))
                .append(Component.text(victim).color(NamedTextColor.WHITE));
    }

    public static Component dodgeReady() {
        return Component.text("Stamina full. You can dodge!").color(NamedTextColor.GREEN);
    }

    public static Component tooTired() {
        return Component.text("Not enough stamina to dodge!").color(NamedTextColor.RED);
    }

    public static Component dodgeUsed() {
        return Component.text("DODGE!").color(NamedTextColor.AQUA);
    }

    public static Component parry() {
        return Component.text("PARRY!").color(NamedTextColor.YELLOW);
    }

    public static Component blocked() {
        return Component.text("BLOCKED!").color(NamedTextColor.YELLOW);
    }
}
