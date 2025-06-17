package com.pessulum.particleportals.InventoryManagement;

import com.pessulum.particleportals.ParticlePortals;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PortalInventoryAnimation {
    ParticlePortals plugin;
    Inventory inventory;
    Player player;
    static Queue<ItemStack> animatedItemsQueue = new LinkedList<>();

    public PortalInventoryAnimation(ParticlePortals plugin, Inventory inventory, Player player) {
        this.plugin = plugin;
        this.inventory = inventory;
        this.player = player;
    }

    static {
        animatedItemsQueue.add(new ItemStack(Material.WHITE_STAINED_GLASS));
        animatedItemsQueue.add(new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS));
        animatedItemsQueue.add(new ItemStack(Material.BLACK_STAINED_GLASS));
        animatedItemsQueue.add(new ItemStack(Material.RED_STAINED_GLASS));
        animatedItemsQueue.add(new ItemStack(Material.ORANGE_STAINED_GLASS));
        animatedItemsQueue.add(new ItemStack(Material.YELLOW_STAINED_GLASS));
        animatedItemsQueue.add(new ItemStack(Material.GREEN_STAINED_GLASS));
        animatedItemsQueue.add(new ItemStack(Material.LIME_STAINED_GLASS));
        animatedItemsQueue.add(new ItemStack(Material.PURPLE_STAINED_GLASS));
        animatedItemsQueue.add(new ItemStack(Material.BLUE_STAINED_GLASS));
        animatedItemsQueue.add(new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS));
    }

    public void addAnimation() {
        new BukkitRunnable() {
            final List<Integer> animationSlots = getEmptySlots();

            @Override
            public void run() {
                ItemStack targetItem = animatedItemsQueue.poll();
                animationSlots.forEach(slot -> {
                    inventory.setItem(slot, targetItem);
                });

                animatedItemsQueue.offer(targetItem);

            }
        }.runTaskTimer(plugin, 0, 20L);
    }


    public List<Integer> getEmptySlots() {
        List<Integer> emptySlots = new ArrayList<>();
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null || contents[i].getType() == Material.AIR) {
                emptySlots.add(i);
            }
        }
        return emptySlots;
    }
}


