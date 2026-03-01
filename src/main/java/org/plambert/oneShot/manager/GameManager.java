package org.plambert.oneShot.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.plambert.oneShot.OneShot;
import org.plambert.oneShot.tasks.StartCountdown;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {

    private final OneShot plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final File arenasFile;
    private FileConfiguration arenasConfig;

    public enum GameState {
        LOBBY, STARTING, PLAYING, END
    }

    public GameManager(OneShot plugin) {
        this.plugin = plugin;
        this.arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        loadArenas();
    }

    public void loadArenas() {
        if (!arenasFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                arenasFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
        arenas.clear();
        ConfigurationSection section = arenasConfig.getConfigurationSection("arenas");
        if (section != null) {
            for (String name : section.getKeys(false)) {
                Arena arena = new Arena(name);
                List<String> spawns = section.getStringList(name + ".spawns");
                if (spawns != null) {
                    for (String s : spawns) {
                        arena.getSpawnPoints().add(stringToLocation(s));
                    }
                }
                String waiting = section.getString(name + ".waitingSpawn");
                if (waiting != null) {
                    arena.setWaitingSpawn(stringToLocation(waiting));
                }
                arena.setMinPlayers(section.getInt(name + ".minPlayers", 2));
                arena.setMaxPlayers(section.getInt(name + ".maxPlayers", 16));
                arenas.put(name, arena);
            }
        }
    }

    public void saveArenas() {
        arenasConfig.set("arenas", null);
        for (Arena arena : arenas.values()) {
            List<String> spawns = new ArrayList<>();
            for (Location loc : arena.getSpawnPoints()) {
                spawns.add(locationToString(loc));
            }
            arenasConfig.set("arenas." + arena.getName() + ".spawns", spawns);
            if (arena.getWaitingSpawn() != null) {
                arenasConfig.set("arenas." + arena.getName() + ".waitingSpawn", locationToString(arena.getWaitingSpawn()));
            }
            arenasConfig.set("arenas." + arena.getName() + ".minPlayers", arena.getMinPlayers());
            arenasConfig.set("arenas." + arena.getName() + ".maxPlayers", arena.getMaxPlayers());
        }
        try {
            arenasConfig.save(arenasFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createArena(String name) {
        if (!arenas.containsKey(name)) {
            arenas.put(name, new Arena(name));
            saveArenas();
        }
    }

    public void deleteArena(String name) {
        arenas.remove(name);
        saveArenas();
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public Map<String, Arena> getArenas() {
        return arenas;
    }

    public void joinArena(Player player, String arenaName) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            String msg = plugin.getConfig().getString("messages.arena_not_exist", "&cCette arène n'existe pas.").replace("&", "§");
            player.sendMessage(msg);
            return;
        }

        if (!arena.isReady()) {
            String msg = plugin.getConfig().getString("messages.arena_not_ready", "&cCette arène n'est pas encore prête à être rejointe. Elle doit avoir au moins 3 points de spawn et un spawn d'attente.").replace("&", "§");
            player.sendMessage(msg);
            return;
        }

        if (plugin.getPlayerManager().isInArena(player)) {
            String msg = plugin.getConfig().getString("messages.already_in_game", "&cVous êtes déjà dans une partie.").replace("&", "§");
            player.sendMessage(msg);
            return;
        }

        if (arena.getState() != GameState.LOBBY && arena.getState() != GameState.STARTING) {
            String msg = plugin.getConfig().getString("messages.arena_started", "&cCette partie a déjà commencé.").replace("&", "§");
            player.sendMessage(msg);
            return;
        }

        if (arena.getPlayers().size() >= arena.getMaxPlayers()) {
            String msg = plugin.getConfig().getString("messages.arena_full", "&cCette arène est pleine.").replace("&", "§");
            player.sendMessage(msg);
            return;
        }

        arena.addPlayer(player.getUniqueId());
        plugin.getPlayerManager().setPlayerArena(player, arenaName);
        
        if (arena.getWaitingSpawn() != null) {
            player.teleport(arena.getWaitingSpawn());
        }
        
        setupPlayerForLobby(player);
        
        if (!plugin.getConfig().getBoolean("bungeecord")) {
            Component emptySub = Component.empty();
            Title welcomeTitle = Title.title(
                Component.text("OneShot", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true),
                emptySub
            );
            player.showTitle(welcomeTitle);
        }

        // Message join
        String prefix = plugin.getConfig().getString("prefix", "&6&lOneShot &8» ").replace("&", "§");
        String msg = plugin.getConfig().getString("messages.join", "&a{player} &eest arrivé ! &7(&b{count}&7/&b{max}&7)")
                .replace("&", "§")
                .replace("{player}", player.getName())
                .replace("{count}", String.valueOf(arena.getPlayers().size()))
                .replace("{max}", String.valueOf(arena.getMaxPlayers()));
        Component joinMessage = Component.text(prefix).append(Component.text(msg));
        broadcastToArena(arena, joinMessage);

        if (arena.getPlayers().size() >= arena.getMinPlayers() && arena.getState() == GameState.LOBBY) {
            arena.setState(GameState.STARTING);
            new StartCountdown(plugin, arena).runTaskTimer(plugin, 0, 20);
        }
    }

    public void leaveArena(Player player) {
        String arenaName = plugin.getPlayerManager().getPlayerArena(player);
        if (arenaName == null) {
            player.sendMessage("§cVous n'êtes pas dans une partie.");
            return;
        }

        Arena arena = arenas.get(arenaName);
        if (arena != null) {
            // Sauvegarder les kills si la partie est en cours ou finie
            if (arena.getState() == GameState.PLAYING || arena.getState() == GameState.END) {
                plugin.getPlayerManager().saveGameKills(player);
            }
            
            arena.removePlayer(player.getUniqueId());
            String prefix = plugin.getConfig().getString("prefix", "&6&lOneShot &8» ").replace("&", "§");
            String msg = plugin.getConfig().getString("messages.leave", "&c{player} &ea quitté le jeu.")
                    .replace("&", "§")
                    .replace("{player}", player.getName());
            Component leaveMessage = Component.text(prefix).append(Component.text(msg));
            broadcastToArena(arena, leaveMessage);
        }

        plugin.getPlayerManager().clearData(player);
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void setupPlayerForLobby(Player player) {
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);

        ItemStack teamSelector = new ItemStack(Material.WHITE_WOOL);
        ItemMeta meta = teamSelector.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("Choisir une Equipe", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false));
            teamSelector.setItemMeta(meta);
        }
        player.getInventory().setItem(0, teamSelector);

        ItemStack weaponSelector = new ItemStack(Material.WOODEN_HOE);
        ItemMeta weaponMeta = weaponSelector.getItemMeta();
        if (weaponMeta != null) {
            weaponMeta.displayName(Component.text("Choisir son Arme", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            weaponSelector.setItemMeta(weaponMeta);
        }
        player.getInventory().setItem(4, weaponSelector);
        
        plugin.getScoreboardSign().setScoreboard(player);
    }

    public void broadcastToArena(Arena arena, Component message) {
        for (java.util.UUID uuid : arena.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendMessage(message);
            }
        }
    }

    public void sendToFallbackServer(Player player) {
        String server = plugin.getConfig().getString("fallback_server", "lobby");
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            out.writeUTF("Connect");
            out.writeUTF(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
    }

    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
    }

    private Location stringToLocation(String s) {
        if (s == null) return null;
        String[] parts = s.split(",");
        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);
        return new Location(world, x, y, z, yaw, pitch);
    }

    public GameState getGameState(Player player) {
        String arenaName = plugin.getPlayerManager().getPlayerArena(player);
        if (arenaName == null) {
            if (plugin.getConfig().getBoolean("bungeecord")) {
                Arena solo = arenas.get("solo");
                return solo != null ? solo.getState() : GameState.LOBBY;
            }
            return GameState.LOBBY;
        }
        Arena arena = arenas.get(arenaName);
        return arena != null ? arena.getState() : GameState.LOBBY;
    }

    public List<Location> getSpawnPoints(Player player) {
        String arenaName = plugin.getPlayerManager().getPlayerArena(player);
        if (arenaName == null) {
            if (plugin.getConfig().getBoolean("bungeecord")) {
                Arena solo = arenas.get("solo");
                return solo != null ? solo.getSpawnPoints() : new ArrayList<>();
            }
            return new ArrayList<>();
        }
        Arena arena = arenas.get(arenaName);
        return arena != null ? arena.getSpawnPoints() : new ArrayList<>();
    }

    public void addSpawnPoint(String arenaName, Location location) {
        Arena arena = arenas.get(arenaName);
        if (arena != null) {
            arena.getSpawnPoints().add(location);
            saveArenas();
        }
    }

    public void setArenaWaitingSpawn(String arenaName, Location location) {
        Arena arena = arenas.get(arenaName);
        if (arena != null) {
            arena.setWaitingSpawn(location);
            saveArenas();
        }
    }

    public void setArenaMinPlayers(String arenaName, int min) {
        Arena arena = arenas.get(arenaName);
        if (arena != null) {
            arena.setMinPlayers(min);
            saveArenas();
        }
    }

    public void setArenaMaxPlayers(String arenaName, int max) {
        Arena arena = arenas.get(arenaName);
        if (arena != null) {
            arena.setMaxPlayers(Math.max(arena.getMinPlayers(), max));
            saveArenas();
        }
    }
}
