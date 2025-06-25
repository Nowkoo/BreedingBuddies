package BreedingBuddies;

import BreedingBuddies.Configurables.Numbers;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;

import java.util.ArrayList;

public class ChunkManager {
	private static ArrayList<ChunkData> stableChunks = PluginData.getStableChunks();
	
	public static void addChunk(Chunk newChunk) {
		for (ChunkData data : stableChunks) {
            if (data.getChunk().equals(newChunk)) {
                return;
            }
        }
        stableChunks.add(new ChunkData(newChunk));
	}
	
	public static void removeChunk(Chunk oldChunk) {
		stableChunks.removeIf(data -> data.getChunk().equals(oldChunk));
	}
	
	public static boolean isStableChunk(Chunk currentChunk) {
		for (ChunkData data : stableChunks) {
            if (data.getChunk().equals(currentChunk)) {
                return true;
            }
        }
        return false;
	}
	
	public static boolean isHabitable(Chunk currentChunk) {
		return animalsContained(currentChunk) <= Numbers.maxAnimalsPerChunk
				&& containsWater(currentChunk);
	}
	
	public static int animalsContained(Chunk currentChunk) {
		int animalsContained = 0;
        for (Entity entity : currentChunk.getEntities()) {
            if (entity instanceof Animals) {
                animalsContained++;
            }
        }
        return animalsContained;
	}
	
	public static boolean containsWater(Chunk currentChunk) {
		return currentChunk.contains(Bukkit.createBlockData(Material.WATER))
				 || currentChunk.contains(Bukkit.createBlockData(Material.WATER_CAULDRON));
	}
	
	public static ArrayList<ChunkData> getStableChunks() {
		return stableChunks;
	}
}
