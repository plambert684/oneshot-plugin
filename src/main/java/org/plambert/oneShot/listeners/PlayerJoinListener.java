package org.plambert.oneShot.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

import org.plambert.oneShot.OneShot;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerJoinListener implements Listener {

    private final OneShot plugin;

    public PlayerJoinListener(OneShot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Charger les données du joueur (kills totaux) au join
        plugin.getPlayerManager().loadPlayerData(player);
        
        if (plugin.getConfig().getBoolean("bungeecord")) {
            // Mode BungeeCord: on rejoint la première arène disponible (qui devrait être la seule)
            String arenaName = "solo";
            if (plugin.getGameManager().getArenas().isEmpty()) {
                plugin.getGameManager().createArena("solo");
            } else {
                arenaName = plugin.getGameManager().getArenas().keySet().iterator().next();
            }
            plugin.getGameManager().joinArena(player, arenaName);
            event.joinMessage(null); // Pas de message de bienvenue global

            Component emptySub = Component.empty();
            Title welcomeTitle = Title.title(
                Component.text("OneShot", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true),
                emptySub
            );
            player.showTitle(welcomeTitle);
        } else {
            // Mode Multi-Arena: TP au spawn du serveur (pas de lobby global)
            player.getInventory().clear();
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
            event.joinMessage(null); // Retirer le message de bienvenue sur le serveur
        }
    }


}
