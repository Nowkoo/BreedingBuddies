package me.Plugins.BreedingBuddies.Listeners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.Plugins.BreedingBuddies.AnimalStates;
import me.Plugins.BreedingBuddies.ChunkArea;
import me.Plugins.BreedingBuddies.ChunkManager;
import me.Plugins.BreedingBuddies.FarmAnimal;
import me.Plugins.BreedingBuddies.OwnershipManager;
import me.Plugins.BreedingBuddies.PluginData;
import me.Plugins.BreedingBuddies.UnownedAnimalsManager;
import me.Plugins.BreedingBuddies.Configurables.Numbers;
import me.Plugins.BreedingBuddies.Events.DayChangeEvent;

public class DayChangeListener implements Listener {

    @EventHandler
    public void onDayChange(DayChangeEvent event) {
        ChunkManager.forceLoadChunks();
        updateUnownedAnimals();
        resetAnimalValues();
        updateAnimalNeeds();
        changeAnimalState();
        manageChunkUse();
        PluginData.saveAllData();
        ChunkManager.allowUnloadChunks();
    }

    private void resetAnimalValues() {
        for (List<FarmAnimal> playerAnimalList : PluginData.getPlayerAnimals().values()) {
            for (FarmAnimal animal : playerAnimalList) {
                animal.setSleptInStable(false);
                animal.setSpaceNeeded(false);
                animal.setWaterNeeded(false);
            }
        }
    }

    private void updateAnimalNeeds() {
        for (ChunkArea chunkArea : PluginData.getChunkAreas()) {
            boolean hasFarmAnimals = processChunkAreaAndUpdateNeeds(chunkArea);

            if (!hasFarmAnimals) {
                chunkArea.increaseDaysAbandoned();
            }
        }
    }

    private boolean processChunkAreaAndUpdateNeeds(ChunkArea chunkArea) {
        boolean spaceNeeded = ChunkManager.spaceNeeded(chunkArea);
        boolean waterNeeded = !ChunkManager.containsWater(chunkArea);

        boolean hasFarmAnimals = false;

        for (Chunk chunk : chunkArea.getChunks()) {
            Entity[] entitiesInChunk = chunk.getEntities();
            for (Entity entity : entitiesInChunk) {
                if (entity instanceof Animals && isFarmAnimal(entity)) {
                    UUID entityUuid = entity.getUniqueId();
                    hasFarmAnimals = true;

                    FarmAnimal farmAnimal = OwnershipManager.getAnimal(entityUuid);
                    farmAnimal.setSleptInStable(true);
                    farmAnimal.setSpaceNeeded(spaceNeeded);
                    farmAnimal.setWaterNeeded(waterNeeded);
                }
            }
        }

        return hasFarmAnimals;
    }

    private boolean isFarmAnimal(Entity entity) {
        UUID entityUuid = entity.getUniqueId();
        return OwnershipManager.hasOwner(entityUuid);
    }

    private void manageChunkUse() {
        ArrayList<ChunkArea> chunkAreas = PluginData.getChunkAreas();
        Iterator<ChunkArea> it = chunkAreas.iterator();
        while (it.hasNext()) {
            ChunkArea chunkArea = it.next();
            if (chunkArea.getDaysAbandoned() > Numbers.maxIrlDaysChunkAbandoned) {
                it.remove();
            }
        }
    }

    public void changeAnimalState() {
        Map<UUID, List<FarmAnimal>> animals = PluginData.getPlayerAnimals();
        Set<UUID> processedAnimals = new HashSet<>();

        for (List<FarmAnimal> animalList : animals.values()) {
            processAnimalList(animalList, processedAnimals);
        }
    }

    private void processAnimalList(List<FarmAnimal> animalList, Set<UUID> processedAnimals) {
        Iterator<FarmAnimal> it = animalList.iterator();
        while (it.hasNext()) {
            FarmAnimal animal = it.next();
            if (animal != null && !processedAnimals.contains(animal.getUuid())) {
                processAnimal(animal, it);
                processedAnimals.add(animal.getUuid());
            }
        }
    }

    private void processAnimal(FarmAnimal animal, Iterator<FarmAnimal> it) {
        updateDaysOut(animal);
        updateState(animal);
        if (animal.getDaysOut() > Numbers.maxIrlDaysOut) {
            handleEscapedAnimal(animal, it);
        }
    }

    private void updateState(FarmAnimal animal) {
        if (!animal.isAnimalHappy()) {
            animal.setState(AnimalStates.SAD);
            animal.decreaseFriendship(Numbers.friendshipLostNotCaring + Numbers.friendshipLostNotFeeding);
        } else {
            animal.setState(AnimalStates.HAPPY);
        }
        animal.setCared(false);
        animal.setFed(false);
    }

    private void updateDaysOut(FarmAnimal animal) {
        if (!animal.isSleptInStable()) {
            animal.incrementDaysOut();
        } else {
            animal.resetDaysOut();
        }
    }

    private void handleEscapedAnimal(FarmAnimal animal, Iterator<FarmAnimal> it) {
        UnownedAnimalsManager.addUnownedAnimal(animal);
        animal.setState(AnimalStates.ABANDONED);
        it.remove();
    }

    public void updateUnownedAnimals() {
        Map<UUID, FarmAnimal> map = PluginData.getUnownedAnimals();
        Iterator<Map.Entry<UUID, FarmAnimal>> entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<UUID, FarmAnimal> entry = entries.next();
            FarmAnimal animal = entry.getValue();
            animal.incrementDaysOut();
            if (animal.getDaysOut() > Numbers.maxIrlDaysOut + 30) {
                entries.remove();
            }
        }
    }
}
