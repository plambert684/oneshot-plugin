package org.plambert.oneShot.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    private final ItemStack item;
    private final ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(Component name) {
        if (meta != null) {
            meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        }
        return this;
    }

    public ItemBuilder lore(List<Component> lore) {
        if (meta != null) {
            List<Component> formattedLore = new ArrayList<>();
            for (Component line : lore) {
                formattedLore.add(line.decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(formattedLore);
        }
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
