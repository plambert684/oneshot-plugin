package org.plambert.oneShot.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.plambert.oneShot.OneShot;
import org.plambert.oneShot.manager.GameManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerCombatListener implements Listener {

    private final OneShot plugin;
    private final Random random = new Random();

    public PlayerCombatListener(OneShot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShoot(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (plugin.getGameManager().getGameState(player) != GameManager.GameState.PLAYING) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (isWeapon(item.getType())) {
            if (event.getAction().name().contains("RIGHT_CLICK")) {
                Firework fw = player.getWorld().spawn(player.getEyeLocation(), Firework.class);
                fw.setShooter(player);
                fw.setVelocity(player.getLocation().getDirection().multiply(2));
                
                FireworkMeta fwm = fw.getFireworkMeta();
                fwm.setPower(2);
                fw.setFireworkMeta(fwm);
                
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
            }
        }
    }

    private boolean isWeapon(Material material) {
        return material.name().endsWith("_HOE") || material == Material.CARROT_ON_A_STICK;
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Firework fw)) return;
        if (!(fw.getShooter() instanceof Player shooter)) return;

        // On détone le feu d'artifice visuel immédiatement à l'impact
        spawnFirework(event.getEntity().getLocation());
        
        // Empêcher les dégâts de l'explosion du feu d'artifice lui-même
        fw.remove();

        if (event.getHitEntity() instanceof Player victim) {
            if (victim.equals(shooter)) return;

            plugin.getPlayerManager().addKill(shooter);
            plugin.getScoreboardSign().updateAll();
            
            String prefix = plugin.getConfig().getString("prefix", "&6&lOneShot &8» ").replace("&", "§");
            String killMsg = plugin.getConfig().getString("messages.player_killed", "&e&lKILL! &7Vous avez éliminé &a{victim}&7.")
                    .replace("&", "§")
                    .replace("{victim}", victim.getName());
            shooter.sendMessage(Component.text(prefix).append(Component.text(killMsg)));
            
            shooter.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 1));
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);

            // Respawn logic or TP back to spawn points
            List<org.bukkit.Location> spawns = plugin.getGameManager().getSpawnPoints(victim);
            if (!spawns.isEmpty()) {
                victim.teleport(spawns.get(random.nextInt(spawns.size())));
            }
            victim.setHealth(20);
            victim.getInventory().setItem(0, new ItemStack(plugin.getPlayerManager().getWeaponSkin(victim)));
            victim.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1f, 1f);
            
            String deathMsg = plugin.getConfig().getString("messages.player_died", "&c&lMORT! &7Vous avez été éliminé par &c{killer}&7.")
                    .replace("&", "§")
                    .replace("{killer}", shooter.getName());
            victim.sendMessage(Component.text(prefix).append(Component.text(deathMsg)));

            if (plugin.getPlayerManager().getKills(shooter) >= 10) {
                String arenaName = plugin.getPlayerManager().getPlayerArena(shooter);
                if (arenaName != null) {
                    org.plambert.oneShot.manager.Arena arena = plugin.getGameManager().getArena(arenaName);
                    if (arena != null) {
                        arena.setState(GameManager.GameState.END);
                        String winMsg = plugin.getConfig().getString("messages.player_won", "&6&lGAGNÉ! &a{player} &ea remporté la victoire !")
                                .replace("&", "§")
                                .replace("{player}", shooter.getName());
                        plugin.getGameManager().broadcastToArena(arena, Component.text(prefix).append(Component.text(winMsg)));
                        // Re-TP players to lobby after some time
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            boolean bungeecord = plugin.getConfig().getBoolean("bungeecord");
                            boolean returnToHub = plugin.getConfig().getBoolean("return_to_hub");
                            List<java.util.UUID> players = new ArrayList<>(arena.getPlayers());
                            for (java.util.UUID uuid : players) {
                                Player p = Bukkit.getPlayer(uuid);
                                if (p != null) {
                                    if (bungeecord) {
                                        plugin.getGameManager().sendToFallbackServer(p);
                                    } else {
                                        plugin.getGameManager().leaveArena(p);
                                        if (returnToHub) {
                                            p.performCommand("hub");
                                        }
                                    }
                                }
                            }
                            
                            if (bungeecord) {
                                if (plugin.getConfig().getBoolean("stop_server_at_end")) {
                                    Bukkit.getScheduler().runTaskLater(plugin, Bukkit::shutdown, 40L);
                                } else {
                                    arena.setState(GameManager.GameState.LOBBY);
                                    // Nettoyage pour nouvelle partie en mode Bungee sans stop
                                    for (java.util.UUID uuid : players) {
                                        Player p = Bukkit.getPlayer(uuid);
                                        if (p != null) {
                                            plugin.getGameManager().leaveArena(p);
                                        }
                                    }
                                }
                            } else {
                                arena.setState(GameManager.GameState.LOBBY);
                            }
                        }, 100L);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (plugin.getGameManager().getGameState(player) == GameManager.GameState.PLAYING) {
            // Désactiver tous les dégâts directs entre joueurs (PvP classique)
            // Et les dégâts causés par les projectiles (car on gère le "one shot" via ProjectileHitEvent)
            event.setCancelled(true);
        } else {
            event.setCancelled(true);
        }
    }

    private void spawnFirework(org.bukkit.Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.addEffect(FireworkEffect.builder()
                .withColor(Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256)))
                .withFade(Color.WHITE)
                .with(FireworkEffect.Type.BURST)
                .trail(true)
                .build());
        fwm.setPower(0);
        fw.setFireworkMeta(fwm);
        fw.detonate();
    }
}
