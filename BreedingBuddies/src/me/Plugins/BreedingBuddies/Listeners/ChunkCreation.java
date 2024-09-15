package me.Plugins.BreedingBuddies.Listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

import me.Plugins.BreedingBuddies.ChunkArea;
import me.Plugins.BreedingBuddies.ChunkManager;
import me.Plugins.BreedingBuddies.ItemUtils;
import me.Plugins.BreedingBuddies.PluginData;
import me.Plugins.BreedingBuddies.SoundManager;
import me.Plugins.BreedingBuddies.Configurables.CustomItems;
import me.Plugins.BreedingBuddies.Configurables.Messages;

public class ChunkCreation implements Listener {
	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			ItemStack itemInHand = event.getItem();
			if (itemInHand != null && ItemUtils.getItemId(itemInHand).equalsIgnoreCase(CustomItems.stableChunkItem)) {
				Player player = event.getPlayer();
				if (player != null) {
					Chunk chunk = player.getLocation().getChunk();
					if (!ChunkManager.isStableChunk(chunk) && chunk != null) {
						ChunkManager.addChunk(chunk);
						itemInHand.setAmount(itemInHand.getAmount() - 1);
						player.sendMessage(Messages.stableChunk);
						SoundManager.playSignSound(player);

					} else if (chunk != null) {
						player.sendMessage(Messages.alreadyStableChunk);
					}
				}
				event.setCancelled(true);
			}
		}
	}
}
