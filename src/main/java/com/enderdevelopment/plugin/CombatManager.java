package com.enderdevelopment.plugin;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Core Roblox-style combat logic: damage computation, combo chains, kill
 * streaks and the combat tag that prevents logging out mid-fight.
 *
 * A combo chain grows with each rapid melee hit; after the third consecutive
 * hit the damage multiplier ramps up (x1, x1.2, x1.5, x2), matching the hype
 * of a Roblox fighting-game combo meter.
 */
public class CombatManager {

    private static final double BASE_FIST_DAMAGE = 3.0;
    private static final long COMBAT_TAG_TICKS = 10L * 20L;

    private final JavaPlugin plugin;
    private final PlayerStats playerStats;
    private final DamageIndicator damageIndicator;
    private final WeaponRegistry weaponRegistry;

    private final Map<UUID, Long> swingCooldownUntil = new ConcurrentHashMap<>();

    public CombatManager(JavaPlugin plugin, PlayerStats playerStats, DamageIndicator damageIndicator) {
        this.plugin = plugin;
        this.playerStats = playerStats;
        this.damageIndicator = damageIndicator;
        this.weaponRegistry = new WeaponRegistry(plugin);
    }

    /**
     * Handles a melee hit. Applies the computed Roblox-style damage to the
     * victim and spawns a floating damage number.
     */
    public void handleHit(EntityDamageByEntityEvent event, Player attacker, LivingEntity victim) {
        // Combat tag the victim.
        if (victim instanceof Player victimPlayer) {
            playerStats.tagCombat(victimPlayer, COMBAT_TAG_TICKS);
        }
        playerStats.tagCombat(attacker, COMBAT_TAG_TICKS);

        // Track last hit for damage-rider detection.
        playerStats.setLastHitId(attacker.getUniqueId(), victim.getEntityId());

        // Build the combo chain.
        playerStats.addCombo(attacker.getUniqueId());
        int combo = playerStats.getCombo(attacker.getUniqueId());
        if (combo > 1) {
            attacker.sendMessage(CombatMessage.prefix().append(CombatMessage.combo(combo)));
        }

        // Compute damage. If the attacker holds a registered combat weapon, use
        // its stats with the combo multiplier; otherwise use base fist damage.
        double baseDamage;
        double knockback;
        boolean comboBoost;

        String weaponId = weaponRegistry.getWeaponId(attacker.getInventory().getItemInMainHand());
        WeaponRegistry.Weapon weapon = weaponId != null ? weaponRegistry.getWeapon(weaponId) : null;

        if (weapon != null) {
            baseDamage = weapon.damage();
            knockback = weapon.knockback();
            comboBoost = weapon.comboBoost();
        } else {
            baseDamage = BASE_FIST_DAMAGE;
            knockback = 0.4;
            comboBoost = false;
        }

        double multiplier = comboBoost ? comboMultiplier(combo) : 1.0;
        double damage = baseDamage * multiplier;

        // Knockback scaled to the weapon.
        victim.setVelocity(victim.getLocation().getDirection().multiply(-1)
                .multiply(knockback).setY(0.4));

        // Apply real damage and show the floating indicator.
        victim.damage(damage, attacker);
        damageIndicator.record(victim.getUniqueId(), damage, "normal");
        damageIndicator.show(victim);
    }

    /**
     * A chain ramp matching a fighting-game combo meter:
     * hits 1-2: x1, hit 3-4: x1.2, hit 5-6: x1.5, 7+: x2.
     */
    private double comboMultiplier(int combo) {
        if (combo >= 7) {
            return 2.0;
        }
        if (combo >= 5) {
            return 1.5;
        }
        if (combo >= 3) {
            return 1.2;
        }
        return 1.0;
    }

    public boolean isOnSwingCooldown(Player p) {
        Long until = swingCooldownUntil.get(p.getUniqueId());
        if (until == null) {
            return false;
        }
        if (System.currentTimeMillis() < until) {
            return true;
        }
        swingCooldownUntil.remove(p.getUniqueId());
        return false;
    }

    public void startSwingCooldown(Player p) {
        swingCooldownUntil.put(p.getUniqueId(), System.currentTimeMillis() + 250L);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}
