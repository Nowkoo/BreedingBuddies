package BreedingBuddies;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;



public class PluginData implements Listener {
	private static Map<UUID, FarmAnimal> unownedAnimals = new HashMap<>();
	private static Map<UUID, List<FarmAnimal>> playerAnimals = new HashMap<>();
	private static ArrayList<ChunkData> stableChunks = new ArrayList<ChunkData>();
	
	private static final String PLAYER_DATA_DIR = "plugins/BreedingBuddies/Data/PlayerData";
	private static final String UNOWNED_ANIMALS_DATA_DIR = "plugins/BreedingBuddies/Data/unownedanimals.json";
	private static final String STABLE_CHUNKS_DATA_DIR = "plugins/BreedingBuddies/Data/stablechunks.json";
	
	public static boolean loaded = false;

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		List<FarmAnimal> animals = PluginData.getPlayerAnimals().get(uuid);
		if (animals != null) {
			savePlayerData(uuid, animals);
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (!loaded) {
			loadAllData();
			Bukkit.getLogger().info("[BreedingBuddies] Loading all data...");
		}
	}
	
	public static void saveAllData() {
		PluginData.saveAllPlayerData();
        PluginData.saveUnownedAnimals();
        PluginData.saveStableChunks();
        Bukkit.getLogger().info("[BreedingBuddies] Saving all data...");
	}
	
	public static void loadAllData() {
		loadUnownedAnimals();
//		loadStableChunks();
		loadAllPlayerData();
		loaded = true;
	}
	
	public static void saveStableChunks() {
	    try {
	        File file = new File(STABLE_CHUNKS_DATA_DIR);
	        file.getParentFile().mkdirs(); // Ensure the directory exists

	        JSONArray chunksJsonArray = new JSONArray();
	        for (ChunkData chunkData : stableChunks) {
	            JSONObject chunkJson = new JSONObject();
	            chunkJson.put("world", chunkData.getChunk().getWorld().getName());
	            chunkJson.put("x", chunkData.getChunk().getX());
	            chunkJson.put("z", chunkData.getChunk().getZ());
	            chunkJson.put("daysAbandoned", chunkData.getDaysAbandoned());

	            chunksJsonArray.add(chunkJson);
	        }

	        FileWriter fileWriter = new FileWriter(file);
	        fileWriter.write(chunksJsonArray.toJSONString());
	        fileWriter.flush();
	        fileWriter.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static void loadStableChunks() {
	    try {
	        File file = new File(STABLE_CHUNKS_DATA_DIR);
	        if (!file.exists()) {
	            return;
	        }

	        JSONParser parser = new JSONParser();
	        String jsonContent = new String(Files.readAllBytes(Paths.get(file.getPath())), StandardCharsets.UTF_8);
	        JSONArray chunksJsonArray = (JSONArray) parser.parse(jsonContent);

	        for (Object chunkObj : chunksJsonArray) {
	            JSONObject chunkJson = (JSONObject) chunkObj;
	            String worldName = (String) chunkJson.get("world");
	            World world = Bukkit.getWorld(worldName);
	            int x = ((Long) chunkJson.get("x")).intValue();
	            int z = ((Long) chunkJson.get("z")).intValue();
	            Chunk chunk = world.getChunkAt(x, z);
	            int daysAbandoned = ((Long) chunkJson.get("daysAbandoned")).intValue();

	            ChunkData chunkData = new ChunkData(chunk);
	            chunkData.setDaysAbandoned(daysAbandoned);
	            stableChunks.add(chunkData);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static void saveUnownedAnimals() {
		try {
			File file = new File(UNOWNED_ANIMALS_DATA_DIR);
	        file.getParentFile().mkdirs(); // Ensure the directory exists
	        
	        JSONArray animalsJsonArray = new JSONArray();
	        
	        for (FarmAnimal animal : unownedAnimals.values()) {
	            JSONObject animalJson = new JSONObject();
	            animalJson.put("uuid", animal.getUuid().toString());
	            animalJson.put("name", animal.getName());
	            JSONArray ownersJsonArray = new JSONArray();
	            for (UUID ownerUUID : animal.getOwnersUuids()) {
	                ownersJsonArray.add(ownerUUID.toString());
	            }
	            animalJson.put("ownersUuids", ownersJsonArray);
	            animalJson.put("entityUuid", animal.getEntity().getUniqueId().toString());
	            animalJson.put("friendshipPoints", animal.getFriendshipPoints());
	            animalJson.put("geneticPoints", animal.getGeneticPoints());
	            animalJson.put("state", animal.getState().toString());
	            animalJson.put("fed", animal.getFed());
	            animalJson.put("cared", animal.getCared());
	            
	            if (animal.getSleptChunk() != null) {
	                JSONObject sleptChunkJson = new JSONObject();
	                sleptChunkJson.put("world", animal.getSleptChunk().getWorld().getName());
	                sleptChunkJson.put("x", animal.getSleptChunk().getX());
	                sleptChunkJson.put("z", animal.getSleptChunk().getZ());
	                animalJson.put("sleptChunk", sleptChunkJson);
	            } else {
	                animalJson.put("sleptChunk", null);
	            }
	            
	            animalJson.put("daysOut", animal.getDaysOut());
	            if (animal.getLastRewardCollected() != null) {
	                animalJson.put("lastRewardCollected", animal.getLastRewardCollected().getTime());
	            } else {
	                animalJson.put("lastRewardCollected", null);
	            }

	            animalsJsonArray.add(animalJson);
	        }
	        
	        JSONObject playerJson = new JSONObject();
	        playerJson.put("animals", animalsJsonArray);
	        
	        FileWriter fileWriter = new FileWriter(file);
	        fileWriter.write(playerJson.toJSONString());
	        fileWriter.flush();
	        fileWriter.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
		
	
	public static void loadUnownedAnimals() {
		try {
			File file = new File(UNOWNED_ANIMALS_DATA_DIR);
	        if (!file.exists()) {
	            return;
	        }
	        JSONParser parser = new JSONParser();
	        JSONObject playerJson = (JSONObject) parser.parse(new FileReader(file));

	        JSONArray animalsJsonArray = (JSONArray) playerJson.get("animals");

	        for (Object animalObj : animalsJsonArray) {
	            JSONObject animalJson = (JSONObject) animalObj;

	            UUID animalUUID = UUID.fromString((String) animalJson.get("uuid"));
	            String name = (String) animalJson.get("name");
	            // Parsear ownersUuids de JSONArray a List<UUID>
	            JSONArray ownersJsonArray = (JSONArray) animalJson.get("ownersUuids");
	            List<UUID> ownersUuids = new ArrayList<>();
	            for (Object ownerObj : ownersJsonArray) {
	                ownersUuids.add(UUID.fromString((String) ownerObj));
	            }
	            // Obtener la Entity del mundo
	            String entityUuidString = (String) animalJson.get("entityUuid");
	            Entity entity = Bukkit.getEntity(UUID.fromString(entityUuidString));
	            // Obtener el Chunk del mundo
	            JSONObject sleptChunkJson = (JSONObject) animalJson.get("sleptChunk");
	            Chunk sleptChunk = null;
	            if (sleptChunkJson != null) {
	                World world = Bukkit.getWorld((String) sleptChunkJson.get("world"));
	                int x = ((Long) sleptChunkJson.get("x")).intValue();
	                int z = ((Long) sleptChunkJson.get("z")).intValue();
	                sleptChunk = world.getChunkAt(x, z);
	            }
	            // Construir el objeto FarmAnimal
	            FarmAnimal farmAnimal = new FarmAnimal(animalUUID, name, ownersUuids, entity);
	            farmAnimal.setSleptChunk(sleptChunk);
	            farmAnimal.setFriendshipPoints(((Long) animalJson.get("friendshipPoints")).intValue());
				farmAnimal.calcGeneticsIfIsMount(entity, ((Long) animalJson.get("geneticPoints")).intValue());
	            farmAnimal.setState(AnimalStates.valueOf((String) animalJson.get("state")));
	            farmAnimal.setFed((boolean) animalJson.get("fed"));
	            farmAnimal.setCared((boolean) animalJson.get("cared"));
	            farmAnimal.setDaysOut(((Long) animalJson.get("daysOut")).intValue());
	            if (animalJson.get("lastRewardCollected") != null) {
	                farmAnimal.setLastRewardCollected(new Date((Long) animalJson.get("lastRewardCollected")));
	            }

	            unownedAnimals.put(animalUUID, farmAnimal);
	        }
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    } 
	}
		
	
	static void saveAllPlayerData() {
		Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
        for (Player player : players) {
        	UUID uuid = player.getUniqueId();
        	List<FarmAnimal> animals = PluginData.getPlayerAnimals().get(uuid);
			if (animals != null) {
				savePlayerData(uuid, animals);
			}
        }
    }

	public static void savePlayerData(UUID playerUUID, List<FarmAnimal> animals) {
	    try {
	        File file = new File(PLAYER_DATA_DIR, playerUUID.toString() + ".json");
	        file.getParentFile().mkdirs(); // Ensure the directory exists
	        
	        JSONArray animalsJsonArray = new JSONArray();
	        
	        for (FarmAnimal animal : animals) {
	            JSONObject animalJson = new JSONObject();
	            animalJson.put("uuid", animal.getUuid().toString());
	            animalJson.put("name", animal.getName());
	            JSONArray ownersJsonArray = new JSONArray();
	            for (UUID ownerUUID : animal.getOwnersUuids()) {
	                ownersJsonArray.add(ownerUUID.toString());
	            }
	            animalJson.put("ownersUuids", ownersJsonArray);
	            animalJson.put("entityUuid", animal.getUuid().toString());
	            animalJson.put("friendshipPoints", animal.getFriendshipPoints());
	            animalJson.put("geneticPoints", animal.getGeneticPoints());
	            animalJson.put("state", animal.getState().toString());
	            animalJson.put("fed", animal.getFed());
	            animalJson.put("cared", animal.getCared());
	            
	            if (animal.getSleptChunk() != null) {
	                JSONObject sleptChunkJson = new JSONObject();
	                sleptChunkJson.put("world", animal.getSleptChunk().getWorld().getName());
	                sleptChunkJson.put("x", animal.getSleptChunk().getX());
	                sleptChunkJson.put("z", animal.getSleptChunk().getZ());
	                animalJson.put("sleptChunk", sleptChunkJson);
	            } else {
	                animalJson.put("sleptChunk", null);
	            }
	            
	            animalJson.put("daysOut", animal.getDaysOut());
	            if (animal.getLastRewardCollected() != null) {
	                animalJson.put("lastRewardCollected", animal.getLastRewardCollected().getTime());
	            } else {
	                animalJson.put("lastRewardCollected", null);
	            }

	            animalsJsonArray.add(animalJson);
	        }
	        
	        JSONObject playerJson = new JSONObject();
	        playerJson.put("animals", animalsJsonArray);
	        
	        FileWriter fileWriter = new FileWriter(file);
	        fileWriter.write(playerJson.toJSONString());
	        fileWriter.flush();
	        fileWriter.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static void loadPlayerData(UUID playerUUID, Map<UUID, FarmAnimal> tempAnimalMap) {
	    try {
	        File file = new File(PLAYER_DATA_DIR, playerUUID.toString() + ".json");
	        if (!file.exists()) {
	            return;
	        }
	        JSONParser parser = new JSONParser();
	        JSONObject playerJson = (JSONObject) parser.parse(new FileReader(file));

	        JSONArray animalsJsonArray = (JSONArray) playerJson.get("animals");
	        List<FarmAnimal> loadedAnimals = new ArrayList<>();

	        for (Object animalObj : animalsJsonArray) {
	            JSONObject animalJson = (JSONObject) animalObj;

	            UUID animalUUID = UUID.fromString((String) animalJson.get("uuid"));

	            // Verificar si el animal ya ha sido cargado en esta sesión
	            FarmAnimal farmAnimal = tempAnimalMap.get(animalUUID);

	            if (farmAnimal == null) {
	                String name = (String) animalJson.get("name");

	                // Parsear ownersUuids de JSONArray a List<UUID>
	                JSONArray ownersJsonArray = (JSONArray) animalJson.get("ownersUuids");
	                List<UUID> ownersUuids = new ArrayList<>();
	                for (Object ownerObj : ownersJsonArray) {
	                    ownersUuids.add(UUID.fromString((String) ownerObj));
	                }

	                // Obtener la Entity del mundo
	                String entityUuidString = (String) animalJson.get("entityUuid");
	                Entity entity = Bukkit.getEntity(UUID.fromString(entityUuidString));

	                // Obtener el Chunk del mundo
	                JSONObject sleptChunkJson = (JSONObject) animalJson.get("sleptChunk");
	                Chunk sleptChunk = null;
	                if (sleptChunkJson != null) {
	                    World world = Bukkit.getWorld((String) sleptChunkJson.get("world"));
	                    int x = ((Long) sleptChunkJson.get("x")).intValue();
	                    int z = ((Long) sleptChunkJson.get("z")).intValue();
	                    sleptChunk = world.getChunkAt(x, z);
	                }

	                // Construir el objeto FarmAnimal
	                farmAnimal = new FarmAnimal(animalUUID, name, ownersUuids, entity);
	                farmAnimal.setSleptChunk(sleptChunk);
	                farmAnimal.setFriendshipPoints(((Long) animalJson.get("friendshipPoints")).intValue());
					farmAnimal.calcGeneticsIfIsMount(entity, ((Long) animalJson.get("geneticPoints")).intValue());
	                farmAnimal.setState(AnimalStates.valueOf((String) animalJson.get("state")));
	                farmAnimal.setFed((boolean) animalJson.get("fed"));
	                farmAnimal.setCared((boolean) animalJson.get("cared"));
	                farmAnimal.setDaysOut(((Long) animalJson.get("daysOut")).intValue());
	                if (animalJson.get("lastRewardCollected") != null) {
	                    farmAnimal.setLastRewardCollected(new Date((Long) animalJson.get("lastRewardCollected")));
	                }

	                // Guardar la instancia única en el mapa temporal
	                tempAnimalMap.put(animalUUID, farmAnimal);
	            }

	            // Añadir el animal cargado a la lista de animales del jugador actual
	            loadedAnimals.add(farmAnimal);

	            // Asegurarse de que el jugador actual está en la lista de propietarios
	            if (!farmAnimal.getOwnersUuids().contains(playerUUID)) {
	                farmAnimal.getOwnersUuids().add(playerUUID);
	            }
	        }

	        // Agregar los animales cargados al mapa playerAnimals
	        playerAnimals.put(playerUUID, loadedAnimals);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	
	public static void loadPlayerDataFix(UUID playerUUID, Map<UUID, FarmAnimal> tempAnimalMap) {
	    try {
	        File file = new File(PLAYER_DATA_DIR, playerUUID.toString() + ".json");
	        if (!file.exists()) {
	            return;
	        }
	        JSONParser parser = new JSONParser();
	        JSONObject playerJson = (JSONObject) parser.parse(new FileReader(file));

	        JSONArray animalsJsonArray = (JSONArray) playerJson.get("animals");

	        for (Object animalObj : animalsJsonArray) {
	            JSONObject animalJson = (JSONObject) animalObj;

	            UUID animalUUID = UUID.fromString((String) animalJson.get("uuid"));

	            // Verificamos si el animal ya ha sido cargado en esta sesión
	            FarmAnimal existingAnimal = tempAnimalMap.get(animalUUID);

	            // Construimos el nuevo animal cargado desde el archivo
	            String name = (String) animalJson.get("name");
	            JSONArray ownersJsonArray = (JSONArray) animalJson.get("ownersUuids");
	            List<UUID> ownersUuids = new ArrayList<>();
	            for (Object ownerObj : ownersJsonArray) {
	                ownersUuids.add(UUID.fromString((String) ownerObj));
	            }
	            String entityUuidString = (String) animalJson.get("entityUuid");
	            Entity entity = Bukkit.getEntity(UUID.fromString(entityUuidString));
	            JSONObject sleptChunkJson = (JSONObject) animalJson.get("sleptChunk");
	            Chunk sleptChunk = null;
	            if (sleptChunkJson != null) {
	                World world = Bukkit.getWorld((String) sleptChunkJson.get("world"));
	                int x = ((Long) sleptChunkJson.get("x")).intValue();
	                int z = ((Long) sleptChunkJson.get("z")).intValue();
	                sleptChunk = world.getChunkAt(x, z);
	            }
	            FarmAnimal loadedAnimal = new FarmAnimal(animalUUID, name, ownersUuids, entity);
	            loadedAnimal.setSleptChunk(sleptChunk);
	            loadedAnimal.setFriendshipPoints(((Long) animalJson.get("friendshipPoints")).intValue());
				loadedAnimal.calcGeneticsIfIsMount(entity, ((Long) animalJson.get("geneticPoints")).intValue());
				loadedAnimal.setState(AnimalStates.valueOf((String) animalJson.get("state")));
	            loadedAnimal.setFed((boolean) animalJson.get("fed"));
	            loadedAnimal.setCared((boolean) animalJson.get("cared"));
	            loadedAnimal.setDaysOut(((Long) animalJson.get("daysOut")).intValue());
	            if (animalJson.get("lastRewardCollected") != null) {
	                loadedAnimal.setLastRewardCollected(new Date((Long) animalJson.get("lastRewardCollected")));
	            }

	            // Si el animal ya existe en el mapa temporal, comparamos la genética
	            if (existingAnimal != null) {
	                // Si la genética del animal cargado es mayor, lo reemplazamos en el mapa temporal
	                if (loadedAnimal.getGeneticPoints() > existingAnimal.getGeneticPoints()) {
	                    tempAnimalMap.put(animalUUID, loadedAnimal);
	                }
	            } else {
	                // Si el animal no existe en el mapa temporal, lo agregamos directamente
	                tempAnimalMap.put(animalUUID, loadedAnimal);
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static void loadAllPlayerData() {
		Map<UUID, FarmAnimal> tempAnimalMap = new HashMap<>();
		
        try {
            File playerDataFolder = new File(PLAYER_DATA_DIR);
            if (!playerDataFolder.exists() || !playerDataFolder.isDirectory()) {
                return;
            }

            // Recorre cada carpeta (cada jugador) dentro de la carpeta PlayerData
            for (File playerFolder : playerDataFolder.listFiles()) {
                if (playerFolder.isFile()) {
                    String fileName = playerFolder.getName();
                    
                    // Verifica y elimina la extensión .json si está presente
                    if (fileName.endsWith(".json")) {
                        String uuidString = fileName.substring(0, fileName.length() - 5);
                        try {
                            UUID playerUUID = UUID.fromString(uuidString);
                            // Llama a la función loadPlayerData con el UUID del jugador
                            loadPlayerData(playerUUID, tempAnimalMap);
                        } catch (IllegalArgumentException e) {
                            // Maneja el caso en que el nombre de archivo no sea un UUID válido
                            Bukkit.getLogger().warning("Invalid player data file: " + fileName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public static void loadAllPlayerDataFix() {
	    Map<UUID, FarmAnimal> tempAnimalMap = new HashMap<>();

	    try {
	        File playerDataFolder = new File(PLAYER_DATA_DIR);
	        if (!playerDataFolder.exists() || !playerDataFolder.isDirectory()) {
	            return;
	        }

	        // Recorre cada carpeta (cada jugador) dentro de la carpeta PlayerData
	        for (File playerFolder : playerDataFolder.listFiles()) {
	            if (playerFolder.isFile()) {
	                String fileName = playerFolder.getName();
	                
	                // Verifica y elimina la extensión .json si está presente
	                if (fileName.endsWith(".json")) {
	                    String uuidString = fileName.substring(0, fileName.length() - 5);
	                    try {
	                        UUID playerUUID = UUID.fromString(uuidString);
	                        // Llama a la función loadPlayerData con el UUID del jugador
	                        loadPlayerData(playerUUID, tempAnimalMap);
	                    } catch (IllegalArgumentException e) {
	                        // Maneja el caso en que el nombre de archivo no sea un UUID válido
	                        Bukkit.getLogger().warning("Invalid player data file: " + fileName);
	                    }
	                }
	            }
	        }
	        
	        // Actualizar playerAnimals con los datos cargados en tempAnimalMap
	        for (Map.Entry<UUID, FarmAnimal> entry : tempAnimalMap.entrySet()) {
	            UUID animalUUID = entry.getKey();
	            FarmAnimal animal = entry.getValue();
	            
	            UUID playerUUID = animal.getOwnersUuids().get(0); // Suponiendo que el primer propietario es el dueño principal
	            
	            List<FarmAnimal> playerAnimalList = playerAnimals.getOrDefault(playerUUID, new ArrayList<>());
	            playerAnimalList.add(animal);
	            playerAnimals.put(playerUUID, playerAnimalList);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public static Map<UUID, FarmAnimal> getUnownedAnimals() {
		return unownedAnimals;
	}

	public static Map<UUID, List<FarmAnimal>> getPlayerAnimals() {
		return playerAnimals;
	}

	public static ArrayList<ChunkData> getStableChunks() {
		return stableChunks;
	}
	
	public static boolean isLoaded() {
		return loaded;
	}
}
