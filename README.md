# Demon Slayer Combat Plugin

A complete Roblox-style combat system for Minecraft Paper servers. This plugin transforms your server's PvP experience with custom weapons, combo multipliers, stamina-based dodging, parrying, combat tagging, kill streaks, and floating damage indicators — all in the energetic arcade style of fighting games.

## Install

1. Download the `plugin.jar` file from the releases section.
2. Place the JAR file into your server's `plugins` folder.
3. Restart your server (or run `/reload confirm` if using Paper).
4. Verify the plugin loaded by checking the console for `EnderCombat enabled - Roblox style combat system loaded.`

## Use

### Commands

- `/combat status` – Check your current combat state and kill streak.
- `/combat reload` – Reload the configuration file (requires `endercombat.reload` permission).
- `/giveweapon <name>` – Give yourself a custom combat weapon (requires `endercombat.giveweapon` permission). Aliases: `/weapon`.
- `/weapon <name>` – Same as `/giveweapon`.

### Available Weapons

- `starter` – Starter Sword (Wooden Sword, 6 damage, 0.8 knockback)
- `katana` – Roblox Katana (Iron Sword, 25 damage, 1.2 knockback, combo boost)
- `hyperlaser` – Hyper Laser (Netherite Sword, 50 damage, 1.6 knockback, combo boost)
- `ninja` – Ninja Sword (Diamond Sword, 18 damage, 1.0 knockback, combo boost)
- `admin` – Admin Sword (Golden Sword, 100 damage, 2.5 knockback, combo boost)

### Combat Mechanics

1. **Custom Weapons** – All weapons have unique damage, knockback, and combo boost stats stored on the item itself.
2. **Combo System** – Chain hits within 3 seconds to build a combo. Combo multipliers: x1 → x1.2 → x1.5 → x2. Combo weapons boost this multiplier.
3. **Stamina & Dodging** – Your stamina regenerates over time. Sprint while sneaking to dodge, which costs 50 stamina and grants temporary invulnerability.
4. **Parrying** – Use your off-hand while in combat to parry, granting brief damage resistance.
5. **Combat Tagging** – After landing or taking a hit, you're tagged for 10 seconds. Logging out while tagged kills your character.
6. **Kill Streaks** – Each kill increases your streak, broadcast to the entire server.
7. **Damage Indicators** – Floating numbers show damage dealt, along with "PARRY!", "DODGE!", and combo notifications.

### Config

Configuration is stored in `plugins/EnderCombat/config.yml`. Adjust combat-tag duration, stamina regeneration rates, dodge cooldowns, combo windows, and more. The `spacebar.enabled` option is prepared for future dodge-on-space functionality.

### Permissions

- `endercombat.reload` – Default: op – Allows reloading the combat config.
- `endercombat.giveweapon` – Default: op – Allows giving custom weapons.

### Requirements

- Paper 1.21.x server (API 1.21)
- Java 21 or higher

---

More Minecraft plugin projects by EnderDevelopment: https://github.com/EnderDevelopment

Project page: https://dash.enderdevelopment.com/community/c8ce392b-8dbf-4d67-8d47-200b1367e586?utm_source=github&utm_medium=readme&utm_campaign=auto_publish&utm_content=c8ce392b-8dbf-4d67-8d47-200b1367e586
