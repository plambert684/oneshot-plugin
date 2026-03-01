package org.plambert.oneShot.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.plambert.oneShot.OneShot;

import org.plambert.oneShot.manager.GameManager;
import org.plambert.oneShot.manager.Arena;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScoreboardSign {

    private final OneShot plugin;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");

    public ScoreboardSign(OneShot plugin) {
        this.plugin = plugin;
    }

    public void setScoreboard(Player player) {
        String title = plugin.getConfig().getString("scoreboard.title", "&6&lOneShot").replace("&", "§");
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("oneshot", Criteria.DUMMY, Component.text(title));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        String arenaName = plugin.getPlayerManager().getPlayerArena(player);
        int playerCount = 0;
        if (arenaName != null) {
            org.plambert.oneShot.manager.Arena arena = plugin.getGameManager().getArena(arenaName);
            if (arena != null) {
                playerCount = arena.getPlayers().size();
            }
        } else {
            playerCount = Bukkit.getOnlinePlayers().size();
        }

        String date = LocalDateTime.now().format(DATE_FORMATTER);

        java.util.List<String> lines = plugin.getConfig().getStringList("scoreboard.lines");
        int scoreValue = lines.size();
        
        // Déterminer les kills à afficher pour {kills}
        int displayKills = plugin.getPlayerManager().getKills(player);
        if (arenaName != null) {
            Arena arena = plugin.getGameManager().getArena(arenaName);
            if (arena != null && arena.getState() == GameManager.GameState.LOBBY) {
                displayKills = plugin.getPlayerManager().getTotalKills(player);
            }
        } else {
            // Hors arène, on affiche les kills totaux
            displayKills = plugin.getPlayerManager().getTotalKills(player);
        }

        for (String line : lines) {
            String formattedLine = line.replace("&", "§")
                    .replace("{kills}", String.valueOf(displayKills))
                    .replace("{total_kills}", String.valueOf(plugin.getPlayerManager().getTotalKills(player)))
                    .replace("{players}", String.valueOf(playerCount))
                    .replace("{arena}", (arenaName != null ? arenaName : "Lobby"))
                    .replace("{date}", date);
            
            Score score = objective.getScore(formattedLine);
            score.setScore(scoreValue);
            scoreValue--;
        }

        player.setScoreboard(scoreboard);
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            setScoreboard(player);
        }
    }
}
