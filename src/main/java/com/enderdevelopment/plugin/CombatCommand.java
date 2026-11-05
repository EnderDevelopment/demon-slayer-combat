package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Handles /combat and /giveweapon.
 */
public class CombatCommand implements CommandExecutor {

    private final Main plugin;
    private final CombatManager combatManager;
    private final WeaponRegistry weaponRegistry;
    private final PlayerStats playerStats;

    public CombatCommand(Main plugin, CombatManager combatManager, WeaponRegistry weaponRegistry, PlayerStats playerStats) {
        this.plugin = plugin;
        this.combatManager = combatManager;
        this.weaponRegistry = weaponRegistry;
        this.playerStats = playerStats;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        String cmd = command.getName();

        if ("giveweapon".equals(cmd)) {
            return giveWeapon(sender, args);
        }
        return combat(sender, args);
    }

    private boolean giveWeapon(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive weapons.");
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /giveweapon <name>").color(NamedTextColor.RED));
            player.sendMessage(Component.text("Available: " + String.join(", ", weaponRegistry.getWeapons().keySet()))
                    .color(NamedTextColor.GRAY));
            return true;
        }

        ItemStack item = weaponRegistry.createWeapon(args[0]);
        if (item == null) {
            player.sendMessage(Component.text("Unknown weapon: " + args[0]).color(NamedTextColor.RED));
            player.sendMessage(Component.text("Available: " + String.join(", ", weaponRegistry.getWeapons().keySet()))
                    .color(NamedTextColor.GRAY));
            return true;
        }

        player.getInventory().addItem(item);
        player.sendMessage(Component.text("Received " + args[0]).color(NamedTextColor.GREEN));
        return true;
    }

    private boolean combat(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /combat <reload|status>").color(NamedTextColor.GRAY));
            return true;
        }
        if ("reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("endercombat.reload")) {
                sender.sendMessage(Component.text("No permission.").color(NamedTextColor.RED));
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage(Component.text("Configuration reloaded.").color(NamedTextColor.GREEN));
            return true;
        }
        if ("status".equalsIgnoreCase(args[0]) && sender instanceof Player player) {
            String combatState = playerStats.isInCombat(player)
                    ? "In combat (" + playerStats.getRemainingCombatSeconds(player) + "s)"
                    : "Out of combat";
            sender.sendMessage(Component.text("Combat tag: " + combatState).color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Kill streak: " + playerStats.getKillStreak(player.getUniqueId()))
                    .color(NamedTextColor.GOLD));
            return true;
        }
        sender.sendMessage(Component.text("Usage: /combat <reload|status>").color(NamedTextColor.GRAY));
        return true;
    }
}
