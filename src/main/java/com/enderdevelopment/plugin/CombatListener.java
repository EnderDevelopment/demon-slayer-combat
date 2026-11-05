package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Glues all the combat pieces to the game: registers melee hits, fires the
 * dodge, keeps combat tagged players from leaving without consent, and
 * broadcasts kill messages + streaks.
 */
public class CombatListener implements Listener {

    private final JavaPlugin plugin;
    private final CombatManager combatManager;
    private final StaminaManager staminaManager;
    private final WeaponRegistry weaponRegistry;
    private final PlayerStats playerStats;

    public CombatListener(JavaPlugin plugin, CombatManager combatManager, StaminaManager staminaManager,
                          WeaponRegistry weaponRegistry, PlayerStats playerStats) {
        this.plugin = plugin;
        this.combatManager = combatManager;
        this.staminaManager = staminaManager;
        this.weaponRegistry = weaponRegistry;
        this.playerStats = playerStats;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMeleeHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }

        // We control melee damage ourselves: cancel the vanilla event and apply
        // our Roblox-style calculation directly. The PlayerDeathEvent handler
        // below deals with streaks and combo resets on a real kill.
        event.setCancelled(true);

        combatManager.handleHit(event, attacker, victim);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDodge(PlayerToggleSprintEvent event) {
        // Holding right-click while sprinting is the Roblox dodge gesture.
        Player p = event.getPlayer();
        if (!event.isSprinting() || !p.isSneaking()) {
            return;
        }
        if (staminaManager.dodge(p)) {
            p.sendMessage(CombatMessage.prefix().append(CombatMessage.dodgeUsed()));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onParry(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) {
            return;
        }
        Player p = event.getPlayer();
        if (!playerStats.isInCombat(p)) {
            return;
        }
        // A parry winds up a brief immunity window.
        p.setInvulnerable(false);
        p.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.RESISTANCE, 6, 4, false, false, false));
        p.sendMessage(CombatMessage.prefix().append(CombatMessage.parry()));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        playerStats.resetCombo(victim.getUniqueId());
        playerStats.resetKillStreak(victim.getUniqueId());

        Player killer = victim.getKiller() instanceof Player k ? k : null;
        if (killer != null && killer != victim) {
            int streak = playerStats.addKill(killer.getUniqueId());
            killer.sendMessage(CombatMessage.prefix().append(CombatMessage.kill(streak)));
            Bukkit.broadcast(CombatMessage.playerKilledBy(killer.getName(), victim.getName()));
        } else if (playerStats.isInCombat(victim)) {
            Bukkit.broadcast(CombatMessage.prefix().append(
                    Component.text(victim.getName() + " fell in combat.").color(NamedTextColor.GRAY)));
        }
        playerStats.resetCombo(killer != null ? killer.getUniqueId() : victim.getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if (playerStats.isInCombat(p)) {
            p.setHealth(0.0);
        }
    }

    public WeaponRegistry getWeaponRegistry() {
        return weaponRegistry;
    }
}
