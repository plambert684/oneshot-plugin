package org.plambert.oneShot.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WeaponGUI {

    public static Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Choisissez votre arme"));

        for (int i = 0; i < 27; i++) {
            inv.setItem(i, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
        }

        inv.setItem(10, new ItemBuilder(Material.WOODEN_HOE).name(Component.text("Wooden Hoe", NamedTextColor.GOLD)).build());
        inv.setItem(11, new ItemBuilder(Material.STONE_HOE).name(Component.text("Stone Hoe", NamedTextColor.GRAY)).build());
        inv.setItem(12, new ItemBuilder(Material.IRON_HOE).name(Component.text("Iron Hoe", NamedTextColor.WHITE)).build());
        inv.setItem(13, new ItemBuilder(Material.GOLDEN_HOE).name(Component.text("Golden Hoe", NamedTextColor.YELLOW)).build());
        inv.setItem(14, new ItemBuilder(Material.DIAMOND_HOE).name(Component.text("Diamond Hoe", NamedTextColor.AQUA)).build());
        inv.setItem(15, new ItemBuilder(Material.NETHERITE_HOE).name(Component.text("Netherite Hoe", NamedTextColor.DARK_PURPLE)).build());
        inv.setItem(16, new ItemBuilder(Material.CARROT_ON_A_STICK).name(Component.text("Carrot on a Stick", NamedTextColor.RED)).build());

        return inv;
    }
}
