package org.plambert.oneShot;

import org.plambert.oneShot.listeners.InventoryListener;
import org.plambert.oneShot.listeners.PlayerJoinListener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.plambert.oneShot.listeners.PlayerLeaveListener;

public final class OneShot extends JavaPlugin {

    @Override
    public void onEnable() {

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLeaveListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);

        getLogger().info("OneShot has been enabled!");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
