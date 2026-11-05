package com.enderdevelopment.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main instance;
    private CombatManager combatManager;
    private StaminaManager staminaManager;
    private DamageIndicator damageIndicator;
    private WeaponRegistry weaponRegistry;
    private PlayerStats playerStats;

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        weaponRegistry = new WeaponRegistry(this);
        playerStats = new PlayerStats(this);
        damageIndicator = new DamageIndicator(this, playerStats);
        staminaManager = new StaminaManager(this);
        combatManager = new CombatManager(this, playerStats, damageIndicator);

        Bukkit.getPluginManager().registerEvents(new CombatListener(this, combatManager, staminaManager, weaponRegistry, playerStats), this);

        if (this.getCommand("combat") != null) {
            this.getCommand("combat").setExecutor(new CombatCommand(this, combatManager, weaponRegistry, playerStats));
        }
        if (this.getCommand("giveweapon") != null) {
            this.getCommand("giveweapon").setExecutor(new CombatCommand(this, combatManager, weaponRegistry, playerStats));
        }

        getLogger().info("EnderCombat enabled - Roblox style combat system loaded.");
    }

    @Override
    public void onDisable() {
        if (playerStats != null) {
            playerStats.shutdown();
        }
        if (staminaManager != null) {
            staminaManager.shutdown();
        }
        if (damageIndicator != null) {
            damageIndicator.shutdown();
        }
        getLogger().info("EnderCombat disabled.");
    }

    public static Main getInstance() {
        return instance;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public StaminaManager getStaminaManager() {
        return staminaManager;
    }

    public DamageIndicator getDamageIndicator() {
        return damageIndicator;
    }

    public WeaponRegistry getWeaponRegistry() {
        return weaponRegistry;
    }

    public PlayerStats getPlayerStats() {
        return playerStats;
    }
}
