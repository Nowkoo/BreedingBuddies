package BreedingBuddies.Listeners;

import BreedingBuddies.*;
import BreedingBuddies.Configurables.Numbers;
import BreedingBuddies.Events.DayChangeEvent;
import org.bukkit.Chunk;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

public class DayChangeListener implements Listener {
	
	@EventHandler
    public void onDayChange(DayChangeEvent event) {
		if (!PluginData.isLoaded()) {
			PluginData.loadStableChunks();
			PluginData.loadUnownedAnimals();
			PluginData.loaded = true;
		}
//		Cleanser.removeDisappearedAnimals();
		updateUnownedAnimals();
		changeAnimalState();
		manageChunkUse();
		PluginData.saveStableChunks();
		PluginData.saveUnownedAnimals();
    }
	
	private void manageChunkUse() {
	    ArrayList<ChunkData> stableChunks = ChunkManager.getStableChunks();
	    Iterator<ChunkData> it = stableChunks.iterator();
	    while (it.hasNext()) {
	        ChunkData chunkData = it.next();
	        updateChunkUse(chunkData);
	        if (chunkData.getDaysAbandoned() > Numbers.maxIrlDaysChunkAbandoned) {
	            it.remove();
	        }
	    }
	}

	private void updateChunkUse(ChunkData chunk) {
		Entity[] entitiesInChunk = chunk.getChunk().getEntities();
		boolean habited = false;
		for (Entity entity : entitiesInChunk) {
			if (entity instanceof Animals && OwnershipManager.hasOwner(entity.getUniqueId())) {
				habited = true;
				break;
			}
		}
		if (!habited) {
			chunk.increaseDaysAbandoned();
		}
	}

	public void changeAnimalState() {
        Map<UUID, List<FarmAnimal>> animals = PluginData.getPlayerAnimals();
        for (List<FarmAnimal> animalList : animals.values()) {
            Iterator<FarmAnimal> it = animalList.iterator();
            while (it.hasNext()) {
                FarmAnimal animal = it.next();
				//si se cambia el sistema de change day hará falta almacenar si la entidad es una montura o no
				Entity entity = animal.getEntity();
                if (animal != null && entity != null) {
					if (MountUtils.isFullStatMount(entity)) {
						updateState(animal);
					} else {
						Chunk sleptChunk = animal.getEntity().getLocation().getChunk();
						animal.setSleptChunk(sleptChunk);
						updateDaysOut(sleptChunk, animal);
						updateState(animal, sleptChunk);
						if (animal.getDaysOut() > Numbers.maxIrlDaysOut) {
							UnownedAnimalsManager.addUnownedAnimal(animal);
							animal.setState(AnimalStates.ESCAPED);
							it.remove();
						}
					}
                }
            }
        }
    }

	private void updateState(FarmAnimal animal, Chunk sleptChunk) {
		if (!animal.getFed() || !animal.getCared() || !goodPlacement(sleptChunk)) {
			animal.setState(AnimalStates.SAD);
			animal.decreaseFriendship(Numbers.friendshipLostNotCaring + Numbers.friendshipLostNotFeeding);
		} else {
			animal.setState(AnimalStates.HAPPY);
		}
		animal.setCared(false);
		animal.setFed(false);
	}

	private void updateState(FarmAnimal animal) {
		if (!animal.getFed() || !animal.getCared()) {
			animal.setState(AnimalStates.SAD);
			animal.decreaseFriendship(Numbers.friendshipLostNotCaring + Numbers.friendshipLostNotFeeding);
		} else {
			animal.setState(AnimalStates.HAPPY);
		}
		animal.setCared(false);
		animal.setFed(false);
	}

	private void updateDaysOut(Chunk sleptChunk, FarmAnimal animal) {
		if (!ChunkManager.isStableChunk(sleptChunk)) {
			animal.incrementDaysOut();
		} else {
			animal.resetDaysOut();
		}
	}

	public boolean goodPlacement(Chunk sleptChunk) {
		if (sleptChunk != null) {
			return ChunkManager.isStableChunk(sleptChunk) 
					&& ChunkManager.isHabitable(sleptChunk);
		}
		return false;
	}
	
	public void updateUnownedAnimals() {
		Map<UUID, FarmAnimal> map = PluginData.getUnownedAnimals();
		Iterator<Map.Entry<UUID, FarmAnimal>> entries = map.entrySet().iterator();
		while (entries.hasNext()) {
		    Map.Entry<UUID, FarmAnimal> entry = entries.next();
		    FarmAnimal animal = entry.getValue();
		    animal.incrementDaysOut();
		    if (animal.getDaysOut() > Numbers.maxIrlDaysOut + 5) {
		    	Entity entity = animal.getEntity();
		    	if (entity != null && !entity.isDead()) {
		    		entity.remove();
		    	}
		    	entries.remove();
		    }
		}	
	}
}
