package org.plambert.oneShot.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.plambert.oneShot.OneShot;
import org.plambert.oneShot.manager.Arena;
import org.plambert.oneShot.utils.AdminGUI;
import org.plambert.oneShot.utils.ColorGUI;
import org.plambert.oneShot.utils.WeaponGUI;

public class InventoryListener implements Listener {

    private final OneShot plugin;

    public InventoryListener(OneShot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        String title = event.getView().getTitle();

        if (title.equals(AdminGUI.TITLE_ARENAS)) {
            event.setCancelled(true);
            if (item.getType() == Material.PAPER) {
                if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
                    String arenaName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
                    Arena arena = plugin.getGameManager().getArena(arenaName);
                    if (arena != null) {
                        if (event.isLeftClick()) {
                            player.openInventory(AdminGUI.getEditArenaInventory(arena));
                        } else if (event.isRightClick()) {
                            plugin.getGameManager().deleteArena(arenaName);
                            player.sendMessage("§cArène " + arenaName + " supprimée.");
                            player.openInventory(AdminGUI.getArenasInventory());
                        }
                    }
                }
            } else if (item.getType() == Material.EMERALD_BLOCK) {
                plugin.getGameManager().saveArenas();
                player.sendMessage("§aArènes sauvegardées !");
                player.closeInventory();
            }
            return;
        }

        if (title.startsWith(AdminGUI.TITLE_EDIT_PREFIX)) {
            event.setCancelled(true);
            String arenaName = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(event.getView().title());
            if (arenaName.startsWith(AdminGUI.TITLE_EDIT_PREFIX)) {
                arenaName = arenaName.substring(AdminGUI.TITLE_EDIT_PREFIX.length());
            }
            Arena arena = plugin.getGameManager().getArena(arenaName);
            if (arena == null) return;

            switch (item.getType()) {
                case ENDER_PEARL:
                    plugin.getGameManager().setArenaWaitingSpawn(arenaName, player.getLocation());
                    player.sendMessage("§aSpawn d'attente défini !");
                    player.openInventory(AdminGUI.getEditArenaInventory(arena));
                    break;
                case DIAMOND_BOOTS:
                    plugin.getGameManager().addSpawnPoint(arenaName, player.getLocation());
                    player.sendMessage("§aPoint de spawn ajouté !");
                    player.openInventory(AdminGUI.getEditArenaInventory(arena));
                    break;
                case PLAYER_HEAD:
                    int min = arena.getMinPlayers();
                    if (event.isLeftClick()) min++;
                    else if (event.isRightClick()) min--;
                    plugin.getGameManager().setArenaMinPlayers(arenaName, min);
                    player.openInventory(AdminGUI.getEditArenaInventory(arena));
                    break;
                case BEACON:
                    int max = arena.getMaxPlayers();
                    if (event.isLeftClick()) max++;
                    else if (event.isRightClick()) max--;
                    plugin.getGameManager().setArenaMaxPlayers(arenaName, max);
                    player.openInventory(AdminGUI.getEditArenaInventory(arena));
                    break;
                case BARRIER:
                    player.openInventory(AdminGUI.getArenasInventory());
                    break;
            }
            return;
        }

        if (title.equals("Choisissez votre couleur")) {
            event.setCancelled(true);
            if (item.getItemMeta() != null && item.getItemMeta().hasDisplayName()) {
                Component displayName = item.getItemMeta().displayName();
                if (displayName != null) {
                    NamedTextColor color = (NamedTextColor) displayName.color();
                    if (color != null) {
                        if (color.equals(NamedTextColor.GRAY)) {
                            player.sendMessage(Component.text("Cette couleur est déjà prise !", NamedTextColor.RED));
                            return;
                        }
                        plugin.getPlayerManager().setPlayerColor(player, color);
                        player.displayName(Component.text(player.getName(), color));
                        player.playerListName(Component.text(player.getName(), color));
                        player.sendMessage(Component.text("Couleur choisie : ", NamedTextColor.GRAY).append(displayName));
                        player.closeInventory();
                    }
                }
            }
            return;
        }

        if (title.equals("Choisissez votre arme")) {
            event.setCancelled(true);
            Material mat = item.getType();
            if (mat != Material.WHITE_STAINED_GLASS_PANE) {
                plugin.getPlayerManager().setWeaponSkin(player, mat);
                
                // Update weapon in inventory slot 4
                ItemStack weapon = new ItemStack(mat);
                org.bukkit.inventory.meta.ItemMeta weaponMeta = weapon.getItemMeta();
                if (weaponMeta != null) {
                    weaponMeta.displayName(Component.text("Choisir son Arme", NamedTextColor.GOLD)
                            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
                    weapon.setItemMeta(weaponMeta);
                }
                player.getInventory().setItem(4, weapon);
                
                player.sendMessage(Component.text("Arme choisie !", NamedTextColor.GREEN));
                player.closeInventory();
            }
            return;
        }

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onItemDrop(org.bukkit.event.player.PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            if (item.getType() == Material.WHITE_WOOL) {
                player.openInventory(ColorGUI.getInventory());
                event.setCancelled(true);
            } else if (item.getType() == Material.WOODEN_HOE || item.getType() == Material.STONE_HOE || item.getType() == Material.IRON_HOE || item.getType() == Material.GOLDEN_HOE || item.getType() == Material.DIAMOND_HOE || item.getType() == Material.NETHERITE_HOE || item.getType() == Material.CARROT_ON_A_STICK) {
                if (plugin.getGameManager().getGameState(player) == org.plambert.oneShot.manager.GameManager.GameState.LOBBY) {
                    player.openInventory(WeaponGUI.getInventory());
                    event.setCancelled(true);
                }
            }

        }

    }

}
