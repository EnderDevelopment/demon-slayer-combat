package com.enderdevelopment.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds per-player runtime data such as the combo counter, kill streak and
 * combat-tag state. It also runs the periodic task that expires combat tags.
 */
public class PlayerStats {

    private final JavaPlugin plugin;

    private final Map<UUID, Integer> comboCount = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> killStreak = new ConcurrentHashMap<>();
    private final Map<UUID, Long> combatTagUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> lastHitId = new ConcurrentHashMap<>();
    private final Map<UUID, Long> comboExpire = new ConcurrentHashMap<>();

    private int taskId = -1;

    public PlayerStats(JavaPlugin plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            for (UUID uuid : combatTagUntil.keySet()) {
                if (combatTagUntil.get(uuid) < now) {
                    combatTagUntil.remove(uuid);
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) {
                        p.sendMessage(CombatMessage.prefix().append(CombatMessage.noLongerInCombat()));
                    }
                }
            }
            for (UUID uuid : comboExpire.keySet()) {
                if (comboExpire.get(uuid) < now) {
                    comboExpire.remove(uuid);
                    comboCount.remove(uuid);
                }
            }
        }, 20L, 20L).getTaskId();
    }

    public void shutdown() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    public void addCombo(UUID uuid) {
        comboCount.merge(uuid, 1, Integer::sum);
        comboExpire.put(uuid, System.currentTimeMillis() + 3000L);
    }

    public int getCombo(UUID uuid) {
        return comboCount.getOrDefault(uuid, 0);
    }

    public void resetCombo(UUID uuid) {
        comboCount.remove(uuid);
        comboExpire.remove(uuid);
    }

    public int addKill(UUID uuid) {
        return killStreak.merge(uuid, 1, Integer::sum);
    }

    public int getKillStreak(UUID uuid) {
        return killStreak.getOrDefault(uuid, 0);
    }

    public void resetKillStreak(UUID uuid) {
        killStreak.remove(uuid);
    }

    public void setLastHitId(UUID uuid, int entityId) {
        lastHitId.put(uuid, entityId);
    }

    public int getLastHitId(UUID uuid) {
        return lastHitId.getOrDefault(uuid, -1);
    }

    public boolean isInCombat(Player player) {
        Long until = combatTagUntil.get(player.getUniqueId());
        return until != null && until > System.currentTimeMillis();
    }

    public void tagCombat(Player player, long durationTicks) {
        long until = System.currentTimeMillis() + durationTicks * 50L;
        combatTagUntil.put(player.getUniqueId(), until);
        if (durationTicks > 0) {
            player.sendMessage(CombatMessage.prefix().append(CombatMessage.inCombat()));
        }
    }

    public int getRemainingCombatSeconds(Player player) {
        Long until = combatTagUntil.get(player.getUniqueId());
        if (until == null) {
            return 0;
        }
        return (int) Math.max(0, (until - System.currentTimeMillis()) / 1000L + 1);
    }
}
