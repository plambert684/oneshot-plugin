package org.plambert.oneShot.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.plambert.oneShot.OneShot;
import org.plambert.oneShot.manager.Arena;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminGUI {

    public static final String TITLE_ARENAS = "Administration - Arènes";
    public static final String TITLE_EDIT_PREFIX = "Edition: ";

    public static Inventory getArenasInventory() {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(TITLE_ARENAS));
        Map<String, Arena> arenas = OneShot.getInstance().getGameManager().getArenas();

        int slot = 0;
        for (Arena arena : arenas.values()) {
            if (slot >= 45) break; // Keep last row for actions

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text("§7Etat: " + (arena.isReady() ? "§aPrête" : "§cIncomplète")));
            lore.add(Component.text("§7Joueurs: §e" + arena.getMinPlayers() + " - " + arena.getMaxPlayers()));
            lore.add(Component.text("§7Spawns: §e" + arena.getSpawnPoints().size()));
            lore.add(Component.text("§7Waiting: " + (arena.getWaitingSpawn() != null ? "§aDéfini" : "§cNon défini")));
            lore.add(Component.text(""));
            lore.add(Component.text("§eClic gauche pour éditer"));
            lore.add(Component.text("§cClic droit pour supprimer"));

            inv.setItem(slot++, new ItemBuilder(Material.PAPER)
                    .name(Component.text(arena.getName(), NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false))
                    .lore(lore)
                    .build());
        }

        // Action buttons
        inv.setItem(49, new ItemBuilder(Material.SUNFLOWER)
                .name(Component.text("Créer une nouvelle arène", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                .lore(List.of(Component.text("§7(Utilisez /oneshot admin create <nom>)")))
                .build());

        inv.setItem(53, new ItemBuilder(Material.EMERALD_BLOCK)
                .name(Component.text("Sauvegarder les arènes", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false))
                .build());

        return inv;
    }

    public static Inventory getEditArenaInventory(Arena arena) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text(TITLE_EDIT_PREFIX + arena.getName()));

        // Waiting Spawn
        inv.setItem(10, new ItemBuilder(Material.ENDER_PEARL)
                .name(Component.text("Définir le spawn d'attente", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .lore(List.of(Component.text("§7Actuel: " + (arena.getWaitingSpawn() != null ? "§aDéfini" : "§cNon défini")),
                        Component.text("§7Cliquez pour définir sur votre position")))
                .build());

        // Add Spawn
        inv.setItem(11, new ItemBuilder(Material.DIAMOND_BOOTS)
                .name(Component.text("Ajouter un point de spawn", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .lore(List.of(Component.text("§7Actuels: §e" + arena.getSpawnPoints().size()),
                        Component.text("§7Cliquez pour ajouter votre position")))
                .build());

        // Min Players
        inv.setItem(13, new ItemBuilder(Material.PLAYER_HEAD)
                .name(Component.text("Min joueurs: " + arena.getMinPlayers(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .lore(List.of(Component.text("§7Clic gauche: +1"), Component.text("§7Clic droit: -1")))
                .build());

        // Max Players
        inv.setItem(14, new ItemBuilder(Material.BEACON)
                .name(Component.text("Max joueurs: " + arena.getMaxPlayers(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                .lore(List.of(Component.text("§7Clic gauche: +1"), Component.text("§7Clic droit: -1")))
                .build());

        // Back button
        inv.setItem(18, new ItemBuilder(Material.BARRIER)
                .name(Component.text("Retour", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                .build());

        return inv;
    }
}
