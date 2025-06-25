package BreedingBuddies.Listeners;

import BreedingBuddies.Configurables.CustomItems;
import BreedingBuddies.Configurables.Messages;
import BreedingBuddies.Configurables.Numbers;
import BreedingBuddies.FarmAnimal;
import BreedingBuddies.ItemUtils;
import BreedingBuddies.OwnershipManager;
import BreedingBuddies.ParticleManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

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
					feed(animal, player, itemInHand);
					event.setCancelled(true);
				} else if (ItemUtils.getItemId(itemInHand).equalsIgnoreCase(CustomItems.caringItem)) {
					care(animal, player, itemInHand);
					event.setCancelled(true);
				}
			} else if (OwnershipManager.hasOwner(entity.getUniqueId()) 
					&& !OwnershipManager.isOwner(player.getUniqueId(), entity.getUniqueId())) {
				player.sendMessage(Messages.notOwner);
				event.setCancelled(true);
			}
		}
	}
	
	public void feed(FarmAnimal animal, Player player, ItemStack itemInHand) {
		if (animal.getFed()) {
			player.sendMessage(String.format(Messages.alreadyFed, animal.getName()));
		} else {
			itemInHand.setAmount(itemInHand.getAmount() - 1);
			animal.increaseFriendship(Numbers.friendshipByFeeding);
			animal.setFed(true);
			ParticleManager.heartParticles(animal.getEntity());
		}
	}
	
	public void care(FarmAnimal animal, Player player, ItemStack itemInHand) {
		if (animal.getCared()) {
			player.sendMessage(String.format(Messages.alreadyCared, animal.getName()));
		} else {
//			itemInHand.setAmount(itemInHand.getAmount() - 1);
			animal.increaseFriendship(Numbers.friendshipByCaring);
			animal.setCared(true);
			ParticleManager.heartParticles(animal.getEntity());
		}
	}
}
