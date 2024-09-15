package me.Plugins.BreedingBuddies.Listeners;

import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.Plugins.BreedingBuddies.AnimalStates;
import me.Plugins.BreedingBuddies.AnimalUtils;
import me.Plugins.BreedingBuddies.FarmAnimal;
import me.Plugins.BreedingBuddies.ItemUtils;
import me.Plugins.BreedingBuddies.OwnershipManager;
import me.Plugins.BreedingBuddies.ParticleManager;
import me.Plugins.BreedingBuddies.SoundManager;
import me.Plugins.BreedingBuddies.UnownedAnimalsManager;
import me.Plugins.BreedingBuddies.Configurables.CustomItems;
import me.Plugins.BreedingBuddies.Configurables.Messages;

public class TamingListener implements Listener {
	private JavaPlugin plugin;

	public TamingListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInteract(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		ItemStack itemInHand = player.getInventory().getItemInMainHand();

		/** PARA VER SI ES UN MYTHICMOB
		 * ActiveMob mythicMob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(entity);
		 * if (mythicMob == null) {
		 * return;
		 * }
		 * MYTHICMOB ID
		 * String mobId = mythicMob.getType().getInternalName();
		 */
		if (event.getHand().equals(EquipmentSlot.HAND)) {
			if (AnimalUtils.isFarmAnimal(entity)) {
				handleFarmAnimalInteraction(player, entity, itemInHand);
			} else if (entity instanceof Player && ItemUtils.getItemId(itemInHand).equalsIgnoreCase(CustomItems.coownershipItem)) {
				handlePlayerInteraction(player, entity, itemInHand);
			}
		}
	}

	private void handlePlayerInteraction(Player player, Entity targetPlayer, ItemStack itemInHand) {
		String entityUuid = ItemUtils.getDataFromItem(itemInHand, plugin);
		UUID uuid = UUID.fromString(entityUuid);
		FarmAnimal animal = OwnershipManager.getAnimal(uuid);
		if (animal == null) {
		    player.sendMessage(Messages.invalidAnimalOrNotFound);
		    return;
		}
		if (entityUuid != null) {
			if (OwnershipManager.isOwner(targetPlayer.getUniqueId(), uuid)) {
				player.sendMessage(String.format(Messages.alreadyOwner, animal.getName()));
			} else {
				OwnershipManager.registerOwnership(targetPlayer.getUniqueId(), animal);
//				targetPlayer.sendMessage(String.format(Messages.nowCoowner, animal.getName()));
//				player.sendMessage(String.format(Messages.ownershipShared, animal.getName()));
				SoundManager.playAmethystClusterStepSound(targetPlayer);
				ParticleManager.cherryParticles(targetPlayer);
				itemInHand.setAmount(itemInHand.getAmount() - 1);
			}
		} else {
			player.sendMessage(Messages.useOnOwnedAnimal);
		}
	}

	private void handleFarmAnimalInteraction(Player player, Entity entity, ItemStack itemInHand) {
		String itemInHandId = ItemUtils.getItemId(itemInHand);

		if (itemInHandId.equalsIgnoreCase(CustomItems.tamingItem)) {
			processTamingItem(player, entity, itemInHand);
		} else if (itemInHandId.equalsIgnoreCase(CustomItems.coownershipItem)) {
			processCoownershipItem(player, entity, itemInHand);
		}
	}

	private void processCoownershipItem(Player player, Entity entity, ItemStack itemInHand) {
		String itemData = ItemUtils.getDataFromItem(itemInHand, plugin);
		if (!OwnershipManager.hasOwner(entity.getUniqueId())) {
			player.sendMessage(Messages.ownerFirst);
		} else if (itemData != null && entity.getUniqueId().toString().equals(itemData)) {
			player.sendMessage(String.format(Messages.alreadyLinked, entity.getCustomName()));
		} else if (OwnershipManager.isOwner(player.getUniqueId(), entity.getUniqueId())) {
			ItemUtils.storeDataInItem(itemInHand, entity.getUniqueId().toString(), plugin);
			SoundManager.playAmethystStepSound(entity);
		} else {
			player.sendMessage(String.format(Messages.onlyOwnerCanShare, entity.getCustomName()));
		}
	}

	private void processTamingItem(Player player, Entity entity, ItemStack itemInHand) {
		if (OwnershipManager.hasOwner(entity.getUniqueId())) {
			return;
		}
		if (name(entity, itemInHand)) {
			tame(player, entity);
			player.sendMessage(String.format(Messages.nowOwner, entity.getCustomName()));
			SoundManager.playAmethystClusterStepSound(entity);
			ParticleManager.cherryParticles(entity);
		} else {
			player.sendMessage(Messages.nameFirst);
		}
	}

	public void tame(Player player, Entity entity) {
		FarmAnimal newAnimal;
		if (UnownedAnimalsManager.isUnowned(entity.getUniqueId()) && UnownedAnimalsManager.getAnimal(entity.getUniqueId()).getState() == AnimalStates.SPAWNED) {
			FarmAnimal oldAnimal = UnownedAnimalsManager.getAnimal(entity.getUniqueId());
			int friendships = oldAnimal.getFriendshipPoints();
			int genetics = oldAnimal.getGeneticPoints();
			UnownedAnimalsManager.removeUnownedAnimal(entity.getUniqueId());
			newAnimal = new FarmAnimal(entity.getUniqueId(), entity.getCustomName(), player.getUniqueId(), genetics, friendships);
		} else if (UnownedAnimalsManager.isUnowned(entity.getUniqueId())) {
			int genetics = UnownedAnimalsManager.getAnimal(entity.getUniqueId()).getGeneticPoints();
			UnownedAnimalsManager.removeUnownedAnimal(entity.getUniqueId());
			newAnimal = new FarmAnimal(entity.getUniqueId(), entity.getCustomName(), player.getUniqueId(), genetics);
		} else {
			newAnimal = new FarmAnimal(entity.getUniqueId(), entity.getCustomName(), player.getUniqueId());
		}
		OwnershipManager.registerOwnership(player.getUniqueId(), newAnimal);
		newAnimal.setState(AnimalStates.HAPPY);
	}

	public boolean name(Entity entity, ItemStack itemInHand) {
		String customName = ItemUtils.getCustomName(itemInHand);
		if (customName == null || ItemUtils.getDataFromItem(itemInHand, plugin) == null) {
			return false;
		} else {
			entity.setCustomName(customName);
			entity.setCustomNameVisible(false);
			itemInHand.setAmount(itemInHand.getAmount() - 1);
			return true;
		}
	}


}
