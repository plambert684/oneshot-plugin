package org.plambert.oneShot.tasks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.plambert.oneShot.OneShot;
import org.plambert.oneShot.manager.GameManager;
import org.plambert.oneShot.manager.Arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StartCountdown extends BukkitRunnable {

    private final OneShot plugin;
    private final Arena arena;
    private int seconds = 10;
    private final Random random = new Random();

    public StartCountdown(OneShot plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
    }

    @Override
    public void run() {
        if (seconds <= 0) {
            startGame();
            cancel();
            return;
        }

        if (arena.getPlayers().size() < arena.getMinPlayers()) {
            String prefix = plugin.getConfig().getString("prefix", "&6&lOneShot &8» ").replace("&", "§");
            String msg = plugin.getConfig().getString("messages.not_enough_players", "&cPas assez de joueurs ! Le compte à rebours est annulé.").replace("&", "§");
            plugin.getGameManager().broadcastToArena(arena, Component.text(prefix).append(Component.text(msg)));
            arena.setState(GameManager.GameState.LOBBY);
            cancel();
            return;
        }

        if (!arena.isReady()) {
            String prefix = plugin.getConfig().getString("prefix", "&6&lOneShot &8» ").replace("&", "§");
            String msg = plugin.getConfig().getString("messages.arena_invalid", "&cL'arène n'est pas configurée correctement (spawns manquants).").replace("&", "§");
            plugin.getGameManager().broadcastToArena(arena, Component.text(prefix).append(Component.text(msg)));
            arena.setState(GameManager.GameState.LOBBY);
            cancel();
            return;
        }

        if (seconds <= 5 || seconds == 10) {
            String prefix = plugin.getConfig().getString("prefix", "&6&lOneShot &8» ").replace("&", "§");
            String msg = plugin.getConfig().getString("messages.game_starting_in", "&eLe jeu commence dans &6{seconds} &esecondes !")
                    .replace("&", "§")
                    .replace("{seconds}", String.valueOf(seconds));
            plugin.getGameManager().broadcastToArena(arena, Component.text(prefix).append(Component.text(msg)));
            for (java.util.UUID uuid : arena.getPlayers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                }
            }
        }

        seconds--;
    }

    private void startGame() {
        arena.setState(GameManager.GameState.PLAYING);
        List<Location> spawns = arena.getSpawnPoints();
        
        // Liste des couleurs disponibles
        List<NamedTextColor> availableColors = new ArrayList<>();
        availableColors.add(NamedTextColor.GREEN);
        availableColors.add(NamedTextColor.YELLOW);
        availableColors.add(NamedTextColor.RED);
        availableColors.add(NamedTextColor.BLUE);
        availableColors.add(NamedTextColor.LIGHT_PURPLE);

        for (java.util.UUID uuid : arena.getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            // Attribution automatique de couleur si non sélectionnée
            if (plugin.getPlayerManager().getPlayerColor(player) == null) {
                for (NamedTextColor color : availableColors) {
                    if (!plugin.getPlayerManager().isColorTaken(color)) {
                        plugin.getPlayerManager().setPlayerColor(player, color);
                        break;
                    }
                }
            }

            player.getInventory().clear();
            player.getInventory().setItem(0, new ItemStack(plugin.getPlayerManager().getWeaponSkin(player)));
            
            if (!spawns.isEmpty()) {
                player.teleport(spawns.get(random.nextInt(spawns.size())));
            }
            
            String msg = plugin.getConfig().getString("messages.game_start", "&a&lC'est parti ! &eAtteignez &610 kills &epour gagner !").replace("&", "§");
            player.showTitle(Title.title(Component.text(msg), Component.empty()));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            
            // Mettre à jour le scoreboard pour passer des kills totaux aux kills de partie (0)
            plugin.getScoreboardSign().setScoreboard(player);
        }
    }
}
