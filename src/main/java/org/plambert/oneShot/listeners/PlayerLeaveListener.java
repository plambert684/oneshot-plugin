package org.plambert.oneShot.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerLeaveListener implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        //Var
        int remainPlayer = 4 - Bukkit.getOnlinePlayers().size();

        //Components
        Component leaveMessage = Component.text(player.getName(), NamedTextColor.GREEN)
                .append(Component.text(" a quitté la partie !", NamedTextColor.GRAY));

        Component actionBar = Component.text(remainPlayer, NamedTextColor.GOLD)
                .append(Component.text(" joueurs avant le décompte.", NamedTextColor.GRAY));

        //Logic
        event.quitMessage(leaveMessage);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendActionBar(actionBar);
        }


    }

}
