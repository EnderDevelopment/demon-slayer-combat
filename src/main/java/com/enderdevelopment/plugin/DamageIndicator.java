package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Renders floating damage numbers above hit enemies, in the arcade style of
 * Roblox fighting games ("-50", "PARRY!", "COMBO x3"). Each indicator is a
 * small, invisible, non-interactive armor stand that self-destructs.
 */
public class DamageIndicator {

    private final JavaPlugin plugin;
    private final PlayerStats playerStats;

    private final Map<UUID, Double> pendingDamage = new ConcurrentHashMap<>();
    private final Map<UUID, String> pendingType = new ConcurrentHashMap<>();

    public DamageIndicator(JavaPlugin plugin, PlayerStats playerStats) {
        this.plugin = plugin;
        this.playerStats = playerStats;
    }

    /**
     * Records damage that still needs to be shown. The listener applies the
     * actual health reduction, then calls {@link #show(org.bukkit.entity.LivingEntity)}.
     */
    public void record(UUID victimId, double damage, String type) {
        pendingDamage.put(victimId, damage);
        pendingType.put(victimId, type);
    }

    public void show(org.bukkit.entity.LivingEntity victim) {
        Double damage = pendingDamage.remove(victim.getUniqueId());
        String type = pendingType.remove(victim.getUniqueId());
        if (damage == null) {
            return;
        }

        Component text;
        if ("parry".equals(type)) {
            text = Component.text("PARRY!").color(NamedTextColor.YELLOW);
        } else if ("dodge".equals(type)) {
            text = Component.text("DODGE!").color(NamedTextColor.AQUA);
        } else {
            text = Component.text("-" + format(damage)).color(NamedTextColor.RED);
        }

        spawnIndicator(victim, text);
    }

    private String format(double damage) {
        if (damage == Math.floor(damage)) {
            return String.valueOf((int) damage);
        }
        return String.format("%.1f", damage);
    }

    private void spawnIndicator(org.bukkit.entity.LivingEntity target, Component text) {
        Location loc = target.getLocation().clone().add(0, target.getHeight() + 0.5, 0);

        final ArmorStand stand = (ArmorStand) target.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setInvulnerable(true);
        stand.setCollidable(false);
        stand.setSilent(true);
        stand.setSmall(true);
        stand.customName(text);
        stand.setCustomNameVisible(true);

        // Float upward, then expire.
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;
                stand.teleport(stand.getLocation().add(0, 0.03, 0));
                if (ticks >= 25 || !stand.isValid()) {
                    stand.remove();
                }
            }
        }, 0L, 1L);
    }

    public void shutdown() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (org.bukkit.entity.Entity e : p.getNearbyEntities(50, 50, 50)) {
                if (e instanceof ArmorStand && e.isCustomNameVisible() && e.getCustomName() != null) {
                    String name = e.getCustomName();
                    if (name.startsWith("-") || name.endsWith("!") || name.startsWith("PARRY") || name.startsWith("DODGE") || name.startsWith("COMBO")) {
                        e.remove();
                    }
                }
            }
        }
    }
}
