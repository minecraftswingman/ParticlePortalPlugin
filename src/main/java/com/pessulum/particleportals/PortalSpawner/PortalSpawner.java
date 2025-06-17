package com.pessulum.particleportals.PortalSpawner;

import com.pessulum.particleportals.GeometryUtils.PortalMath;
import com.pessulum.particleportals.InventoryManagement.PortalInventory;
import com.pessulum.particleportals.InventoryManagement.PortalInventoryManager;
import com.pessulum.particleportals.ParticlePortals;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PortalSpawner {

    final static Particle.DustOptions mainDust = new Particle.DustOptions(Color.PURPLE, 1.0F);
    ;
    private static ParticlePortals plugin;
    PortalSpawner(ParticlePortals plugin)  {
        PortalSpawner.plugin = plugin;

    }


    protected static void spawnCubePortal(Player player, Location center, String identifier) {
        File file = new File(plugin.getDataFolder(), "InventoryData.yml");
        YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
        int duration = plugin.getConfig().getInt("portal.portal-duration");
        // Load wool type
        Material woolType = Material.RED_WOOL;
        Inventory colorInv = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + "." + identifier + ".color"));
        if (colorInv != null) {
            for (ItemStack item : colorInv) {
                if (item != null && item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
                    woolType = item.getType();
                    break;
                }
            }
        }

        Particle.DustOptions dustOptions = new Particle.DustOptions(
                PortalInventory.translateWoolColorToColor(woolType), 0.6F
        );

        PortalMath portalMath = new PortalMath(plugin, duration);

        if (portalMath.cachedLocalCubeVectors[0][0] == null) {
            portalMath.cacheCubePortalData(center, 0.5, 0.1);
        }

        new BukkitRunnable() {
            int tick = 0;

            public void run() {
                if (tick++ >= duration * 20) {
                    cancel();
                    return;
                }

                for (int i = 0; i < PortalMath.MAX_LAYERS; i++) {
                    for (int j = 0; j < PortalMath.MAX_PARTICLES; j++) {
                        Vector vec = portalMath.cachedLocalCubeVectors[i][j];
                        if (vec == null) continue;


                        Location loc = center.clone().add(portalMath.cachedRotatedCubeLocations[i][j]);
                        loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 0, dustOptions);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }


protected static void spawnSquarePortal(Player player, Location location, String Identifier, int rotatingAngle) {
    File file = new File(plugin.getDataFolder(), "InventoryData.yml");
    YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
    int duration = plugin.getConfig().getInt("portal.portal-duration");

    Material woolType = null;
    Inventory colorInv = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + "." + Identifier + ".color"));
    if (colorInv == null){ woolType = Material.RED_WOOL;}
    else {
        for (ItemStack item : colorInv) {
            if (item.getItemMeta().hasEnchants()) {
                woolType = item.getType();
            }
        }
    }

    Particle.DustOptions dustOptions = new Particle.DustOptions(PortalInventory.translateWoolColorToColor(woolType), 0.8F);
    PortalMath portalMath =  new PortalMath(plugin, plugin.getConfig().getInt("portal.portal-duration"));
    if (portalMath.cachedSquareFrames.isEmpty()) {portalMath.cacheSquarePortalData(rotatingAngle);}
    new BukkitRunnable() {
        int ticks = 0;
        int frameIndex = 0;

        @Override
        public void run() {
            if (++ticks >= duration * 20) {
                cancel();
                return;
            }
            List<Vector> frame = portalMath.cachedSquareFrames.get(frameIndex);
            List<Vector> outline = portalMath.cachedSquareOutline.get(frameIndex);

            for (Vector vec : outline) {
                Location loc = location.clone().add(vec);
                loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 0, mainDust);
                }

            for (Vector vec : frame) {
                Location loc = location.clone().add(vec);
                loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 0, dustOptions);

            }

            frameIndex = (frameIndex + 1) % portalMath.cachedSquareFrames.size();
        }
    }.runTaskTimer(plugin, 0L, 1L);

}

protected static void spawnHexagonPortal(Player player, Location location, String Identifier,int rotatingAngle) {
    File file = new File(plugin.getDataFolder(), "InventoryData.yml");
    YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
    int duration = plugin.getConfig().getInt("portal.portal-duration");

    Material woolType = null;
    Inventory colorInv = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + "." + Identifier + ".color"));
    if (colorInv == null){ woolType = Material.RED_WOOL;}
    else {
        for (ItemStack item : colorInv) {
            if (item.getItemMeta().hasEnchants()) {
                woolType = item.getType();
            }
        }
    }

    Particle.DustOptions dustOptions = new Particle.DustOptions(PortalInventory.translateWoolColorToColor(woolType), 0.8F);
    PortalMath portalMath =  new PortalMath(plugin, plugin.getConfig().getInt("portal.portal-duration"));
    if (portalMath.cachedHexagonFrames.isEmpty()) {portalMath.cacheHexagonPortalData(rotatingAngle);}
    Location loc = location.clone();
    new BukkitRunnable() {
        int ticks = 0;
        int frameIndex = 0;

        @Override
        public void run() {
            if (++ticks >= duration * 20) {
                cancel();
                return;
            }
            List<Vector> frame = portalMath.cachedHexagonFrames.get(frameIndex);
            List<Vector> outline = portalMath.cachedHexagonOutline.get(frameIndex);
            if (plugin.getConfig().getBoolean("portal.has-outline"))
                for (Vector vec : outline) {
                    Location loc2 = location.clone().add(vec);
                    loc2.getWorld().spawnParticle(Particle.REDSTONE, loc2, 0, mainDust);
                }
            else outline.forEach(vec ->  loc.getWorld().spawnParticle(Particle.REDSTONE, loc.add(vec), 0, dustOptions));


            for (Vector vec : frame) {
                Location loc = location.clone().add(vec);
                loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 0, dustOptions);

            }

            frameIndex = (frameIndex + 1) % portalMath.cachedHexagonFrames.size();
        }
    }.runTaskTimer(plugin, 0L, 1L);

}


protected static void spawnTrianglePortal(Player player, Location location ,String Identifier ,int rotatingAngle) {
    File file = new File(plugin.getDataFolder(), "InventoryData.yml");
    YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
    int duration = plugin.getConfig().getInt("portal.portal-duration");

    Material woolType = null;
    Inventory colorInv = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + "." + Identifier + ".color"));
    if (colorInv == null){ woolType = Material.RED_WOOL;}
    else {
        for (ItemStack item : colorInv) {
            if (item.getItemMeta().hasEnchants()) {
                woolType = item.getType();
            }
        }
    }

    Particle.DustOptions dustOptions = new Particle.DustOptions(PortalInventory.translateWoolColorToColor(woolType), 0.8F);
    PortalMath portalMath =  new PortalMath(plugin, plugin.getConfig().getInt("portal.portal-duration"));
    if (portalMath.cachedTriangleFrames.isEmpty()) {portalMath.cacheTrianglePortalData(rotatingAngle);}
    Location loc = location.clone();
    new BukkitRunnable() {
        int ticks = 0;
        int frameIndex = 0;

        @Override
        public void run() {
            if (++ticks >= duration * 20) {
                cancel();
                return;
            }
            List<Vector> frame = portalMath.cachedTriangleFrames.get(frameIndex);
            List<Vector> outline = portalMath.cachedTriangleOutline.get(frameIndex);
            if (plugin.getConfig().getBoolean("portal.has-outline"))
                for (Vector vec : outline) {
                    Location loc2 = location.clone().add(vec);
                    loc2.getWorld().spawnParticle(Particle.REDSTONE, loc2, 0, mainDust);
                }
            else outline.forEach(vec ->  loc.getWorld().spawnParticle(Particle.REDSTONE, loc.add(vec), 0, dustOptions));


            for (Vector vec : frame) {
                Location loc = location.clone().add(vec);
                loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 0, dustOptions);

            }

            frameIndex = (frameIndex + 1) % portalMath.cachedTriangleFrames.size();
        }
    }.runTaskTimer(plugin, 0L, 1L);

}


protected static void spawnTorusPortal(Player player, String Identifier,Location location) {
    File file = new File(plugin.getDataFolder(), "InventoryData.yml");
    YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);

    Material woolType = null;
    Inventory colorInv = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + "." + Identifier + ".color"));
    if (colorInv == null){ woolType = Material.RED_WOOL;}
    else {
        for (ItemStack item : colorInv) {
            if (item.getItemMeta().hasEnchants()) {
                woolType = item.getType();
            }
        }
    }
    int duration = plugin.getConfig().getInt("portal.portal-duration");
    Location loc = location.clone();

    Particle.DustOptions dustOptions = new Particle.DustOptions(PortalInventory.translateWoolColorToColor(woolType), 4.5F);
    Particle.DustOptions dustOptions2 = new Particle.DustOptions(Color.PURPLE, 4.8F);
    PortalMath portalMath = new PortalMath(plugin, plugin.getConfig().getInt("portal.portal-duration"));

    if (portalMath.cachedTorusResults.isEmpty()) portalMath.cacheTorusPortalData(location);

    new BukkitRunnable() {
        private int counter = 0;
        public void run() {
            for (int i  = 0; i < portalMath.cachedTorusResults.size(); i++) {
                if (i % 2 == 0) {
                    double[] position = portalMath.cachedTorusResults.get(i);
                    loc.add(position[0], position[1], position[2]);
                    loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 0, dustOptions);
                    loc.subtract(position[0], position[1], position[2]);
                }
            }

            for (int i  = 0; i < portalMath.cachedCircleResults.size(); i++) {
                if (i % 4 == 0) {
                    double[] position = portalMath.cachedCircleResults.get(i);
                    location.add(position[0], position[1], position[2]);
                    location.getWorld().spawnParticle(Particle.REDSTONE, location, 0, dustOptions2);
                    location.subtract(position[0], position[1], position[2]);
                }
            }
            counter++;
            if ((double) counter / 20 >= duration) cancel();
        }
    }.runTaskTimer(plugin, 0, 1L);
}
protected static void spawnSpherePortal(Player player, String Identifier, Location location) {
    File file = new File(plugin.getDataFolder(), "InventoryData.yml");
    YamlConfiguration modify = YamlConfiguration.loadConfiguration(file);
    Material woolType = null;

    Inventory colorInv = PortalInventoryManager.fromBase64(modify.getString(player.getUniqueId() + "." + Identifier + ".color"));
    if (colorInv == null) {
        woolType = Material.RED_WOOL;
    } else {
        for (ItemStack item : colorInv) {
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
                woolType = item.getType();
                break;
            }
        }
    }

    Location loc = location.clone();
    int duration = plugin.getConfig().getInt("portal.portal-duration");

    PortalMath portalMath = new PortalMath(plugin, plugin.getConfig().getInt("portal.portal-duration"));
    Particle.DustOptions dustOptions = new Particle.DustOptions(PortalInventory.translateWoolColorToColor(woolType), 1F);

    if (portalMath.cachedSphereResults.isEmpty()) portalMath.cacheSpherePortalData(location);


    new BukkitRunnable() {
        int counter = 0;

        @Override
        public void run() {
            if (counter >= duration * 20) {
                cancel();
                return;
            }


            int startIndex = counter * portalMath.cachedSphereBaseLocations.size();
            int endIndex = Math.min(startIndex + portalMath.cachedSphereBaseLocations.size(), portalMath.cachedSphereResults.size());

            for (int i = startIndex; i < endIndex; i++) {
                double[] rotated = portalMath.cachedSphereResults.get(i);
                Location particleLoc = location.clone().add(rotated[0], rotated[1], rotated[2]);
                particleLoc.getWorld().spawnParticle(Particle.REDSTONE, particleLoc, 0, dustOptions);
            }

            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 0, mainDust);

            counter++;
        }
    }.runTaskTimer(plugin, 0, 1L);


}

}
