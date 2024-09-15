package me.Plugins.BreedingBuddies.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.Plugins.BreedingBuddies.ItemUtils;
import me.Plugins.BreedingBuddies.Configurables.CustomItems;

public class AnvilRenameListener implements Listener {
    private final JavaPlugin plugin;

    public AnvilRenameListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAnvilRename(PrepareAnvilEvent event) {
    	ItemStack item = event.getResult();
    	String renameText = event.getInventory().getRenameText();
        if (item != null && renameText != null && ItemUtils.getItemId(item).equalsIgnoreCase(CustomItems.tamingItem)) {
            ItemUtils.storeDataInItem(item, renameText, plugin);
        }
    }
}
