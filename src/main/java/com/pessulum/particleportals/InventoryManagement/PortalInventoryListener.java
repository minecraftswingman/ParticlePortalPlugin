package com.pessulum.particleportals.InventoryManagement;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.pessulum.particleportals.ParticlePortals;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;

public class PortalInventoryListener implements Listener {
    public static BiMap<Inventory, Inventory> PortalInventorys = HashBiMap.create();
    protected static BiMap<ItemStack, Inventory> PortalColors = HashBiMap.create();


    ParticlePortals plugin;
    public PortalInventoryListener(ParticlePortals plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (e.getView().getTitle().equals("Portals")) {
            new PortalInventoryAnimation(plugin, e.getInventory(), (Player) e.getPlayer()).addAnimation();
        }
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) throws IOException {
        Player player = (Player) e.getWhoClicked();
        File file = new File(plugin.getDataFolder(), "InventoryData.yml");
        YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);



        if (e.getView().getTitle().equals("Portals") ) {
            e.setCancelled(true);
            if (!e.getCurrentItem().getType().equals(Material.ENDER_PEARL))     {return;}
            if (!e.getCurrentItem().getItemMeta().hasLore()){
                player.sendMessage(ChatColor.RED  +ChatColor.BOLD.toString() + "You Have Not Created This Portal Yet. Run " + ChatColor.YELLOW + ChatColor.BOLD + "/portal create");return;}

            String inputString = e.getCurrentItem().getItemMeta().getLore().get(1);
            String strippedString = ChatColor.stripColor(inputString);
            String Identifier = strippedString.replace("Portal-Key: ", "");

            Inventory inventory = e.getClickedInventory();
            ItemStack clickedItem = e.getCurrentItem();
            NamespacedKey key = new NamespacedKey(plugin, "Identifier");
            ItemMeta meta = clickedItem.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(key, PersistentDataType.STRING, Identifier);
            clickedItem.setItemMeta(meta);

            if (PortalColors.get(clickedItem) != null ) {
                player.openInventory(PortalColors.get(clickedItem));
            }
            else if(PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + "." + Identifier  + ".color")) != null &&
                    modify.contains(player.getUniqueId() + "." + Identifier  + ".color")) {
                Inventory ColorInventory = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + "." + Identifier  + ".color"));

                for (ItemStack item : ColorInventory.getContents()) {
                    ItemMeta itemMeta = item.getItemMeta();
                    PersistentDataContainer itemContainer = itemMeta.getPersistentDataContainer();
                    itemContainer.set(key, PersistentDataType.STRING, Identifier);
                    item.setItemMeta(itemMeta);
                }

                PortalInventorys.put(inventory, ColorInventory);
                PortalColors.put(clickedItem/*Portal Item*/, ColorInventory);

                player.openInventory(ColorInventory);

            }  else {

                Inventory ColorInventory =new PortalInventory(plugin).createColorInventory(player);
                PortalInventorys.put(inventory, ColorInventory);
                PortalColors.put(clickedItem/*Portal Item*/, ColorInventory);
                player.openInventory(ColorInventory);
            }

        }

        if (e.getView().getTitle().equals("Portal-Colors")) {
            e.setCancelled(true);

            ItemStack portalItem = null;
            if (PortalColors.inverse().get(e.getClickedInventory()) != null) {
                portalItem = PortalColors.inverse().get(e.getClickedInventory());
            }  else {
                for (ItemStack item :PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId()  +".inventory")).getContents()) {
                    if (item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "Identifier"), PersistentDataType.STRING)) {
                        String Identifier = item.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "Identifier"), PersistentDataType.STRING);
                        if (modify.contains(player.getUniqueId() + "." + Identifier + ".color"))
                            portalItem = item;
                            break;
                        } else {break;}
                    }
                }

            String inputString = portalItem.getItemMeta().getLore().get(1) ;
            String strippedString = ChatColor.stripColor(inputString);
            String Identifier = strippedString.replace("Portal-Key: ", "");
            ItemStack clickedItem = e.getCurrentItem();
            assert clickedItem != null;


            Inventory colorInv = e.getClickedInventory();
            Inventory inventory = (PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + ".inventory")) == null) ?
                    PortalInventorys.inverse().get(colorInv): PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + ".inventory"));
            if (clickedItem.getType().equals(Material.BARRIER)) {
                player.openInventory(inventory);
                return;
            }

            for (ItemStack item : colorInv.getContents()) {
                assert !item.getType().equals(Material.BARRIER);
                
                if (item.getItemMeta().hasEnchants() && !item.isSimilar(clickedItem)) {
                    ItemMeta clickedItemMeta = clickedItem.getItemMeta();
                    clickedItemMeta.addEnchant(Enchantment.SILK_TOUCH, 1, false);
                    clickedItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    clickedItem.setItemMeta(clickedItemMeta);

                    ItemMeta meta = item.getItemMeta();
                    meta.removeEnchant(Enchantment.SILK_TOUCH);
                    meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(meta);
                }
                else {
                    ItemMeta clickedItemMeta = clickedItem.getItemMeta();
                    clickedItemMeta.addEnchant(Enchantment.SILK_TOUCH, 1, false);
                    clickedItemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    clickedItem.setItemMeta(clickedItemMeta);
                }
            }

                modify.set(player.getUniqueId() + "." + Identifier  + ".color", null);
                modify.set(player.getUniqueId() + "." + Identifier  + ".color", PortalInventoryManager.toColorPortalBase64(colorInv));
                modify.save(file);
                PortalColors.replace(portalItem, colorInv);
            }
        }
}
