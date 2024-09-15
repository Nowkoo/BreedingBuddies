package me.Plugins.BreedingBuddies.Listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.Plugins.BreedingBuddies.ChunkManager;
import me.Plugins.BreedingBuddies.FarmAnimal;
import me.Plugins.BreedingBuddies.ItemUtils;
import me.Plugins.BreedingBuddies.OwnershipManager;
import me.Plugins.BreedingBuddies.PluginData;
import me.Plugins.BreedingBuddies.SoundManager;
import me.Plugins.BreedingBuddies.Configurables.CustomItems;
import me.Plugins.BreedingBuddies.Configurables.Messages;
import me.Plugins.BreedingBuddies.Configurables.Numbers;

public class FarmReport implements Listener {
	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			ItemStack item = event.getItem();
			if (item != null && ItemUtils.getItemId(item).equalsIgnoreCase(CustomItems.farmReportItem)) {
				Player player = event.getPlayer();
				generateReport(player);
				SoundManager.playBookOpenSound(player);
			}
		}
	}
	
	public void generateReport(Player player) {
		boolean animalsOut = false;
		boolean spaceNeeded = false;
		boolean waterNeeded = false;
		int animalsFed = 0;
		int animalsCared = 0;
		List<FarmAnimal> playerAnimals = OwnershipManager.getAnimals(player.getUniqueId());
		for (FarmAnimal animal : playerAnimals) {
			if (!animal.isSleptInStable()) animalsOut = true;
			if (animal.isSpaceNeeded()) spaceNeeded = true;
			if (animal.isWaterNeeded()) waterNeeded = true;
			if (animal.getFed()) animalsFed++; 
			if (animal.getCared()) animalsCared++; 
		}
		player.sendMessage(ChatColor.GOLD + "§nFarm Report:");
		player.sendMessage(String.format(Messages.animalsFed, animalsFed));
		player.sendMessage(String.format(Messages.animalsCared, animalsCared));
		player.sendMessage(String.format(Messages.animalsOwned, playerAnimals.size()));
		if (animalsOut) player.sendMessage(Messages.animalsOut);
		if (spaceNeeded) player.sendMessage(Messages.spaceNeeded);
		if (waterNeeded) player.sendMessage(Messages.waterNeeded);
	}
}
