package me.Plugins.BreedingBuddies.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.Plugins.BreedingBuddies.FarmAnimal;
import me.Plugins.BreedingBuddies.ItemUtils;
import me.Plugins.BreedingBuddies.OwnershipManager;
import me.Plugins.BreedingBuddies.ParticleManager;
import me.Plugins.BreedingBuddies.Configurables.CustomItems;
import me.Plugins.BreedingBuddies.Configurables.Messages;
import me.Plugins.BreedingBuddies.Configurables.Numbers;

public class AnimalCareListener implements Listener {
	@EventHandler
	public void onInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		ItemStack itemInHand = player.getInventory().getItemInMainHand();
		
		if(event.getHand().equals(EquipmentSlot.HAND)) {
			if (OwnershipManager.isOwner(player.getUniqueId(), entity.getUniqueId())) {
				FarmAnimal animal = OwnershipManager.getAnimal(entity.getUniqueId(), player.getUniqueId());
				if (ItemUtils.getItemId(itemInHand).equalsIgnoreCase(CustomItems.universalFeed)) {
					feed(animal, player, itemInHand, entity);
				} else if (ItemUtils.getItemId(itemInHand).equalsIgnoreCase(CustomItems.caringItem)) {
					care(animal, player, entity);
				}
			} else if (OwnershipManager.hasOwner(entity.getUniqueId()) 
					&& !OwnershipManager.isOwner(player.getUniqueId(), entity.getUniqueId())) {
				player.sendMessage(Messages.notOwner);
				event.setCancelled(true);
			}
		}
	}
	
	public void feed(FarmAnimal animal, Player player, ItemStack itemInHand, Entity entity) {
		if (animal.getFed()) {
			player.sendMessage(String.format(Messages.alreadyFed, animal.getName()));
		} else {
			itemInHand.setAmount(itemInHand.getAmount() - 1);
			animal.increaseFriendship(Numbers.friendshipByFeeding);
			animal.setFed(true);
			ParticleManager.heartParticles(entity);
		}
	}
	
	public void care(FarmAnimal animal, Player player, Entity entity) {
		if (animal.getCared()) {
			player.sendMessage(String.format(Messages.alreadyCared, animal.getName()));
		} else {
			animal.increaseFriendship(Numbers.friendshipByCaring);
			animal.setCared(true);
			ParticleManager.heartParticles(entity);
		}
	}
}
