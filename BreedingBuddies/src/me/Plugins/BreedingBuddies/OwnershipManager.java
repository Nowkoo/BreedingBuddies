package me.Plugins.BreedingBuddies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;

public class OwnershipManager {
	
	public static void registerOwnership(UUID playerUuid, FarmAnimal animal) {
	    List<FarmAnimal> animals = PluginData.getPlayerAnimals().getOrDefault(playerUuid, new ArrayList<>());
	    if (!animals.contains(animal)) {
	        animals.add(animal);
	        PluginData.getPlayerAnimals().put(playerUuid, animals);
	    }
	    if (!animal.getOwnersUuids().contains(playerUuid)) {
	        animal.getOwnersUuids().add(playerUuid);
	    }
	}
	
	public void removeOwnership (UUID playerUuid, FarmAnimal animal) {
		List<FarmAnimal> animals = PluginData.getPlayerAnimals().get(playerUuid);
		if (animals != null) {
			animals.remove(animal);
			PluginData.getPlayerAnimals().put(playerUuid, animals);
			animal.getOwnersUuids().remove(playerUuid);
		}
	}
	
	public static void removeAnimal(UUID animalUuid) {
		PluginData.getPlayerAnimals().forEach((uuid, animals) -> {
            animals.removeIf(animal -> animal.getUuid().equals(animalUuid));
        });
	}
	
	public static List<FarmAnimal> getAnimals(UUID playerUuid) {
		return PluginData.getPlayerAnimals().getOrDefault(playerUuid, new ArrayList<>());
    }
	
	public static boolean isOwner(UUID playerUuid, UUID animalUuid) {
        List<FarmAnimal> animals = PluginData.getPlayerAnimals().get(playerUuid);
        if (animals != null) {
            for (FarmAnimal animal : animals) {
                if (animal.getUuid().equals(animalUuid)) {
                    return true;
                }
            }
        }
        return false;
    }
	
	public static boolean hasOwner(UUID animalUuid) {
        return PluginData.getPlayerAnimals().values().stream()
                .flatMap(List::stream)
                .anyMatch(animal -> animal.getUuid().equals(animalUuid) && !animal.getOwnersUuids().isEmpty());
    }
	
	public static ArrayList<UUID> getOwners(UUID animalUuid) {
        for (List<FarmAnimal> animals : PluginData.getPlayerAnimals().values()) {
            for (FarmAnimal animal : animals) {
                if (animal.getUuid().equals(animalUuid)) {
                    return new ArrayList<>(animal.getOwnersUuids());
                }
            }
        }
        return new ArrayList<>();
    }
	
	public static FarmAnimal getAnimal(UUID animalUuid, UUID playerUuid) {
		List<FarmAnimal> animals = PluginData.getPlayerAnimals().get(playerUuid);
		
		if (animals != null) {
			for (FarmAnimal animal : animals) {
				if (animal.getUuid().equals(animalUuid)) {
					return animal;
				}
			}
		}
		return null;
	}
	
	public static FarmAnimal getAnimal(UUID animalUuid) {
		return PluginData.getPlayerAnimals().values().stream()
                .flatMap(List::stream)
                .filter(animal -> animal.getUuid().equals(animalUuid))
                .findFirst()
                .orElse(null);
	}
}
