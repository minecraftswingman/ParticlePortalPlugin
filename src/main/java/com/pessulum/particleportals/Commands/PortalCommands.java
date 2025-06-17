package com.pessulum.particleportals.Commands;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.pessulum.particleportals.InventoryManagement.PortalInventory;
import com.pessulum.particleportals.InventoryManagement.PortalInventoryManager;
import com.pessulum.particleportals.ParticlePortals;
import com.pessulum.particleportals.PortalKey;
import com.pessulum.particleportals.PortalSpawner.CustomPortals;
import com.pessulum.particleportals.PortalSpawner.Portal;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PortalCommands implements CommandExecutor {
    HashMap<UUID, Long> cooldowns = new HashMap<UUID, Long>();
    ParticlePortals plugin;
    public static BiMap<String, Player> playerHomeClick = HashBiMap.create();
    public static BiMap<String, Player> playerDestinationClick = HashBiMap.create();


    public PortalCommands(ParticlePortals plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (args.length == 0) {
                player.sendMessage(
                        ChatColor.YELLOW + ChatColor.BOLD.toString() + "./portal" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " create #name #PortalKey\n" +
                                ChatColor.YELLOW + ChatColor.BOLD + "./portal" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " disband #PortalKey\n" +
                                ChatColor.YELLOW + ChatColor.BOLD + "./portal" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " spawn #PortalKey\n" +
                                ChatColor.YELLOW + ChatColor.BOLD + "./portal" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " inventory\n" +
                                ChatColor.YELLOW + ChatColor.BOLD + "./portal" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " set home current #PortalKey\n" +
                                ChatColor.YELLOW + ChatColor.BOLD + "./portal" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " set home click #PortalKey\n" +
                                ChatColor.YELLOW + ChatColor.BOLD + "./portal" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " set destination current #PortalKey\n" +
                                ChatColor.YELLOW + ChatColor.BOLD + "./portal" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " set destination click #PortalKey \n" +
                                ChatColor.YELLOW + ChatColor.BOLD + "./portal" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + " set shape #ShapeType #PortalKey \n");
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("create")) {
                    File file = new File(plugin.getDataFolder(), "InventoryData.yml");
                    YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
                    String name = args[1];
                    String Identifier = args[2];

                    if (Identifier.length() > 10) {
                        player.sendMessage(ChatColor.RED + "Portal Key Cannot Be More Than 10 Characters Long.");
                        return true;
                    }
                    Inventory inventory = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + ".inventory"));
                    if (inventory == null) {
                        try {
                            inventory = new PortalInventory(plugin).createInventory(player);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    boolean portalCreated = false;

                    for (ItemStack item : inventory.getContents()) {
                        if (item == null) continue;
                        if (!item.getType().equals(Material.ENDER_PEARL)) continue;

                        String Identifier2 = "";
                        if (item.getItemMeta().hasLore()) {
                            String inputString = item.getItemMeta().getLore().get(1);
                            String strippedString = ChatColor.stripColor(inputString);
                            Identifier2 = strippedString.replace("Portal-Key: ", "");
                        }

                        if (!portalCreated && !item.getItemMeta().hasLore() && item.getType().equals(Material.ENDER_PEARL) && !Identifier2.equals(Identifier)) {
                            ItemMeta meta = item.getItemMeta();
                            meta.setLore(Arrays.asList(ChatColor.RED + ChatColor.BOLD.toString() + "Name: " + ChatColor.RED + ChatColor.BOLD + name, ChatColor.RED + ChatColor.BOLD.toString() + "Portal-Key: " + ChatColor.RED + ChatColor.BOLD + Identifier));
                            item.setItemMeta(meta);
                            portalCreated = true;
                            break;
                        } else if (Identifier2.equals(Identifier) && plugin.getConfig().contains("portal." + player.getUniqueId() + "." + Identifier)) {
                            player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Portal Key " + Identifier + " Is Already In Use.");
                            return true;
                        }
                    }

                    if (portalCreated) {
                        player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "Portal Created!");
                        plugin.saveConfig();
                    } else {
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You Have Reached The Maximum Amount Of Portals");
                    }


                    plugin.getConfig().set("portal." + player.getUniqueId() + ".portal-key", Identifier);
                    plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".owner", player.getName());
                    plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".name", name);
                    PortalKey.setMappedKey(player.getUniqueId(), Identifier);
                    modify.set(player.getUniqueId() + ".inventory", null);
                    modify.set(player.getUniqueId() + ".inventory", PortalInventoryManager.toPortalBase64(inventory));
                    try {
                        modify.save(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    plugin.saveConfig();
                } else player.sendMessage(ChatColor.RED + "Invalid Command");
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("disband")) {
                    String inputIdentifier = args[1];
                    File file = new File(plugin.getDataFolder(), "InventoryData.yml");
                    YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);

                    // Load inventory from YAML
                    String encodedInventory = modify.getString(player.getUniqueId() + ".inventory");
                    Inventory inventory = PortalInventoryManager.fromBase64(encodedInventory);

                    if (inventory == null) {
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Please run " + ChatColor.GOLD + ChatColor.BOLD + "/portal inventory");
                        return true;
                    }

                    boolean portalFound = false;

                    for (int i = 0; i < inventory.getSize(); i++) {
                        ItemStack item = inventory.getItem(i);
                        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) continue;

                        List<String> lore = item.getItemMeta().getLore();
                        if (lore.size() < 2) continue;

                        String loreLine = ChatColor.stripColor(lore.get(1));
                        String Identifier = loreLine.replace("Portal-Key: ", "");

                        if (inputIdentifier.equals(Identifier)) {
                            portalFound = true;

                            ItemStack clonedItem = item.clone();
                            ItemMeta meta = clonedItem.getItemMeta();
                            if (meta != null) {
                                meta.setLore(null);
                                clonedItem.setItemMeta(meta);
                                inventory.setItem(i, clonedItem);
                            }

                            String configPath = "portal." + player.getUniqueId() + "." + Identifier;
                            if (plugin.getConfig().contains(configPath)) {
                                plugin.getConfig().set(configPath, null);
                            }

                            if (modify.contains(player.getUniqueId() + "." + Identifier + ".color")) {
                                modify.set(player.getUniqueId() + "." + Identifier + ".color", null);
                            }
                            if (modify.contains(player.getUniqueId() + "." + Identifier)) {
                                modify.set(player.getUniqueId() + "." + Identifier, null);
                            }

                            modify.set(player.getUniqueId() + ".inventory", PortalInventoryManager.toPortalBase64(inventory));

                            player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "Portal Disbanded!");
                            break;
                        }
                    }

                    if (!portalFound) {
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There is no portal with that key.");
                        return true;
                    }

                    plugin.saveConfig();
                    try {
                        modify.save(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (args[0].equalsIgnoreCase("spawn")) {
                    String inputIdentifier = args[1];
                    File file = new File(plugin.getDataFolder(), "InventoryData.yml");
                    YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
                    Inventory inventory = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + ".inventory"));
                    if (inventory == null) {
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Please Run " + ChatColor.GOLD + ChatColor.BOLD + "/portal inventory");
                        return true;
                    }
                    boolean portalFound = false;
                    for (int i = 0; i < inventory.getSize(); i++) {
                        if (inventory.getItem(i) == null) continue;
                        if (!inventory.getItem(i).hasItemMeta()) continue;
                        if (!inventory.getItem(i).getItemMeta().hasLore()) continue;

                        String inputString = inventory.getItem(i).getItemMeta().getLore().get(1);
                        String strippedString = ChatColor.stripColor(inputString);
                        String Identifier = strippedString.replace("Portal-Key: ", "");
                        if (inputIdentifier.equals(Identifier)) {
                            portalFound = true;

                            if (plugin.getConfig().contains("portal." + player.getUniqueId() + "." + Identifier + ".destination")
                                    && plugin.getConfig().contains("portal." + player.getUniqueId() + "." + Identifier + ".home") &&
                                    plugin.getConfig().getBoolean("portal." + player.getUniqueId() + "." + Identifier + ".has-destination")
                                    && plugin.getConfig().getBoolean("portal." + player.getUniqueId() + "." + Identifier + ".has-home")) {

                                Material woolType = null;
                                Inventory colorInv = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + "." + Identifier + ".color"));
                                if (colorInv == null) {
                                    woolType = Material.RED_WOOL;
                                } else {
                                    for (ItemStack item : colorInv) {
                                        if (item.getItemMeta().hasEnchants()) {
                                            woolType = item.getType();
                                        }
                                    }
                                }
                                Location homeLocation;
                                if (!plugin.getConfig().getBoolean("portal." + player.getUniqueId() + "." + Identifier + ".is-home-free")) {
                                    String homeString = plugin.getConfig().getString("portal." + player.getUniqueId() + "." + Identifier + ".home");
                                    String[] homeParts = homeString.split(", ");
                                    World homeWorld = Bukkit.getWorld(homeParts[0]);
                                    double homeX = Double.parseDouble(homeParts[1]);
                                    double homeY = Double.parseDouble(homeParts[2]);
                                    double homeZ = Double.parseDouble(homeParts[3]);
                                    homeLocation = new Location(homeWorld, homeX, homeY, homeZ);
                                } else
                                    homeLocation = player.getLocation().clone().add(player.getFacing().getDirection().normalize().multiply(2)).add(0, 1.5, 0);

                                if (plugin.getConfig().getBoolean("portal." + player.getUniqueId() + "." + Identifier + ".has-destination")) {

                                    String destinationString = plugin.getConfig().getString("portal." + player.getUniqueId() + "." + Identifier + ".destination");
                                    String[] destinationParts = destinationString.split(", ");
                                    World destinationWorld = Bukkit.getWorld(destinationParts[0]);
                                    double destinationX = Double.parseDouble(destinationParts[1]);
                                    double destinationY = Double.parseDouble(destinationParts[2]);
                                    double destinationZ = Double.parseDouble(destinationParts[3]);

                                    Location destinationLocation = new Location(destinationWorld, destinationX, destinationY, destinationZ);
                                    Portal.createPortal(plugin, player, Identifier, woolType, homeLocation, destinationLocation);
                                    plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".active", true);

                                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                                            plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".active", false),
                                            plugin.getConfig().getInt("portal.portal-duration") * 20L);



                                } else {
                                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Portal Key " + Identifier + "  Does Not Have A Destination");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Portal Does Not Have A Home Or A Destination ");
                            }
                        }
                    }
                    if (!portalFound) {
                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There Is No Portal With That Key");
                        return true;
                    }

                }
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("inventory")) {

                    File file = new File(plugin.getDataFolder(), "InventoryData.yml");
                    YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
                    Inventory inventory = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + ".inventory"));

                    if (modify.contains(player.getUniqueId().toString()) && modify.contains(player.getUniqueId() + ".inventory")
                            && inventory != null && !Objects.equals(modify.getString(player.getUniqueId() + ".inventory"), "")) {
                        player.openInventory(inventory);
                    } else {
                        Inventory newInventory = null;
                        try {
                            newInventory = new PortalInventory(plugin).createInventory(player);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        assert newInventory != null;
                        player.openInventory(newInventory);
                    }
                }


            } else if (args.length == 4) {
                if (args[0].equalsIgnoreCase("set")) {
                    if (args[1].equalsIgnoreCase("home")) {
                        if (args[2].equalsIgnoreCase("current")) {
                            String inputIdentifier = args[3];
                            File file = new File(plugin.getDataFolder(), "InventoryData.yml");
                            YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
                            Inventory inventory = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + ".inventory"));
                            if (inventory == null) {
                                try {
                                    inventory = new PortalInventory(plugin).createInventory(player);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            boolean portalFound = false;

                            for (int i = 0; i < inventory.getSize(); i++) {
                                if (inventory.getItem(i) == null) continue;
                                if (!inventory.getItem(i).hasItemMeta()) continue;
                                if (inventory.getItem(i).getItemMeta().hasLore()) {
                                    String inputString = inventory.getItem(i).getItemMeta().getLore().get(1);
                                    String strippedString = ChatColor.stripColor(inputString);
                                    String Identifier = strippedString.replace("Portal-Key: ", "");
                                    if (inputIdentifier.equals(Identifier)) {
                                        Location location = player.getLocation().add(0, 3, 0);
                                        plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".home", location.getWorld().getName() + ", " +
                                                location.getX() + ", " + location.getY() + ", " + location.getZ());
                                        plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".has-home", true);
                                        plugin.saveConfig();
                                        portalFound = true;
                                        player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "Portal Home Set!");
                                        break;
                                    }
                                }
                            }
                            if (!portalFound) {
                                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There Is No Portal With That Key");
                            }

                        } else if (args[2].equalsIgnoreCase("click")) {
                            String inputIdentifier = args[3];
                            File file = new File(plugin.getDataFolder(), "InventoryData.yml");
                            YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
                            Inventory inventory = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + ".inventory"));
                            boolean portalFound = false;
                            for (int i = 0; i < inventory.getSize(); i++) {
                                if (inventory.getItem(i) == null) continue;
                                if (!inventory.getItem(i).hasItemMeta()) continue;
                                if (inventory.getItem(i).getItemMeta().hasLore()) {
                                    String inputString = inventory.getItem(i).getItemMeta().getLore().get(1);
                                    String strippedString = ChatColor.stripColor(inputString);
                                    String Identifier = strippedString.replace("Portal-Key: ", "");
                                    if (playerHomeClick.containsValue(player)) {
                                        playerHomeClick.inverse().replace(player, Identifier);
                                    } else {
                                        playerHomeClick.put(Identifier, player);
                                    }
                                    if (inputIdentifier.equals(Identifier)) {
                                        portalFound = true;
                                        new BukkitRunnable() {
                                            int counter = 0;
                                            final String message = ChatColor.RED + ChatColor.BOLD.toString() + "Click The Block That You Would Like To Set As Your Home!";

                                            @Override
                                            public void run() {
                                                counter += 1;
                                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                                                if (!playerHomeClick.containsValue(player)) {
                                                    cancel();
                                                } else if (counter >= 10) {
                                                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You Took To Long!");
                                                    cancel();
                                                }
                                            }

                                        }.runTaskTimer(plugin, 0, 20L);
                                        break;
                                    }
                                }
                            }

                            if (!portalFound) {
                                player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There Is No Portal With That Key");
                            }


                        } else if (args[2].equalsIgnoreCase("free")) {
                            String inputIdentifier = args[3];
                            File file = new File(plugin.getDataFolder(), "InventoryData.yml");
                            YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
                            Inventory inventory = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + ".inventory"));
                            Location location = player.getLocation().clone();

                            for (int i = 0; i < inventory.getSize(); i++) {
                                if (inventory.getItem(i) == null) continue;
                                if (!inventory.getItem(i).hasItemMeta()) continue;
                                if (inventory.getItem(i).getItemMeta().hasLore()) {
                                    String inputString = inventory.getItem(i).getItemMeta().getLore().get(1);
                                    String strippedString = ChatColor.stripColor(inputString);
                                    String Identifier = strippedString.replace("Portal-Key: ", "");
                                    if (inputIdentifier.equals(Identifier)) {
                                        plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".is-home-free", true);
                                        plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".home", location.getWorld().getName() + ", " +
                                                location.getX() + ", " + location.getY() + ", " + location.getZ());
                                        plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".has-home", true);
                                        player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "Home Has Been Set To Free!");
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                                    }
                                }
                            }
                            plugin.saveConfig();
                            }
                        } else if (args[1].equalsIgnoreCase("destination")) {
                            if (args[2].equalsIgnoreCase("current")) {
                                String inputIdentifier = args[3];
                                File file = new File(plugin.getDataFolder(), "InventoryData.yml");
                                YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
                                Inventory inventory = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + ".inventory"));
                                boolean portalFound = false;
                                for (int i = 0; i < inventory.getSize(); i++) {
                                    if (inventory.getItem(i) == null) continue;
                                    if (!inventory.getItem(i).hasItemMeta()) continue;
                                    if (inventory.getItem(i).getItemMeta().hasLore()) {
                                        String inputString = inventory.getItem(i).getItemMeta().getLore().get(1);
                                        String strippedString = ChatColor.stripColor(inputString);
                                        String Identifier = strippedString.replace("Portal-Key: ", "");
                                        if (inputIdentifier.equals(Identifier)) {
                                            Location location = player.getLocation().add(0, 3, 0);
                                            plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".destination", location.getWorld().getName() + ", " +
                                                    location.getX() + ", " + location.getY() + ", " + location.getZ());
                                            plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".has-destination", true);

                                            plugin.saveConfig();
                                            portalFound = true;
                                            player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "Portal Destination Set!");
                                            break;
                                        }
                                    }
                                }
                                if (!portalFound) {
                                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There Is No Portal With That Key");
                                }

                            } else if (args[2].equalsIgnoreCase("click")) {
                                String inputIdentifier = args[3];
                                File file = new File(plugin.getDataFolder(), "InventoryData.yml");
                                YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
                                Inventory inventory = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + ".inventory"));
                                boolean portalFound = false;
                                for (int i = 0; i < inventory.getSize(); i++) {
                                    if (inventory.getItem(i) == null) continue;
                                    if (!inventory.getItem(i).hasItemMeta()) continue;
                                    if (inventory.getItem(i).getItemMeta().hasLore()) {
                                        String inputString = inventory.getItem(i).getItemMeta().getLore().get(1);
                                        String strippedString = ChatColor.stripColor(inputString);
                                        String Identifier = strippedString.replace("Portal-Key: ", "");
                                        if (playerDestinationClick.containsValue(player)) {
                                            playerDestinationClick.inverse().replace(player, Identifier);
                                        } else {
                                            playerDestinationClick.put(Identifier, player);
                                        }
                                        if (inputIdentifier.equals(Identifier)) {
                                            portalFound = true;
                                            new BukkitRunnable() {
                                                int counter = 0;
                                                final String message = ChatColor.RED + ChatColor.BOLD.toString() + "Click The Block That You Would Like To Set As Your Destination!";

                                                @Override
                                                public void run() {
                                                    counter += 1;
                                                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
                                                    if (!playerDestinationClick.containsValue(player)) {
                                                        cancel();
                                                    } else if (counter >= 10) {
                                                        player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "You Took To Long!");
                                                        cancel();
                                                    }
                                                }

                                            }.runTaskTimer(plugin, 0, 20L);
                                            break;
                                        }
                                    }
                                }

                                if (!portalFound) {
                                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There Is No Portal With That Key");
                                }
                            }

                        } else if (args[1].equalsIgnoreCase("shape")) {
                            if (!Objects.equals(CustomPortals.fromShapeName(args[2]), null)) {
                                CustomPortals portal = CustomPortals.fromShapeName(args[2]);
                                String inputIdentifier = args[3];
                                File file = new File(plugin.getDataFolder(), "InventoryData.yml");
                                YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
                                Inventory inventory = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + ".inventory"));

                                boolean portalFound = false;
                                for (int i = 0; i < inventory.getSize(); i++) {
                                    if (inventory.getItem(i) == null) continue;
                                    if (!inventory.getItem(i).hasItemMeta()) continue;
                                    if (inventory.getItem(i).getItemMeta().hasLore()) {
                                        String inputString = inventory.getItem(i).getItemMeta().getLore().get(1);
                                        String strippedString = ChatColor.stripColor(inputString);
                                        String Identifier = strippedString.replace("Portal-Key: ", "");

                                        if (inputIdentifier.equals(Identifier)) {
                                            portalFound = true;
                                            plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".shape", portal.getShapeName());
                                            plugin.saveConfig();
                                            player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "Portal Shape Set!");
                                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 10, 29);

                                            break;
                                        }
                                    }
                                }

                                if (!portalFound) {
                                    player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "There Is No Portal With That Key");
                                }
                        } else {player.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "That Shape Is Currently Unavailable");}

                    }

                }
            }
        }
        return false;
    }
}


