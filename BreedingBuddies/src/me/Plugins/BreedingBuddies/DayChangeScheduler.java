package me.Plugins.BreedingBuddies;

import java.time.LocalTime;
import java.time.ZoneId;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.Plugins.BreedingBuddies.Events.DayChangeEvent;

public class DayChangeScheduler extends BukkitRunnable {
    private final JavaPlugin plugin;
    private final int targetHour;
    private final int targetMinute = 30;
    private LocalTime lastCheckTime;

    public DayChangeScheduler(JavaPlugin plugin, int targetHour) {
        this.plugin = plugin;
        this.targetHour = targetHour;
        this.lastCheckTime = LocalTime.now(ZoneId.systemDefault());
    }

    @Override
    public void run() {
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
//        Bukkit.broadcastMessage("Current time: " + now.toString());
        if (now.getHour() == targetHour && now.getMinute() == targetMinute 
                && (lastCheckTime.getMinute() != targetMinute)) {
//            Bukkit.broadcastMessage("DayChangeEvent is being called.");
            plugin.getServer().getPluginManager().callEvent(new DayChangeEvent());
        }
        lastCheckTime = now;
    }

    public void startScheduler() {
        this.runTaskTimer(plugin, 0L, 1200L);
    }
}
