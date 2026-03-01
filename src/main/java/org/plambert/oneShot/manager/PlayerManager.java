package org.plambert.oneShot.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.plambert.oneShot.OneShot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    
    private final OneShot plugin;
    private final Map<UUID, NamedTextColor> playerColors = new HashMap<>();
    private final Map<UUID, Integer> gameKills = new HashMap<>();
    private final Map<UUID, Integer> totalKills = new HashMap<>();
    private final Map<UUID, Material> weaponSkins = new HashMap<>();
    private final Map<UUID, String> playerArena = new HashMap<>();

    public PlayerManager(OneShot plugin) {
        this.plugin = plugin;
    }

    public void setPlayerArena(Player player, String arenaName) {
        if (arenaName == null) {
            playerArena.remove(player.getUniqueId());
        } else {
            playerArena.put(player.getUniqueId(), arenaName);
            // Charger les kills totaux depuis la DB quand on rejoint une arène (si pas déjà fait au join)
            if (!totalKills.containsKey(player.getUniqueId())) {
                totalKills.put(player.getUniqueId(), plugin.getDatabaseManager().getKills(player.getUniqueId()));
            }
            // Initialiser les kills de la partie
            gameKills.put(player.getUniqueId(), 0);
        }
    }

    public void loadPlayerData(Player player) {
        totalKills.put(player.getUniqueId(), plugin.getDatabaseManager().getKills(player.getUniqueId()));
    }

    public String getPlayerArena(Player player) {
        return playerArena.get(player.getUniqueId());
    }

    public boolean isInArena(Player player) {
        return playerArena.containsKey(player.getUniqueId());
    }

    public void setPlayerColor(Player player, NamedTextColor color) {
        playerColors.put(player.getUniqueId(), color);
    }

    public boolean isColorTaken(NamedTextColor color) {
        return playerColors.containsValue(color);
    }

    public NamedTextColor getPlayerColor(Player player) {
        return playerColors.get(player.getUniqueId());
    }

    public void removePlayerColor(Player player) {
        playerColors.remove(player.getUniqueId());
    }

    public int getKills(Player player) {
        return gameKills.getOrDefault(player.getUniqueId(), 0);
    }

    public int getTotalKills(Player player) {
        return totalKills.getOrDefault(player.getUniqueId(), 0);
    }

    public void addKill(Player player) {
        int newKills = getKills(player) + 1;
        gameKills.put(player.getUniqueId(), newKills);
    }

    public void saveGameKills(Player player) {
        int gKills = getKills(player);
        int tKills = getTotalKills(player) + gKills;
        totalKills.put(player.getUniqueId(), tKills);
        // Sauvegarder en DB
        plugin.getDatabaseManager().updateKills(player.getUniqueId(), tKills);
    }

    public void resetKills(Player player) {
        gameKills.put(player.getUniqueId(), 0);
    }

    public Material getWeaponSkin(Player player) {
        return weaponSkins.getOrDefault(player.getUniqueId(), Material.WOODEN_HOE);
    }

    public void setWeaponSkin(Player player, Material material) {
        weaponSkins.put(player.getUniqueId(), material);
    }

    public void clearData(Player player) {
        playerColors.remove(player.getUniqueId());
        gameKills.remove(player.getUniqueId());
        totalKills.remove(player.getUniqueId());
        weaponSkins.remove(player.getUniqueId());
        playerArena.remove(player.getUniqueId());
        
        player.displayName(Component.text(player.getName()));
        player.playerListName(Component.text(player.getName()));
    }

}
