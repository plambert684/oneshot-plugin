package org.plambert.oneShot.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        //Var
        Player player = event.getPlayer();
        int remainPlayer = 4 - Bukkit.getOnlinePlayers().size();

        //Components
        Component joinMessage = Component.text(player.getName(), NamedTextColor.GREEN)
                .append(Component.text(" a rejoint la partie !", NamedTextColor.GRAY));

        Component emptySub = Component.empty();

        Title welcomeTitle = Title.title(
            Component.text("OneShot", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true),
            emptySub
        );

        Component actionBar = Component.text(remainPlayer, NamedTextColor.GOLD)
                .append(Component.text(" joueurs avant le décompte.", NamedTextColor.GRAY));

        //Logic
        event.joinMessage(joinMessage);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendActionBar(actionBar);
        }

        player.showTitle(welcomeTitle);

        setupInventory(player);

    }

    private void setupInventory(Player player) {

        player.getInventory().clear();
        player.setGameMode(org.bukkit.GameMode.ADVENTURE);

        ItemStack teamSelector = new ItemStack(Material.WHITE_WOOL);
        ItemMeta meta = teamSelector.getItemMeta();

        if (meta != null) {

            meta.displayName(Component.text("Choisir une Equipe", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            teamSelector.setItemMeta(meta);
        }

        player.getInventory().setItem(4, teamSelector);
    }

}
