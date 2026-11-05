package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Defines the catalog of custom Roblox-style weapons. Damage, knockback and
 * combo multipliers are stored in the item's persistent data, so any stack
 * handed out carries its full combat stats.
 */
public class WeaponRegistry {

    private final JavaPlugin plugin;
    private final Map<String, Weapon> weapons = new LinkedHashMap<>();

    public record Weapon(String id, String displayName, Material material, double damage,
                         double knockback, boolean comboBoost, String lore) {
    }

    public WeaponRegistry(JavaPlugin plugin) {
        this.plugin = plugin;

        weapons.put("starter", new Weapon(
                "starter", "Starter Sword", Material.WOODEN_SWORD, 6.0, 0.8, false,
                "The classic blade. Every fighter starts somewhere."));

        weapons.put("katana", new Weapon(
                "katana", "Roblox Katana", Material.IRON_SWORD, 25.0, 1.2, true,
                "Mastery of the blade. Combo boosts multiply damage."));

        weapons.put("hyperlaser", new Weapon(
                "hyperlaser", "Hyper Laser", Material.NETHERITE_SWORD, 50.0, 1.6, true,
                "Ultra high-damage blade."));

        weapons.put("ninja", new Weapon(
                "ninja", "Ninja Sword", Material.DIAMOND_SWORD, 18.0, 1.0, true,
                "Quick and light. Favours rapid combos."));

        weapons.put("admin", new Weapon(
                "admin", "Admin Sword", Material.GOLDEN_SWORD, 100.0, 2.5, true,
                "One-shot power. Use with caution."));
    }

    /** Builds a full ItemStack for a weapon id. Returns null for an unknown id. */
    public ItemStack createWeapon(String id) {
        Weapon w = weapons.get(id);
        if (w == null) {
            return null;
        }
        ItemStack item = new ItemStack(w.material());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        meta.displayName(Component.text(w.displayName()).color(NamedTextColor.GOLD));

        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, "combat_weapon");
        data.set(key, PersistentDataType.STRING, w.id());
        data.set(new NamespacedKey(plugin, "weapon_damage"), PersistentDataType.DOUBLE, w.damage());
        data.set(new NamespacedKey(plugin, "weapon_knockback"), PersistentDataType.DOUBLE, w.knockback());
        data.set(new NamespacedKey(plugin, "weapon_combo"), PersistentDataType.BYTE, (byte) (w.comboBoost() ? 1 : 0));

        meta.lore(java.util.List.of(
                Component.text("Damage: " + format(w.damage())).color(NamedTextColor.RED),
                Component.text("Knockback: " + format(w.knockback())).color(NamedTextColor.DARK_AQUA),
                Component.text(w.comboBoost() ? "Combo boost: enabled" : "Combo boost: none").color(NamedTextColor.YELLOW),
                Component.text(w.lore()).color(NamedTextColor.GRAY)));

        // A sharp visual cue without relying on vanilla enchant names.
        Enchantment sword = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("sharpness"));
        if (sword != null) {
            meta.addEnchant(sword, Math.max(1, (int) Math.min(5, w.damage() / 4)), true);
        }

        item.setItemMeta(meta);
        return item;
    }

    /** Reads the weapon id from an item's persistent data. Null when not a combat weapon. */
    public String getWeaponId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        return data.get(new NamespacedKey(plugin, "combat_weapon"), PersistentDataType.STRING);
    }

    public Weapon getWeapon(String id) {
        return weapons.get(id);
    }

    public Map<String, Weapon> getWeapons() {
        return weapons;
    }

    private String format(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }
        return String.format("%.1f", value);
    }
}
