package com.enderdevelopment.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements a Roblox fighting-game style stamina bar used for dodging.
 * Dodging costs stamina and grants a short, untouchable dash that spawns a
 * "DODGE!" damage indicator on nearby enemies.
 */
public class StaminaManager {

    private static final double MAX_STAMINA = 100.0;
    private static final double DODGE_COST = 50.0;
    private static final long DODGE_COOLDOWN_TICKS = 8L * 20L;

    private final JavaPlugin plugin;

    private final Map<UUID, Double> stamina = new ConcurrentHashMap<>();
    private final Map<UUID, Long> dodgeCooldownUntil = new ConcurrentHashMap<>();

    private int taskId = -1;

    public StaminaManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {
        // Regenerate stamina over time.
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                double current = getStamina(p);
                double next = Math.min(MAX_STAMINA, current + 6.0);
                stamina.put(p.getUniqueId(), next);
            }
        }, 10L, 10L).getTaskId();
    }

    public double getStamina(Player p) {
        return stamina.getOrDefault(p.getUniqueId(), MAX_STAMINA);
    }

    public boolean isFull(Player p) {
        return getStamina(p) >= MAX_STAMINA;
    }

    public boolean canDodge(Player p) {
        if (getStamina(p) < DODGE_COST) {
            p.sendMessage(CombatMessage.prefix().append(CombatMessage.tooTired()));
            return false;
        }
        long until = dodgeCooldownUntil.getOrDefault(p.getUniqueId(), 0L);
        if (System.currentTimeMillis() < until) {
            p.sendMessage(CombatMessage.prefix().append(ComponentHelper.secondsLeft(until)));
            return false;
        }
        return true;
    }

    public boolean dodge(Player p) {
        if (!canDodge(p)) {
            return false;
        }
        stamina.put(p.getUniqueId(), Math.max(0, getStamina(p) - DODGE_COST));
        dodgeCooldownUntil.put(p.getUniqueId(), System.currentTimeMillis() + DODGE_COOLDOWN_TICKS * 50L);

        p.setVelocity(p.getLocation().getDirection().multiply(1.5).setY(0.6));
        p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 10, 4, false, false, false));
        return true;
    }

    public void shutdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }
}
