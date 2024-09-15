package me.Plugins.BreedingBuddies.Listeners;

import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import me.Plugins.BreedingBuddies.OwnershipManager;
import me.Plugins.BreedingBuddies.UnownedAnimalsManager;

public class AnimalDeathListener implements Listener {
	@EventHandler
	public void onAnimalDeath(EntityDeathEvent event) {
		if (event.getEntity() instanceof Animals) {
            Entity animal = event.getEntity();
            OwnershipManager.removeAnimal(animal.getUniqueId());
            UnownedAnimalsManager.removeUnownedAnimal(animal.getUniqueId());
        }
	}
}
