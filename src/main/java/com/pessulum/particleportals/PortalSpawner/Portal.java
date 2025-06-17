package com.pessulum.particleportals.PortalSpawner;


import com.pessulum.particleportals.ParticlePortals;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;
public class Portal {
    public static HashMap<UUID, Double> cooldowns = new HashMap<>();
    public static HashMap<UUID, Boolean> portalRunMap = new HashMap<>();


    public static void createPortal (ParticlePortals plugin, Player player, String Identifier, Material woolType, Location startingLocation, Location endingLocation) {
        if (checkCoolDown(player)) {
            spawnPortal(plugin, player, Identifier, woolType, startingLocation, endingLocation);
                spawnPortal(plugin, player, Identifier, woolType, endingLocation, startingLocation);

        }
        else if (!cooldowns.containsKey(player.getUniqueId())){
            spawnPortal(plugin, player, Identifier, woolType, startingLocation, endingLocation);
            spawnPortal(plugin, player, Identifier, woolType, endingLocation, startingLocation);
            setCoolDown(player, plugin.getConfig().getInt("portal.command-cooldown"));
            return;
        } else {
            player.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Please Wait " + (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis() / 1000));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_FALL, 1.0F, 1.0F);
            return;
        }
        setCoolDown(player, plugin.getConfig().getInt("portal.command-cooldown"));


    }


    public static void setCoolDown(Player player, int seconds) {
        double delay =(double)(System.currentTimeMillis() / 1000 + seconds);
        cooldowns.put(player.getUniqueId(), delay);
    }

    public static boolean checkCoolDown(Player player) {
        if (cooldowns.containsKey(player.getUniqueId())) {
            return cooldowns.get(player.getUniqueId()) <= (double)System.currentTimeMillis() / 1000;
        }
        return false;
    }



    public static void spawnPortal(ParticlePortals plugin, Player player, String Identifier,Material woolType ,Location startingLoc, Location endingLoc) {
        PortalSpawner spawner = new PortalSpawner(plugin);
        double duration  = plugin.getConfig().getDouble("portal.portal-duration");
        String shapeName = plugin.getConfig().getString("portal." + player.getUniqueId() + "." + Identifier + ".shape");
        int rotatingAngle = plugin.getConfig().getInt("portal.rotation");
        CustomPortals portalShape = CustomPortals.fromShapeName(shapeName);
        Location clonedLoc = startingLoc.clone();
            switch (portalShape) {
                case CUBE:
                    PortalSpawner.spawnCubePortal(player, clonedLoc, Identifier);
                    break;
                case SQUARE:
                    PortalSpawner.spawnSquarePortal(player, clonedLoc, Identifier, rotatingAngle);
                    break;
                case TRIANGLE:
                    PortalSpawner.spawnTrianglePortal(player, clonedLoc, Identifier, rotatingAngle);
                    break;
                case TORUS:
                    PortalSpawner.spawnTorusPortal(player, Identifier, clonedLoc);
                    break;
                case SPHERE:
                    PortalSpawner.spawnSpherePortal(player, Identifier, clonedLoc);
                    break;
                default:
                    PortalSpawner.spawnHexagonPortal(player, clonedLoc, Identifier, rotatingAngle);
                    break;
            }

        new BukkitRunnable() {
            int counter = 0;
            final double durationTicks = duration * 20;
            final UUID uuid = player.getUniqueId();

            @Override
            public void run() {
                if (counter >= durationTicks) {
                    Portal.portalRunMap.remove(uuid);
                    this.cancel();
                    return;
                }

                if (Portal.portalRunMap.getOrDefault(uuid, false)) {
                    counter++;
                    return;
                }

                if (player.getLocation().distance(startingLoc) <= 2) {
                    Portal.portalRunMap.put(uuid, true);

                    player.teleport(endingLoc.clone().add(0, 0.1, 0));
                    player.setVelocity(endingLoc.getDirection().multiply(0.5));
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0F, 1.0F);
                    teleportationMessage(player, startingLoc, endingLoc);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Portal.portalRunMap.remove(uuid);
                    }, 20L);

                    this.cancel();
                    return;
                }

                counter++;
            }
        }.runTaskTimer(plugin, 0L, 1L);


    }

    private static void teleportationMessage(Player player, Location startingLoc, Location endingLoc) {
        player.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "Teleported successfully!");
        player.sendMessage(ChatColor.GRAY + "From: " + formatLocation(startingLoc));
        player.sendMessage(ChatColor.GRAY + "To: " + formatLocation(endingLoc));
    }

    private static String formatLocation(Location loc) {
        return ChatColor.YELLOW + "x: " + ChatColor.WHITE + loc.getBlockX() + ChatColor.YELLOW +
                ", y: " + ChatColor.WHITE + loc.getBlockY() + ChatColor.YELLOW +
                ", z: " + ChatColor.WHITE + loc.getBlockZ();
    }


}
