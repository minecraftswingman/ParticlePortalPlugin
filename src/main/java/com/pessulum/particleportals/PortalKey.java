package com.pessulum.particleportals;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public class PortalKey {


    private static Inventory colorInventory;
    protected static BiMap<UUID, String> IdentifierHolder = HashBiMap.create();
    UUID uuid;
    String identifier;
    static ParticlePortals plugin;
    public PortalKey(ParticlePortals plugin, UUID uuid, String identifier, Inventory colorInventory) {
        this.uuid = uuid;
        this.identifier = identifier;
        PortalKey.plugin = plugin;
        PortalKey.colorInventory = colorInventory;
    }

    public static PortalKey createPortalKey(ParticlePortals plugin, UUID uuid, String identifier, Inventory colorInventory) {
        return new PortalKey(plugin, uuid, identifier, colorInventory);
    }
    public static BiMap<UUID, String> setMappedKey(UUID uuid, String Identifier) {
        IdentifierHolder.put(uuid, Identifier);
        return IdentifierHolder;
    }
    public static BiMap<UUID, String> getMappedKey() {
        return IdentifierHolder;
    }

   public static void disbandPortalKey(PortalKey portalKey) {
        plugin.getConfig().set(".portal." + portalKey.getIdentifier(), null);
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    public void setColorInventory(Inventory colorInventory) {
        PortalKey.colorInventory = colorInventory;
    }

    public String getIdentifier() {
        return identifier;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Inventory getColorInventory() {
        return colorInventory;
    }






}