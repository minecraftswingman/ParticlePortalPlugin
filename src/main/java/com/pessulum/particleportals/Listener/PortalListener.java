package com.pessulum.particleportals.Listener;

import com.pessulum.particleportals.Commands.PortalCommands;
import com.pessulum.particleportals.ParticlePortals;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PortalListener implements Listener {
    private static ParticlePortals plugin;
    public PortalListener(ParticlePortals plugin) {
        PortalListener.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock() != null && PortalCommands.playerHomeClick.containsValue(e.getPlayer())) {
            Player player = e.getPlayer();
            e.setCancelled(true);
            String Identifier = PortalCommands.playerHomeClick.inverse().get(e.getPlayer());

            plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".home", e.getClickedBlock().getLocation().add(0, 3, 0).getWorld().getName() + ", "
                    + e.getClickedBlock().getLocation().getX() + ", " + (e.getClickedBlock().getLocation().getY() + 3) + ", " + e.getClickedBlock().getLocation().getZ());
            plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".has-home", true);
            plugin.saveConfig();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 29);
            PortalCommands.playerHomeClick.remove(Identifier, e.getPlayer());
            player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "Portal Home Set!");
            e.setCancelled(false);
        } else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock() != null && PortalCommands.playerDestinationClick.containsValue(e.getPlayer())) {
            Player player = e.getPlayer();
            e.setCancelled(true);
            String Identifier = PortalCommands.playerDestinationClick.inverse().get(e.getPlayer());
            plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".destination", e.getClickedBlock().getLocation().getWorld().getName() + ", " +
                    e.getClickedBlock().getLocation().getX() + ", " + (e.getClickedBlock().getLocation().getY() + 3) + ", " + e.getClickedBlock().getLocation().getZ());
            plugin.getConfig().set("portal." + player.getUniqueId() + "." + Identifier + ".has-destination", true);
            plugin.saveConfig();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 29);
            PortalCommands.playerDestinationClick.remove(Identifier, e.getPlayer());
            player.sendMessage(ChatColor.GOLD + ChatColor.BOLD.toString() + "Portal Destination Set!");
            e.setCancelled(false);
        }

    }



}
