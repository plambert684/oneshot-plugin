package org.plambert.oneShot;

import org.plambert.oneShot.listeners.InventoryListener;
import org.plambert.oneShot.listeners.PlayerJoinListener;
import org.plambert.oneShot.listeners.PlayerLeaveListener;
import org.plambert.oneShot.listeners.PlayerCombatListener;
import org.plambert.oneShot.manager.DatabaseManager;
import org.plambert.oneShot.manager.GameManager;
import org.plambert.oneShot.manager.PlayerManager;

import org.plambert.oneShot.utils.ScoreboardSign;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class OneShot extends JavaPlugin {

    private static OneShot instance;
    private PlayerManager playerManager;
    private GameManager gameManager;
    private ScoreboardSign scoreboardSign;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.init();
        this.playerManager = new PlayerManager(this);
        this.gameManager = new GameManager(this);
        this.scoreboardSign = new ScoreboardSign(this);

        //Register event listeners
        org.bukkit.plugin.PluginManager pm = getServer().getPluginManager();

        pm.registerEvents(new PlayerJoinListener(this), this);
        pm.registerEvents(new PlayerLeaveListener(this), this);
        pm.registerEvents(new InventoryListener(this), this);
        pm.registerEvents(new PlayerCombatListener(this), this);

        getCommand("oneshot").setExecutor(new OneShotCommand());

        // BungeeCord channel registration
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getLogger().info("OneShot has been enabled!");
    }

    @Override
    public void onDisable() {
        if (this.databaseManager != null) {
            this.databaseManager.close();
        }
    }

    public static OneShot getInstance() {
        return instance;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public ScoreboardSign getScoreboardSign() {
        return scoreboardSign;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    private class OneShotCommand implements CommandExecutor {
        @Override
        public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
            if (!(sender instanceof Player player)) return true;

            if (!player.hasPermission("oneshot.admin") && args.length > 0 && args[0].equalsIgnoreCase("admin")) {
                player.sendMessage("§cVous n'avez pas la permission.");
                return true;
            }

            if (args.length == 0) {
                String prefix = OneShot.this.getConfig().getString("prefix", "§6§lOneShot §8» ");
                player.sendMessage("§7§m---------------------------------------");
                player.sendMessage("§6§lOneShot Help §7(v" + OneShot.this.getDescription().getVersion() + ")");
                player.sendMessage(" ");
                player.sendMessage("§e/oneshot join <arène> §8- §7Rejoindre une partie");
                player.sendMessage("§e/oneshot leave §8- §7Quitter la partie actuelle");
                
                if (player.hasPermission("oneshot.admin")) {
                    player.sendMessage(" ");
                    player.sendMessage("§c§lAdministration:");
                    player.sendMessage("§c/oneshot admin gui §8- §7Ouvrir le menu de gestion");
                    player.sendMessage("§c/oneshot admin create <nom> §8- §7Créer une arène");
                    player.sendMessage("§c/oneshot admin delete <nom> §8- §7Supprimer une arène");
                    player.sendMessage("§c/oneshot admin setwaiting <nom> §8- §7Spawn d'attente");
                    player.sendMessage("§c/oneshot admin addspawn <nom> §8- §7Ajouter un spawn");
                    player.sendMessage("§c/oneshot admin save §8- §7Forcer la sauvegarde");
                }
                player.sendMessage("§7§m---------------------------------------");
                return true;
            }

            if (args[0].equalsIgnoreCase("join") && args.length >= 2) {
                gameManager.joinArena(player, args[1]);
                return true;
            }

            if (args[0].equalsIgnoreCase("leave")) {
                gameManager.leaveArena(player);
                return true;
            }

            if (args[0].equalsIgnoreCase("admin")) {
                if (args.length < 2) {
                    player.openInventory(org.plambert.oneShot.utils.AdminGUI.getArenasInventory());
                    return true;
                }
                String sub = args[1].toLowerCase();

                if (sub.equalsIgnoreCase("gui")) {
                    player.openInventory(org.plambert.oneShot.utils.AdminGUI.getArenasInventory());
                    return true;
                }

                if (args.length < 3) {
                    player.sendMessage("§cUsage: /oneshot admin " + sub + " <nomArène> [valeur]");
                    return true;
                }

                String arenaName = args[2];

                switch (sub) {
                    case "create":
                        if (OneShot.this.getConfig().getBoolean("bungeecord") && !gameManager.getArenas().isEmpty()) {
                            player.sendMessage("§cMode BungeeCord activé : une seule arène maximum autorisée.");
                            return true;
                        }
                        gameManager.createArena(arenaName);
                        player.sendMessage("§aArène " + arenaName + " créée !");
                        break;

                    case "delete":
                        gameManager.deleteArena(arenaName);
                        player.sendMessage("§cArène " + arenaName + " supprimée.");
                        break;

                    case "setwaiting":
                        gameManager.setArenaWaitingSpawn(arenaName, player.getLocation());
                        player.sendMessage("§aSpawn d'attente défini pour " + arenaName);
                        break;

                    case "addspawn":
                        org.plambert.oneShot.manager.Arena arena = gameManager.getArena(arenaName);
                        gameManager.addSpawnPoint(arenaName, player.getLocation());
                        player.sendMessage("§aSpawn de jeu ajouté pour " + arenaName + " (" + (arena != null ? arena.getSpawnPoints().size() : "?") + ")");
                        break;

                    case "setmin":
                        if (args.length < 4) {
                            player.sendMessage("§cUsage: /oneshot admin setmin <nom> <nombre>");
                            return true;
                        }
                        try {
                            int min = Integer.parseInt(args[3]);
                            gameManager.setArenaMinPlayers(arenaName, min);
                            player.sendMessage("§eMin joueurs pour " + arenaName + " défini à " + Math.max(1, min));
                        } catch (NumberFormatException e) {
                            player.sendMessage("§cNombre invalide.");
                        }
                        break;

                    case "setmax":
                        if (args.length < 4) {
                            player.sendMessage("§cUsage: /oneshot admin setmax <nom> <nombre>");
                            return true;
                        }
                        try {
                            int max = Integer.parseInt(args[3]);
                            gameManager.setArenaMaxPlayers(arenaName, max);
                            player.sendMessage("§eMax joueurs pour " + arenaName + " défini à " + max);
                        } catch (NumberFormatException e) {
                            player.sendMessage("§cNombre invalide.");
                        }
                        break;

                    case "save":
                        gameManager.saveArenas();
                        player.sendMessage("§aArènes sauvegardées !");
                        break;

                    default:
                        player.sendMessage("§cSous-commande inconnue.");
                        break;
                }
                return true;
            }

            return false;
        }
    }
}
