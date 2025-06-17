package com.pessulum.particleportals;

import com.pessulum.particleportals.Commands.PortalCommands;
import com.pessulum.particleportals.Commands.TabCompletion;
import com.pessulum.particleportals.InventoryManagement.PortalInventoryListener;
import com.pessulum.particleportals.Listener.PortalListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class ParticlePortals extends JavaPlugin {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveConfig();
        this.getCommand("portal").setExecutor(new PortalCommands(this));
        this.getServer().getPluginManager().registerEvents(new PortalListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PortalInventoryListener(this), this);
        this.getCommand("portal").setTabCompleter(new TabCompletion(this));

        File file = new File(getDataFolder(), "InventoryData.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            } 
        }


    }

}
