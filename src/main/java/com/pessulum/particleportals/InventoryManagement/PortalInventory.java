package com.pessulum.particleportals.InventoryManagement;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.pessulum.particleportals.ParticlePortals;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class PortalInventory {
    protected static BiMap<UUID, Inventory> playerPortalInventorys = HashBiMap.create();
    protected static BiMap<UUID, Inventory> playerColorInventorys = HashBiMap.create();

    ParticlePortals plugin;

    public PortalInventory(ParticlePortals plugin) {
        this.plugin = plugin;
    }

    public Inventory createInventory(Player player) throws IOException {
        Inventory inventory = Bukkit.createInventory(player, 27, "Portals");
        ItemStack enderPearl = new ItemStack(Material.ENDER_PEARL);

        int itemNumber = 1;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i != 9 && i != 10 && i != 11 && i != 12 && i != 13 && i != 14 && i != 16 && i % 2 == 0) {
                ItemMeta meta = enderPearl.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD +ChatColor.BOLD.toString()+ "Portal: " + itemNumber);
                enderPearl.setItemMeta(meta);
                inventory.setItem(i, enderPearl);
                itemNumber++;
            }
        }


        playerPortalInventorys.put(player.getUniqueId(), inventory);
        File file = new File(plugin.getDataFolder(), "InventoryData.yml");
        YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
        modify.set(player.getUniqueId() + ".inventory", PortalInventoryManager.toPortalBase64(inventory));
        modify.save(file);

        return inventory;
    }

    public Inventory createColorInventory(Player player) throws IOException {

        Inventory colorInventory = Bukkit.createInventory(player, 9, "Portal-Colors");

        colorInventory.setItem(0, new ItemStack(Material.BARRIER));
        colorInventory.setItem(1, new ItemStack(Material.PURPLE_WOOL));
        colorInventory.setItem(2, new ItemStack(Material.BLUE_WOOL));
        colorInventory.setItem(3, new ItemStack(Material.GREEN_WOOL));
        colorInventory.setItem(4, new ItemStack(Material.RED_WOOL));
        colorInventory.setItem(5, new ItemStack(Material.ORANGE_WOOL));
        colorInventory.setItem(6, new ItemStack(Material.YELLOW_WOOL));
        colorInventory.setItem(7, new ItemStack(Material.BLACK_WOOL));
        colorInventory.setItem(8, new ItemStack(Material.WHITE_WOOL));

        playerColorInventorys.put(player.getUniqueId(), colorInventory);


        return colorInventory;
    }



    public static Color translateWoolColorToColor(Material wool) {
        if (wool == null) return Color.RED;
        switch (wool) {
            case WHITE_WOOL:
                return Color.WHITE;
            case YELLOW_WOOL:
                return Color.YELLOW;
            case ORANGE_WOOL:
                return Color.fromRGB(255, 143, 2); // Using Color.fromRGB for custom colors
            case PURPLE_WOOL:
                return Color.PURPLE;
            case BLUE_WOOL:
                return Color.BLUE;
            case GREEN_WOOL:
                return Color.GREEN;
            case BLACK_WOOL:
                return Color.BLACK;
            case GRAY_WOOL:
                return Color.GRAY;
            default:
                return Color.RED;
        }
    }



}