package com.pessulum.particleportals.InventoryManagement;

import com.pessulum.particleportals.ParticlePortals;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class PortalInventoryManager {
    ParticlePortals plugin;
    public PortalInventoryManager(ParticlePortals plugin) {
        this.plugin = plugin;
    }


    public static String toPortalBase64(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream data = new BukkitObjectOutputStream(outputStream);
            data.writeInt(inventory.getSize());
            data.writeObject("Portals");
            for (int i = 0; i < inventory.getSize(); i++) {
                data.writeObject(inventory.getItem(i));
            }
            data.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }public static String toColorPortalBase64(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream data = new BukkitObjectOutputStream(outputStream);
            data.writeInt(inventory.getSize());
            data.writeObject("Portal-Colors");
            for (int i = 0; i < inventory.getSize(); i++) {
                data.writeObject(inventory.getItem(i));
            }
            data.close();

            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static Inventory fromBase64(String base64) {

        if (base64 == null) return null;
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
            BukkitObjectInputStream data = new BukkitObjectInputStream(stream);
            int size = data.readInt();
            if (size % 9 != 0) return null;

            Inventory inventory = Bukkit.createInventory(null, size, data.readObject().toString());

            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) data.readObject());
            }
            data.close();

            return inventory;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
