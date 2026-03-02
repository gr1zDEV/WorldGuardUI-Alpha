package com.ezinnovations.ezwggui.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SchedulerUtil {
    private final Plugin plugin;
    private final boolean folia;

    public SchedulerUtil(Plugin plugin) {
        this.plugin = plugin;
        this.folia = isFolia();
    }

    private boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void runTask(Player player, Runnable runnable) {
        if (folia) {
            player.getScheduler().run(plugin, task -> runnable.run(), null);
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public void runTaskLater(Player player, Runnable runnable, long ticks) {
        if (folia) {
            player.getScheduler().runDelayed(plugin, task -> runnable.run(), null, ticks);
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, runnable, ticks);
    }

    public void runAtLocation(Location location, Runnable runnable) {
        if (folia) {
            Bukkit.getRegionScheduler().run(plugin, location, task -> runnable.run());
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }

    public void runForEntity(Entity entity, Runnable runnable) {
        if (folia) {
            entity.getScheduler().run(plugin, task -> runnable.run(), null);
            return;
        }
        Bukkit.getScheduler().runTask(plugin, runnable);
    }
}
