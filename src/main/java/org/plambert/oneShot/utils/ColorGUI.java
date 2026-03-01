package org.plambert.oneShot.utils;

import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.Base64;
import org.plambert.oneShot.OneShot;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class ColorGUI {

    private static final String GRAY_HEAD = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2ZkM2YzYTA1NTVjZTI3YjVlYTM1NWU1Y2I4MTY5ZGM2NThhYmRjYzMxYmUxZmE3OTM5MTY2YzVjOTRkYSJ9fX0=";

    public static Inventory getInventory() {

        Inventory inv = Bukkit.createInventory(null, 27, Component.text("Choisissez votre couleur"));

        for (int i = 0; i < 9; i++) {
            inv.setItem(i, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
        }

        for (int i = 18; i < 27; i++) {
            inv.setItem(i, new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
        }

        OneShot plugin = OneShot.getInstance();

        inv.setItem(9, createColorItem(plugin, "Vert", NamedTextColor.GREEN, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTI1YjhlZWQ1YzU2NWJkNDQwZWM0N2M3OWMyMGQ1Y2YzNzAxNjJiMWQ5YjVkZDMxMDBlZDYyODNmZTAxZDZlIn19fQ=="));
        inv.setItem(10, createColorItem(plugin, "Jaune", NamedTextColor.YELLOW, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTg5YjkzZmQ2MTZlZDM2NzBjY2Y2NDdhMGY5MzgwMzk4YzBkNDYxNTYzNGYyZGVmZjQ2YzZlZGJkYzcxMjg4NSJ9fX0="));
        inv.setItem(11, createColorItem(plugin, "Rouge", NamedTextColor.RED, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWMxNDYwMGFjZTUwNjk1YzdjOWJjZjA5ZTQyYWZkOWY1M2M5ZTIwZGFhMTUyNGM5NWRiNDE5N2RkMzExNjQxMiJ9fX0="));
        inv.setItem(12, createColorItem(plugin, "Bleu", NamedTextColor.BLUE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjEwZTM3NGNkYzJiYTk1YmI3MmYxYTAzNmM3N2RhMzUwOTkzNWExYWJkMjRiNjhjNmIzNTkxNjkwYjEwM2ZlZCJ9fX0="));
        inv.setItem(13, createColorItem(plugin, "Rose", NamedTextColor.LIGHT_PURPLE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGNmMjgzNTE4MGNiZmVjM2IzMTdkNmE0NzQ5MWE3NGFlNzE0MzViYTE2OWE1NzkyNWI5MDk2ZWEyZjljNjFiNiJ9fX0="));

        return inv;
    }

    private static ItemStack createColorItem(OneShot plugin, String name, NamedTextColor color, String texture) {
        if (plugin.getPlayerManager().isColorTaken(color)) {
            return createCustomHead(GRAY_HEAD, name + " (Pris)", NamedTextColor.GRAY);
        } else {
            return createCustomHead(texture, name, color);
        }
    }

    private static ItemStack createCustomHead(String base64Texture, String name, NamedTextColor color) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));

            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());

            String decodedJson = new String(Base64.getDecoder().decode(base64Texture));
            String urlString = decodedJson.split("\"url\":\"")[1].split("\"")[0];

            PlayerTextures textures = profile.getTextures();

            try {
                textures.setSkin(new URL(urlString));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
            head.setItemMeta(meta);
        }
        return head;
    }

}
